//
//  ConfirmAwakeIntent.swift
//  WakeupClock
//
//  确认清醒意图：处理防重新入睡的确认操作
//

import Foundation
import AppIntents
import AlarmKit

// MARK: - 确认清醒意图

@available(iOS 26.0, *)
struct ConfirmAwakeAppIntent: LiveActivityIntent {
    static var title: LocalizedStringResource = "确认清醒"
    static var description = IntentDescription("确认您已经清醒")
    static var openAppWhenRun = false // 不需要打开应用，只需确认
    
    @Parameter(title: "原闹钟ID")
    var originalAlarmId: String
    
    @Parameter(title: "提醒序号")
    var reminderIndex: Int
    
    init(originalAlarmId: String, reminderIndex: Int) {
        self.originalAlarmId = originalAlarmId
        self.reminderIndex = reminderIndex
    }
    
    init() {
        self.originalAlarmId = ""
        self.reminderIndex = 0
    }
    
    func perform() async throws -> some IntentResult {
        // 发送确认通知
        await MainActor.run {
            NotificationCenter.default.post(
                name: .confirmAwake,
                object: nil,
                userInfo: [
                    "originalAlarmId": originalAlarmId,
                    "reminderIndex": reminderIndex
                ]
            )
        }
        
        return .result()
    }
}

// MARK: - 通知名称扩展

extension Notification.Name {
    static let confirmAwake = Notification.Name("confirmAwake")
}
