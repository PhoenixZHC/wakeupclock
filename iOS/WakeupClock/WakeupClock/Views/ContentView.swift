//
//  ContentView.swift
//  WakeupClock
//
//  主内容视图：根据应用状态显示不同界面
//

import SwiftUI
import SwiftData
import Combine
import StoreKit

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.requestReview) private var requestReview
    @EnvironmentObject var alarmManager: AlarmManager
    @EnvironmentObject var themeManager: ThemeManager
    @Query private var alarms: [AlarmModel]
    @Query private var settings: [AppSettings]
    
    @State private var currentView: AppViewState = .dashboard
    @State private var activeAlarm: AlarmModel?
    @State private var cancellables = Set<AnyCancellable>()
    @State private var showSafetyNotice = false
    @State private var showLowVolumeAlert = false
    
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
            // 检查是否有待处理的闹钟（从锁屏状态唤醒时）
            checkPendingAlarm()
            // 首次打开安全提示
            checkSafetyNoticeIfNeeded()
            // 初始化睡前提醒并检测当前音量
            initializeVolumeReminder()
            // 设置 requestReview 环境值给 AppReviewManager
            AppReviewManager.shared.setRequestReviewAction { requestReview() }
        }
        .onChange(of: scenePhase) { oldPhase, newPhase in
            // 当应用从后台进入前台时，检查待处理的闹钟
            if newPhase == .active && oldPhase != .active {
                checkPendingAlarm()
                checkSafetyNoticeIfNeeded()
                // 检测当前音量
                checkVolumeOnForeground()
                // 记录应用打开并检查是否需要请求评价（从后台/非活跃状态进入前台时记录）
                AppReviewManager.shared.recordAppOpen()
            }
        }
        .alert(LocalizedString("safetyNoticeTitle"), isPresented: $showSafetyNotice) {
            Button(LocalizedString("safetyNoticeAgree")) {
                acceptSafetyNotice()
            }
        } message: {
            Text(LocalizedString("safetyNoticeMessage"))
        }
        .alert(LocalizedString("lowVolumeAlertTitle"), isPresented: $showLowVolumeAlert) {
            Button(LocalizedString("ok")) { }
        } message: {
            Text(LocalizedString("lowVolumeAlertMessage"))
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
            .receive(on: DispatchQueue.main)
            .sink { [self] notification in
                guard let alarmId = notification.userInfo?["alarmId"] as? String else { return }
                
                // 如果已经在闹钟界面，忽略
                guard currentView != .alarmLockdown else { return }
                
                // 尝试查找闹钟
                if let alarm = alarms.first(where: { $0.id == alarmId }) {
                    #if DEBUG
                    print("🔔 从通知触发闹钟: \(alarm.label) at \(alarm.time)")
                    #endif
                    // 播放闹钟声音
                    SoundManager.shared.playAlarmSound(level: .normal)
                    activeAlarm = alarm
                    currentView = .alarmLockdown
                } else {
                    #if DEBUG
                    print("⚠️ 未找到闹钟 ID: \(alarmId)，将在稍后重试")
                    #endif
                    // 闹钟数据可能还没加载，保存到待处理队列
                    PendingAlarmManager.setPendingAlarm(id: alarmId)
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

    // MARK: - 首次安全提示

    private func getOrCreateAppSettings() -> AppSettings {
        if let existing = settings.first {
            return existing
        }

        let created = AppSettings()
        modelContext.insert(created)
        do {
            try modelContext.save()
        } catch {
            #if DEBUG
            print("❌ 创建默认 AppSettings 失败: \(error)")
            #endif
        }
        return created
    }

    private func checkSafetyNoticeIfNeeded() {
        // 闹钟任务界面不弹窗，避免影响流程
        guard currentView != .alarmLockdown else { return }

        let appSettings = getOrCreateAppSettings()
        if appSettings.hasAcceptedSafetyNotice == false {
            showSafetyNotice = true
        }
    }

    private func acceptSafetyNotice() {
        let appSettings = getOrCreateAppSettings()
        appSettings.hasAcceptedSafetyNotice = true
        do {
            try modelContext.save()
        } catch {
            #if DEBUG
            print("❌ 保存安全提示同意状态失败: \(error)")
            #endif
        }
        showSafetyNotice = false

        // 如果此时有待处理闹钟（比如锁屏解锁触发），同意后继续进入任务
        checkPendingAlarm()
    }
    
    /// 初始化睡前提醒并检测当前音量
    private func initializeVolumeReminder() {
        let appSettings = getOrCreateAppSettings()
        let volumeManager = VolumeCheckManager.shared
        
        // 如果启用了睡前提醒，设置定时通知（会在内部检查权限）
        if appSettings.enableVolumeReminder {
            // 先请求权限（如果已授权会直接返回 true）
            Task {
                let granted = await NotificationManager.shared.requestAuthorization()
                if granted {
                    volumeManager.scheduleDailyReminder(settings: appSettings)
                }
            }
        }
        
        // 检测当前音量，如果过低则提醒
        if volumeManager.checkVolumeOnAppOpen() {
            // 延迟显示，避免与安全提示冲突
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                // 只有在没有显示安全提示时才显示音量警告
                if !showSafetyNotice {
                    showLowVolumeAlert = true
                }
            }
        }
    }
    
    /// 应用进入前台时检测音量
    private func checkVolumeOnForeground() {
        let volumeManager = VolumeCheckManager.shared
        
        // 检测当前音量，如果过低则提醒
        if volumeManager.checkVolumeOnAppOpen() {
            // 延迟显示，避免与其他弹窗冲突
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                if !showSafetyNotice {
                    showLowVolumeAlert = true
                }
            }
        }
    }
    
    /// 检查是否有待处理的闹钟（用于从锁屏状态唤醒应用时）
    private func checkPendingAlarm() {
        // 如果已经在闹钟界面，不重复检查
        guard currentView != .alarmLockdown else { return }
        
        // 延迟执行，确保 SwiftData 已加载完成
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            // 再次检查状态
            guard currentView != .alarmLockdown else { return }
            
            // 检查是否有待处理的闹钟（不消费，先检查）
            guard PendingAlarmManager.hasPendingAlarm() else { return }
            
            // 消费待处理的闹钟
            if let alarmId = PendingAlarmManager.consumePendingAlarm() {
                if let alarm = alarms.first(where: { $0.id == alarmId }) {
                    #if DEBUG
                    print("🔔 从待处理队列恢复闹钟: \(alarm.label) at \(alarm.time)")
                    #endif
                    
                    // 触发闹钟界面
                    SoundManager.shared.playAlarmSound(level: .normal)
                    activeAlarm = alarm
                    currentView = .alarmLockdown
                } else {
                    #if DEBUG
                    print("⚠️ 待处理闹钟未找到: \(alarmId)")
                    #endif
                }
            }
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
