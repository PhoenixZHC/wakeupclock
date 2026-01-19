//
//  SettingsView.swift
//  WakeupClock
//
//  设置页面
//

import SwiftUI
import SwiftData

struct SettingsView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @EnvironmentObject var userStatsManager: UserStatsManager
    @Environment(\.dismiss) var dismiss
    @Environment(\.modelContext) private var modelContext
    @Query private var settings: [AppSettings]
    
    @State private var selectedLanguage = "zh"
    @State private var showResetAlert = false
    @State private var showUsageGuide = false
    @State private var refreshID = UUID()
    
    /// 获取或创建设置对象
    private var currentSettings: AppSettings {
        if let existing = settings.first {
            return existing
        }
        // 如果没有设置对象，创建一个新的
        let newSettings = AppSettings()
        modelContext.insert(newSettings)
        try? modelContext.save()
        return newSettings
    }
    
    var body: some View {
        NavigationView {
            Form {
                // 显示模式
                Section(header: Text(LocalizedString("themeMode"))) {
                    Picker("", selection: Binding(
                        get: { themeManager.themeMode },
                        set: { themeManager.setThemeMode($0) }
                    )) {
                        Text(LocalizedString("themeAuto")).tag(ThemeMode.auto)
                        Text(LocalizedString("themeLight")).tag(ThemeMode.light)
                        Text(LocalizedString("themeDark")).tag(ThemeMode.dark)
                    }
                    .pickerStyle(.segmented)
                }
                
                // 语言设置
                Section(header: Text(LocalizedString("language"))) {
                    Picker("", selection: $selectedLanguage) {
                        Text("中文").tag("zh")
                        Text("English").tag("en")
                    }
                    .pickerStyle(.segmented)
                    .onChange(of: selectedLanguage) { oldValue, newValue in
                        UserDefaults.standard.set(newValue, forKey: "appLanguage")
                        // 立即刷新界面
                        refreshID = UUID()
                    }
                }
                
                // 防赖床模式设置
                Section {
                    // 使用本地状态来驱动UI，避免直接依赖Query结果
                    AntiSnoozeSettingsView(settings: currentSettings)
                } header: {
                    Text(LocalizedString("antiSnoozeTitle"))
                } footer: {
                    Text(LocalizedString("antiSnoozeDesc"))
                }
                
                // 数据管理
                Section(header: Text(LocalizedString("dataManagement"))) {
                    Button(action: {
                        showResetAlert = true
                    }) {
                        HStack {
                            Image(systemName: "arrow.counterclockwise")
                            Text(LocalizedString("resetData"))
                            Spacer()
                        }
                        .foregroundColor(.red)
                    }
                }
                
                // 使用说明
                Section(header: Text(LocalizedString("help"))) {
                    Button(action: {
                        showUsageGuide = true
                    }) {
                        HStack {
                            Image(systemName: "questionmark.circle")
                            Text(LocalizedString("usageGuide"))
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                // 应用信息
                Section(header: Text(LocalizedString("about"))) {
                    HStack {
                        Text(LocalizedString("version"))
                        Spacer()
                        Text(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle(LocalizedString("settings"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(LocalizedString("back")) {
                        dismiss()
                    }
                }
            }
            .alert(LocalizedString("confirmReset"), isPresented: $showResetAlert) {
                Button(LocalizedString("cancel"), role: .cancel) {}
                Button(LocalizedString("reset"), role: .destructive) {
                    userStatsManager.clearData()
                }
            } message: {
                Text(LocalizedString("resetConfirm"))
            }
            .sheet(isPresented: $showUsageGuide) {
                UsageGuideView()
            }
            .id(refreshID)
            .onAppear {
                selectedLanguage = UserDefaults.standard.string(forKey: "appLanguage") ?? "zh"
            }
        }
    }
}

// MARK: - 使用指南视图

struct UsageGuideView: View {
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    // 标题和副标题
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "bell.badge.fill")
                                .font(.system(size: 32))
                                .foregroundColor(.blue)
                            Text(LocalizedString("usageGuide"))
                                .font(.system(size: 28, weight: .bold))
                        }
                        Text(LocalizedString("guideSubtitle"))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    .padding(.horizontal)
                    
                    // 系统级闹钟特性
                    GuideSection(
                        title: LocalizedString("recommendedPractices"),
                        color: .blue,
                        items: [
                            GuideTip(
                                icon: "shield.checkered",
                                title: LocalizedString("guideTip1Title"),
                                description: LocalizedString("guideTip1Desc"),
                                iconColor: .blue
                            ),
                            GuideTip(
                                icon: "speaker.wave.3.fill",
                                title: LocalizedString("guideTip2Title"),
                                description: LocalizedString("guideTip2Desc"),
                                iconColor: .blue
                            ),
                            GuideTip(
                                icon: "applewatch",
                                title: LocalizedString("guideTip3Title"),
                                description: LocalizedString("guideTip3Desc"),
                                iconColor: .blue
                            ),
                            GuideTip(
                                icon: "iphone.and.arrow.forward",
                                title: LocalizedString("guideTip4Title"),
                                description: LocalizedString("guideTip4Desc"),
                                iconColor: .blue
                            )
                        ]
                    )
                    
                    // 注意事项
                    GuideSection(
                        title: LocalizedString("notRecommendedPractices"),
                        color: .orange,
                        items: [
                            GuideTip(
                                icon: "checkerboard.shield",
                                title: LocalizedString("guideTip5Title"),
                                description: LocalizedString("guideTip5Desc"),
                                iconColor: .orange
                            ),
                            GuideTip(
                                icon: "power",
                                title: LocalizedString("guideTip6Title"),
                                description: LocalizedString("guideTip6Desc"),
                                iconColor: .orange
                            ),
                            GuideTip(
                                icon: "eye.fill",
                                title: LocalizedString("guideTip7Title"),
                                description: LocalizedString("guideTip7Desc"),
                                iconColor: .orange
                            ),
                            GuideTip(
                                icon: "battery.100.bolt",
                                title: LocalizedString("guideTip8Title"),
                                description: LocalizedString("guideTip8Desc"),
                                iconColor: .orange
                            )
                        ]
                    )
                    
                    // AlarmKit 技术说明
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Image(systemName: "app.badge.checkmark.fill")
                                .font(.title2)
                                .foregroundColor(.blue)
                            Text(LocalizedString("systemLimitations"))
                                .font(.headline)
                                .foregroundColor(.primary)
                        }
                        
                        Text(LocalizedString("systemLimitationsDesc"))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color.blue.opacity(0.1))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color.blue.opacity(0.3), lineWidth: 1)
                    )
                    .padding(.horizontal)
                    
                    // 版本信息
                    HStack {
                        Spacer()
                        VStack(spacing: 4) {
                            Text("Powered by AlarmKit")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                            Text("iOS 26.0+")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                    }
                    .padding(.top, 8)
                }
                .padding(.vertical, 20)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(LocalizedString("done")) {
                        dismiss()
                    }
                    .fontWeight(.semibold)
                }
            }
        }
    }
}

