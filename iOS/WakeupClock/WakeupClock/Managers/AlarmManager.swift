//
//  AlarmManager.swift
//  WakeupClock
//
//  闹钟管理器：负责闹钟的增删改查（使用 AlarmKit）
//

import Foundation
import SwiftData
import Combine

/// 闹钟管理器（单例）
@MainActor
class AlarmManager: ObservableObject {
    static let shared = AlarmManager()
    
    @Published var alarms: [AlarmModel] = []
    
    var modelContext: ModelContext?
    private var cancellables = Set<AnyCancellable>()
    
    private init() {
        // AlarmKit 会处理所有触发逻辑，不需要 Timer 检查
    }
    
    /// 设置ModelContext（在App启动时调用）
    func setup(modelContext: ModelContext) {
        self.modelContext = modelContext
        loadAlarms()
    }
    
    // MARK: - 数据操作
    
    /// 加载所有闹钟
    private func loadAlarms() {
        guard let modelContext = modelContext else { return }
        
        let descriptor = FetchDescriptor<AlarmModel>(
            sortBy: [SortDescriptor(\.time)]
        )
        
        do {
            alarms = try modelContext.fetch(descriptor)
        } catch {
            #if DEBUG
            print("加载闹钟失败: \(error)")
            #endif
            alarms = []
        }
    }
    
    /// 添加闹钟
    func addAlarm(_ alarm: AlarmModel) {
        guard let modelContext = modelContext else { return }
        
        modelContext.insert(alarm)
        
        do {
            try modelContext.save()
            loadAlarms()
            
            // 使用 AlarmKit 调度
            Task {
                if #available(iOS 26.0, *) {
                    try? await AlarmKitManager.shared.scheduleAlarm(alarm)
                }
            }
        } catch {
            #if DEBUG
            print("添加闹钟失败: \(error)")
            #endif
        }
    }
    
    /// 更新闹钟
    func updateAlarm(_ alarm: AlarmModel) {
        guard let modelContext = modelContext else { return }
        
        do {
            try modelContext.save()
            loadAlarms()
            
            // 使用 AlarmKit 更新
            Task {
                if #available(iOS 26.0, *) {
                    try? await AlarmKitManager.shared.scheduleAlarm(alarm)
                }
            }
        } catch {
            #if DEBUG
            print("更新闹钟失败: \(error)")
            #endif
        }
    }
    
    /// 删除闹钟
    func deleteAlarm(_ alarm: AlarmModel) {
        guard let modelContext = modelContext else { return }
        
        // 取消 AlarmKit 闹钟
        Task {
            if #available(iOS 26.0, *) {
                try? await AlarmKitManager.shared.cancelAlarm(alarm)
            }
        }
        
        modelContext.delete(alarm)
        
        do {
            try modelContext.save()
            loadAlarms()
        } catch {
            #if DEBUG
            print("删除闹钟失败: \(error)")
            #endif
        }
    }
    
    /// 切换闹钟启用状态
    func toggleAlarm(_ alarm: AlarmModel) {
        alarm.enabled.toggle()
        updateAlarm(alarm)
    }
    
    // MARK: - 辅助方法
    
    /// 获取下一个即将响起的闹钟
    func getNextAlarm() -> AlarmModel? {
        let now = Date()
        let calendar = Calendar.current
        
        var nextAlarm: AlarmModel?
        var nextTime: Date?
        
        for alarm in alarms where alarm.enabled {
            guard let (hour, minute) = alarm.timeComponents else { continue }
            
            // 检查未来8天内的触发时间
            for dayOffset in 0..<8 {
                guard let checkDate = calendar.date(byAdding: .day, value: dayOffset, to: now) else { continue }
                
                var components = calendar.dateComponents([.year, .month, .day], from: checkDate)
                components.hour = hour
                components.minute = minute
                components.second = 0
                
                guard let triggerTime = calendar.date(from: components),
                      triggerTime > now else { continue }
                
                if alarm.shouldTrigger(on: triggerTime) {
                    if let currentNextTime = nextTime {
                        if triggerTime < currentNextTime {
                            nextTime = triggerTime
                            nextAlarm = alarm
                        }
                    } else {
                        nextTime = triggerTime
                        nextAlarm = alarm
                    }
                    break
                }
            }
        }
        
        return nextAlarm
    }
    
    /// 获取距离下一个闹钟的倒计时文本
    func getCountdownText() -> String? {
        guard let nextAlarm = getNextAlarm(),
              let (hour, minute) = nextAlarm.timeComponents else {
            return nil
        }
        
        let now = Date()
        let calendar = Calendar.current
        
        // 找到下一个触发时间
        for dayOffset in 0..<8 {
            guard let checkDate = calendar.date(byAdding: .day, value: dayOffset, to: now) else { continue }
            
            var components = calendar.dateComponents([.year, .month, .day], from: checkDate)
            components.hour = hour
            components.minute = minute
            components.second = 0
            
            guard let triggerTime = calendar.date(from: components),
                  triggerTime > now,
                  nextAlarm.shouldTrigger(on: triggerTime) else { continue }
            
            let timeInterval = triggerTime.timeIntervalSince(now)
            let totalMinutes = Int(timeInterval / 60)
            let days = totalMinutes / (24 * 60)
            let hours = (totalMinutes % (24 * 60)) / 60
            let minutes = totalMinutes % 60
            
            if days > 0 {
                return String(format: LocalizedString("remainingDays"), days, hours)
            } else if hours > 0 {
                return String(format: LocalizedString("remainingHours"), hours, minutes)
            } else {
                return String(format: LocalizedString("remainingMinutes"), minutes)
            }
        }
        
        return nil
    }
}

// MARK: - 通知名称扩展

extension Notification.Name {
    static let alarmTriggered = Notification.Name("alarmTriggered")
    static let alarmTriggeredFromNotification = Notification.Name("alarmTriggeredFromNotification")
}
