<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />
</div>

# 醒了么 - 智能闹钟应用

一个功能强大的智能闹钟 iOS 应用，帮助用户克服起床困难。应用包含多种任务系统，用户必须完成任务才能关闭闹钟，确保真正醒来。

基于 iOS 26 的 **AlarmKit** 系统级闹钟框架开发，提供与系统闹钟同等的可靠性。

## 项目结构

```
iOS/WakeupClock/WakeupClock/
├── WakeupClockApp.swift          # 应用入口
├── Info.plist                    # 应用配置
├── Models/                       # 数据模型
│   ├── AlarmModel.swift          # 闹钟模型
│   ├── AlarmMetadata.swift       # AlarmKit 元数据
│   ├── WakeUpRecord.swift        # 起床记录模型
│   └── AppSettings.swift         # 应用设置模型
├── Managers/                     # 管理器类
│   ├── AlarmManager.swift        # 闹钟管理器
│   ├── AlarmKitManager.swift     # AlarmKit 系统闹钟管理器
│   ├── AlarmSoundManager.swift   # 闹钟声音资源管理器
│   ├── NotificationManager.swift # 通知管理器
│   ├── SoundManager.swift        # 音频播放管理器
│   ├── UserStatsManager.swift    # 用户统计管理器
│   └── ThemeManager.swift        # 主题管理器
├── Views/                        # 视图层
│   ├── ContentView.swift         # 主内容视图
│   ├── DashboardView.swift       # 主界面
│   ├── AlarmRowView.swift        # 闹钟列表项
│   ├── AddAlarmView.swift        # 添加闹钟视图
│   ├── AlarmLockdownView.swift   # 闹钟响铃界面（含背景视频）
│   ├── CalendarView.swift        # 打卡日历视图
│   ├── SettingsView.swift        # 设置页面
│   ├── NotificationDebugView.swift # 通知调试视图
│   └── Missions/                 # 解锁任务视图
│       ├── MathMissionView.swift      # 数学任务
│       ├── MemoryMissionView.swift    # 记忆任务
│       ├── OrderMissionView.swift     # 顺序任务
│       ├── ShakeMissionView.swift     # 点击任务
│       └── TypingMissionView.swift    # 打字任务
├── Sounds/                       # 闹钟声音文件
│   ├── alarm1.mp3 - alarm7.mp3   # 7 种自定义闹钟声音
├── Videos/                       # 闹钟背景视频
│   ├── work.mp4                  # 上班场景
│   ├── date.mp4                  # 约会场景
│   ├── flight.mp4                # 赶飞机场景
│   ├── train.mp4                 # 赶火车场景
│   ├── meeting.mp4               # 会议场景
│   ├── doctor.mp4                # 看病场景
│   ├── interview.mp4             # 面试场景
│   └── exam.mp4                  # 考试场景
├── AppIntents/                   # App Intents
│   ├── AlarmAppIntents.swift     # 闹钟相关意图
│   └── ConfirmAwakeIntent.swift  # 确认清醒意图
├── Utilities/                    # 工具类
│   ├── HolidayChecker.swift      # 节假日检查器
│   ├── ColorExtension.swift      # 颜色扩展
│   └── Localization.swift        # 国际化支持
├── en.lproj/                     # 英文本地化
└── zh-Hans.lproj/                # 简体中文本地化
```

## 功能特性

### 核心功能
- ✅ **闹钟管理**: 添加、删除、启用/禁用闹钟
- ✅ **多种重复模式**: 一次、工作日、自定义日期
- ✅ **跳过节假日**: 智能跳过国家法定节假日
- ✅ **8 种闹钟标签**: 上班、约会、赶飞机、赶火车、会议、看病、面试、考试
- ✅ **场景背景视频**: 根据闹钟标签播放对应的背景视频
- ✅ **7 种自定义声音**: 随机播放，AlarmKit 和应用内保持一致
- ✅ **5 种解锁任务**: 数学、记忆、顺序、摇晃、打字
- ✅ **防赖床模式**: 完成任务后定期确认是否清醒
- ✅ **起床打卡**: 记录每天起床时间
- ✅ **连续天数统计**: 追踪连续准时起床的天数
- ✅ **多语言支持**: 中文/英文
- ✅ **主题切换**: 自动/日间/夜间模式

### AlarmKit 系统级闹钟特性
- ✅ **系统级可靠性**: 即使应用被关闭或设备重启也能正常工作
- ✅ **突破静音模式**: 自动突破静音、勿扰和专注模式
- ✅ **锁屏显示**: 在锁屏界面和灵动岛显示闹钟
- ✅ **Apple Watch 联动**: 配对的 Apple Watch 同步震动提醒
- ✅ **自定义声音**: 支持自定义闹钟铃声

### 防赖床模式
完成闹钟任务后，系统会定期发送提醒确认是否真的清醒：
- 可设置提醒间隔：1/2/3/5 分钟
- 可设置提醒次数：1/2/3/4 次
- 每次提醒都是系统级闹钟，使用自定义声音

## 开发环境要求

- **Xcode**: 16.0 或更高版本
- **iOS**: 26.0 或更高版本
- **Swift**: 5.9 或更高版本

## 安装和运行

1. 使用 Xcode 打开 `iOS/WakeupClock/WakeupClock.xcodeproj`
2. 选择目标设备（推荐真机测试）
3. 点击运行按钮（⌘R）

## 技术架构

| 技术 | 用途 |
|------|------|
| **AlarmKit** | 系统级闹钟调度和管理 |
| **SwiftData** | 数据持久化 |
| **SwiftUI** | 用户界面 |
| **AVFoundation** | 音频和视频播放 |
| **App Intents** | 闹钟交互意图 |
| **Combine** | 响应式编程 |

## 主要模块说明

### AlarmKitManager
负责 iOS 26+ 系统级闹钟的调度和管理：
- 调度主闹钟和防赖床提醒
- 管理自定义声音
- 处理闹钟权限

### AlarmSoundManager
统一管理闹钟声音资源：
- 支持 7 种自定义音频文件（alarm1-7.mp3）
- 为 AlarmKit 和应用内播放提供一致的声音
- 自动回退到程序生成的音效

### SoundManager
负责应用内闹钟声音播放：
- 支持音频文件循环播放
- 三阶段音量递增（普通→大声→超大声）
- 与 AlarmKit 声音保持一致

## 任务系统

用户必须完成以下任务之一才能关闭闹钟：

| 任务 | 说明 |
|------|------|
| **数学任务** | 解答数学题，难度可调 |
| **记忆任务** | 记住并点击发光的方块 |
| **顺序任务** | 按顺序点击数字 |
| **摇晃任务** | 快速点击按钮多次 |
| **打字任务** | 输入显示的文本 |

## 自定义闹钟声音

### 声音文件
应用内置 7 种闹钟声音（`alarm1.mp3` - `alarm7.mp3`），闹钟触发时随机选择。

### 添加/替换声音
1. 将音频文件放入 `WakeupClock/Sounds/` 文件夹
2. 在 Xcode 中添加到项目（勾选 "Copy items if needed" 和 "Add to targets"）

### 文件要求
- **格式**: MP3 或 M4A
- **时长**: 5-30 秒（循环播放）
- **采样率**: 44100 Hz

## 注意事项

1. **AlarmKit 权限**: 首次使用需授予闹钟权限
2. **系统版本**: 需要 iOS 26.0 或更高版本
3. **真机测试**: 建议在真机上测试闹钟功能
4. **电量**: 确保设备有足够电量，低电量可能导致设备关机

## 许可证

版权所有 © 2024-2026 WakeupClock
