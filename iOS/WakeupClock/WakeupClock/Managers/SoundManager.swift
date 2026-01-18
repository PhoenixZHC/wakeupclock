//
//  SoundManager.swift
//  WakeupClock
//
//  音频管理器：负责闹钟声音的播放和控制
//  支持播放音频文件和程序生成的音效
//

import Foundation
import AVFoundation
import Combine

/// 音频管理器（单例）
class SoundManager: ObservableObject {
    static let shared = SoundManager()
    
    @Published var isPlaying: Bool = false
    @Published var currentVolumeLevel: VolumeLevel = .normal
    @Published var currentSound: AlarmSound = .beep
    
    // 音频文件播放器
    private var audioPlayer: AVAudioPlayer?
    
    // 程序生成音效的组件
    private var audioEngine: AVAudioEngine?
    private var oscillatorNode: AVAudioPlayerNode?
    private var gainNode: AVAudioMixerNode?
    private var generatedSoundTimer: Timer?
    private var soundState: Int = 0
    
    private var isAudioSessionConfigured = false
    
    private init() {
        // 延迟配置音频会话，避免在初始化时就配置
    }
    
    // MARK: - 音频会话配置
    
    /// 配置音频会话（仅在需要播放时调用）
    private func configureAudioSessionForPlayback() {
        let audioSession = AVAudioSession.sharedInstance()
        
        do {
            // 使用 .playback category 确保即使在静音模式下也能播放
            // 使用 .mixWithOthers 允许与视频同时播放
            try audioSession.setCategory(.playback, mode: .default, options: [.mixWithOthers])
            try audioSession.setActive(true)
            isAudioSessionConfigured = true
            
            #if DEBUG
            print("✅ 音频会话配置成功")
            #endif
        } catch {
            #if DEBUG
            print("❌ 配置音频会话失败: \(error)")
            #endif
        }
    }
    
