//
//  AlarmKitManager.swift
//  WakeupClock
//
//  AlarmKit 管理器：使用 iOS 26+ 的系统级闹钟
//

import Foundation
import AlarmKit
import ActivityKit
import AppIntents
import SwiftUI

/// AlarmKit 管理器（单例）
@available(iOS 26.0, *)
@Observable
class AlarmKitManager {
    static let shared = AlarmKitManager()
    
    var isAuthorized = false
    
    /// 当前闹钟使用的声音（用于应用内播放保持一致）
    private(set) var currentAlarmSound: AlarmSound?
    
    private let alarmManager = AlarmKit.AlarmManager.shared
    private let soundResourceManager = AlarmSoundResourceManager.shared
    
    private init() {
        Task {
            await checkAuthorization()
        }
    }
    
    // MARK: - 权限管理
    
    /// 请求 AlarmKit 权限
    func requestAuthorization() async -> Bool {
        switch alarmManager.authorizationState {
        case .notDetermined:
            do {
                let state = try await alarmManager.requestAuthorization()
                await MainActor.run {
                    self.isAuthorized = (state == .authorized)
                }
                return state == .authorized
            } catch {
                #if DEBUG
                print("❌ AlarmKit 授权失败: \(error)")
                #endif
                return false
            }
        case .denied:
            return false
        case .authorized:
            await MainActor.run {
                self.isAuthorized = true
            }
            return true
        @unknown default:
            return false
        }
    }
    
    /// 检查授权状态
    func checkAuthorization() async {
        let state = alarmManager.authorizationState
        await MainActor.run {
            self.isAuthorized = (state == .authorized)
        }
    }
    
    // MARK: - 闹钟调度
    
    /// 为闹钟调度系统级 AlarmKit 闹钟
    func scheduleAlarm(_ alarm: AlarmModel) async throws {
        guard isAuthorized else {
            throw AlarmKitError.notAuthorized
        }
        
        // 先取消旧闹钟
        try? alarmManager.cancel(id: UUID(uuidString: alarm.id)!)
        
        guard alarm.enabled else { return }
        
        // 根据重复模式创建 schedule
        let schedule = try createSchedule(for: alarm)
        
        // 创建 alarm configuration
        let config = try createAlarmConfiguration(for: alarm, schedule: schedule)
        
        // 调度闹钟
        _ = try await alarmManager.schedule(id: UUID(uuidString: alarm.id)!, configuration: config)
        
        #if DEBUG
        print("✅ AlarmKit 闹钟已调度: \(alarm.id) at \(alarm.time)")
        #endif
    }
    
    /// 取消闹钟
    func cancelAlarm(_ alarm: AlarmModel) async throws {
        guard let uuid = UUID(uuidString: alarm.id) else { return }
        try alarmManager.cancel(id: uuid)
        
        #if DEBUG
        print("🗑️ AlarmKit 闹钟已取消: \(alarm.id)")
        #endif
    }
    
    /// 取消所有闹钟
    func cancelAllAlarms() async throws {
        let alarms = try alarmManager.alarms
        for alarm in alarms {
            try alarmManager.cancel(id: alarm.id)
        }
        
        #if DEBUG
        print("🗑️ 所有 AlarmKit 闹钟已取消")
        #endif
    }
    
    // MARK: - 防重新入睡功能
    
    /// 调度防重新入睡提醒闹钟
    func scheduleAntiSnoozeReminders(
        originalAlarmId: String,
        intervalMinutes: Int,
        count: Int
    ) async throws {
        guard isAuthorized else {
            throw AlarmKitError.notAuthorized
        }
        
        let now = Date()
        
        for index in 1...count {
            // 计算触发时间
            let triggerTime = now.addingTimeInterval(TimeInterval(intervalMinutes * index * 60))
            
            // 创建提醒配置
            let config = try createAntiSnoozeConfiguration(
                originalAlarmId: originalAlarmId,
                reminderIndex: index,
                totalCount: count,
                triggerTime: triggerTime
            )
            
            // 使用新的UUID调度提醒
            let reminderUUID = UUID()
            _ = try await alarmManager.schedule(id: reminderUUID, configuration: config)
            
            #if DEBUG
            let formatter = DateFormatter()
            formatter.dateFormat = "HH:mm:ss"
            formatter.timeZone = TimeZone.current
            print("✅ 防重新入睡提醒 \(index)/\(count) 已调度: \(formatter.string(from: triggerTime))")
            #endif
        }
    }
    
