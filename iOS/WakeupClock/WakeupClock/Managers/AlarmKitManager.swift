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
    
    /// UserDefaults 键：主闹钟 ID -> 其防赖床提醒的 AlarmKit ID 列表（删除主闹钟时用于只取消这些提醒，不影响其它闹钟）
    private static let antiSnoozeReminderIdsKey = "WakeupClock.AntiSnoozeReminderIds"
    
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
        guard let alarmUUID = UUID(uuidString: alarm.id) else {
            throw AlarmKitError.invalidAlarmId
        }
        
        try? alarmManager.cancel(id: alarmUUID)
        guard alarm.enabled else { return }
        
        let schedule = try createSchedule(for: alarm)
        let config = try createAlarmConfiguration(for: alarm, schedule: schedule)
        _ = try await alarmManager.schedule(id: alarmUUID, configuration: config)
        
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
    
    /// 按 ID 取消单条闹钟（用于防赖床提醒完成后只取消该条）
    func cancelAlarm(id: String) async throws {
        guard let uuid = UUID(uuidString: id) else { return }
        try alarmManager.cancel(id: uuid)
        #if DEBUG
        print("🗑️ AlarmKit 闹钟已取消: \(id)")
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
    
    /// 调度防赖床提醒（与主闹钟一样需完成任务才能停止；与其它闹钟时间冲突的时段不排）
    /// 注意：调用时机的 now 即「用户完成主闹钟任务的时刻」，间隔由此刻起算，避免主闹钟还在做题时提醒就响
    func scheduleAntiSnoozeReminders(
        originalAlarmId: String,
        allAlarms: [AlarmModel],
        intervalMinutes: Int,
        count: Int
    ) async throws {
        guard isAuthorized else {
            throw AlarmKitError.notAuthorized
        }
        
        let now = Date() // 完成主闹钟任务的时刻，第一次提醒 = now + interval，第二次 = now + 2*interval
        guard let originalAlarm = allAlarms.first(where: { $0.id == originalAlarmId }) else { return }
        var scheduledCount = 0
        
        for index in 1...count {
            let triggerTime = now.addingTimeInterval(TimeInterval(intervalMinutes * index * 60))
            
            // 方案一：与其它闹钟时间冲突则跳过该次提醒
            let hasConflict = allAlarms.contains { alarm in
                alarm.id != originalAlarmId && alarm.shouldTrigger(on: triggerTime)
            }
            if hasConflict {
                #if DEBUG
                let formatter = DateFormatter()
                formatter.dateFormat = "HH:mm"
                formatter.timeZone = TimeZone.current
                print("⏭️ 防赖床提醒跳过（与其它闹钟冲突）: \(formatter.string(from: triggerTime))")
                #endif
                continue
            }
            
            scheduledCount += 1
            let reminderUUID = UUID()
            let config = try createAntiSnoozeConfiguration(
                originalAlarmId: originalAlarmId,
                originalAlarmLabel: originalAlarm.label,
                reminderAlarmId: reminderUUID.uuidString,
                triggerTime: triggerTime
            )
            _ = try await alarmManager.schedule(id: reminderUUID, configuration: config)
            saveReminderId(reminderUUID.uuidString, forOriginalAlarmId: originalAlarmId)
            
            #if DEBUG
            let formatter = DateFormatter()
            formatter.dateFormat = "HH:mm:ss"
            formatter.timeZone = TimeZone.current
            print("✅ 防赖床提醒 \(scheduledCount) 已调度: \(formatter.string(from: triggerTime))")
            #endif
        }
    }
    
    /// 取消防重新入睡提醒（仅取消该主闹钟关联的提醒，不影响其它主闹钟）
    func cancelAntiSnoozeReminders(originalAlarmId: String) async throws {
        let reminderIds = consumeReminderIds(forOriginalAlarmId: originalAlarmId)
        for id in reminderIds {
            try? await cancelAlarm(id: id)
            #if DEBUG
            print("🗑️ 取消防赖床提醒: \(id)")
            #endif
        }
    }
    
    /// 将防赖床提醒 ID 存入 UserDefaults，便于删除主闹钟时只取消这些
    private func saveReminderId(_ reminderId: String, forOriginalAlarmId originalAlarmId: String) {
        var map = loadReminderIdsMap()
        map[originalAlarmId, default: []].append(reminderId)
        saveReminderIdsMap(map)
    }
    
    /// 取出并清除该主闹钟对应的所有提醒 ID
    private func consumeReminderIds(forOriginalAlarmId originalAlarmId: String) -> [String] {
        var map = loadReminderIdsMap()
        let ids = map.removeValue(forKey: originalAlarmId) ?? []
        saveReminderIdsMap(map)
        return ids
    }
    
    private func loadReminderIdsMap() -> [String: [String]] {
        guard let data = UserDefaults.standard.data(forKey: Self.antiSnoozeReminderIdsKey),
              let decoded = try? JSONDecoder().decode([String: [String]].self, from: data) else {
            return [:]
        }
        return decoded
    }
    
    private func saveReminderIdsMap(_ map: [String: [String]]) {
        guard let data = try? JSONEncoder().encode(map) else { return }
        UserDefaults.standard.set(data, forKey: Self.antiSnoozeReminderIdsKey)
    }
    
    /// 创建防重新入睡提醒配置（与主闹钟相同：解锁按钮，打开应用做任务才能停）
    private func createAntiSnoozeConfiguration(
        originalAlarmId: String,
        originalAlarmLabel: String,
        reminderAlarmId: String,
        triggerTime: Date
    ) throws -> AlarmKit.AlarmManager.AlarmConfiguration<WakeupAlarmMetadata> {
        let title = LocalizedString("alarm_msg_\(originalAlarmLabel)")
        let unlockText: LocalizedStringResource = LocalizedStringResource(stringLiteral: LocalizedString("unlockAlarm"))
        let alertContent = AlarmPresentation.Alert(
            title: LocalizedStringResource(stringLiteral: title),
            stopButton: AlarmButton(
                text: unlockText,
                textColor: .white,
                systemImageName: "lock.open.fill"
            )
        )
        let presentation = AlarmPresentation(alert: alertContent)
        let attributes = AlarmAttributes(
            presentation: presentation,
            metadata: WakeupAlarmMetadata(alarmLabel: originalAlarmLabel),
            tintColor: Color.orange
        )
        // 打开应用并显示原闹钟的任务；完成时只取消本条提醒
        let unlockIntent = ViewAlarmAppIntent(alarmId: originalAlarmId, reminderAlarmId: reminderAlarmId)
        let selectedSound = AlarmSound.randomAvailable()
        let customSound = getAlarmSound(for: selectedSound)
        return AlarmKit.AlarmManager.AlarmConfiguration(
            schedule: .fixed(triggerTime),
            attributes: attributes,
            stopIntent: unlockIntent,
            sound: customSound
        )
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
            var components = calendar.dateComponents([.year, .month, .day], from: Date())
            components.hour = hour
            components.minute = minute
            components.second = 0
            guard let triggerDate = calendar.date(from: components) else {
                throw AlarmKitError.invalidDate
            }
            let finalDate = triggerDate > Date() ? triggerDate : (calendar.date(byAdding: .day, value: 1, to: triggerDate) ?? triggerDate)
            return .fixed(finalDate)
            
        case .workdays, .custom:
            if alarm.skipHolidays {
                // 跳过节假日：只排「下一个合法日期」的 fixed，实际响铃由节假日决定
                guard let nextDate = nextValidTriggerDate(for: alarm) else {
                    throw AlarmKitError.invalidDate
                }
                return .fixed(nextDate)
            }
            
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
    
    /// 跳过节假日时：计算下一个应响铃的日期（匹配星期且非节假日）
    private func nextValidTriggerDate(for alarm: AlarmModel) -> Date? {
        guard let (hour, minute) = alarm.timeComponents else { return nil }
        let calendar = Calendar.current
        let now = Date()
        for dayOffset in 0..<60 {
            guard let day = calendar.date(byAdding: .day, value: dayOffset, to: now) else { continue }
            var comps = calendar.dateComponents([.year, .month, .day], from: day)
            comps.hour = hour
            comps.minute = minute
            comps.second = 0
            guard let trigger = calendar.date(from: comps), trigger > now else { continue }
            if alarm.shouldTrigger(on: trigger) {
                return trigger
            }
        }
        return nil
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
    case invalidAlarmId
    
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
        case .invalidAlarmId:
            return "无效的闹钟 ID"
        }
    }
}