    /// 停用音频会话
    private func deactivateAudioSession() {
        guard isAudioSessionConfigured else { return }
        
        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setActive(false, options: .notifyOthersOnDeactivation)
            isAudioSessionConfigured = false
        } catch {
            #if DEBUG
            print("停用音频会话失败（可忽略）: \(error)")
            #endif
        }
    }
    
    // MARK: - 播放控制
    
    /// 播放闹钟声音（使用 AlarmKit 选择的声音，保持一致）
    func playAlarmSound(level: VolumeLevel) {
        // 获取 AlarmKit 当前使用的声音，保持应用内和系统闹钟声音一致
        if #available(iOS 26.0, *) {
            let sound = AlarmKitManager.shared.getCurrentSound()
            playAlarmSound(level: level, sound: sound)
        } else {
            // iOS 26 以下随机选择
            let randomSound = AlarmSound.randomAvailable()
            playAlarmSound(level: level, sound: randomSound)
        }
    }
    
    /// 播放指定类型的闹钟声音
    func playAlarmSound(level: VolumeLevel, sound: AlarmSound) {
        stopAlarmSound() // 先停止之前的播放
        
        // 配置音频会话
        configureAudioSessionForPlayback()
        
        currentVolumeLevel = level
        currentSound = sound
        isPlaying = true
        
        #if DEBUG
        print("🔊 播放闹钟声音: \(sound.displayName), 音量级别: \(level)")
        #endif
        
        // 根据声音类型选择播放方式
        if sound.isGenerated {
            // 使用程序生成的音效
            playGeneratedSound(level: level)
        } else if let url = sound.fileURL {
            // 使用音频文件
            playAudioFile(url: url, level: level)
        } else {
            // 文件不存在，回退到程序生成的音效
            #if DEBUG
            print("⚠️ 音频文件不存在，使用程序生成的音效")
            #endif
            playGeneratedSound(level: level)
        }
    }
    
    // MARK: - 音频文件播放
    
    /// 播放音频文件
    private func playAudioFile(url: URL, level: VolumeLevel) {
        do {
            audioPlayer = try AVAudioPlayer(contentsOf: url)
            guard let player = audioPlayer else { return }
            
            // 设置音量
            let volume: Float = {
                switch level {
                case .normal: return 0.5
                case .loud: return 0.8
                case .superLoud: return 1.0
                }
            }()
            player.volume = volume
            
            // 设置循环播放
            player.numberOfLoops = -1 // 无限循环
            
            player.prepareToPlay()
            player.play()
            
            #if DEBUG
            print("▶️ 开始播放音频文件: \(url.lastPathComponent)")
            #endif
        } catch {
            #if DEBUG
            print("❌ 播放音频文件失败: \(error)")
            #endif
            // 回退到程序生成的音效
            playGeneratedSound(level: level)
        }
    }
    
    // MARK: - 程序生成音效
    
    /// 播放程序生成的音效（哔哔声）
    private func playGeneratedSound(level: VolumeLevel) {
        soundState = 0
        
        // 创建音频引擎
        audioEngine = AVAudioEngine()
        guard let audioEngine = audioEngine else { return }
        
        // 创建播放节点
        oscillatorNode = AVAudioPlayerNode()
        guard let oscillatorNode = oscillatorNode else { return }
        
        audioEngine.attach(oscillatorNode)
        
        // 创建增益节点（控制音量）
        gainNode = AVAudioMixerNode()
        guard let gainNode = gainNode else { return }
        audioEngine.attach(gainNode)
        
        // 设置音量
        let volume: Float = {
            switch level {
            case .normal: return 0.4
            case .loud: return 0.7
            case .superLoud: return 1.0
            }
        }()
        gainNode.volume = volume
        
        // 获取标准格式
        let standardFormat = AVAudioFormat(standardFormatWithSampleRate: 44100, channels: 2)
        guard let format = standardFormat else { return }
        
        // 连接节点
        audioEngine.connect(oscillatorNode, to: gainNode, format: format)
        audioEngine.connect(gainNode, to: audioEngine.mainMixerNode, format: format)
        
        // 启动引擎
        do {
            try audioEngine.start()
        } catch {
            #if DEBUG
            print("❌ 启动音频引擎失败: \(error)")
            #endif
            isPlaying = false
            return
        }
        
        // 启动警笛声模式
        startSirenPattern()
    }
    
    /// 警笛声模式（双频交替）
    private func startSirenPattern() {
        playTone(frequency: 440.0, duration: 0.4)
        
        generatedSoundTimer = Timer.scheduledTimer(withTimeInterval: 0.4, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            self.soundState = (self.soundState + 1) % 2
            let frequency: Float = self.soundState == 0 ? 440.0 : 880.0
            self.playTone(frequency: frequency, duration: 0.4)
        }
    }
    
    /// 播放指定频率和时长的音调
    private func playTone(frequency: Float, duration: Double) {
        guard let oscillatorNode = oscillatorNode,
              let audioEngine = audioEngine,
              audioEngine.isRunning else { return }
        
        let inputFormat = oscillatorNode.outputFormat(forBus: 0)
        let sampleRate = inputFormat.sampleRate
        let channelCount = inputFormat.channelCount
        
        guard sampleRate > 0, channelCount > 0 else { return }
        
        let frameCount = AVAudioFrameCount(sampleRate * duration)
        guard let buffer = AVAudioPCMBuffer(pcmFormat: inputFormat, frameCapacity: frameCount) else { return }
        buffer.frameLength = frameCount
        
        guard let channelData = buffer.floatChannelData else { return }
        
        // 生成带有淡入淡出的正弦波
        let fadeFrames = Int(sampleRate * 0.01) // 10ms 淡入淡出
        
        for channel in 0..<Int(channelCount) {
            let channelDataValue = channelData[channel]
            for frame in 0..<Int(frameCount) {
                var amplitude: Float = 0.6
                
                // 淡入
                if frame < fadeFrames {
                    amplitude *= Float(frame) / Float(fadeFrames)
                }
                // 淡出
                if frame > Int(frameCount) - fadeFrames {
                    amplitude *= Float(Int(frameCount) - frame) / Float(fadeFrames)
                }
                
                let sample = sin(2.0 * Float.pi * frequency * Float(frame) / Float(sampleRate))
                channelDataValue[frame] = sample * amplitude
            }
        }
        
        // 停止当前播放并播放新缓冲区
        oscillatorNode.stop()
        oscillatorNode.scheduleBuffer(buffer, at: nil, options: []) { }
        oscillatorNode.play()
    }
    
    /// 停止闹钟声音
    func stopAlarmSound() {
        // 停止音频文件播放
        audioPlayer?.stop()
        audioPlayer = nil
        
        // 停止程序生成的音效
        generatedSoundTimer?.invalidate()
        generatedSoundTimer = nil
        
        oscillatorNode?.stop()
        audioEngine?.stop()
        
        oscillatorNode = nil
        gainNode = nil
        audioEngine = nil
        
        isPlaying = false
        soundState = 0
        
        // 停用音频会话
        deactivateAudioSession()
    }
    
    /// 更新音量等级
    func updateVolumeLevel(_ level: VolumeLevel) {
        if isPlaying {
            // 保持当前声音类型，只更新音量
            let currentSoundType = currentSound
            playAlarmSound(level: level, sound: currentSoundType)
        } else {
            currentVolumeLevel = level
        }
    }
}
