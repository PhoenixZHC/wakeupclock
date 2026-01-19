# App Store 上架完整指南

## 📋 准备工作清单

在开始之前，请确保你已经准备好：
- ✅ 苹果开发者账号（已拥有）
- ✅ 应用图标（1024x1024 像素，已配置深色模式）
- ✅ 应用截图（至少需要 iPhone 和 iPad 的截图）
- ✅ 应用描述（中英文）
- ✅ 隐私政策 URL
- ✅ 技术支持 URL

---

## 应用基本信息

| 项目 | 内容 |
|------|------|
| **应用名称（中文）** | 起了么 |
| **应用名称（英文）** | Wakeup Clock |
| **Bundle ID** | `com.haoce.WakeupClock` |
| **SKU** | `WakeupClock-001` |
| **主要语言** | 简体中文 |
| **支持语言** | 简体中文、英语 |
| **最低系统版本** | iOS 26.0 |
| **支持设备** | iPhone、iPad |
| **应用分类** | 效率 / 生活 |
| **内容分级** | 4+ |
| **开发团队** | 6JD336D7RQ |

---

## 隐私政策和技术支持 URL

### 使用 GitHub Pages 部署

将项目中的 `docs` 文件夹部署到 GitHub Pages：

1. 在 GitHub 仓库设置中启用 Pages
2. 选择 `main` 分支的 `/docs` 文件夹
3. 获得以下 URL：

| 页面 | URL |
|------|-----|
| **隐私政策** | `https://PhoenixZHC.github.io/wakeupclock/privacy.html` |
| **技术支持** | `https://PhoenixZHC.github.io/wakeupclock/support.html` |

---

## 第一步：在 App Store Connect 创建应用

### 1.1 登录 App Store Connect