    /// 取消防重新入睡提醒
    func cancelAntiSnoozeReminders(originalAlarmId: String) async throws {
        // 获取所有闹钟
        let alarms = try alarmManager.alarms
        
        // 由于无法直接访问 Alarm 的 attributes，我们只能取消所有不是原始闹钟的闹钟
        // 原始闹钟的 ID 应该是有效的 UUID 格式
        guard let originalUUID = UUID(uuidString: originalAlarmId) else {
            #if DEBUG
            print("⚠️ 无效的原始闹钟ID: \(originalAlarmId)")
            #endif
            return
        }
        
        // 取消所有非原始闹钟（即防重新入睡提醒）
        for alarm in alarms {
            if alarm.id != originalUUID {
                try alarmManager.cancel(id: alarm.id)
                #if DEBUG
                print("🗑️ 取消防重新入睡提醒: \(alarm.id)")
                #endif
            }
        }
    }
    
    /// 创建防重新入睡提醒配置
    private func createAntiSnoozeConfiguration(
        originalAlarmId: String,
        reminderIndex: Int,
        totalCount: Int,
        triggerTime: Date
    ) throws -> AlarmKit.AlarmManager.AlarmConfiguration<WakeupAlarmMetadata> {
        // 创建提醒标题和按钮文字
        let titleKey: String
        if reminderIndex == 1 {
            titleKey = "antiSnoozeReminder1" // "还醒着吗？"
        } else if reminderIndex == totalCount {
            titleKey = "antiSnoozeReminderLast" // "最后确认"
        } else {
            titleKey = "antiSnoozeReminder" // "确认清醒"
        }
        
        // 创建 alert presentation（只有一个"我醒了"按钮）
        let awakeText: LocalizedStringResource = "我醒了"
        let alertContent = AlarmPresentation.Alert(
            title: LocalizedStringResource(stringLiteral: LocalizedString(titleKey)),
            stopButton: AlarmButton(
                text: awakeText,
                textColor: .white,
                systemImageName: "checkmark.circle.fill"
            )
        )
        
        let presentation = AlarmPresentation(alert: alertContent)
        
        // 创建 attributes
        let attributes = AlarmAttributes(
            presentation: presentation,
            metadata: WakeupAlarmMetadata(alarmLabel: "antiSnooze"),
            tintColor: Color.orange
        )
        
        // 创建确认清醒意图
        let confirmIntent = ConfirmAwakeAppIntent(
            originalAlarmId: originalAlarmId,
            reminderIndex: reminderIndex
        )
        
        // 随机选择一个自定义声音
        let selectedSound = AlarmSound.randomAvailable()
        let customSound = getAlarmSound(for: selectedSound)
        
        // 创建配置（使用固定时间和自定义声音）
        let config = AlarmKit.AlarmManager.AlarmConfiguration(
            schedule: .fixed(triggerTime),
            attributes: attributes,
            stopIntent: confirmIntent,
            sound: customSound
        )
        
        #if DEBUG
        print("🔊 防重新入睡提醒 \(reminderIndex) 使用声音: \(selectedSound.displayName)")
        #endif
        
        return config
    }
    
    // MARK: - 私有辅助方法
    
    /// 创建闹钟 schedule
    private func createSchedule(for alarm: AlarmModel) throws -> Alarm.Schedule {
        guard let (hour, minute) = alarm.timeComponents else {
            throw AlarmKitError.invalidTime
        }
        
        let calendar = Calendar.current
        
        switch alarm.repeatModeEnum {
        case .once:
            // 响一次：使用固定日期
            var components = calendar.dateComponents([.year, .month, .day], from: Date())
            components.hour = hour
            components.minute = minute
            components.second = 0
            
            guard let triggerDate = calendar.date(from: components) else {
                throw AlarmKitError.invalidDate
            }
            
            // 如果时间已过，设置为明天
            let finalDate = triggerDate > Date() ? triggerDate : calendar.date(byAdding: .day, value: 1, to: triggerDate)!
            
            return .fixed(finalDate)
            
        case .workdays, .custom:
            // 工作日或自定义：使用 relative schedule
            let time = Alarm.Schedule.Relative.Time(hour: hour, minute: minute)
            
            let weekdays: [Locale.Weekday]
            if alarm.repeatModeEnum == .workdays {
                weekdays = [.monday, .tuesday, .wednesday, .thursday, .friday]
            } else {
                weekdays = alarm.customDays.compactMap { dayIndex -> Locale.Weekday? in
                    switch dayIndex {
                    case 0: return .sunday
                    case 1: return .monday
                    case 2: return .tuesday
                    case 3: return .wednesday
                    case 4: return .thursday
                    case 5: return .friday
                    case 6: return .saturday
                    default: return nil
                    }
                }
            }
            
            if weekdays.isEmpty {
                throw AlarmKitError.noWeekdaysSelected
            }
            
            return .relative(.init(time: time, repeats: .weekly(weekdays)))
        }
    }
    
