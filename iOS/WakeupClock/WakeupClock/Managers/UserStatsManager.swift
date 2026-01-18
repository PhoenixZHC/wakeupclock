//
//  UserStatsManager.swift
//  WakeupClock
//
//  用户统计管理器：负责起床打卡记录和连续天数统计
//

import Foundation
import SwiftData
import Combine

/// 用户统计管理器（单例）
@MainActor
class UserStatsManager: ObservableObject {
    static let shared = UserStatsManager()
    
    @Published var history: [WakeUpRecord] = []
    @Published var streak: Int = 0
    
    private var modelContext: ModelContext?
    
    private init() {}
    
    /// 设置ModelContext
    func setup(modelContext: ModelContext) {
        self.modelContext = modelContext
        loadHistory()
        calculateStreak()
    }
    
    // MARK: - 数据操作
    
    /// 加载历史记录
    private func loadHistory() {
        guard let modelContext = modelContext else { return }
        
        let descriptor = FetchDescriptor<WakeUpRecord>(
            sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
        )
        
        do {
            history = try modelContext.fetch(descriptor)
            calculateStreak()
        } catch {
            #if DEBUG
            print("加载历史记录失败: \(error)")
            #endif
            history = []
        }
    }
    
    /// 记录一次起床
    func recordWakeUp(alarmLabel: String? = nil) {
        guard let modelContext = modelContext else { return }
        
        let now = Date()
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let dateStr = formatter.string(from: now)
        
        // 检查今天是否已经记录过
        if let existingRecord = history.first(where: { $0.date == dateStr }) {
            // 如果今天已经记录过但没有alarmLabel，更新它
            if existingRecord.alarmLabel == nil && alarmLabel != nil {
                existingRecord.alarmLabel = alarmLabel
                do {
                    try modelContext.save()
                    loadHistory()
                } catch {
                    #if DEBUG
                    print("更新记录失败: \(error)")
                    #endif
                }
            }
            return // 今天已经记录过了
        }
        
        // 创建新记录
        let record = WakeUpRecord.createFromNow(alarmLabel: alarmLabel)
        modelContext.insert(record)
        
        do {
            try modelContext.save()
            loadHistory()
        } catch {
            #if DEBUG
            print("记录起床失败: \(error)")
            #endif
        }
    }
    
    /// 清空所有数据
    func clearData() {
        guard let modelContext = modelContext else { return }
        
        for record in history {
            modelContext.delete(record)
        }
        
        do {
            try modelContext.save()
            loadHistory()
        } catch {
            #if DEBUG
            print("清空数据失败: \(error)")
            #endif
        }
    }
    
    // MARK: - 统计计算
    
    /// 计算连续天数
    private func calculateStreak() {
        guard !history.isEmpty else {
            streak = 0
            return
        }
        
        let calendar = Calendar.current
        let today = Date()
        let todayStr = dateString(from: today)
        let yesterdayStr = dateString(from: calendar.date(byAdding: .day, value: -1, to: today) ?? today)
        
        // 获取所有唯一日期并排序
        let uniqueDates = Array(Set(history.map { $0.date })).sorted(by: >)
        
        // 如果最新记录不是今天或昨天，连续天数归零
        guard let latestDate = uniqueDates.first,
              latestDate == todayStr || latestDate == yesterdayStr else {
            streak = 0
            return
        }
        
        // 计算连续天数
        var currentStreak = 0
        var checkDate = latestDate == todayStr ? today : calendar.date(byAdding: .day, value: -1, to: today) ?? today
        
        for dateStr in uniqueDates {
            let recordDate = dateFromString(dateStr) ?? today
            if calendar.isDate(recordDate, inSameDayAs: checkDate) {
                currentStreak += 1
                checkDate = calendar.date(byAdding: .day, value: -1, to: checkDate) ?? checkDate
            } else {
                break
            }
        }
        
        streak = currentStreak
    }
    
    // MARK: - 辅助方法
    
    private func dateString(from date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }
    
    private func dateFromString(_ string: String) -> Date? {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.date(from: string)
    }
}
