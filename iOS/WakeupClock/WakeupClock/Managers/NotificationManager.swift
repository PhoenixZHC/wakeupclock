//
//  NotificationManager.swift
//  WakeupClock
//
//  通知管理器：负责本地通知的调度和管理
//

import Foundation
@preconcurrency import UserNotifications
import Combine

/// 通知管理器（单例）
@MainActor
class NotificationManager: ObservableObject {
    static let shared = NotificationManager()
    
    @Published var authorizationStatus: UNAuthorizationStatus = .notDetermined
    
    private init() {
        checkAuthorizationStatus()
    }
    
    // MARK: - 权限管理
    
    /// 请求通知权限
    func requestAuthorization() async -> Bool {
        do {
            let granted = try await UNUserNotificationCenter.current().requestAuthorization(
                options: [.alert, .sound, .badge]
            )
            
            checkAuthorizationStatus()
            return granted
        } catch {
            #if DEBUG
            print("请求通知权限失败: \(error)")
            #endif
            return false
        }
    }
    
    /// 检查授权状态
    func checkAuthorizationStatus() {
        UNUserNotificationCenter.current().getNotificationSettings { @Sendable settings in
            Task { @MainActor in
                NotificationManager.shared.authorizationStatus = settings.authorizationStatus
            }
        }
    }
    
    // MARK: - 通知调度
    
    /// 为闹钟调度通知（调度未来30天的通知，并定期更新）
    func scheduleAlarm(_ alarm: AlarmModel) {
        guard alarm.enabled else {
            cancelAlarm(alarm)
            return
        }
        
        // 先取消该闹钟的所有旧通知
        cancelAlarm(alarm)
        
        let calendar = Calendar.current
        let now = Date()
        
        // 获取备份通知设置
        let enableBackup = UserDefaults.standard.bool(forKey: "enableBackupNotifications") 
        let backupInterval = UserDefaults.standard.integer(forKey: "backupNotificationInterval")
        let backupCount = UserDefaults.standard.integer(forKey: "backupNotificationCount")
        
        // 如果未设置，使用默认值
        let shouldEnableBackup = UserDefaults.standard.object(forKey: "enableBackupNotifications") != nil ? enableBackup : true
        let interval = backupInterval > 0 ? backupInterval : 60
        let count = backupCount > 0 ? backupCount : 5
        
        // 为未来30天内的每次触发时间创建通知
        for dayOffset in 0..<30 {
            guard let checkDate = calendar.date(byAdding: .day, value: dayOffset, to: now) else { continue }
            
            guard let (hour, minute) = alarm.timeComponents else { continue }
            
            var components = calendar.dateComponents([.year, .month, .day], from: checkDate)
            components.hour = hour
            components.minute = minute
            components.second = 0
            
            guard let triggerDate = calendar.date(from: components),
                  triggerDate > now,
                  alarm.shouldTrigger(on: triggerDate) else { continue }
            
            // 创建主通知
            scheduleNotification(for: alarm, at: triggerDate, suffix: "main")
            
            // 创建备份通知（如果启用）
            if shouldEnableBackup {
                for i in 1...count {
                    if let backupDate = calendar.date(byAdding: .second, value: i * interval, to: triggerDate) {
                        scheduleNotification(for: alarm, at: backupDate, suffix: "backup\(i)")
                    }
                }
            }
        }
        
        // 设置定期更新通知的任务（当通知快用完时重新调度）
        scheduleNotificationRefresh(for: alarm)
    }
    
    /// 调度单条通知
    private func scheduleNotification(for alarm: AlarmModel, at date: Date, suffix: String) {
        let center = UNUserNotificationCenter.current()
        let calendar = Calendar.current
        
        // 创建通知内容
        let content = UNMutableNotificationContent()
        
        // 主通知和备份通知使用不同的标题
        if suffix == "main" {
            content.title = getAlarmTitle(for: alarm)
            content.body = getAlarmBody(for: alarm)
        } else {
            // 备份通知标题
            content.title = getAlarmTitle(for: alarm)
            content.body = LocalizedString("backupNotificationBody")
        }
        
        content.sound = .default
        content.categoryIdentifier = "ALARM_CATEGORY"
        content.userInfo = [
            "alarmId": alarm.id,
            "alarmTime": alarm.time,
            "alarmLabel": alarm.label,
            "notificationType": suffix
        ]
        
        // 创建日期触发器
        let dateComponents = calendar.dateComponents([.year, .month, .day, .hour, .minute, .second], from: date)
        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: false)
        
