//
//  AppReviewManager.swift
//  WakeupClock
//
//  应用评价管理器：负责在合适的时机引导用户评价
//

import Foundation
import StoreKit
import SwiftUI

/// 应用评价管理器（单例）
@MainActor
class AppReviewManager {
    static let shared = AppReviewManager()
    
    private let openCountKey = "appOpenCount"
    private let reviewRequestCountKey = "reviewRequestCount"
    private let lastReviewRequestVersionKey = "lastReviewRequestVersion"
    private let lastOpenTimestampKey = "lastOpenTimestamp"
    
    // 评价请求的触发点（第3、6、9次打开）
    private let reviewTriggerPoints = [3, 6, 9]
    
    // 每个版本最多请求3次
    private let maxRequestsPerVersion = 3
    
    // 两次打开之间的最小间隔（秒），避免短时间内重复计数
    private let minOpenInterval: TimeInterval = 30
    
    // 用于存储 requestReview 的闭包
    private var requestReviewAction: (() -> Void)?
    
    private init() {}
    
    /// 记录应用打开并检查是否需要请求评价
    func recordAppOpen() {
        let now = Date().timeIntervalSince1970
        let lastOpenTime = UserDefaults.standard.double(forKey: lastOpenTimestampKey)
        
        // 如果距离上次打开时间太短，不计数（避免视图切换导致的重复计数）
        if now - lastOpenTime < minOpenInterval && lastOpenTime > 0 {
            #if DEBUG
            print("⏸️ 距离上次打开时间过短，跳过计数")
            #endif
            return
        }
        
        // 更新最后打开时间
        UserDefaults.standard.set(now, forKey: lastOpenTimestampKey)
        
        // 增加打开次数
        let currentCount = UserDefaults.standard.integer(forKey: openCountKey)
        let newCount = currentCount + 1
        UserDefaults.standard.set(newCount, forKey: openCountKey)
        
        #if DEBUG
        print("📱 应用打开次数: \(newCount)")
        #endif
        
        // 检查当前版本
        let currentVersion = getCurrentAppVersion()
        let lastRequestVersion = UserDefaults.standard.string(forKey: lastReviewRequestVersionKey)
        
        // 如果版本更新了，重置请求次数
        if lastRequestVersion != currentVersion {
            UserDefaults.standard.set(0, forKey: reviewRequestCountKey)
            UserDefaults.standard.set(currentVersion, forKey: lastReviewRequestVersionKey)
            #if DEBUG
            print("🔄 检测到新版本，重置评价请求计数")
            #endif
        }
        
        // 检查是否应该请求评价
        if shouldRequestReview(openCount: newCount) {
            requestReview()
        }
    }
    
    /// 检查是否应该请求评价
    private func shouldRequestReview(openCount: Int) -> Bool {
        // 检查是否在触发点
        guard reviewTriggerPoints.contains(openCount) else {
            return false
        }
        
        // 检查当前版本的请求次数
        let requestCount = UserDefaults.standard.integer(forKey: reviewRequestCountKey)
        guard requestCount < maxRequestsPerVersion else {
            #if DEBUG
            print("⏸️ 已达到本版本最大请求次数（\(maxRequestsPerVersion)次），不再请求")
            #endif
            return false
        }
        
        return true
    }
    
    /// 设置 requestReview 环境值（在 SwiftUI View 中调用）
    func setRequestReviewAction(_ action: @escaping () -> Void) {
        self.requestReviewAction = action
    }
    
    /// 请求评价
    private func requestReview() {
        // 增加请求次数
        let currentRequestCount = UserDefaults.standard.integer(forKey: reviewRequestCountKey)
        let newRequestCount = currentRequestCount + 1
        UserDefaults.standard.set(newRequestCount, forKey: reviewRequestCountKey)
        
        #if DEBUG
        print("⭐ 请求用户评价（第\(newRequestCount)次）")
        #endif
        
        // 延迟一小段时间，确保界面已完全加载
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
            // 优先使用 SwiftUI 的新 API
            if let action = self?.requestReviewAction {
                action()
            } else {
                #if DEBUG
                print("⚠️ requestReview 环境值未设置，评价请求失败")
                #endif
            }
        }
    }
    
    /// 获取当前应用版本
    private func getCurrentAppVersion() -> String {
        return Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"
    }
}
