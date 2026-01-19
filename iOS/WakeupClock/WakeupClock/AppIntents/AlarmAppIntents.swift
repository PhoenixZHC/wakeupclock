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
class PendingAlarmManager {
    static let shared = PendingAlarmManager()
    
    private let pendingAlarmKey = "PendingAlarmId"
    private let pendingAlarmTimeKey = "PendingAlarmTime"
    
    private init() {}
    
    /// 设置待处理的闹钟
    func setPendingAlarm(id: String) {
        UserDefaults.standard.set(id, forKey: pendingAlarmKey)
        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: pendingAlarmTimeKey)
        
        #if DEBUG
        print("💾 保存待处理闹钟: \(id)")
        #endif
    }
    
    /// 获取并清除待处理的闹钟（2分钟内有效）
    func consumePendingAlarm() -> String? {
        guard let alarmId = UserDefaults.standard.string(forKey: pendingAlarmKey),
              let timestamp = UserDefaults.standard.object(forKey: pendingAlarmTimeKey) as? TimeInterval else {
            return nil
        }
        
        // 检查是否在 2 分钟内
        let elapsed = Date().timeIntervalSince1970 - timestamp
        guard elapsed <= 120 else {
            // 超时，清除
            clearPendingAlarm()
            #if DEBUG
            print("⏰ 待处理闹钟已超时，已清除")
            #endif
            return nil
        }
        
        // 清除并返回
        clearPendingAlarm()
        
        #if DEBUG
        print("📤 消费待处理闹钟: \(alarmId)")
        #endif
        
        return alarmId
    }
    
    /// 清除待处理的闹钟
    func clearPendingAlarm() {
        UserDefaults.standard.removeObject(forKey: pendingAlarmKey)
        UserDefaults.standard.removeObject(forKey: pendingAlarmTimeKey)
    }
    
    /// 检查是否有待处理的闹钟（不消费）
    func hasPendingAlarm() -> Bool {
        guard let _ = UserDefaults.standard.string(forKey: pendingAlarmKey),
              let timestamp = UserDefaults.standard.object(forKey: pendingAlarmTimeKey) as? TimeInterval else {
            return false
        }
        
        let elapsed = Date().timeIntervalSince1970 - timestamp
        return elapsed <= 120
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
    
    init(alarmId: String) {
        self.alarmId = alarmId
    }
    
    init() {
        self.alarmId = ""
    }
    
    func perform() async throws -> some IntentResult {
        // 验证 alarmId 不为空
        guard !alarmId.isEmpty else {
            #if DEBUG
            print("⚠️ ViewAlarmAppIntent: alarmId 为空")
            #endif
            return .result()
        }
        
        // 先保存到 UserDefaults，确保即使应用未完全启动也不会丢失
        PendingAlarmManager.shared.setPendingAlarm(id: alarmId)
        
        #if DEBUG
        print("🔔 ViewAlarmAppIntent 执行: \(alarmId)")
        #endif
        
        // 延迟发送通知，给应用一点时间初始化
        try? await Task.sleep(nanoseconds: 100_000_000) // 0.1 秒
        
        // 发送通知（如果应用已在前台运行）
        await MainActor.run {
            NotificationCenter.default.post(
                name: .alarmTriggeredFromAlarmKit,
                object: nil,
                userInfo: ["alarmId": alarmId]
            )
        }
        
        return .result()
    }
}

// MARK: - 通知名称扩展

extension Notification.Name {
    static let alarmTriggeredFromAlarmKit = Notification.Name("alarmTriggeredFromAlarmKit")
}