        // 创建请求（使用唯一标识符）
        let dateStr = calendar.dateComponents([.year, .month, .day], from: date)
        let identifier = "\(alarm.id)_\(dateStr.year ?? 0)_\(dateStr.month ?? 0)_\(dateStr.day ?? 0)_\(suffix)"
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        
        // 添加通知
        center.add(request) { @Sendable error in
            #if DEBUG
            if let error = error {
                print("调度通知失败 [\(suffix)]: \(error)")
            } else {
                let formatter = DateFormatter()
                formatter.dateFormat = "HH:mm:ss"
                print("✅ 通知已调度 [\(suffix)]: \(formatter.string(from: date))")
            }
            #endif
        }
    }
    
    /// 定期刷新通知（确保通知不会用完）
    private func scheduleNotificationRefresh(for alarm: AlarmModel) {
        // 使用后台任务或应用启动时重新调度
        // 这里我们会在应用启动和闹钟更新时自动重新调度
    }
    
    /// 取消闹钟的所有通知（包括备份通知）
    func cancelAlarm(_ alarm: AlarmModel) {
        let center = UNUserNotificationCenter.current()
        let alarmId = alarm.id // 提取ID，避免在闭包中捕获整个alarm对象
        
        // 获取所有待发送的通知
        center.getPendingNotificationRequests { @Sendable requests in
            // 匹配该闹钟的所有通知（包括主通知和备份通知）
            let identifiersToRemove = requests
                .filter { $0.identifier.hasPrefix("\(alarmId)_") }
                .map { $0.identifier }
            
            if !identifiersToRemove.isEmpty {
                #if DEBUG
                print("取消 \(identifiersToRemove.count) 条通知（闹钟ID: \(alarmId)）")
                #endif
                center.removePendingNotificationRequests(withIdentifiers: identifiersToRemove)
            }
        }
    }
    
    /// 获取待处理通知数量（用于调试）
    func getPendingNotificationCount(completion: @escaping (Int) -> Void) {
        UNUserNotificationCenter.current().getPendingNotificationRequests { requests in
            completion(requests.count)
        }
    }
    
    /// 获取指定闹钟的待处理通知列表（用于调试）
    func getPendingNotifications(for alarmId: String, completion: @escaping ([UNNotificationRequest]) -> Void) {
        UNUserNotificationCenter.current().getPendingNotificationRequests { requests in
            let alarmRequests = requests.filter { $0.identifier.hasPrefix("\(alarmId)_") }
            completion(alarmRequests)
        }
    }
    
    /// 重新调度所有启用的闹钟（用于应用启动时刷新通知）
    func refreshAllNotifications(for alarms: [AlarmModel]) {
        for alarm in alarms where alarm.enabled {
            scheduleAlarm(alarm)
        }
    }
    
    /// 取消所有通知
    func cancelAllNotifications() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
    }
    
    // MARK: - 辅助方法
    
    /// 获取闹钟通知标题
    private func getAlarmTitle(for alarm: AlarmModel) -> String {
        let labelKey = "alarm_msg_\(alarm.label)"
        return LocalizedString(labelKey)
    }
    
    /// 获取闹钟通知内容
    private func getAlarmBody(for alarm: AlarmModel) -> String {
        return String(format: LocalizedString("alarmBody"), alarm.time)
    }
    
    /// 设置通知类别和操作
    func setupNotificationCategories() {
        let startMissionAction = UNNotificationAction(
            identifier: "START_MISSION",
            title: LocalizedString("startMission"),
            options: [.foreground]
        )
        
        let category = UNNotificationCategory(
            identifier: "ALARM_CATEGORY",
            actions: [startMissionAction],
            intentIdentifiers: [],
            options: [.customDismissAction]
        )
        
        UNUserNotificationCenter.current().setNotificationCategories([category])
    }
    
    /// 清除应用角标
    func clearBadge() {
        UNUserNotificationCenter.current().setBadgeCount(0) { error in
            #if DEBUG
            if let error = error {
                print("清除角标失败: \(error)")
            }
            #endif
        }
    }
}
