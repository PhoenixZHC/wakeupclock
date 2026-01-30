# 别睡了 - 智能闹钟应用

一个功能强大的智能闹钟应用，帮助用户克服起床困难。应用包含多种任务系统，用户必须完成任务才能关闭闹钟，确保真正醒来。

本项目包含 **iOS 应用**（基于 AlarmKit）、**Android 应用**（Compose + Room）以及**官网静态服务**（Node/Express，提供版本接口与 APK 下载）。

---

## 项目结构

- **iOS/** — SwiftUI + AlarmKit，系统级闹钟
- **Android/** — Kotlin + Jetpack Compose，Material 3
- **server/** — Express 静态服务与 `/api/version` 接口

### iOS 应用目录（WakeupClock）

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
│   ├── CalendarView.swift       # 打卡日历视图
│   ├── SettingsView.swift       # 设置页面
│   ├── NotificationDebugView.swift # 通知调试视图
│   └── Missions/                 # 解锁任务视图
│       ├── MathMissionView.swift    # 数学任务
│       ├── MemoryMissionView.swift  # 记忆任务
│       ├── OrderMissionView.swift   # 顺序任务
│       ├── ShakeMissionView.swift   # 点击任务
│       └── TypingMissionView.swift  # 打字任务
├── Sounds/                       # 闹钟声音文件（alarm1.mp3 - alarm7.mp3）
├── Videos/                       # 闹钟背景视频（场景标签对应）
├── AppIntents/                   # App Intents（闹钟意图、确认清醒）
├── Utilities/                    # 节假日检查、颜色扩展、国际化
├── en.lproj/                     # 英文本地化
└── zh-Hans.lproj/                # 简体中文本地化
```

---

## 功能特性

### 核心功能
- **闹钟管理**：添加、删除、启用/禁用闹钟
- **多种重复模式**：一次、工作日、自定义日期
- **跳过节假日**：智能跳过国家法定节假日（iOS 使用节假日接口）
- **8 种闹钟标签**：上班、约会、赶飞机、赶火车、会议、看病、面试、考试
- **场景背景视频**：根据闹钟标签播放对应背景视频
- **7 种自定义声音**：随机播放，与系统闹钟保持一致
- **5 种解锁任务**：数学、记忆、顺序、摇晃、打字
- **防赖床模式**：完成任务后定期确认是否清醒
- **起床打卡**：记录每天起床时间
- **连续天数统计**：追踪连续准时起床天数
- **多语言支持**：中文/英文
- **主题切换**：自动/日间/夜间模式

### 系统级闹钟特性（iOS AlarmKit）
- 应用关闭或设备重启后仍可响铃
- 突破静音、勿扰、专注模式
- 锁屏与灵动岛显示
- Apple Watch 同步震动
- 自定义闹钟铃声

### 防赖床模式
- 提醒间隔可设：3/5/10 分钟
- 提醒次数可设：1/2/3 次
- 每次为系统级闹钟，使用自定义声音

---

## 技术架构

### iOS
| 技术 | 用途 |
|------|------|
| AlarmKit | 系统级闹钟调度与管理 |
| SwiftData | 数据持久化 |
| SwiftUI | 用户界面 |
| AVFoundation | 音频与视频播放 |
| App Intents | 闹钟交互意图 |
| Combine | 响应式数据流 |

### Android
| 技术 | 用途 |
|------|------|
| Jetpack Compose | 声明式 UI |
| Room | 本地数据库 |
| DataStore | 设置持久化 |
| Media3 (ExoPlayer) | 闹钟视频播放 |
| Navigation Compose | 页面导航 |

### 官网服务（server）
| 技术 | 用途 |
|------|------|
| Express | 静态文件与 API |
| GET /api/version | 返回 versionName、versionCode、apkUrl，供客户端检测更新 |

---

## 主要模块说明

### AlarmKitManager（iOS）
- 调度主闹钟与防赖床提醒
- 管理自定义声音与闹钟权限

### AlarmSoundManager（iOS）
- 统一管理 7 种自定义音频
- 为 AlarmKit 与应用内播放提供一致音源，支持回退到程序生成音效

### SoundManager（iOS）
- 应用内闹钟响铃：循环播放、三阶段音量递增（普通→大声→超大声）

### 任务系统
用户须完成以下任务之一才能关闭闹钟：

| 任务 | 说明 |
|------|------|
| 数学任务 | 解答数学题，难度可调 |
| 记忆任务 | 记住并点击发光的方块 |
| 顺序任务 | 按顺序点击数字 |
| 摇晃任务 | 快速点击按钮多次 |
| 打字任务 | 输入显示的文本 |

---

## 自定义闹钟声音

- 应用内置 7 种闹钟声音（`alarm1.mp3` - `alarm7.mp3`），触发时随机选择。
- 格式建议：MP3/M4A，5–30 秒，44100 Hz，便于循环播放。

---

## 开发环境要求

- **iOS**：Xcode 16+，iOS 26+，Swift 5.9+
- **Android**：Android Studio，JDK 17，minSdk 35 / targetSdk 36
- **官网服务**：Node.js，Express

---

## 许可证

[![CC BY-NC-ND 4.0](https://img.shields.io/badge/License-CC%20BY-NC-ND%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc-nd/4.0/deed.zh)

本项目采用 [知识共享 署名-非商业性使用-禁止演绎 4.0 国际许可协议](https://creativecommons.org/licenses/by-nc-nd/4.0/deed.zh) 进行许可。

- ✅ 可查看与学习代码、可分享（需注明出处）
- ❌ 不可用于商业目的、不可修改后发布

如需商业授权，请联系：2081577684@qq.com  

版权所有 © 2024-2026 别睡了 (Wakeup Clock)