// MARK: - 辅助组件

struct GuideSection: View {
    let title: String
    let color: Color
    let items: [GuideTip]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(title)
                .font(.title3)
                .fontWeight(.bold)
                .padding(.horizontal)
            
            ForEach(items) { item in
                HStack(alignment: .top, spacing: 12) {
                    Image(systemName: item.icon)
                        .font(.title3)
                        .foregroundColor(item.iconColor)
                        .frame(width: 28)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(item.title)
                            .font(.subheadline)
                            .fontWeight(.semibold)
                        Text(item.description)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}

struct GuideTip: Identifiable {
    let id = UUID()
    let icon: String
    let title: String
    let description: String
    let iconColor: Color
}

// MARK: - 防赖床设置子视图

struct AntiSnoozeSettingsView: View {
    @Bindable var settings: AppSettings
    
    var body: some View {
        Toggle(LocalizedString("enableAntiSnooze"), isOn: $settings.enableAntiSnooze)
        
        if settings.enableAntiSnooze {
            Picker(LocalizedString("antiSnoozeInterval"), selection: $settings.antiSnoozeInterval) {
                Text("1 \(LocalizedString("minutes"))").tag(1)
                Text("2 \(LocalizedString("minutes"))").tag(2)
                Text("3 \(LocalizedString("minutes"))").tag(3)
                Text("5 \(LocalizedString("minutes"))").tag(5)
            }
            
            Picker(LocalizedString("antiSnoozeCount"), selection: $settings.antiSnoozeCount) {
                Text("1 \(LocalizedString("times"))").tag(1)
                Text("2 \(LocalizedString("times"))").tag(2)
                Text("3 \(LocalizedString("times"))").tag(3)
                Text("4 \(LocalizedString("times"))").tag(4)
            }
        }
    }
}
