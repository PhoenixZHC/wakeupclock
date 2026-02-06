//
//  AlarmAppIntents.swift
//  WakeupClock
//
//  AlarmKit 意图处理：处理查看闹钟的操作
//

import Foundation
import AppIntents
import AlarmKit

// MARK: - 待处理闹钟管理器

/// 管理待处理的闹钟（用于应用未完全启动时保存状态）
/// 使用静态方法直接操作 UserDefaults，避免 Swift 6 并发问题
enum PendingAlarmManager {
    private static let pendingAlarmKey = "PendingAlarmId"
    private static let pendingAlarmTimeKey = "PendingAlarmTime"
    private static let pendingReminderAlarmIdKey = "PendingReminderAlarmId"
    
    /// 设置待处理的闹钟（主闹钟或防赖床提醒）
    static func setPendingAlarm(id: String, reminderId: String? = nil) {
        UserDefaults.standard.set(id, forKey: pendingAlarmKey)
        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: pendingAlarmTimeKey)
        if let rid = reminderId {
            UserDefaults.standard.set(rid, forKey: pendingReminderAlarmIdKey)
        } else {
            UserDefaults.standard.removeObject(forKey: pendingReminderAlarmIdKey)
        }
        #if DEBUG
        print("💾 保存待处理闹钟: \(id)" + (reminderId != nil ? ", 提醒ID: \(reminderId!)" : ""))
        #endif
    }
    
    /// 获取并清除待处理的闹钟（2分钟内有效）
    static func consumePendingAlarm() -> (alarmId: String?, reminderAlarmId: String?) {
        guard let alarmId = UserDefaults.standard.string(forKey: pendingAlarmKey),
              let timestamp = UserDefaults.standard.object(forKey: pendingAlarmTimeKey) as? TimeInterval else {
            return (nil, nil)
        }
        let elapsed = Date().timeIntervalSince1970 - timestamp
        let timeout: TimeInterval = 300 // 5 分钟，给用户更多时间从锁屏/后台打开应用
        guard elapsed <= timeout else {
            clearPendingAlarm()
            #if DEBUG
            print("⏰ 待处理闹钟已超时，已清除")
            #endif
            return (nil, nil)
        }
        let reminderId = UserDefaults.standard.string(forKey: pendingReminderAlarmIdKey)
        clearPendingAlarm()
        #if DEBUG
        print("📤 消费待处理闹钟: \(alarmId)" + (reminderId != nil ? ", 提醒ID: \(reminderId!)" : ""))
        #endif
        return (alarmId, reminderId)
    }
    
    /// 清除待处理的闹钟
    static func clearPendingAlarm() {
        UserDefaults.standard.removeObject(forKey: pendingAlarmKey)
        UserDefaults.standard.removeObject(forKey: pendingAlarmTimeKey)
        UserDefaults.standard.removeObject(forKey: pendingReminderAlarmIdKey)
    }
    
    /// 检查是否有待处理的闹钟（不消费）
    static func hasPendingAlarm() -> Bool {
        guard let _ = UserDefaults.standard.string(forKey: pendingAlarmKey),
              let timestamp = UserDefaults.standard.object(forKey: pendingAlarmTimeKey) as? TimeInterval else {
            return false
        }
        let elapsed = Date().timeIntervalSince1970 - timestamp
        return elapsed <= 300
    }
}

// MARK: - 查看/解锁闹钟意图

@available(iOS 26.0, *)
struct ViewAlarmAppIntent: LiveActivityIntent {
    static var title: LocalizedStringResource = "解锁闹钟"
    static var description = IntentDescription("打开应用并显示闹钟解锁任务")
    static var openAppWhenRun = true
    
    @Parameter(title: "闹钟ID")
    var alarmId: String
    
    /// 防赖床提醒的 AlarmKit UUID；非空表示本次是防赖床提醒，完成任务后只取消此条
    @Parameter(title: "提醒闹钟ID")
    var reminderAlarmId: String?
    
    init(alarmId: String, reminderAlarmId: String? = nil) {
        self.alarmId = alarmId
        self.reminderAlarmId = reminderAlarmId
    }
    
    init() {
        self.alarmId = ""
        self.reminderAlarmId = nil
    }
    
    func perform() async throws -> some IntentResult {
        guard !alarmId.isEmpty else {
            #if DEBUG
            print("⚠️ ViewAlarmAppIntent: alarmId 为空")
            #endif
            return .result()
        }
        
        await PendingAlarmManager.setPendingAlarm(id: alarmId, reminderId: reminderAlarmId)
        
        #if DEBUG
        print("🔔 ViewAlarmAppIntent 执行: \(alarmId)" + (reminderAlarmId != nil ? ", 提醒ID: \(reminderAlarmId!)" : ""))
        #endif
        
        try? await Task.sleep(nanoseconds: 100_000_000)
        
        // 在 MainActor 闭包外构建 userInfo，避免 Swift 6 并发捕获可变变量的问题
        let finalAlarmId = alarmId
        let finalReminderAlarmId = reminderAlarmId
        await MainActor.run {
            var userInfo: [String: Any] = ["alarmId": finalAlarmId]
            if let rid = finalReminderAlarmId {
                userInfo["reminderAlarmId"] = rid
            }
            NotificationCenter.default.post(
                name: .alarmTriggeredFromAlarmKit,
                object: nil,
                userInfo: userInfo
            )
        }
        
        return .result()
    }
}

// MARK: - 通知名称扩展

extension Notification.Name {
    static let alarmTriggeredFromAlarmKit = Notification.Name("alarmTriggeredFromAlarmKit")
}