1. 访问 [App Store Connect](https://appstoreconnect.apple.com/)
2. 使用你的苹果开发者账号登录
3. 点击 "我的 App" → "+" → "新建 App"

### 1.2 填写应用信息

**必填信息：**
- **平台**: 选择 iOS
- **名称**: "起了么"（最多 30 个字符）
- **主要语言**: 简体中文
- **Bundle ID**: `com.haoce.WakeupClock`
- **SKU**: `WakeupClock-001`

**可选信息：**
- **用户访问权限**: 选择 "无"（不需要登录）

### 1.3 创建 Bundle ID（如果还没有）

1. 访问 [Apple Developer](https://developer.apple.com/account/)
2. 进入 "Certificates, Identifiers & Profiles"
3. 点击 "Identifiers" → "+"
4. 选择 "App IDs" → 继续
5. 填写：
   - **描述**: Wakeup Clock App
   - **Bundle ID**: `com.haoce.WakeupClock`（选择 Explicit）
6. 启用功能：
   - ✅ Background Modes（后台音频播放）
7. 保存并注册

---

## 第二步：准备应用素材

### 2.1 应用图标

- ✅ **已配置**：1024x1024 像素
- ✅ **支持深色模式**
- ✅ **支持着色模式**
- **位置**: `Assets.xcassets/AppIcon`

### 2.2 应用截图

**必需截图（iPhone）：**
- 6.7 英寸显示屏：1290 x 2796 像素
- 6.5 英寸显示屏：1242 x 2688 像素
- 5.5 英寸显示屏：1242 x 2208 像素

**必需截图（iPad）：**
- 12.9 英寸显示屏：2048 x 2732 像素
- 11 英寸显示屏：1668 x 2388 像素

**建议截图内容（5张）：**
1. 主界面 - 展示时钟和闹钟列表
2. 添加闹钟 - 展示时间选择和标签选择
3. 闹钟响铃界面 - 展示场景视频和解锁任务
4. 任务界面 - 展示数学/记忆任务
5. 打卡日历 - 展示连续起床记录

### 2.3 应用描述

**应用描述（中文）：**
```
起了么 - 智能闹钟应用

专治起床困难户，告别赖床从今天开始！

【系统级闹钟】
基于 iOS 26 AlarmKit 框架，与系统闹钟同等可靠性：
• 即使应用关闭或设备重启也能正常响铃
• 自动突破静音、勿扰和专注模式
• 锁屏界面和灵动岛显示闹钟
• Apple Watch 同步震动提醒

【任务解锁系统】
必须完成任务才能关闭闹钟，确保真正清醒：
• 数学解题：解答数学题，唤醒大脑
• 记忆方块：记住并点击发光的方块
• 数字顺序：按顺序点击数字
• 快速点击：连续点击按钮多次
• 打字任务：输入显示的文本

【防赖床模式】
完成任务后定期确认是否清醒，防止重新入睡：
• 可设置提醒间隔：1/2/3/5 分钟
• 可设置提醒次数：1-4 次
• 每次提醒都是系统级闹钟

【智能闹钟管理】
• 8 种闹钟标签：上班、约会、赶飞机、赶火车、会议、看病、面试、考试
• 多种重复模式：响一次、工作日、自定义
• 智能跳过国家法定节假日
• 根据标签播放对应场景视频
• 7 种自定义闹钟铃声

【起床打卡记录】
• 记录每天起床时间
• 追踪连续准时起床天数
• 日历视图查看历史记录

【个性化设置】
• 支持中英文切换
• 深色/浅色主题自动适配
• 精美的用户界面设计

让起床变得不再困难，从今天开始，养成早起的好习惯！
```

**应用描述（英文）：**
```
Wakeup Clock - Smart Alarm App

Cure for difficulty waking up, say goodbye to staying in bed!

【System-Level Alarms】
Built on iOS 26 AlarmKit framework, as reliable as system alarms:
• Works even when app is closed or device restarts
• Auto override Silent, Do Not Disturb, and Focus modes
• Display on lock screen and Dynamic Island
• Apple Watch sync vibration alerts

【Mission Unlock System】
Must complete missions to stop alarm, ensuring you're truly awake:
• Math Solver: Solve math problems, wake up your brain
• Memory Blocks: Remember and tap glowing squares
• Number Order: Tap numbers in sequence
• Rapid Tapping: Tap button multiple times quickly
• Typing Mission: Type displayed text

【Anti-Snooze Mode】
Periodic confirmation after completing tasks to prevent falling back asleep:
• Configurable reminder interval: 1/2/3/5 minutes
• Configurable reminder count: 1-4 times
• Each reminder is a system-level alarm

【Smart Alarm Management】
• 8 alarm labels: Work, Date, Flight, Train, Meeting, Doctor, Interview, Exam
• Multiple repeat modes: Once, Workdays, Custom
• Smart skip national holidays
• Scene videos based on alarm labels
• 7 custom alarm sounds

【Wake-up Check-in】
• Record daily wake-up time
• Track consecutive on-time wake-up days
• Calendar view for history

【Personalization】
• Support Chinese/English switching
• Dark/Light theme auto-adaptation
• Beautiful user interface design

Make waking up no longer difficult, start today, develop the good habit of early rising!
```

**关键词（最多 100 个字符）：**
```
闹钟,起床,打卡,任务,早起,习惯,提醒,定时,时钟,alarm,wakeup,clock,timer,morning
```

**宣传文本（最多 170 个字符）：**
```
基于 iOS 26 系统级闹钟，必须完成任务才能关闭，专治起床困难户！
```

**版本说明（1.0 首次发布）：**
```
起了么 1.0 首次发布！

• 系统级闹钟：基于 AlarmKit，突破静音和勿扰模式
• 5 种解锁任务：数学、记忆、顺序、点击、打字
• 防赖床模式：定期确认是否清醒
• 8 种闹钟标签：配合场景视频
• 起床打卡：记录连续起床天数
• 中英文支持
• 深色/浅色主题
```

---

## 第三步：配置 Xcode 项目

### 3.1 检查项目配置

在 Xcode 中：

1. **选择项目** → **Target "WakeupClock"** → **General**
2. **检查以下设置：**
   - **Display Name**: 起了么（通过本地化文件配置）
   - **Bundle Identifier**: `com.haoce.WakeupClock`
   - **Version**: `1.0`（营销版本号）
   - **Build**: `1`（构建号，每次上传需要递增）
   - **Deployment Target**: `26.0`

### 3.2 配置签名和证书

1. **选择项目** → **Target "WakeupClock"** → **Signing & Capabilities**
2. **配置：**
   - ✅ 勾选 "Automatically manage signing"
   - **Team**: 选择你的开发团队（6JD336D7RQ）
   - **Bundle Identifier**: `com.haoce.WakeupClock`

### 3.3 已配置的权限说明

| 权限 | Info.plist Key | 说明 |
|------|----------------|------|
| AlarmKit | `NSAlarmKitUsageDescription` | 需要闹钟权限以提供可靠的唤醒服务 |
| 后台音频 | `UIBackgroundModes: audio` | 闹钟响铃时播放声音 |

---

## 第四步：构建和归档

### 4.1 清理项目

在 Xcode 菜单栏：**Product** → **Clean Build Folder** (Shift + Cmd + K)

### 4.2 选择构建目标

在 Xcode 顶部工具栏：
- **设备选择**: 选择 "Any iOS Device (arm64)"
- **Scheme**: 选择 "WakeupClock"

### 4.3 归档应用

1. 在 Xcode 菜单栏：**Product** → **Archive**
2. 等待构建完成
3. 构建完成后，会自动打开 **Organizer** 窗口

### 4.4 验证和上传

1. 选择归档，点击 **"Validate App"**
2. 验证通过后，点击 **"Distribute App"**
3. 选择 **"App Store Connect"** → **"Upload"**
4. 等待上传完成

---

## 第五步：在 App Store Connect 配置

### 5.1 App 隐私

在 App Store Connect 中填写 App 隐私信息：

**选择：** "不收集数据"

> 注：应用仅在设备本地存储闹钟设置和打卡记录，不上传任何数据到服务器。

### 5.2 配置应用分类和评级

1. **应用分类：**
   - **主要分类**: 效率
   - **次要分类**: 生活

2. **内容版权：**
   ```
   © 2024-2026 起了么
   ```

3. **应用评级：** 4+（无限制内容）

### 5.3 填写 URL

| 项目 | URL |
|------|-----|
| **隐私政策 URL** | `https://PhoenixZHC.github.io/wakeupclock/privacy.html` |
| **技术支持 URL** | `https://PhoenixZHC.github.io/wakeupclock/support.html` |

---

## 第六步：提交审核

### 6.1 最终检查清单

- ✅ 应用图标已添加（含深色模式）
- ✅ 截图已上传（iPhone + iPad）
- ✅ 应用描述已填写（中英文）
- ✅ 关键词已填写
- ✅ 隐私政策 URL 已填写
- ✅ 技术支持 URL 已填写
- ✅ App 隐私已配置
- ✅ 构建版本已选择
- ✅ 版本号正确

### 6.2 提交审核

1. 点击 **"提交以供审核"**
2. 回答出口合规问题：选择 **"否"**（不使用加密）
3. 选择内容权利：选择 **"否"**
4. 点击 **"提交"**

### 6.3 审核时间

- 通常 24-48 小时
- 首次提交可能需要更长时间

---

## 常见审核被拒原因及解决方案

| 被拒原因 | 解决方案 |
|----------|----------|
| 缺少功能演示 | 在审核备注中说明如何测试闹钟功能 |
| 权限说明不清 | 确保 Info.plist 中的权限说明清晰明确 |
| 隐私政策问题 | 确保隐私政策 URL 可访问且内容完整 |
| 应用崩溃 | 在真机上充分测试所有功能 |
| iOS 版本限制 | 说明应用需要 iOS 26+ 的 AlarmKit 功能 |

**审核备注建议：**
```
本应用使用 iOS 26 的 AlarmKit 框架实现系统级闹钟功能。

测试闹钟功能：
1. 打开应用，点击右下角 "+" 添加闹钟
2. 设置一个 1-2 分钟后的时间
3. 保存闹钟，等待触发
4. 闹钟响起后，点击"解锁闹钟"按钮
5. 完成随机任务（如数学题）即可关闭闹钟

注意：需要在首次使用时授予 AlarmKit 权限。
```

---

## 版本号管理

| 类型 | 说明 | 示例 |
|------|------|------|
| **Marketing Version** | 用户看到的版本号 | 1.0, 1.1, 2.0 |
| **Build Number** | 每次上传必须递增 | 1, 2, 3, 4... |

**更新应用时：**
1. 修改版本号（如 1.0 → 1.1）
2. 递增构建号（如 1 → 2）
3. 重新归档和上传
4. 在 App Store Connect 中创建新版本

---

## 完成！

按照以上步骤，你的应用就可以成功上架 App Store 了！

如果遇到问题：
- [Apple 开发者文档](https://developer.apple.com/documentation/)
- [App Store Connect 帮助](https://help.apple.com/app-store-connect/)
- [App Store 审核指南](https://developer.apple.com/app-store/review/guidelines/)

祝你上架顺利！🚀
