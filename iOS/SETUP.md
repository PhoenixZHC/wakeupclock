# iOS项目设置指南

## 在Xcode中创建项目

由于这是一个纯代码项目，你需要在Xcode中手动创建项目并添加文件。以下是详细步骤：

### 1. 创建新项目

1. 打开Xcode
2. 选择 `File` > `New` > `Project...`
3. 选择 `iOS` > `App`
4. 填写项目信息：
   - **Product Name**: `WakeupClock`
   - **Team**: 选择你的开发团队
   - **Organization Identifier**: 例如 `com.yourname`
   - **Interface**: `SwiftUI`
   - **Language**: `Swift`
   - **Storage**: `SwiftData`
   - **Minimum Deployments**: `iOS 17.0`
5. 选择保存位置为 `iOS/` 目录
6. 点击 `Create`

### 2. 删除默认文件

删除Xcode自动生成的以下文件：
- `ContentView.swift`（我们将使用自己的版本）
- `WakeupClockApp.swift`（我们将使用自己的版本）

### 3. 添加项目文件

将 `iOS/WakeupClock/` 目录下的所有文件添加到Xcode项目中：

1. 在Xcode中右键点击项目根目录
2. 选择 `Add Files to "WakeupClock"...`
3. 选择 `iOS/WakeupClock/` 目录
4. 确保勾选：
   - ✅ `Copy items if needed`（如果文件不在项目目录中）
   - ✅ `Create groups`（不是folder references）
   - ✅ `Add to targets: WakeupClock`
5. 点击 `Add`

### 4. 配置项目设置

#### 4.1 Info.plist配置

1. 在项目导航器中找到 `Info.plist`
2. 确保包含以下配置：
   - `NSUserNotificationsUsageDescription`: "需要通知权限以便在闹钟时间提醒您起床"
   - `UIBackgroundModes`: 包含 `audio` 和 `processing`

#### 4.2 Capabilities配置

1. 选择项目target
2. 进入 `Signing & Capabilities` 标签
3. 添加以下Capabilities：
   - `Background Modes`
     - ✅ Audio, AirPlay, and Picture in Picture
     - ✅ Background processing
   - `Push Notifications`（如果需要）

#### 4.3 Build Settings

确保以下设置正确：
- **Swift Language Version**: Swift 5
- **iOS Deployment Target**: 17.0
- **Swift Compiler - Language**: Swift

### 5. 添加依赖

本项目使用系统框架，无需添加外部依赖：
- SwiftUI（系统框架）
- SwiftData（系统框架）
- UserNotifications（系统框架）
- AVFoundation（系统框架）
- Combine（系统框架）

### 6. 运行项目

1. 选择目标设备或模拟器（iOS 17.0+）
2. 点击运行按钮（⌘R）
3. 首次运行时会请求通知权限，请选择"允许"

## 项目结构说明

添加文件后，你的Xcode项目结构应该如下：

```
WakeupClock/
├── WakeupClockApp.swift
├── Info.plist
├── Models/
│   ├── AlarmModel.swift
│   ├── WakeUpRecord.swift
│   └── AppSettings.swift
├── Managers/
│   ├── AlarmManager.swift
│   ├── NotificationManager.swift
│   ├── SoundManager.swift
│   ├── UserStatsManager.swift
│   └── ThemeManager.swift
├── Views/
│   ├── ContentView.swift
│   ├── DashboardView.swift
│   ├── AlarmRowView.swift
│   ├── AddAlarmView.swift
│   ├── AlarmLockdownView.swift
│   ├── SettingsView.swift
│   └── Missions/
│       ├── MathMissionView.swift
│       ├── MemoryMissionView.swift
│       ├── OrderMissionView.swift
│       ├── ShakeMissionView.swift
│       └── TypingMissionView.swift
└── Utilities/
    ├── HolidayChecker.swift
    └── Localization.swift
```

## 常见问题

### Q: 编译错误 "Cannot find type 'ModelContext'"
A: 确保项目使用SwiftData，并且iOS部署目标为17.0或更高。

### Q: 通知不工作
A: 确保：
1. 应用已请求并获得通知权限
2. 设备未处于勿扰模式
3. 通知设置中已启用该应用的通知

### Q: 音频不播放
A: 确保：
1. 设备音量未静音
2. 已配置后台音频模式
3. 音频会话已正确设置

### Q: 数据不保存
A: 确保：
1. SwiftData模型已正确配置
2. ModelContainer已正确初始化
3. 有写入权限

## 测试建议

1. **通知测试**: 设置一个1-2分钟后的闹钟，锁屏后等待通知
2. **任务测试**: 触发闹钟后，测试各种任务类型
3. **数据持久化测试**: 添加闹钟后，关闭应用重新打开，检查数据是否保存
4. **主题测试**: 切换不同主题模式，检查UI是否正确更新

## 下一步

项目设置完成后，你可以：
1. 运行应用并测试基本功能
2. 根据需要调整UI和样式
3. 添加更多功能（如Widget、Shortcuts等）
4. 优化性能和电池使用

## 需要帮助？

如果遇到问题，请检查：
1. Xcode版本是否为15.0+
2. iOS部署目标是否为17.0+
3. 所有文件是否已正确添加到项目中
4. 项目设置是否正确配置
