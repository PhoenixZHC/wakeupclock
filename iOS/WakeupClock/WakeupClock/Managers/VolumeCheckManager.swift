//
//  VolumeCheckManager.swift
//  WakeupClock
//
//  睡前音量提醒管理器：
//  1. 每天定时发送睡前提醒通知
//  2. 打开应用时检测音量，如果过低则提示用户
//

import Foundation
import AVFoundation
import UserNotifications
import Combine

/// 音量检测管理器（单例）
@MainActor
class VolumeCheckManager: ObservableObject {
    static let shared = VolumeCheckManager()
    
    /// 音量过低的阈值（低于 30% 认为过低）
    private let lowVolumeThreshold: Float = 0.3
    
    /// 是否显示音量过低警告
    @Published var showLowVolumeAlert: Bool = false
    @Published var currentVolume: Float = 0.0
    
    private let audioSession = AVAudioSession.sharedInstance()
    
    private init() {
        // 配置音频会话以获取音量信息
        do {
            try audioSession.setCategory(.ambient, mode: .default)
            try audioSession.setActive(true)
        } catch {
            #if DEBUG
            print("❌ 配置音频会话失败: \(error)")
            #endif
        }
    }
    
    // MARK: - 音量检测
    
    /// 获取当前媒体音量（0.0-1.0）
    func getCurrentVolume() -> Float {
        return audioSession.outputVolume
    }
    
    /// 检查当前音量是否过低（打开应用时调用）
    /// 返回 true 表示音量过低需要提醒
    func checkVolumeOnAppOpen() -> Bool {
        currentVolume = getCurrentVolume()
        let isLow = currentVolume < lowVolumeThreshold
        
        #if DEBUG
        print("🔊 检测当前音量: \(Int(currentVolume * 100))%，阈值: \(Int(lowVolumeThreshold * 100))%，是否过低: \(isLow)")
        #endif
        
        return isLow
    }
    
    // MARK: - 睡前定时提醒
    
    /// 设置每日睡前提醒通知
    func scheduleDailyReminder(settings: AppSettings) {
        guard settings.enableVolumeReminder else {
            #if DEBUG
            print("睡前提醒功能未启用")
            #endif
            cancelDailyReminder()
            return
        }
        
        let center = UNUserNotificationCenter.current()
        
        Task {
            // 1. 先检查通知权限
            let notificationSettings = await center.notificationSettings()
            
            #if DEBUG
            print("📱 通知权限状态: \(notificationSettings.authorizationStatus.rawValue)")
            // 0 = notDetermined, 1 = denied, 2 = authorized, 3 = provisional, 4 = ephemeral
            #endif
            
            guard notificationSettings.authorizationStatus == .authorized ||
                  notificationSettings.authorizationStatus == .provisional else {
                #if DEBUG
                print("⚠️ 通知权限未授予，无法调度睡前提醒")
                #endif
                return
            }
            
            // 2. 取消之前的通知
            center.removePendingNotificationRequests(withIdentifiers: ["bedtime_volume_reminder"])
            
            // 等待一小段时间确保取消完成
            try? await Task.sleep(nanoseconds: 100_000_000) // 0.1秒
            
            // 3. 创建日期触发器（每天重复在指定时间）
            var dateComponents = DateComponents()
            dateComponents.hour = settings.volumeReminderHour
            dateComponents.minute = settings.volumeReminderMinute
            
            let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
            
            // 4. 创建通知内容
            let content = UNMutableNotificationContent()
            content.title = LocalizedString("volumeReminderTitle")
            content.body = LocalizedString("volumeReminderBody")
            content.sound = .default
            content.categoryIdentifier = "VOLUME_REMINDER"
            content.interruptionLevel = .timeSensitive
            
            let request = UNNotificationRequest(
                identifier: "bedtime_volume_reminder",
                content: content,
                trigger: trigger
            )
            
            // 5. 添加通知
            do {
                try await center.add(request)
                
                #if DEBUG
                print("✅ 已调度每日睡前提醒，时间: \(String(format: "%02d:%02d", settings.volumeReminderHour, settings.volumeReminderMinute))")
                
                // 打印待处理的通知以便调试
                let pending = await center.pendingNotificationRequests()
                print("📋 当前待处理通知数量: \(pending.count)")
                for req in pending {
                    if let calendarTrigger = req.trigger as? UNCalendarNotificationTrigger {
                        print("   - \(req.identifier): \(req.content.title) @ \(calendarTrigger.dateComponents.hour ?? 0):\(calendarTrigger.dateComponents.minute ?? 0)")
                    } else {
                        print("   - \(req.identifier): \(req.content.title)")
                    }
                }
                #endif
            } catch {
                #if DEBUG
                print("❌ 调度睡前提醒失败: \(error)")
                #endif
            }
        }
    }
    
    /// 取消睡前提醒
    func cancelDailyReminder() {
        let center = UNUserNotificationCenter.current()
        center.removePendingNotificationRequests(withIdentifiers: ["bedtime_volume_reminder"])
    }
}