    /// 创建 alarm configuration
    private func createAlarmConfiguration(for alarm: AlarmModel, schedule: Alarm.Schedule?) throws -> AlarmKit.AlarmManager.AlarmConfiguration<WakeupAlarmMetadata> {
        // 获取闹钟标题
        let title = LocalizedString("alarm_msg_\(alarm.label)")
        
        // 方案1：强制解锁模式（只有一个按钮，必须打开应用）
        let unlockText: LocalizedStringResource = LocalizedStringResource(stringLiteral: LocalizedString("unlockAlarm"))
        let alertContent = AlarmPresentation.Alert(
            title: LocalizedStringResource(stringLiteral: title),
            stopButton: AlarmButton(
                text: unlockText, 
                textColor: .white, 
                systemImageName: "lock.open.fill"
            )
        )
        
        /* 方案2：双按钮模式（如需恢复，取消注释此部分并注释上面的 alertContent）
        let alertContent = AlarmPresentation.Alert(
            title: LocalizedStringResource(stringLiteral: title),
            stopButton: AlarmButton(text: "停止", textColor: .white, systemImageName: "stop.circle"),
            secondaryButton: AlarmButton(text: "查看", textColor: .black, systemImageName: "eye.fill"),
            secondaryButtonBehavior: .custom
        )
        */
        
        let presentation = AlarmPresentation(alert: alertContent)
        
        // 创建 attributes
        let attributes = AlarmAttributes(
            presentation: presentation,
            metadata: WakeupAlarmMetadata(alarmLabel: alarm.label),
            tintColor: Color.blue
        )
        
        // 创建解锁意图（打开应用并启动任务）
        let unlockIntent = ViewAlarmAppIntent(alarmId: alarm.id)
        
        // 随机选择一个声音并保存，用于应用内播放保持一致
        let selectedSound = AlarmSound.randomAvailable()
        currentAlarmSound = selectedSound
        
        // 获取自定义声音
        let customSound = getAlarmSound(for: selectedSound)
        
        // 创建配置（包含自定义声音）
        // 注意：参数顺序必须是 schedule, attributes, stopIntent, sound
        let config = AlarmKit.AlarmManager.AlarmConfiguration<WakeupAlarmMetadata>(
            schedule: schedule,
            attributes: attributes,
            stopIntent: unlockIntent,
            sound: customSound
        )
        
        #if DEBUG
        print("🔊 AlarmKit 使用声音: \(selectedSound.displayName)")
        #endif
        
        return config
    }
    
    /// 获取 AlarmKit 闹钟声音
    /// - Parameter sound: 指定的声音类型
    /// - Returns: 自定义声音或默认声音
    private func getAlarmSound(for sound: AlarmSound) -> AlertConfiguration.AlertSound {
        // 如果是程序生成的音效，使用默认声音
        guard !sound.isGenerated else {
            return AlertConfiguration.AlertSound.default
        }
        
        // 使用自定义声音文件
        return AlertConfiguration.AlertSound.named(sound.fileNameWithExtension)
    }
    
    /// 获取当前闹钟应该使用的声音（供应用内播放使用）
    func getCurrentSound() -> AlarmSound {
        return currentAlarmSound ?? AlarmSound.randomAvailable()
    }
}

// MARK: - 错误定义

enum AlarmKitError: Error, LocalizedError {
    case notAuthorized
    case invalidTime
    case invalidDate
    case noWeekdaysSelected
    
    var errorDescription: String? {
        switch self {
        case .notAuthorized:
            return "未授予 AlarmKit 权限"
        case .invalidTime:
            return "无效的时间格式"
        case .invalidDate:
            return "无效的日期"
        case .noWeekdaysSelected:
            return "未选择任何星期"
        }
    }
}
