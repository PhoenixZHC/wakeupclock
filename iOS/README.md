# 醒了么 iOS 应用

这是"醒了么"闹钟应用的iOS原生版本，使用Swift和SwiftUI开发。

## 项目结构

```
iOS/WakeupClock/
├── WakeupClockApp.swift          # 应用入口
├── Models/                       # 数据模型
│   ├── AlarmModel.swift          # 闹钟模型
│   ├── WakeUpRecord.swift        # 起床记录模型
│   └── AppSettings.swift         # 应用设置模型
├── Managers/                     # 管理器类
│   ├── AlarmManager.swift        # 闹钟管理器
│   ├── NotificationManager.swift # 通知管理器
│   ├── SoundManager.swift        # 音频管理器
│   ├── UserStatsManager.swift    # 用户统计管理器
│   └── ThemeManager.swift       # 主题管理器
├── Views/                        # 视图层
│   ├── ContentView.swift         # 主内容视图
│   ├── DashboardView.swift       # 主界面
│   ├── AlarmRowView.swift        # 闹钟列表项
│   ├── AddAlarmView.swift        # 添加闹钟视图
│   ├── AlarmLockdownView.swift   # 闹钟响铃界面
│   ├── SettingsView.swift        # 设置页面
│   └── Missions/                 # 任务视图
│       ├── MathMissionView.swift      # 数学任务
│       ├── MemoryMissionView.swift    # 记忆任务
│       ├── OrderMissionView.swift     # 顺序任务
│       ├── ShakeMissionView.swift     # 摇晃任务
│       └── TypingMissionView.swift    # 打字任务
└── Utilities/                    # 工具类
    ├── HolidayChecker.swift      # 节假日检查器
    └── Localization.swift        # 国际化支持
```

## 功能特性

### 核心功能
- ✅ 闹钟管理（添加、删除、启用/禁用）
- ✅ 多种重复模式（一次、工作日、自定义）
- ✅ 跳过节假日功能
- ✅ 多种任务类型（数学、记忆、顺序、摇晃、打字）
- ✅ 起床打卡记录
- ✅ 连续天数统计
- ✅ 多语言支持（中文/英文）
- ✅ 主题切换（自动/日间/夜间）

### 技术特性
- 使用SwiftData进行数据持久化
- 使用UserNotifications进行本地通知
- 使用AVFoundation播放闹钟声音
- 使用Combine进行响应式编程
- 遵循Apple人机界面指南

## 开发环境要求

- Xcode 15.0 或更高版本
- iOS 17.0 或更高版本
- Swift 5.9 或更高版本

## 安装和运行

1. 使用Xcode打开项目
2. 选择目标设备或模拟器
3. 点击运行按钮（⌘R）

## 配置说明

### 通知权限
应用首次启动时会请求通知权限，这是闹钟功能正常工作的必要条件。

### 后台模式
应用配置了以下后台模式：
- `audio`: 允许在后台播放音频
- `processing`: 允许后台处理

### 数据存储
所有数据使用SwiftData存储在本地，包括：
- 闹钟列表
- 起床记录
- 应用设置

## 主要类说明

### AlarmManager
负责闹钟的增删改查和触发逻辑。使用定时器每秒检查是否有闹钟需要触发。

### NotificationManager
负责本地通知的调度和管理。为每个启用的闹钟调度未来7天的通知。

### SoundManager
负责闹钟声音的播放。使用AVAudioEngine生成警笛效果的声音。

### UserStatsManager
负责起床打卡记录和连续天数统计。

### ThemeManager
负责应用主题管理，支持自动、日间、夜间三种模式。

## 任务系统

应用包含5种任务类型，用户需要完成其中一种才能关闭闹钟：

1. **数学任务**: 解答数学题（根据难度不同，题目数量和复杂度不同）
2. **记忆任务**: 记住并点击发光的方块
3. **顺序任务**: 按顺序点击数字
4. **摇晃任务**: 快速点击按钮多次
5. **打字任务**: 输入显示的文本

## 注意事项

1. **后台运行限制**: iOS对后台运行有严格限制，主要依赖通知系统来触发闹钟
2. **通知可靠性**: 确保应用有通知权限，否则闹钟无法正常工作
3. **节假日数据**: 当前使用简化的节假日列表，实际应用中建议集成第三方API获取完整数据
4. **音频播放**: 确保设备音量未静音，否则可能听不到闹钟声音

## 后续优化建议

1. 集成完整的节假日API
2. 添加Widget扩展
3. 添加Shortcuts集成
4. 优化电池使用
5. 添加更多任务类型
6. 支持自定义闹钟声音
7. 添加社交分享功能

## 许可证

版权所有 © 2024 WakeupClock
