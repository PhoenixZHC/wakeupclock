//
//  AlarmLockdownView.swift
//  WakeupClock
//
//  闹钟响铃界面：显示闹钟信息并启动任务
//

import SwiftUI
import AVFoundation
import AVKit

struct AlarmLockdownView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @EnvironmentObject var soundManager: SoundManager
    
    let alarm: AlarmModel
    let onSolved: () -> Void
    
    @State private var showMission = false
    @State private var volumeStage: VolumeLevel = .normal
    @State private var activeMission: MissionType = .math
    @State private var volumeTimer: Timer?
    @State private var flashOpacity: Double = 0.0
    @State private var currentTime = Date()
    @State private var timeTimer: Timer?
    @State private var flashTimer: Timer?
    
    var body: some View {
        ZStack {
            // 视频背景
            videoBackgroundView
            
            // 黑色遮罩（降低不透明度，让视频更清晰可见）
            Color.black.opacity(0.3)
                .ignoresSafeArea()
            
            // 红色闪烁边缘（报警灯效果）
            if volumeStage == .superLoud {
                redFlashOverlay
            }
            
            // 内容
            if showMission {
                missionView
                    .edgesIgnoringSafeArea([]) // 不忽略安全区域
            } else {
                alarmDisplayView
                    .edgesIgnoringSafeArea([]) // 不忽略安全区域
            }
        }
        .ignoresSafeArea(.all, edges: [.bottom]) // 只在底部忽略安全区域
        .onAppear {
            startAlarm()
            selectRandomMission()
            startTimeTimer()
            startFlashAnimation()
        }
        .onDisappear {
            stopAlarm()
            stopTimeTimer()
            stopFlashAnimation()
        }
    }
    
    // MARK: - 子视图
    
    private var videoBackgroundView: some View {
        Group {
            if let videoName = getVideoName(for: alarm.label),
               let videoURL = findVideoURL(for: videoName) {
                // 有视频文件，播放视频
                VideoPlayerView(videoURL: videoURL)
                    .ignoresSafeArea()
                    .onAppear {
                        #if DEBUG
                        print("🎬 找到视频: \(videoName).mp4 at \(videoURL.path)")
                        #endif
                    }
            } else {
                // 没有视频文件，使用渐变背景
                LinearGradient(
                    colors: [
                        Color.black,
                        Color(hex: "1a1a2e"),
                        Color.black
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()
                .onAppear {
                    #if DEBUG
                    print("⚠️ 未找到视频文件，使用渐变背景。Label: \(alarm.label)")
                    #endif
                }
            }
        }
    }
    
    /// 查找视频文件URL
    private func findVideoURL(for videoName: String) -> URL? {
        // 方法1: 在Videos子目录查找（最可能的位置）
        if let url = Bundle.main.url(forResource: videoName, withExtension: "mp4", subdirectory: "Videos") {
            return url
        }
        
        // 方法2: 直接在bundle根目录查找
        if let url = Bundle.main.url(forResource: videoName, withExtension: "mp4") {
            return url
        }
        
        // 方法3: 尝试完整路径（用于调试）
        if let bundlePath = Bundle.main.resourcePath {
            let videosPath = (bundlePath as NSString).appendingPathComponent("Videos")
            let fullPath = (videosPath as NSString).appendingPathComponent("\(videoName).mp4")
            if FileManager.default.fileExists(atPath: fullPath) {
                return URL(fileURLWithPath: fullPath)
            }
        }
        
        return nil
    }
    
    private var redFlashOverlay: some View {
        ZStack {
            // 顶部边缘
            Rectangle()
                .fill(Color.red.opacity(flashOpacity))
                .frame(height: 8)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
            
            // 底部边缘
            Rectangle()
                .fill(Color.red.opacity(flashOpacity))
                .frame(height: 8)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            
            // 左侧边缘
            Rectangle()
                .fill(Color.red.opacity(flashOpacity))
                .frame(width: 8)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
            
            // 右侧边缘
            Rectangle()
                .fill(Color.red.opacity(flashOpacity))
                .frame(width: 8)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .trailing)
        }
        .ignoresSafeArea()
    }
    
    private var alarmDisplayView: some View {
        VStack(spacing: 32) {
            // 时间（放在上面，自适应字体大小）
            Text(currentTimeString)
                .font(.system(size: 120, weight: .black, design: .rounded))
                .minimumScaleFactor(0.5) // 允许缩小到50%以避免换行
                .lineLimit(1) // 强制单行显示
                .foregroundColor(.white)
                .shadow(color: .black.opacity(0.5), radius: 10, x: 0, y: 5)
            
            // 图标（放在时间下面）
            ZStack {
                Circle()
                    .fill(volumeStage == .superLoud ? Color.red.opacity(0.3) : Color.white.opacity(0.1))
                    .frame(width: 112, height: 112)
                    .blur(radius: 20)
                
                Image(systemName: volumeStage == .superLoud ? "exclamationmark.triangle.fill" : categoryIconName)
                    .font(.system(size: 56))
                    .foregroundColor(volumeStage == .superLoud ? .red : .white)
            }
            
            // 消息
            VStack(spacing: 8) {
                Text(stageText)
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(volumeStage == .superLoud ? .red : .white)
                
                Text(subText)
                    .font(.system(size: 18, weight: .medium))
                    .foregroundColor(.gray)
            }
            
            // 开始任务按钮
            Button(action: {
                showMission = true
            }) {
                Text(LocalizedString("startMission"))
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.black)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.white.opacity(0.9))
                    .cornerRadius(16)
            }
            .padding(.horizontal, 32)
            
            Text(LocalizedString("completeMission"))
                .font(.system(size: 12))
                .foregroundColor(.gray)
        }
        .padding()
    }
    
    private var missionView: some View {
        Group {
            switch activeMission {
            case .math:
                MathMissionView(
                    difficulty: alarm.difficultyEnum,
                    onComplete: onSolved
                )
                
            case .memory:
                MemoryMissionView(
                    difficulty: alarm.difficultyEnum,
                    onComplete: onSolved
                )
                
            case .order:
                OrderMissionView(
                    difficulty: alarm.difficultyEnum,
                    onComplete: onSolved
                )
                
            case .shake:
                ShakeMissionView(
                    difficulty: alarm.difficultyEnum,
                    onComplete: onSolved
                )
                
            case .typing:
                TypingMissionView(
                    difficulty: alarm.difficultyEnum,
                    onComplete: onSolved
                )
            }
        }
    }
    
    // MARK: - 计算属性
    
    private var categoryIconName: String {
        switch alarm.label {
        case "work": return "briefcase.fill"
        case "date": return "heart.fill"
        case "flight": return "airplane"
        case "train": return "tram.fill"
        case "meeting": return "person.3.fill"
        case "doctor": return "cross.case.fill"
        case "interview": return "person.badge.plus"
        case "exam": return "graduationcap.fill"
        default: return "bell.fill"
        }
    }
    
    private var stageText: String {
        switch volumeStage {
        case .normal:
            return LocalizedString("alarm_msg_\(alarm.label)")
        case .loud:
            return LocalizedString("getUpNow")
        case .superLoud:
            return LocalizedString("emergency")
        }
    }
    
    private var subText: String {
        switch volumeStage {
        case .normal:
            return LocalizedString("earlyBird")
        case .loud:
            return LocalizedString("alarm_msg_\(alarm.label)")
        case .superLoud:
            return LocalizedString("noiseBombing")
        }
    }
    
    // MARK: - 方法
    
    private func startAlarm() {
        // 开始播放声音
        soundManager.playAlarmSound(level: .normal)
        volumeStage = .normal
        
        // 15秒后升级音量
        volumeTimer = Timer.scheduledTimer(withTimeInterval: 15.0, repeats: true) { [self] _ in
            switch volumeStage {
            case .normal:
                volumeStage = .loud
                soundManager.playAlarmSound(level: .loud)
            case .loud:
                volumeStage = .superLoud
                soundManager.playAlarmSound(level: .superLoud)
            case .superLoud:
                break // 已经是最大音量
            }
        }
    }
    
    private func stopAlarm() {
        volumeTimer?.invalidate()
        volumeTimer = nil
        soundManager.stopAlarmSound()
    }
    
    /// 上次选择的任务类型（用于避免连续重复）
    private static var lastMission: MissionType?
    
    private func selectRandomMission() {
        var missions: [MissionType] = [.math, .memory, .order, .shake, .typing]
        
        // 如果有上次的任务，从列表中移除以避免连续重复
        if let last = Self.lastMission, missions.count > 1 {
            missions.removeAll { $0 == last }
        }
        
        let selected = missions.randomElement() ?? .math
        Self.lastMission = selected
        activeMission = selected
        
        #if DEBUG
        print("🎯 随机选择任务: \(selected.rawValue)")
        #endif
    }
    
    // MARK: - 视频相关
    
    /// 获取视频文件名（如果存在）
    private func getVideoName(for label: String) -> String? {
        switch label {
        case "work": return "work"
        case "date": return "date"
        case "flight": return "flight"
        case "train": return "train"
        case "meeting": return "meeting"
        case "doctor": return "doctor"
        case "interview": return "interview"
        case "exam": return "exam"
        default: return nil // other类型没有视频
        }
    }
    
    /// 检查视频文件是否存在
    private func hasVideo(for label: String) -> Bool {
        guard let videoName = getVideoName(for: label) else { return false }
        return Bundle.main.url(forResource: videoName, withExtension: "mp4") != nil
    }
    
    // MARK: - 时间相关
    
    private var currentTimeString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: currentTime)
    }
    
    private func startTimeTimer() {
        timeTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [self] _ in
            currentTime = Date()
        }
    }
    
    private func stopTimeTimer() {
        timeTimer?.invalidate()
        timeTimer = nil
    }
    
    // MARK: - 闪烁动画
    
    private func startFlashAnimation() {
        // 使用Timer实现闪烁效果
        flashTimer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: true) { [self] _ in
            withAnimation(.easeInOut(duration: 0.3)) {
                flashOpacity = flashOpacity > 0.5 ? 0.0 : 0.8
            }
        }
    }
    
    private func stopFlashAnimation() {
        flashTimer?.invalidate()
        flashTimer = nil
        flashOpacity = 0.0
    }
}

