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
    /// 防赖床提醒的 AlarmKit UUID；非空表示当前是提醒响铃，完成任务后只取消此条
    @State private var activeReminderAlarmId: String?
    @State private var cancellables = Set<AnyCancellable>()
    @State private var showSafetyNotice = false
    @State private var showLowVolumeAlert = false
    /// 「刚响过」恢复：滑动/音量键关闭闹钟后未走 stopIntent，提示用户是否现在完成任务
    @State private var showRecentAlarmRecoveryAlert = false
    @State private var recoveryAlarm: AlarmModel?
    /// 上次检测音量的时间戳（用于防抖，避免下拉通知栏等短暂切换时重复检测）
    @State private var lastVolumeCheckTime: Date?
    
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
            checkPendingAlarm()
            checkSafetyNoticeIfNeeded()
            initializeVolumeReminder()
            AppReviewManager.shared.setRequestReviewAction { requestReview() }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                alarmManager.refreshSkipHolidaysAlarms()
            }
            // 多次延迟检查 pending，应对系统 intent 投递或 SwiftData 加载稍慢（如滑动/音量键关闭后用户再打开应用）
            scheduleDelayedPendingChecks(delays: [1.5, 3.0])
        }
        .onChange(of: scenePhase) { oldPhase, newPhase in
            if newPhase == .active && oldPhase != .active {
                checkPendingAlarm()
                checkSafetyNoticeIfNeeded()
                // 每次从非 active 回到前台都检测音量，防抖 10 秒内不重复（避免下拉通知栏等短暂切换重复提示）
                checkVolumeOnForeground()
                AppReviewManager.shared.recordAppOpen()
                alarmManager.refreshSkipHolidaysAlarms()
                scheduleDelayedPendingChecks(delays: [1.0, 2.0])
                tryShowRecentAlarmRecovery()
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
            Button(LocalizedString("lowVolumeAlertAutoAdjust")) {
                VolumeCheckManager.shared.setSystemVolume(to: 0.7)
            }
            Button(LocalizedString("lowVolumeAlertDismiss"), role: .cancel) { }
        } message: {
            Text(LocalizedString("lowVolumeAlertMessage"))
        }
        .alert(LocalizedString("recentAlarmRecoveryTitle"), isPresented: $showRecentAlarmRecoveryAlert) {
            Button(LocalizedString("recentAlarmRecoveryYes")) {
                if let alarm = recoveryAlarm {
                    SoundManager.shared.playAlarmSound(level: .normal)
                    activeAlarm = alarm
                    activeReminderAlarmId = nil
                    currentView = .alarmLockdown
                }
                recoveryAlarm = nil
            }
            Button(LocalizedString("recentAlarmRecoveryNo"), role: .cancel) {
                recoveryAlarm = nil
            }
        } message: {
            Text(LocalizedString("recentAlarmRecoveryMessage"))
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
                guard currentView != .alarmLockdown else { return }
                
                let reminderAlarmId = notification.userInfo?["reminderAlarmId"] as? String
                if let alarm = alarms.first(where: { $0.id == alarmId }) {
                    #if DEBUG
                    print("🔔 从通知触发闹钟: \(alarm.label) at \(alarm.time)" + (reminderAlarmId != nil ? " [防赖床提醒]" : ""))
                    #endif
                    SoundManager.shared.playAlarmSound(level: .normal)
                    activeAlarm = alarm
                    activeReminderAlarmId = reminderAlarmId
                    currentView = .alarmLockdown
                } else {
                    #if DEBUG
                    print("⚠️ 未找到闹钟 ID: \(alarmId)，将在稍后重试")
                    #endif
                    PendingAlarmManager.setPendingAlarm(id: alarmId, reminderId: reminderAlarmId)
                }
            }
            .store(in: &cancellables)
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
    
    /// 应用进入前台时检测音量（带防抖，10 秒内不重复检测）
    private func checkVolumeOnForeground() {
        let now = Date()
        // 防抖：10 秒内不重复检测，避免下拉通知栏等短暂切换时重复提示
        if let lastCheck = lastVolumeCheckTime, now.timeIntervalSince(lastCheck) < 10 {
            return
        }
        lastVolumeCheckTime = now
        
        let volumeManager = VolumeCheckManager.shared
        if volumeManager.checkVolumeOnAppOpen() {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                if !showSafetyNotice {
                    showLowVolumeAlert = true
                }
            }
        }
    }
    
    /// 检查是否有待处理的闹钟（用于从锁屏/后台唤醒时；内带 0.5s 延迟以等 SwiftData）
    private func checkPendingAlarm() {
        guard currentView != .alarmLockdown else { return }
        let alarmsSnapshot = alarms
        let settingsSnapshot = settings
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            guard self.currentView != .alarmLockdown else { return }
            guard PendingAlarmManager.hasPendingAlarm() else { return }
            let (alarmId, reminderAlarmId) = PendingAlarmManager.consumePendingAlarm()
            guard let alarmId = alarmId, let alarm = alarmsSnapshot.first(where: { $0.id == alarmId }) else {
                if alarmId != nil {
                    #if DEBUG
                    print("⚠️ 待处理闹钟未找到: \(alarmId!)")
                    #endif
                }
                return
            }
            // 防止延迟投递的 intent：若闹钟/防赖床的「预定触发时间」已过去过久（超过 5 分钟），不再恢复界面
            let calendar = Calendar.current
            let now = Date()
            guard let (hour, minute) = alarm.timeComponents,
                  let baseTrigger = calendar.date(bySettingHour: hour, minute: minute, second: 0, of: now) else {
                return
            }
            let interval = settingsSnapshot.first?.antiSnoozeInterval ?? 3
            let count = settingsSnapshot.first?.antiSnoozeCount ?? 2
            let latestTrigger: Date = reminderAlarmId != nil
                ? (calendar.date(byAdding: .minute, value: interval * count, to: baseTrigger) ?? baseTrigger)
                : baseTrigger
            let grace: TimeInterval = 300 // 5 分钟
            if now.timeIntervalSince(latestTrigger) > grace {
                #if DEBUG
                print("⏰ 待处理闹钟已过期（预定 \(alarm.time)" + (reminderAlarmId != nil ? " 最后防赖床约 \(interval * count) 分钟后" : "") + "），不再恢复")
                #endif
                return
            }
            #if DEBUG
            print("🔔 从待处理队列恢复闹钟: \(alarm.label) at \(alarm.time)" + (reminderAlarmId != nil ? " [防赖床提醒]" : ""))
            #endif
            // 不在此处播放声音：AlarmLockdownView.onAppear 会调用 startAlarm() 统一播放，避免重复播放
            activeAlarm = alarm
            activeReminderAlarmId = reminderAlarmId
            currentView = .alarmLockdown
        }
    }
    
    /// 在指定延迟后再次执行 checkPendingAlarm，提高 intent 延迟触达时的命中率
    private func scheduleDelayedPendingChecks(delays: [Double]) {
        for delay in delays {
            DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                self.checkPendingAlarm()
            }
        }
    }
    
    /// 若用户通过滑动或音量键关闭了系统闹钟（未走 stopIntent），进入应用后提示是否现在完成任务
    private func tryShowRecentAlarmRecovery() {
        let key = "LastRecentAlarmRecoveryShown"
        let interval: TimeInterval = 300 // 5 分钟内不重复弹
        let now = Date().timeIntervalSince1970
        if now - (UserDefaults.standard.double(forKey: key)) < interval { return }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            guard self.currentView != .alarmLockdown else { return }
            guard !PendingAlarmManager.hasPendingAlarm() else { return }
            let calendar = Calendar.current
            let nowDate = Date()
            for minuteOffset in 0..<3 {
                guard let checkDate = calendar.date(byAdding: .minute, value: -minuteOffset, to: nowDate) else { continue }
                if let alarm = alarms.first(where: { $0.enabled && $0.shouldTrigger(on: checkDate) }) {
                    UserDefaults.standard.set(now, forKey: key)
                    recoveryAlarm = alarm
                    showRecentAlarmRecoveryAlert = true
                    return
                }
            }
        }
    }
    
    private func handleMissionSolved() {
        let alarmLabel = activeAlarm?.label
        let alarmId = activeAlarm?.id
        let isReminder = activeReminderAlarmId != nil
        UserStatsManager.shared.recordWakeUp(alarmLabel: alarmLabel)
        SoundManager.shared.stopAlarmSound()
        
        if isReminder {
            // 防赖床提醒：只取消本条提醒，不调度新提醒、不修改主闹钟
            if let reminderId = activeReminderAlarmId {
                Task {
                    if #available(iOS 26.0, *) {
                        try? await AlarmKitManager.shared.cancelAlarm(id: reminderId)
                    }
                }
            }
        } else {
            // 主闹钟：若是“响一次”则禁用；并调度防赖床提醒
            if let alarm = activeAlarm, alarm.repeatModeEnum == .once {
                alarm.enabled = false
                alarmManager.updateAlarm(alarm)
            }
            // 防赖床提醒在「完成主闹钟任务」之后再调度，间隔从完成时刻起算，避免 8:00 响铃、8:03 还没做完题就响第一次提醒
            if let alarmId = alarmId {
                scheduleAntiSnoozeIfNeeded(alarmId: alarmId)
            }
            // 跳过节假日的重复闹钟：完成后重新排下一次合法日期，否则不会再响
            if let alarm = activeAlarm, alarm.skipHolidays && alarm.repeatModeEnum != .once {
                alarmManager.updateAlarm(alarm)
            }
        }
        
        withAnimation(.easeInOut(duration: 0.3)) {
            activeAlarm = nil
            activeReminderAlarmId = nil
            currentView = .dashboard
        }
    }
    
    /// 在用户完成主闹钟任务后调用；防赖床的两次提醒从「当前完成时刻」起算间隔（如完成时 8:05，间隔 3 分钟则 8:08、8:11），不会在 8:03 就响
    private func scheduleAntiSnoozeIfNeeded(alarmId: String) {
        let appSettings = settings.first ?? AppSettings()
        guard appSettings.enableAntiSnooze else { return }
        guard alarms.first(where: { $0.id == alarmId }) != nil else { return }
        
        Task {
            do {
                if #available(iOS 26.0, *) {
                    try await AlarmKitManager.shared.scheduleAntiSnoozeReminders(
                        originalAlarmId: alarmId,
                        allAlarms: alarms,
                        intervalMinutes: appSettings.antiSnoozeInterval,
                        count: appSettings.antiSnoozeCount
                    )
                }
                #if DEBUG
                print("✅ 防赖床提醒已调度")
                #endif
            } catch {
                #if DEBUG
                print("❌ 调度防赖床提醒失败: \(error)")
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
