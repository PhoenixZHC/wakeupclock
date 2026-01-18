# App Store 上架完整指南

## 📋 准备工作清单

在开始之前，请确保你已经准备好：
- ✅ 苹果开发者账号（已拥有）
- ✅ 应用图标（1024x1024 像素）
- ✅ 应用截图（至少需要 iPhone 和 iPad 的截图）
- ✅ 应用描述（中英文）
- ✅ 隐私政策 URL（如果应用收集用户数据）

## 第一步：在 App Store Connect 创建应用

### 1.1 登录 App Store Connect

1. 访问 [App Store Connect](https://appstoreconnect.apple.com/)
2. 使用你的苹果开发者账号登录
3. 点击 "我的 App" → "+" → "新建 App"

### 1.2 填写应用信息

**必填信息：**
- **平台**: 选择 iOS
- **名称**: "醒了么" 或 "Wakeup Clock"（最多 30 个字符）
- **主要语言**: 简体中文
- **Bundle ID**: `com.haoce.WakeupClock`（需要先在开发者中心创建）
- **SKU**: 可以填写 `WakeupClock-001`（唯一标识符，不会显示给用户）

**可选信息：**
- **用户访问权限**: 如果不需要登录，选择 "无"
- **App Store Connect 访问权限**: 选择你的角色

### 1.3 创建 Bundle ID（如果还没有）

如果 Bundle ID 还没有创建：

1. 访问 [Apple Developer](https://developer.apple.com/account/)
2. 进入 "Certificates, Identifiers & Profiles"
3. 点击 "Identifiers" → "+"
4. 选择 "App IDs" → 继续
5. 填写：
   - **描述**: Wakeup Clock App
   - **Bundle ID**: `com.haoce.WakeupClock`（选择 Explicit）
6. 启用功能（如果需要）：
   - Push Notifications（你的应用使用通知）
   - Background Modes（你的应用使用后台模式）
7. 保存并注册

## 第二步：准备应用素材

### 2.1 应用图标

- **尺寸**: 1024x1024 像素
- **格式**: PNG（无透明背景）
- **位置**: 在 Xcode 中添加到 `Assets.xcassets/AppIcon`

### 2.2 应用截图

**必需截图（iPhone）：**
- 6.7 英寸显示屏（iPhone 14 Pro Max, 15 Pro Max 等）
- 6.5 英寸显示屏（iPhone 11 Pro Max, XS Max 等）
- 5.5 英寸显示屏（iPhone 8 Plus 等）

**必需截图（iPad）：**
- 12.9 英寸显示屏（iPad Pro）
- 11 英寸显示屏（iPad Pro）

**截图要求：**
- 至少需要 1 张，最多 10 张
- 建议提供 3-5 张展示主要功能的截图
- 分辨率要求：
  - iPhone 6.7": 1290 x 2796 像素
  - iPhone 6.5": 1242 x 2688 像素
  - iPhone 5.5": 1242 x 2208 像素
  - iPad 12.9": 2048 x 2732 像素
  - iPad 11": 1668 x 2388 像素

**如何生成截图：**
1. 在 Xcode 模拟器中运行应用
2. 使用 Cmd + S 截图
3. 或者使用真机截图

### 2.3 应用描述

准备以下内容：

**应用描述（中文）：**
```
醒了么 - 智能闹钟应用

告别起床困难，开启高效一天！

【核心功能】
• 智能闹钟管理：支持多种重复模式，自动跳过节假日
• 任务解锁系统：必须完成数学、记忆、顺序等任务才能关闭闹钟
• 起床打卡记录：追踪你的起床时间，记录每一天的坚持
• 连续天数统计：看看你能坚持多少天准时起床
• 多场景视频：根据闹钟类型播放不同场景视频

【任务类型】
• 数学解密：解答数学题，唤醒你的大脑
• 记忆方块：记住并点击发光的方块
• 数字顺序：按顺序点击数字
• 快速点击：快速点击按钮多次
• 打字任务：输入显示的文本

【特色功能】
• 支持中英文切换
• 深色/浅色主题自动适配
• 精美的用户界面设计
• 流畅的使用体验

让起床变得不再困难，从今天开始，养成早起的好习惯！
```

**应用描述（英文）：**
```
Wakeup Clock - Smart Alarm App

Say goodbye to difficulty waking up, start your efficient day!

【Core Features】
• Smart Alarm Management: Multiple repeat modes, automatically skip holidays
• Mission Unlock System: Must complete math, memory, order tasks to turn off alarm
• Wake-up Check-in: Track your wake-up time, record every day of persistence
• Streak Statistics: See how many days you can wake up on time
• Multi-scene Videos: Play different scene videos based on alarm type

【Mission Types】
• Math Decryption: Solve math problems, wake up your brain
• Memory Blocks: Remember and tap glowing squares
• Number Order: Tap numbers in sequence
• Rapid Tapping: Tap button multiple times quickly
• Typing Mission: Type displayed text

【Special Features】
• Support Chinese/English switching
• Dark/Light theme auto-adaptation
• Beautiful user interface design
• Smooth user experience

Make waking up no longer difficult, start today, develop the good habit of early rising!
```

**关键词（最多 100 个字符）：**
```
闹钟,起床,打卡,任务,早起,习惯,提醒,定时,时钟,alarm,wakeup,clock,timer
```

**宣传文本（可选，最多 170 个字符）：**
```
智能闹钟应用，必须完成任务才能关闭闹钟，告别起床困难！
```

### 2.4 隐私政策

如果你的应用收集用户数据，需要提供隐私政策 URL。

**你的应用收集的数据：**
- 通知权限（用于闹钟提醒）
- 本地数据存储（闹钟设置、打卡记录）

**隐私政策模板（需要放在你的网站上）：**
```
隐私政策

【数据收集】
本应用仅收集以下数据：
1. 通知权限：用于在设定时间提醒用户起床
2. 本地存储：闹钟设置、起床打卡记录等数据仅存储在设备本地

【数据使用】
所有数据仅用于应用功能，不会上传到服务器或分享给第三方。

【数据安全】
所有数据存储在设备本地，我们不会访问或传输您的个人数据。

【联系方式】
如有疑问，请联系：[你的邮箱]
```

## 第三步：配置 Xcode 项目

### 3.1 检查项目配置

在 Xcode 中：

1. **选择项目** → **Target "WakeupClock"** → **General**
2. **检查以下设置：**
   - **Display Name**: 醒了么（已在 Info.plist 配置）
   - **Bundle Identifier**: `com.haoce.WakeupClock`
   - **Version**: `1.0`（营销版本号）
   - **Build**: `1`（构建号，每次上传需要递增）
   - **Deployment Target**: `17.6`（最低支持版本）

### 3.2 配置签名和证书

1. **选择项目** → **Target "WakeupClock"** → **Signing & Capabilities**
2. **配置：**
   - ✅ 勾选 "Automatically manage signing"
   - **Team**: 选择你的开发团队（6JD336D7RQ）
   - **Bundle Identifier**: `com.haoce.WakeupClock`

Xcode 会自动管理证书和配置文件。

### 3.3 添加应用图标

1. 在项目导航器中找到 `Assets.xcassets` → `AppIcon`
2. 将 1024x1024 的图标拖拽到 "Universal" 槽位
3. 可选：添加深色模式图标

## 第四步：构建和归档

### 4.1 清理项目

1. 在 Xcode 菜单栏：**Product** → **Clean Build Folder** (Shift + Cmd + K)

### 4.2 选择构建目标

1. 在 Xcode 顶部工具栏：
   - **设备选择**: 选择 "Any iOS Device (arm64)" 或 "Generic iOS Device"
   - **Scheme**: 选择 "WakeupClock"

### 4.3 归档应用

1. 在 Xcode 菜单栏：**Product** → **Archive**
2. 等待构建完成（可能需要几分钟）
3. 构建完成后，会自动打开 **Organizer** 窗口

### 4.4 验证归档

在 Organizer 窗口中：

1. 选择刚创建的归档
2. 点击 **"Validate App"** 按钮
3. 选择分发方式：**"App Store Connect"**
4. 点击 **"Next"**
5. 选择你的团队和证书
6. 点击 **"Validate"**
7. 等待验证完成（检查是否有错误）

**常见验证错误：**
- **缺少应用图标**: 确保已添加 1024x1024 图标
- **缺少隐私政策**: 如果应用需要，在 App Store Connect 中添加
- **版本号冲突**: 确保 Build 号是新的

## 第五步：上传到 App Store Connect

### 5.1 分发应用

在 Organizer 窗口中：

1. 选择归档
2. 点击 **"Distribute App"** 按钮
3. 选择 **"App Store Connect"**
4. 点击 **"Next"**
5. 选择 **"Upload"**（不是 Export）
6. 点击 **"Next"**
7. 选择分发选项：
   - ✅ **"Upload your app's symbols"**（用于崩溃报告）
   - ✅ **"Manage Version and Build Number"**（如果需要）
8. 点击 **"Next"**
9. 选择你的团队和证书
10. 点击 **"Next"**
11. 检查信息，点击 **"Upload"**
12. 等待上传完成（可能需要 10-30 分钟）

### 5.2 检查上传状态

1. 登录 [App Store Connect](https://appstoreconnect.apple.com/)
2. 进入你的应用
3. 点击 **"TestFlight"** 或 **"App Store"** 标签
4. 在 **"构建版本"** 部分查看上传状态：
   - **处理中**: 正在处理，等待 10-30 分钟
   - **可用**: 可以用于提交审核

## 第六步：在 App Store Connect 配置应用信息

### 6.1 添加应用截图和描述

1. 进入 **"App Store"** 标签
2. 点击 **"准备提交"** 或 **"+"** 创建新版本
3. 填写版本信息：
   - **版本号**: `1.0`
   - **此版本的新增内容**: 填写更新说明

4. **上传截图：**
   - 点击截图区域
   - 上传准备好的截图（至少 1 张）

5. **填写应用信息：**
   - **应用描述**: 粘贴之前准备的中文描述
   - **关键词**: 填写关键词（最多 100 字符）
   - **宣传文本**: 填写宣传文本（可选）
   - **技术支持网址**: 填写你的网站或 GitHub 链接
   - **营销网址**: 可选

6. **本地化信息（如果需要英文版本）：**
   - 点击 **"+"** 添加语言
   - 选择 **"英语"**
   - 填写英文描述和截图

### 6.2 配置应用分类和评级

1. **应用分类：**
   - **主要分类**: 选择 "效率" 或 "生活"
   - **次要分类**: 可选

2. **内容版权：**
   - 填写版权信息，例如：`© 2024 你的名字`

3. **应用评级：**
   - 点击 **"分级"**
   - 回答问卷（你的应用应该是 4+ 或 9+）
   - 保存

### 6.3 配置隐私信息

1. **隐私政策 URL**（如果需要）:
   - 填写你的隐私政策网址

2. **App 隐私：**
   - 点击 **"App 隐私"**
   - 选择你的应用收集的数据类型：
     - ✅ **标识符**: 设备 ID（如果需要）
     - ✅ **使用数据**: 应用交互（可选）
   - 填写数据使用目的

### 6.4 选择构建版本

1. 在 **"构建版本"** 部分
2. 点击 **"+"** 或 **"选择构建版本"**
3. 选择已上传的构建版本
4. 点击 **"完成"**

## 第七步：提交审核

### 7.1 最终检查

在提交前，检查以下内容：

- ✅ 应用图标已添加
- ✅ 截图已上传（至少 1 张）
- ✅ 应用描述已填写
- ✅ 构建版本已选择
- ✅ 版本号正确
- ✅ 隐私信息已配置（如果需要）

### 7.2 提交审核

1. 滚动到页面底部
2. 点击 **"提交以供审核"** 按钮
3. 回答出口合规问题（通常选择 "否"）
4. 选择内容权利（通常选择 "否"）
5. 点击 **"提交"**

### 7.3 审核状态

提交后，你可以在 App Store Connect 中查看审核状态：

- **等待审核**: 已提交，等待 Apple 审核
- **审核中**: Apple 正在审核
- **待开发者发布**: 审核通过，等待你发布
- **已拒绝**: 审核被拒，查看原因并修复

**审核时间：**
- 通常 24-48 小时
- 首次提交可能需要更长时间

### 7.4 审核被拒怎么办

如果审核被拒：

1. **查看拒绝原因：**
   - 在 App Store Connect 中查看 **"解决方案中心"**
   - 阅读 Apple 的反馈

2. **常见拒绝原因：**
   - 缺少功能说明
   - 截图不符合要求
   - 隐私政策问题
   - 应用崩溃或功能异常

3. **修复问题：**
   - 根据反馈修复问题
   - 更新应用版本号（Build 号）
   - 重新归档和上传
   - 重新提交审核

## 第八步：发布应用

### 8.1 审核通过后

1. 当状态变为 **"待开发者发布"** 时
2. 在 App Store Connect 中点击 **"发布此版本"**
3. 应用会在几小时内出现在 App Store 中

### 8.2 自动发布

如果你在提交时选择了 **"自动发布"**，审核通过后会自动发布。

## 📝 重要提示

### 版本号管理

- **Marketing Version (版本号)**: 用户看到的版本，如 `1.0`, `1.1`, `2.0`
- **Build Number (构建号)**: 每次上传必须递增，如 `1`, `2`, `3`

### 更新应用

更新应用时：
1. 修改版本号（如 `1.0` → `1.1`）
2. 递增构建号（如 `1` → `2`）
3. 重新归档和上传
4. 在 App Store Connect 中创建新版本

### 测试版本（TestFlight）

在正式发布前，可以使用 TestFlight 进行测试：

1. 上传构建版本后
2. 在 **"TestFlight"** 标签中添加测试用户
3. 测试用户可以通过 TestFlight 应用安装测试版本

## 🎉 完成！

按照以上步骤，你的应用就可以成功上架 App Store 了！

如果遇到问题，可以：
- 查看 [Apple 开发者文档](https://developer.apple.com/documentation/)
- 访问 [App Store Connect 帮助](https://help.apple.com/app-store-connect/)
- 联系 Apple 开发者支持

祝你上架顺利！🚀
