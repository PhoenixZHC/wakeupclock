//
//  ThemeManager.swift
//  WakeupClock
//
//  主题管理器：负责应用主题和外观管理
//

import Foundation
import SwiftUI
import Combine

/// 主题管理器（单例）
@MainActor
class ThemeManager: ObservableObject {
    static let shared = ThemeManager()
    
    @Published var themeMode: ThemeMode = .auto
    @Published var isDark: Bool = false
    
    private var cancellables = Set<AnyCancellable>()
    private var themeCheckTimer: Timer?
    
    private init() {
        // 从UserDefaults加载主题设置
        if let savedTheme = UserDefaults.standard.string(forKey: "themeMode"),
           let theme = ThemeMode(rawValue: savedTheme) {
            themeMode = theme
        }
        
        updateTheme()
        startThemeCheckTimer()
    }
    
    /// 设置主题模式
    func setThemeMode(_ mode: ThemeMode) {
        themeMode = mode
        UserDefaults.standard.set(mode.rawValue, forKey: "themeMode")
        updateTheme()
    }
    
    /// 更新主题
    private func updateTheme() {
        switch themeMode {
        case .auto:
            // 根据时间自动切换：18:00-6:00为夜间模式
            let hour = Calendar.current.component(.hour, from: Date())
            isDark = hour >= 18 || hour < 6
            
        case .light:
            isDark = false
            
        case .dark:
            isDark = true
        }
    }
    
    /// 启动主题检查定时器（仅在自动模式下）
    private func startThemeCheckTimer() {
        themeCheckTimer = Timer.scheduledTimer(withTimeInterval: 60.0, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            Task { @MainActor [weak self] in
                guard let self = self, self.themeMode == .auto else { return }
                self.updateTheme()
            }
        }
    }
}
