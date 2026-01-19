//
//  AlarmModel.swift
//  WakeupClock
//
//  闹钟数据模型
//

import Foundation
import SwiftData

/// 任务类型枚举
enum MissionType: String, Codable, CaseIterable {
    case math = "MATH"
    case shake = "SHAKE"
    case memory = "MEMORY"
    case order = "ORDER"
    case typing = "TYPING"
}

/// 难度等级枚举
enum Difficulty: Int, Codable, CaseIterable {
    case easy = 1
    case medium = 2
    case hard = 3
}

/// 重复模式枚举
enum RepeatMode: String, Codable, CaseIterable {
    case once = "ONCE"
    case workdays = "WORKDAYS"
    case custom = "CUSTOM"
}

/// 闹钟数据模型
@Model
final class AlarmModel {
    /// 唯一标识符
    @Attribute(.unique) var id: String
    
    /// 闹钟时间 (格式: "HH:mm")
    var time: String
    
    /// 是否启用
    var enabled: Bool
    
    /// 标签分类 (work, date, flight, train, meeting, doctor, interview, exam, other)
    var label: String
    
    /// 任务类型
    var missionType: String // 存储为字符串，运行时转换为MissionType
    
    /// 难度等级
    var difficulty: Int // 存储为Int，运行时转换为Difficulty
    
    /// 重复模式
    var repeatMode: String // 存储为字符串，运行时转换为RepeatMode
    
    /// 自定义重复日期 (0=周日, 1=周一, ..., 6=周六)
    var customDays: [Int]
    
    /// 是否跳过节假日
    var skipHolidays: Bool
    
    /// 创建时间
    var createdAt: Date
    
    init(
        id: String = UUID().uuidString,
        time: String,
        enabled: Bool = true,
        label: String = "other",
        missionType: MissionType = .math,
        difficulty: Difficulty = .medium,
        repeatMode: RepeatMode = .workdays,
        customDays: [Int] = [],
        skipHolidays: Bool = false
    ) {
        self.id = id
        self.time = time
        self.enabled = enabled
        self.label = label
        self.missionType = missionType.rawValue
        self.difficulty = difficulty.rawValue
        self.repeatMode = repeatMode.rawValue
        self.customDays = customDays
        self.skipHolidays = skipHolidays
        self.createdAt = Date()
    }
    
    // MARK: - 计算属性（方便访问枚举类型）
    
    var missionTypeEnum: MissionType {
        get { MissionType(rawValue: missionType) ?? .math }
        set { missionType = newValue.rawValue }
    }
    
    var difficultyEnum: Difficulty {
        get { Difficulty(rawValue: difficulty) ?? .medium }
        set { difficulty = newValue.rawValue }
    }
    
    var repeatModeEnum: RepeatMode {
        get { RepeatMode(rawValue: repeatMode) ?? .workdays }
        set { repeatMode = newValue.rawValue }
    }
    
    // MARK: - 辅助方法
    
    /// 获取时间的小时和分钟
    var timeComponents: (hour: Int, minute: Int)? {
        let components = time.split(separator: ":")
        guard components.count == 2,
              let hour = Int(components[0]),
              let minute = Int(components[1]) else {
            return nil
        }
        return (hour, minute)
    }
    
    /// 检查闹钟是否应该在指定日期触发
    func shouldTrigger(on date: Date) -> Bool {
        guard enabled else { return false }
        
        guard let (hour, minute) = timeComponents else { return false }
        
        let calendar = Calendar.current
        let dateComponents = calendar.dateComponents([.year, .month, .day, .hour, .minute, .weekday], from: date)
        
        // 检查时间是否匹配
        guard dateComponents.hour == hour && dateComponents.minute == minute else {
            return false
        }
        
        // 检查重复模式
        switch repeatModeEnum {
        case .once:
            // 只响一次，检查是否是创建日期
            return calendar.isDate(createdAt, inSameDayAs: date)
            
        case .workdays:
            // 工作日：周一到周五
            if let weekday = dateComponents.weekday {
                // weekday: 1=周日, 2=周一, ..., 7=周六
                let isWorkday = (2...6).contains(weekday)
                if !isWorkday { return false }
            }
            
        case .custom:
            // 自定义日期
            if let weekday = dateComponents.weekday {
                // 转换为0=周日, 1=周一, ..., 6=周六的格式
                let dayIndex = weekday == 1 ? 0 : weekday - 1
                if !customDays.contains(dayIndex) { return false }
            }
        }
        
        // 检查是否跳过节假日（使用智能判断，考虑调休上班日）
        if skipHolidays && HolidayChecker.shouldSkipAlarm(date) {
            return false
        }
        
        return true
    }
}
