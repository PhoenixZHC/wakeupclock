//
//  Localization.swift
//  WakeupClock
//
//  国际化支持：提供多语言字符串
//

import Foundation

/// 初始化应用语言（方案A：首次启动跟随系统；用户手动选择后固定）
func InitializeAppLanguageIfNeeded() {
    let defaults = UserDefaults.standard

    // 用户已手动设置过语言，则不覆盖
    if defaults.string(forKey: "appLanguage") != nil {
        return
    }

    // 读取系统首选语言
    let preferred = Locale.preferredLanguages.first?.lowercased() ?? ""

    // 中文（含简繁）都归为 zh，其余默认英文
    let detected: String
    if preferred.contains("zh") {
        detected = "zh"
    } else {
        detected = "en"
    }

    defaults.set(detected, forKey: "appLanguage")

    #if DEBUG
    print("🌐 初始化语言为: \(detected) (system: \(preferred))")
    #endif
}

/// 获取本地化字符串
/// - Parameters:
///   - key: 字符串键
///   - args: 格式化参数
/// - Returns: 本地化后的字符串
func LocalizedString(_ key: String, _ args: CVarArg...) -> String {
    let language = UserDefaults.standard.string(forKey: "appLanguage") ?? "zh"
    let bundle = Bundle.main
    
    // 获取本地化字符串
    var localizedString = NSLocalizedString(key, bundle: bundle, comment: "")
    
    // 如果找不到，尝试从翻译字典获取
    if localizedString == key {
        localizedString = Translations.shared.getString(for: key, language: language)
    }
    
    // 格式化参数
    if !args.isEmpty {
        return String(format: localizedString, arguments: args)
    }
    
    return localizedString
}

/// 翻译管理器
class Translations {
    static let shared = Translations()
    
