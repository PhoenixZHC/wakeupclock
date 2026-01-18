//
//  AlarmAppIntents.swift
//  WakeupClock
//
//  AlarmKit 意图处理：处理查看闹钟的操作
//

import Foundation
import AppIntents
import AlarmKit

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
        // 发送通知，应用会响应并显示任务界面
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