// MARK: - 视频播放器视图

struct VideoPlayerView: UIViewRepresentable {
    let videoURL: URL
    
    func makeUIView(context: Context) -> UIView {
        let containerView = VideoContainerView()
        containerView.backgroundColor = .black
        
        #if DEBUG
        print("🎥 VideoPlayerView makeUIView 调用")
        print("📍 视频URL: \(videoURL.path)")
        print("📂 文件存在: \(FileManager.default.fileExists(atPath: videoURL.path))")
        #endif
        
        let playerLayer = AVPlayerLayer()
        playerLayer.videoGravity = .resizeAspectFill
        containerView.layer.addSublayer(playerLayer)
        containerView.playerLayer = playerLayer
        
        let playerItem = AVPlayerItem(url: videoURL)
        // 配置播放项以减少解码错误
        playerItem.preferredForwardBufferDuration = 1.0
        playerItem.canUseNetworkResourcesForLiveStreamingWhilePaused = false
        
        let player = AVPlayer(playerItem: playerItem)
        playerLayer.player = player
        player.isMuted = true  // 视频静音，让闹钟声音播放
        // 设置自动播放策略
        player.automaticallyWaitsToMinimizeStalling = false
        
        // 保存引用
        context.coordinator.player = player
        context.coordinator.playerLayer = playerLayer
        context.coordinator.playerItem = playerItem
        context.coordinator.containerView = containerView
        
        // 设置循环播放
        context.coordinator.setupLooping()
        
        // 监听播放状态
        context.coordinator.observePlayerItem()
        
        // 监听应用状态变化
        context.coordinator.setupAppStateObservers()
        
        #if DEBUG
        print("▶️ 开始播放视频")
        #endif
        
        // 延迟一帧后开始播放，确保布局完成
        DispatchQueue.main.async {
            player.play()
        }
        
        return containerView
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {
        // frame 更新由 VideoContainerView 的 layoutSubviews 处理
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator()
    }
    
    class Coordinator: NSObject {
        var player: AVPlayer?
        var playerLayer: AVPlayerLayer?
        var playerItem: AVPlayerItem?
        var containerView: VideoContainerView?
        var loopObserver: NSObjectProtocol?
        var foregroundObserver: NSObjectProtocol?
        var backgroundObserver: NSObjectProtocol?
        
        func setupLooping() {
            guard let _ = player, let playerItem = playerItem else { return }
            
            // 使用通知来监听播放结束
            loopObserver = NotificationCenter.default.addObserver(
                forName: .AVPlayerItemDidPlayToEndTime,
                object: playerItem,
                queue: .main
            ) { [weak self] _ in
                self?.restartPlayback()
            }
        }
        
        func setupAppStateObservers() {
            // 监听应用进入前台
            foregroundObserver = NotificationCenter.default.addObserver(
                forName: UIApplication.willEnterForegroundNotification,
                object: nil,
                queue: .main
            ) { [weak self] _ in
                #if DEBUG
                print("📱 应用进入前台，恢复视频播放")
                #endif
                self?.player?.play()
            }
            
            // 监听应用进入后台
            backgroundObserver = NotificationCenter.default.addObserver(
                forName: UIApplication.didEnterBackgroundNotification,
                object: nil,
                queue: .main
            ) { [weak self] _ in
                #if DEBUG
                print("📱 应用进入后台，暂停视频")
                #endif
                self?.player?.pause()
            }
        }
        
        func observePlayerItem() {
            guard let playerItem = playerItem else { return }
            
            // 监听播放状态，确保视频能正常播放
            playerItem.addObserver(
                self,
                forKeyPath: "status",
                options: [.new, .initial],
                context: nil
            )
        }
        
        override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
            if keyPath == "status" {
                if let item = object as? AVPlayerItem {
                    if item.status == .readyToPlay {
                        DispatchQueue.main.async { [weak self] in
                            #if DEBUG
                            print("✅ 视频准备就绪，开始播放")
                            #endif
                            self?.player?.play()
                        }
                    } else if item.status == .failed {
                        #if DEBUG
                        DispatchQueue.main.async {
                            print("❌ 视频播放失败: \(item.error?.localizedDescription ?? "未知错误")")
                        }
                        #endif
                    }
                }
            }
        }
        
        private func restartPlayback() {
            guard let player = player else { return }
            // 重置到开始位置并重新播放
            DispatchQueue.main.async { [weak self] in
                #if DEBUG
                print("🔄 视频播放结束，重新开始循环")
                #endif
                player.seek(to: .zero) { finished in
                    if finished {
                        self?.player?.play()
                    }
                }
            }
        }
        
        deinit {
            if let observer = loopObserver {
                NotificationCenter.default.removeObserver(observer)
            }
            if let observer = foregroundObserver {
                NotificationCenter.default.removeObserver(observer)
            }
            if let observer = backgroundObserver {
                NotificationCenter.default.removeObserver(observer)
            }
            playerItem?.removeObserver(self, forKeyPath: "status")
            player?.pause()
            player = nil
        }
    }
}

// MARK: - 视频容器视图（自动处理布局）

class VideoContainerView: UIView {
    var playerLayer: AVPlayerLayer?
    
    override func layoutSubviews() {
        super.layoutSubviews()
        // 确保 playerLayer 始终填满容器
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        playerLayer?.frame = bounds
        CATransaction.commit()
    }
}
