//
//  WakeupClockApp.swift
//  WakeupClock
//
//  Created by Phoenix
//  Copyright © 2024 WakeupClock. All rights reserved.
//

import SwiftUI
import SwiftData
import UserNotifications

// 通知代理类（单例，避免被释放）
class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()
    var container: ModelContainer?
    
    private override init() {
        super.init()
    }
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        // 用户点击通知后的处理（目前不需要特殊处理）
        completionHandler()
    }
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // 即使应用在前台也显示通知
        completionHandler([.banner, .sound, .badge])
    }
}

@main
struct WakeupClockApp: App {
    // 配置SwiftData模型容器
    let container: ModelContainer
    
    init() {
        // 定义数据模型
        let schema = Schema([
            AlarmModel.self,
            WakeUpRecord.self,
            AppSettings.self
        ])
        
        // 创建模型配置
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)

        // 优雅处理初始化错误，避免崩溃
        do {
            container = try ModelContainer(for: schema, configurations: [config])
        } catch {
            // 如果初始化失败，创建一个内存存储的容器作为后备方案
            // 这样应用不会崩溃，但数据不会持久化
            let fallbackConfig = ModelConfiguration(schema: schema, isStoredInMemoryOnly: true)
            do {
                container = try ModelContainer(for: schema, configurations: [fallbackConfig])
            } catch {
                // 如果连内存存储都失败，使用默认配置
                container = try! ModelContainer(for: schema, configurations: [ModelConfiguration(schema: schema)])
            }
        }
        
        // 确保 AppSettings 存在
        initializeAppSettings()

        // 方案A：首次启动自动跟随系统语言；用户手动选择后固定
        InitializeAppLanguageIfNeeded()
        
        // 请求 AlarmKit 权限
        if #available(iOS 26.0, *) {
            Task {
                _ = await AlarmKitManager.shared.requestAuthorization()
            }
        }
        
        // 预加载节假日数据
        HolidayChecker.preloadHolidays()
        
        // 设置通知代理（使用单例避免被释放）
        NotificationDelegate.shared.container = container
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
    }
    
    /// 初始化应用设置（确保设置对象存在）
    private func initializeAppSettings() {
        let context = container.mainContext
        
        // 检查是否已有设置
        let descriptor = FetchDescriptor<AppSettings>()
        do {
            let existingSettings = try context.fetch(descriptor)
            if existingSettings.isEmpty {
                // 创建默认设置
                let defaultSettings = AppSettings()
                context.insert(defaultSettings)
                try context.save()
                #if DEBUG
                print("✅ 已创建默认应用设置")
                #endif
            }
        } catch {
            #if DEBUG
            print("❌ 初始化应用设置失败: \(error)")
            #endif
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .modelContainer(container)
                .environmentObject(AlarmManager.shared)
                .environmentObject(SoundManager.shared)
                .environmentObject(UserStatsManager.shared)
                .environmentObject(ThemeManager.shared)
        }
    }
}