    private let translations: [String: [String: String]] = [
        "zh": [
            "appName": "别睡了",
            "dataManagement": "数据管理",
            "version": "版本",
            "confirmReset": "确认重置",
            "reset": "重置",
            "slogan": "专治起床困难户",
            "myAlarms": "我的闹钟",
            "noAlarms": "暂无闹钟，快去添加一个吧！",
            "newAlarm": "新建闹钟",
            "timeLabel": "时间",
            "labelLabel": "标签",
            "repeatLabel": "重复",
            "repeatOnce": "响一次",
            "repeatWorkdays": "工作日",
            "repeatCustom": "自定义",
            "selectDaysLabel": "选择日期",
            "skipHolidaysLabel": "法定节假日不响铃",
            "skipHolidaysDesc": "智能跳过国家法定假期",
            "cancel": "取消",
            "saveAlarm": "保存闹钟",
            "delete": "删除",
            "day_1": "一", "day_2": "二", "day_3": "三", "day_4": "四", "day_5": "五", "day_6": "六", "day_0": "日",
            "everyday": "每天",
            "notSet": "未设置",
            "skipHolidaysTag": "节假日不响",
            "remainingDays": "还剩 %d天 %d小时",
            "remainingHours": "还剩 %d小时 %d分",
            "remainingMinutes": "还剩 %d分",
            "wakeUp": "该起床了",
            "earlyBird": "早起身体好",
            "getUpNow": "立刻起床！",
            "lateWarning": "再不起床就迟到了！",
            "emergency": "紧急警报",
            "noiseBombing": "噪音轰炸倒计时！",
            "startMission": "开始解除任务",
            "completeMission": "完成任务以关闭闹钟",
            "systemLocked": "别睡了 WakeGuard 系统锁定中",
            "mathMission": "任务: 数学解题",
            "crazyClick": "疯狂点击!",
            "clickInstruction": "连续点击按钮 20 次即可关闭闹钟",
            "clickMe": "点我!",
            "clicksLeft": "还剩 %d 次",
            "clear": "清除",
            "confirm": "确认",
            "wrongAnswer": "答案错误，请重试!",
            "changeQuestion": "换一题",
            "streakSentence": "已坚持准时起床 %d 天，坚持下去！",
            "alarmBody": "闹钟时间：%@",
            "label_work": "上班",
            "label_date": "约会",
            "label_flight": "赶飞机",
            "label_train": "赶火车",
            "label_meeting": "会议",
            "label_doctor": "看病",
            "label_interview": "面试",
            "label_exam": "考试",
            "label_other": "其他",
            "alarm_msg_work": "起床搬砖啦！迟到要扣钱的！",
            "alarm_msg_date": "约会别迟到，印象分很重要！",
            "alarm_msg_flight": "赶飞机啦！误机就麻烦了！",
            "alarm_msg_train": "火车不等人，快点起床！",
            "alarm_msg_meeting": "会议要开始了，精神点！",
            "alarm_msg_doctor": "预约了医生，健康第一！",
            "alarm_msg_interview": "面试加油！穿得精神点！",
            "alarm_msg_exam": "考试顺利！起来复习了！",
            "alarm_msg_other": "新的一天，该起床了！",
            "mission_MEMORY": "记忆方块",
            "mission_ORDER": "数字顺序",
            "orderInstruction": "请按顺序点击数字 (1-%d)",
            "orderReset": "顺序错误，重置!",
            "typingInstruction": "请输入上方显示的文字",
            "typingPlaceholder": "在此输入...",
            "typingError": "输入错误，请重新检查！",
            "themeMode": "显示模式",
            "themeAuto": "自动",
            "themeLight": "日间",
            "themeDark": "夜间",
            "language": "语言",
            "settings": "设置",
            "back": "返回",
            "resetData": "重置数据",
            "resetConfirm": "确定要清空所有历史数据吗？",
            "about": "关于",
            "encouragementMessage": "下定决心告别起床困难",
            "calendar": "打卡日历",
            "memoryInstruction": "记住发光的方块",
            "memoryRecall": "点击刚才发光的方块",
            "memoryReady": "准备好了",
            "done": "完成",
            "help": "帮助",
            "usageGuide": "使用指南",
            "guideSubtitle": "基于 iOS 26 系统级闹钟",
            "recommendedPractices": "✅ 系统级闹钟特性",
            "notRecommendedPractices": "⚠️ 注意事项",
            "guideTip1Title": "系统级可靠性保障",
            "guideTip1Desc": "本应用使用 iOS 26 的 AlarmKit 系统框架，闹钟由系统管理，即使应用被关闭或设备重启后也能正常工作。",
            "guideTip2Title": "自动突破静音和勿扰",
            "guideTip2Desc": "闹钟会自动突破静音模式、勿扰模式和专注模式，无需担心听不到闹钟声。系统会确保闹钟以足够的音量播放。",
            "guideTip3Title": "跨设备联动提醒",
            "guideTip3Desc": "如果您的 iPhone 与 Apple Watch 已配对，闹钟触发时会同步在 Apple Watch 上震动提醒，提供双重保障。",
            "guideTip4Title": "锁屏和灵动岛显示",
            "guideTip4Desc": "闹钟触发时会在锁屏界面和灵动岛（Dynamic Island）显示，您可以直接在锁屏上停止闹钟或查看详情。",
            "guideTip5Title": "需要授予闹钟权限",
            "guideTip5Desc": "首次使用会弹出授权请求。如果拒绝授权，需要到\"设置 > 别睡了 > 闹钟\"中手动开启。",
            "guideTip6Title": "设备关机时无法触发",
            "guideTip6Desc": "如果设备完全关机，闹钟无法触发。请确保睡前设备保持开机状态并有足够电量。",
            "guideTip7Title": "点击解锁按钮开始任务",
            "guideTip7Desc": "闹钟触发时，点击\"解锁闹钟\"按钮会打开应用并开始解锁任务。必须完成任务才能关闭闹钟。",
            "guideTip8Title": "建议充电过夜",
            "guideTip8Desc": "虽然系统闹钟很可靠，但低电量可能导致设备自动关机。建议睡前给设备充电。",
            "systemLimitations": "iOS 26 AlarmKit 技术说明",
            "systemLimitationsDesc": "本应用采用 Apple 在 iOS 26 中推出的系统级闹钟框架 AlarmKit。相比传统的通知方式，系统闹钟拥有更高的优先级和可靠性，能够突破静音、勿扰等限制。这项技术与系统自带的\"时钟\"应用使用相同的底层机制，确保您的重要事项不会被遗漏。\\n\\n注意：本应用仅支持 iOS 26.0 及以上系统版本。",
            "calendarError": "无法加载日历",
            "backupNotificationBody": "闹钟提醒：请确认是否已起床",
            "notificationSettings": "通知设置",
            "enableBackupNotifications": "启用备份通知",
            "backupNotificationsDesc": "在主通知后发送多条备份提醒，确保您不会错过闹钟",
            "notificationInterval": "通知间隔",
            "notificationCount": "通知数量",
            "seconds": "秒",
            "notifications": "条",
            "notificationDebug": "通知调试",
            "pendingNotifications": "待处理通知",
            "refresh": "刷新",
            "totalNotifications": "总通知数",
            "notificationId": "通知ID",
            "triggerTime": "触发时间",
            "unknown": "未知",
            "unlockAlarm": "解锁闹钟",
            "antiSnoozeTitle": "防赖床模式",
            "enableAntiSnooze": "启用防赖床模式",
            "antiSnoozeInterval": "提醒间隔",
            "antiSnoozeCount": "提醒次数",
            "antiSnoozeDesc": "完成任务后，系统会定期发送提醒确认您是否真的清醒，有效防止重新入睡赖床",
            "antiSnoozeReminder1": "还醒着吗？",
            "antiSnoozeReminder": "确认清醒",
            "antiSnoozeReminderLast": "最后确认",
            "imAwake": "我醒了",
            "minutes": "分钟",
            "times": "次",
            "safetyNoticeTitle": "使用提醒（请先阅读）",
            "safetyNoticeMessage": "本应用面向起床困难用户设计。闹钟响起时可能出现较大音量的提示音、震动或持续提醒，以帮助你及时醒来。\n\n如你有心脏病、高血压等心血管疾病，或对突发响声敏感，请谨慎使用，必要时请先咨询医生。\n\n为保证唤醒效果，请勿将媒体音量调得过低，并建议先测试闹铃音量与提醒方式。",
            "safetyNoticeAgree": "我已阅读并同意",
            // 闹钟声音名称
            "sound_alarm1": "闹钟 1",
            "sound_alarm2": "闹钟 2",
            "sound_alarm3": "闹钟 3",
            "sound_alarm4": "闹钟 4",
            "sound_alarm5": "闹钟 5",
            "sound_alarm6": "闹钟 6",
            "sound_alarm7": "闹钟 7",
            "sound_beep": "哔哔声",
            // 音量提醒
            "volumeReminderSectionTitle": "睡前音量提醒",
            "volumeReminderTitle": "早点休息吧",
            "enableVolumeReminder": "启用睡前提醒",
            "volumeReminderDesc": "每天在设定时间提醒您检查手机音量，确保闹钟能正常唤醒",
            "volumeReminderBody": "睡前别忘了检查手机音量，确保闹钟能正常唤醒你",
            "volumeReminderTime": "提醒时间",
            // 音量过低警告
            "lowVolumeAlertTitle": "音量较低",
            "lowVolumeAlertMessage": "当前手机音量较低，可能会影响闹钟唤醒效果，建议调高音量",
            "ok": "知道了"
        ],
        "en": [
            "appName": "Wakeup Clock",
            "dataManagement": "Data Management",
            "version": "Version",
            "confirmReset": "Confirm Reset",
            "reset": "Reset",
            "slogan": "Cure for difficulty waking up",
            "myAlarms": "My Alarms",
            "noAlarms": "No alarms yet, add one!",
            "newAlarm": "New Alarm",
            "timeLabel": "Time",
            "labelLabel": "Label",
            "repeatLabel": "Repeat",
            "repeatOnce": "Once",
            "repeatWorkdays": "Workdays",
            "repeatCustom": "Custom",
            "selectDaysLabel": "Select Days",
            "skipHolidaysLabel": "Skip Public Holidays",
            "skipHolidaysDesc": "Auto skip national holidays",
            "cancel": "Cancel",
            "saveAlarm": "Save Alarm",
            "delete": "Delete",
            "day_1": "M", "day_2": "T", "day_3": "W", "day_4": "T", "day_5": "F", "day_6": "S", "day_0": "S",
            "everyday": "Everyday",
            "notSet": "Not Set",
            "skipHolidaysTag": "Skip Holidays",
            "remainingDays": "%d days %d hours remaining",
            "remainingHours": "%d hours %d minutes remaining",
            "remainingMinutes": "%d minutes remaining",
            "wakeUp": "Time to wake up",
            "earlyBird": "Early bird gets the worm",
            "getUpNow": "Get up NOW!",
            "lateWarning": "You're gonna be late!",
            "emergency": "EMERGENCY ALERT",
            "noiseBombing": "Noise bombing countdown!",
            "startMission": "Start Mission",
            "completeMission": "Complete mission to stop alarm",
            "systemLocked": "Wakeup Clock WakeGuard Locked",
            "mathMission": "Mission: Math Solver",
            "crazyClick": "Crazy Click!",
            "clickInstruction": "Click 20 times to stop",
            "clickMe": "Click Me!",
            "clicksLeft": "%d left",
            "clear": "Clear",
            "confirm": "Confirm",
            "wrongAnswer": "Wrong answer, try again!",
            "changeQuestion": "Skip",
            "streakSentence": "Consistent on-time wake-up for %d days. Keep going!",
            "alarmBody": "Alarm time: %@",
            "label_work": "Work",
            "label_date": "Date",
            "label_flight": "Flight",
            "label_train": "Train",
            "label_meeting": "Meeting",
            "label_doctor": "Doctor",
            "label_interview": "Interview",
            "label_exam": "Exam",
            "label_other": "Other",
            "alarm_msg_work": "Time to make money! Don't be late!",
            "alarm_msg_date": "Don't be late for your date!",
            "alarm_msg_flight": "Don't miss your flight! Fly away!",
            "alarm_msg_train": "The train is leaving! Hurry!",
            "alarm_msg_meeting": "Meeting starts soon! Wake up!",
            "alarm_msg_doctor": "Doctor's appointment today!",
            "alarm_msg_interview": "Good luck on the interview! Get up!",
            "alarm_msg_exam": "Ace that exam! Time to study!",
            "alarm_msg_other": "Time to wake up! Fresh start!",
            "mission_MEMORY": "Memory Matrix",
            "mission_ORDER": "Order Tap",
            "orderInstruction": "Tap numbers in order (1-%d)",
            "orderReset": "Wrong order, reset!",
            "typingInstruction": "Type the text shown above",
            "typingPlaceholder": "Type here...",
            "typingError": "Incorrect, please check again!",
            "themeMode": "Display Mode",
            "themeAuto": "Auto",
            "themeLight": "Light",
            "themeDark": "Dark",
            "language": "Language",
            "settings": "Settings",
            "back": "Back",
            "resetData": "Reset Data",
            "resetConfirm": "Are you sure to clear all history?",
            "about": "About",
            "encouragementMessage": "Determined to overcome morning struggles",
            "calendar": "Check-in Calendar",
            "memoryInstruction": "Remember the glowing tiles",
            "memoryRecall": "Tap the tiles that were glowing",
            "memoryReady": "Ready",
            "done": "Done",
            "help": "Help",
            "usageGuide": "Usage Guide",
            "guideSubtitle": "Based on iOS 26 System-Level Alarms",
            "recommendedPractices": "✅ System Alarm Features",
            "notRecommendedPractices": "⚠️ Important Notes",
            "guideTip1Title": "System-Level Reliability",
            "guideTip1Desc": "This app uses iOS 26's AlarmKit framework. Alarms are managed by the system and will work even if the app is closed or device is restarted.",
            "guideTip2Title": "Auto Override Silent & Do Not Disturb",
            "guideTip2Desc": "Alarms automatically override Silent mode, Do Not Disturb, and Focus modes. No need to worry about missing alarms. The system ensures proper volume.",
            "guideTip3Title": "Cross-Device Synchronization",
            "guideTip3Desc": "If your iPhone is paired with Apple Watch, alarms will vibrate on your watch simultaneously, providing double assurance.",
            "guideTip4Title": "Lock Screen & Dynamic Island",
            "guideTip4Desc": "Alarms display on lock screen and Dynamic Island. You can stop alarms or view details directly from the lock screen.",
            "guideTip5Title": "Alarm Permission Required",
            "guideTip5Desc": "First use will prompt for authorization. If denied, go to Settings > WakeUp? > Alarms to enable manually.",
            "guideTip6Title": "Won't Trigger When Powered Off",
            "guideTip6Desc": "Alarms cannot trigger if device is completely powered off. Ensure device stays on with sufficient battery before sleep.",
            "guideTip7Title": "Tap Unlock to Start Mission",
            "guideTip7Desc": "When alarm rings, tap 'Unlock Alarm' button to open app and start unlock mission. Must complete mission to stop alarm.",
            "guideTip8Title": "Recommend Charging Overnight",
            "guideTip8Desc": "Although system alarms are reliable, low battery may cause automatic shutdown. Recommend charging device before sleep.",
            "systemLimitations": "iOS 26 AlarmKit Technology",
            "systemLimitationsDesc": "This app uses AlarmKit, the system-level alarm framework introduced by Apple in iOS 26. Compared to traditional notifications, system alarms have higher priority and reliability, capable of overriding Silent and Do Not Disturb modes. This technology uses the same underlying mechanism as the built-in Clock app, ensuring your important events are never missed.\\n\\nNote: This app requires iOS 26.0 or later.",
            "calendarError": "Cannot load calendar",
            "backupNotificationBody": "Alarm Reminder: Please confirm if you're awake",
            "notificationSettings": "Notification Settings",
            "enableBackupNotifications": "Enable Backup Notifications",
            "backupNotificationsDesc": "Send multiple backup reminders after main notification to ensure you don't miss the alarm",
            "notificationInterval": "Notification Interval",
            "notificationCount": "Notification Count",
            "seconds": "seconds",
            "notifications": "notifications",
            "notificationDebug": "Notification Debug",
            "pendingNotifications": "Pending Notifications",
            "refresh": "Refresh",
            "totalNotifications": "Total Notifications",
            "notificationId": "Notification ID",
            "triggerTime": "Trigger Time",
            "unknown": "Unknown",
            "unlockAlarm": "Unlock Alarm",
            "antiSnoozeTitle": "Anti-Snooze Mode",
            "enableAntiSnooze": "Enable Anti-Snooze Mode",
            "antiSnoozeInterval": "Reminder Interval",
            "antiSnoozeCount": "Reminder Count",
            "antiSnoozeDesc": "After completing tasks, system will send periodic reminders to confirm you're truly awake, preventing you from staying in bed",
            "antiSnoozeReminder1": "Still Awake?",
            "antiSnoozeReminder": "Confirm Awake",
            "antiSnoozeReminderLast": "Final Check",
            "imAwake": "I'm Awake",
            "minutes": "minutes",
            "times": "times",
            "safetyNoticeTitle": "Safety Notice (Please Read)",
            "safetyNoticeMessage": "This app is designed for people who have difficulty waking up. When an alarm rings, it may use loud sounds, vibration, or repeated reminders to help you wake up.\n\nIf you have heart disease, high blood pressure, or are sensitive to sudden loud sounds, please use with caution and consult a doctor if needed.\n\nFor best results, avoid setting your media volume too low, and test the alarm volume and reminder behavior in advance.",
            "safetyNoticeAgree": "I Understand and Agree",
            // Alarm sound names
            "sound_alarm1": "Alarm 1",
            "sound_alarm2": "Alarm 2",
            "sound_alarm3": "Alarm 3",
            "sound_alarm4": "Alarm 4",
            "sound_alarm5": "Alarm 5",
            "sound_alarm6": "Alarm 6",
            "sound_alarm7": "Alarm 7",
            "sound_beep": "Beep",
            // Volume reminder
            "volumeReminderSectionTitle": "Bedtime Volume Reminder",
            "volumeReminderTitle": "Time to Rest",
            "enableVolumeReminder": "Enable Bedtime Reminder",
            "volumeReminderDesc": "Get a daily reminder at your set time to check phone volume and ensure alarms can wake you up",
            "volumeReminderBody": "Don't forget to check your phone volume before bed to ensure alarms can wake you up",
            "volumeReminderTime": "Reminder Time",
            // Low volume alert
            "lowVolumeAlertTitle": "Low Volume",
            "lowVolumeAlertMessage": "Your phone volume is low, which may affect alarm effectiveness. Please consider increasing the volume.",
            "ok": "OK"
        ]
    ]
    
    func getString(for key: String, language: String) -> String {
        return translations[language]?[key] ?? translations["zh"]?[key] ?? key
    }
}
