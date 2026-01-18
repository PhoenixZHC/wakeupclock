//
//  AlarmSoundManager.swift
//  WakeupClock
//
//  闹钟音频资源管理器：统一管理 AlarmKit 和应用内的闹钟声音
//

import Foundation
import AVFoundation

/// 闹钟声音类型
enum AlarmSound: String, CaseIterable, Codable {
    // 内置音效（使用音频文件 alarm1-7.mp3）
    case alarm1 = "alarm1"
    case alarm2 = "alarm2"
    case alarm3 = "alarm3"
    case alarm4 = "alarm4"
    case alarm5 = "alarm5"
    case alarm6 = "alarm6"
    case alarm7 = "alarm7"
    
    // 备用音效（程序生成，当音频文件不存在时使用）
    case beep = "beep"
    
    /// 显示名称（本地化）
    var displayName: String {
        switch self {
        case .alarm1: return LocalizedString("sound_alarm1")
        case .alarm2: return LocalizedString("sound_alarm2")
        case .alarm3: return LocalizedString("sound_alarm3")
        case .alarm4: return LocalizedString("sound_alarm4")
        case .alarm5: return LocalizedString("sound_alarm5")
        case .alarm6: return LocalizedString("sound_alarm6")
        case .alarm7: return LocalizedString("sound_alarm7")
        case .beep: return LocalizedString("sound_beep")
        }
    }
    
    /// 音频文件名（不含扩展名）
    var fileName: String {
        return rawValue
    }
    
    /// 完整的音频文件名（含扩展名）
    /// AlarmKit 要求包含扩展名
    var fileNameWithExtension: String {
        return "\(rawValue).mp3"
    }
    
    /// 是否为程序生成的音效
    var isGenerated: Bool {
        return self == .beep
    }
    
    /// 获取音频文件 URL
    var fileURL: URL? {
        // 首先尝试在 Sounds 子目录查找
        if let url = Bundle.main.url(forResource: fileName, withExtension: "mp3", subdirectory: "Sounds") {
            return url
        }
        // 然后尝试在 bundle 根目录查找
        if let url = Bundle.main.url(forResource: fileName, withExtension: "mp3") {
            return url
        }
        // 尝试 m4a 格式
        if let url = Bundle.main.url(forResource: fileName, withExtension: "m4a", subdirectory: "Sounds") {
            return url
        }
        if let url = Bundle.main.url(forResource: fileName, withExtension: "m4a") {
            return url
        }
        return nil
    }
    
    /// 检查音频文件是否存在
    var isAvailable: Bool {
        return fileURL != nil || isGenerated
    }
    
    /// 获取所有可用的声音（不包含备用音效）
    static var availableSounds: [AlarmSound] {
        return AlarmSound.allCases.filter { $0.isAvailable && !$0.isGenerated }
    }
    
    /// 随机获取一个可用的声音
    static func randomAvailable() -> AlarmSound {
        let available = availableSounds
        if available.isEmpty {
            return .beep
        }
        return available.randomElement() ?? .beep
    }
}

/// 闹钟音频资源管理器
class AlarmSoundResourceManager {
    static let shared = AlarmSoundResourceManager()
    
    private init() {
        #if DEBUG
        checkAvailableSounds()
        #endif
    }
    
    /// 检查并打印可用的声音
    private func checkAvailableSounds() {
        print("🔊 闹钟声音资源检查:")
        for sound in AlarmSound.allCases {
            if sound.isGenerated {
                print("  ✅ \(sound.rawValue): 程序生成")
            } else if let url = sound.fileURL {
                print("  ✅ \(sound.rawValue): \(url.lastPathComponent)")
            } else {
                print("  ❌ \(sound.rawValue): 文件不存在")
            }
        }
        print("  📋 可用声音数量: \(AlarmSound.availableSounds.count)/\(AlarmSound.allCases.count)")
    }
    
    /// 获取用于 AlarmKit 的声音名称
    /// - Parameter sound: 闹钟声音类型
    /// - Returns: 用于 AlertConfiguration.AlertSound.named() 的文件名
    func getAlarmKitSoundName(for sound: AlarmSound) -> String? {
        // AlarmKit 需要文件名包含扩展名
        guard !sound.isGenerated else { return nil }
        
        // 检查文件是否存在
        if let url = sound.fileURL {
            return url.lastPathComponent
        }
        return nil
    }
    
    /// 获取随机的 AlarmKit 声音名称
    func getRandomAlarmKitSoundName() -> String? {
        let availableFileBasedSounds = AlarmSound.allCases.filter { 
            !$0.isGenerated && $0.fileURL != nil 
        }
        guard let randomSound = availableFileBasedSounds.randomElement() else {
            return nil
        }
        return getAlarmKitSoundName(for: randomSound)
    }
    
    /// 创建 AVAudioPlayer 用于应用内播放
    /// - Parameter sound: 闹钟声音类型
    /// - Returns: 配置好的 AVAudioPlayer，如果文件不存在则返回 nil
    func createAudioPlayer(for sound: AlarmSound) -> AVAudioPlayer? {
        guard let url = sound.fileURL else { return nil }
        
        do {
            let player = try AVAudioPlayer(contentsOf: url)
            player.prepareToPlay()
            return player
        } catch {
            #if DEBUG
            print("❌ 创建音频播放器失败 (\(sound.rawValue)): \(error)")
            #endif
            return nil
        }
    }
}
