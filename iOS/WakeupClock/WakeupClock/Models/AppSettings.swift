//
//  AppSettings.swift
//  WakeupClock
//
//  应用设置数据模型
//

import Foundation
import SwiftData

/// 主题模式枚举
enum ThemeMode: String, Codable, CaseIterable {
    case auto = "auto"
    case light = "light"
    case dark = "dark"
}

/// 音量等级枚举
enum VolumeLevel: String, Codable, CaseIterable {
    case normal = "NORMAL"
    case loud = "LOUD"
    case superLoud = "SUPER_LOUD"
}

/// 应用设置数据模型
@Model
final class AppSettings {
    /// 唯一标识符（单例模式，只有一个设置对象）
    @Attribute(.unique) var id: String
    
    /// 语言设置 (zh/en)
    var language: String
    
    /// 主题模式
    var themeMode: String // 存储为字符串，运行时转换为ThemeMode
    
    /// 音量等级
    var volumeLevel: String // 存储为字符串，运行时转换为VolumeLevel
    
    /// 启用备份通知
    var enableBackupNotifications: Bool
    
    /// 备份通知间隔（秒）
    var backupNotificationInterval: Int
    
    /// 备份通知数量
    var backupNotificationCount: Int
    
    /// 启用防重新入睡功能
    var enableAntiSnooze: Bool
    
    /// 防重新入睡提醒间隔（分钟）
    var antiSnoozeInterval: Int
    
    /// 防重新入睡提醒次数
    var antiSnoozeCount: Int

    /// 是否已阅读并同意首次安全提示
    var hasAcceptedSafetyNotice: Bool = false
    
    /// 启用音量提醒功能
    var enableVolumeReminder: Bool = false
    
    /// 音量提醒阈值（0.0-1.0，低于此值会提醒）
    var volumeReminderThreshold: Double = 0.3
    
    /// 音量提醒时间（小时，默认21点即晚上9点）
    var volumeReminderHour: Int = 21
    
    /// 音量提醒时间（分钟，默认0分）
    var volumeReminderMinute: Int = 0
    
    init(
        id: String = "default",
        language: String = "zh",
        themeMode: ThemeMode = .auto,
        volumeLevel: VolumeLevel = .normal,
        enableBackupNotifications: Bool = true,
        backupNotificationInterval: Int = 60,
        backupNotificationCount: Int = 5,
        enableAntiSnooze: Bool = true,
        antiSnoozeInterval: Int = 3,
        antiSnoozeCount: Int = 2,
        hasAcceptedSafetyNotice: Bool = false,
        enableVolumeReminder: Bool = false,
        volumeReminderThreshold: Double = 0.3,
        volumeReminderHour: Int = 21,
        volumeReminderMinute: Int = 0
    ) {
        self.id = id
        self.language = language
        self.themeMode = themeMode.rawValue
        self.volumeLevel = volumeLevel.rawValue
        self.enableBackupNotifications = enableBackupNotifications
        self.backupNotificationInterval = backupNotificationInterval
        self.backupNotificationCount = backupNotificationCount
        self.enableAntiSnooze = enableAntiSnooze
        self.antiSnoozeInterval = antiSnoozeInterval
        self.antiSnoozeCount = antiSnoozeCount
        self.hasAcceptedSafetyNotice = hasAcceptedSafetyNotice
        self.enableVolumeReminder = enableVolumeReminder
        self.volumeReminderThreshold = volumeReminderThreshold
        self.volumeReminderHour = volumeReminderHour
        self.volumeReminderMinute = volumeReminderMinute
    }
    
    // MARK: - 计算属性
    
    var themeModeEnum: ThemeMode {
        get { ThemeMode(rawValue: themeMode) ?? .auto }
        set { themeMode = newValue.rawValue }
    }
    
    var volumeLevelEnum: VolumeLevel {
        get { VolumeLevel(rawValue: volumeLevel) ?? .normal }
        set { volumeLevel = newValue.rawValue }
    }
}
