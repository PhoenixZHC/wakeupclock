//
//  ContentView.swift
//  WakeupClock
//
//  主内容视图：根据应用状态显示不同界面
//

import SwiftUI
import SwiftData
import Combine

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @EnvironmentObject var alarmManager: AlarmManager
    @EnvironmentObject var themeManager: ThemeManager
    @Query private var alarms: [AlarmModel]
    @Query private var settings: [AppSettings]
    
    @State private var currentView: AppViewState = .dashboard
    @State private var activeAlarm: AlarmModel?
    @State private var cancellables = Set<AnyCancellable>()
    
    var body: some View {
        ZStack {
            // 使用ZStack而不是Group，避免视图切换时的布局问题
            switch currentView {
            case .dashboard:
                DashboardView()
                    .environmentObject(alarmManager)
                    .environmentObject(themeManager)
                    .transition(.opacity)
                
            case .settings:
                SettingsView()
                    .environmentObject(themeManager)
                    .environmentObject(userStatsManager)
                    .transition(.opacity)
                
            case .alarmLockdown:
                if let alarm = activeAlarm {
                    AlarmLockdownView(alarm: alarm) {
                        handleMissionSolved()
                    }
                    .environmentObject(themeManager)
                    .environmentObject(SoundManager.shared)
                    .transition(.opacity)
                    .ignoresSafeArea() // 确保全屏显示
                }
            }
        }
        .preferredColorScheme(themeManager.isDark ? .dark : .light)
        .onAppear {
            setupManagers()
            observeAlarmKitIntents()
        }
    }
    
    // MARK: - 设置方法
    
    private func setupManagers() {
        // 设置ModelContext给各个管理器
        alarmManager.setup(modelContext: modelContext)
        UserStatsManager.shared.setup(modelContext: modelContext)
    }
    
    private func observeAlarmKitIntents() {
        // 监听 AlarmKit 解锁意图触发的闹钟
        NotificationCenter.default.publisher(for: .alarmTriggeredFromAlarmKit)
            .sink { notification in
                if let alarmId = notification.userInfo?["alarmId"] as? String,
                   let alarm = alarms.first(where: { $0.id == alarmId }) {
                    // 播放闹钟声音
                    SoundManager.shared.playAlarmSound(level: .normal)
                    activeAlarm = alarm
                    currentView = .alarmLockdown
                }
            }
            .store(in: &cancellables)
        
        // 监听确认清醒意图
        NotificationCenter.default.publisher(for: .confirmAwake)
            .sink { notification in
                guard let originalAlarmId = notification.userInfo?["originalAlarmId"] as? String,
                      let reminderIndex = notification.userInfo?["reminderIndex"] as? Int else {
                    return
                }
                
                // 用户确认清醒，取消剩余的提醒
                Task {
                    await handleConfirmAwake(originalAlarmId: originalAlarmId, reminderIndex: reminderIndex)
                }
            }
            .store(in: &cancellables)
    }
    
    private func handleConfirmAwake(originalAlarmId: String, reminderIndex: Int) async {
        // 取消所有防重新入睡提醒
        do {
            try await AlarmKitManager.shared.cancelAntiSnoozeReminders(originalAlarmId: originalAlarmId)
            #if DEBUG
            print("✅ 用户确认清醒，已取消剩余提醒")
            #endif
        } catch {
            #if DEBUG
            print("❌ 取消防重新入睡提醒失败: \(error)")
            #endif
        }
    }
    
    private func handleMissionSolved() {
        // 任务完成后的处理
        let alarmLabel = activeAlarm?.label
        let alarmId = activeAlarm?.id
        UserStatsManager.shared.recordWakeUp(alarmLabel: alarmLabel)
        SoundManager.shared.stopAlarmSound()
        
        // 如果是"响一次"模式，禁用闹钟
        if let alarm = activeAlarm, alarm.repeatModeEnum == .once {
            alarm.enabled = false
            alarmManager.updateAlarm(alarm)
        }
        
        // 调度防重新入睡提醒
        if let alarmId = alarmId {
            scheduleAntiSnoozeIfNeeded(alarmId: alarmId)
        }
        
        // 使用withAnimation确保流畅过渡，避免界面错位
        withAnimation(.easeInOut(duration: 0.3)) {
            activeAlarm = nil
            currentView = .dashboard
        }
    }
    
    private func scheduleAntiSnoozeIfNeeded(alarmId: String) {
        // 获取设置
        let appSettings = settings.first ?? AppSettings()
        
        // 如果启用了防重新入睡功能
        guard appSettings.enableAntiSnooze else { return }
        
        Task {
            do {
                try await AlarmKitManager.shared.scheduleAntiSnoozeReminders(
                    originalAlarmId: alarmId,
                    intervalMinutes: appSettings.antiSnoozeInterval,
                    count: appSettings.antiSnoozeCount
                )
                
                #if DEBUG
                print("✅ 防重新入睡提醒已调度")
                #endif
            } catch {
                #if DEBUG
                print("❌ 调度防重新入睡提醒失败: \(error)")
                #endif
            }
        }
    }
    
    private var userStatsManager: UserStatsManager {
        UserStatsManager.shared
    }
}

/// 应用视图状态
enum AppViewState {
    case dashboard
    case settings
    case alarmLockdown
}
