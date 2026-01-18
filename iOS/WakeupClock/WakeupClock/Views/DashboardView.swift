//
//  DashboardView.swift
//  WakeupClock
//
//  主界面：显示时钟和闹钟列表
//

import SwiftUI
import SwiftData

struct DashboardView: View {
    @EnvironmentObject var alarmManager: AlarmManager
    @EnvironmentObject var themeManager: ThemeManager
    @Query private var alarms: [AlarmModel]
    
    @State private var currentTime = Date()
    @State private var showAddAlarm = false
    @State private var showSettings = false
    @State private var showCalendar = false
    
    var body: some View {
        NavigationView {
            ZStack {
                // 背景
                backgroundView
                
                ScrollView {
                    VStack(spacing: 24) {
                        // 标题和设置按钮
                        headerView
                        
                        // 连续天数提示或鼓励语
                        if UserStatsManager.shared.streak > 0 {
                            streakView
                        } else {
                            encouragementView
                        }
                        
                        // 时钟显示
                        clockView
                        
                        // 闹钟列表
                        alarmListView
                    }
                    .padding()
                }
                
                // 添加闹钟按钮
                addAlarmButton
            }
            .navigationBarHidden(true)
        }
        .sheet(isPresented: $showAddAlarm) {
            AddAlarmView()
                .environmentObject(alarmManager)
        }
        .sheet(isPresented: $showSettings) {
            SettingsView()
                .environmentObject(themeManager)
                .environmentObject(UserStatsManager.shared)
        }
        .sheet(isPresented: $showCalendar) {
            CalendarView()
                .environmentObject(themeManager)
                .presentationDetents([.medium, .large])
        }
        .onAppear {
            startTimer()
        }
    }
    
    // MARK: - 子视图
    
    private var backgroundView: some View {
        Group {
            if themeManager.isDark {
                Color.black.ignoresSafeArea()
            } else {
                LinearGradient(
                    colors: [Color(hex: "F9FAFB"), Color(hex: "F3F4F6")],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()
            }
        }
    }
    
    private var headerView: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(LocalizedString("appName"))
                    .font(.system(size: 28, weight: .black, design: .rounded))
                    .foregroundColor(themeManager.isDark ? .white : .primary)
                
                Text(LocalizedString("slogan"))
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(themeManager.isDark ? .gray : .secondary)
            }
            
            Spacer()
            
            Button(action: {
                showSettings = true
            }) {
                Image(systemName: "gearshape.fill")
                    .font(.system(size: 20))
                    .foregroundColor(themeManager.isDark ? .gray : .primary)
                    .padding(8)
                    .background(
                        Circle()
                            .fill(themeManager.isDark ? Color.gray.opacity(0.2) : Color.white.opacity(0.6))
                    )
            }
        }
        .padding(.horizontal)
    }
    
    private var streakView: some View {
        Button(action: {
            showCalendar = true
        }) {
            HStack(spacing: 8) {
                Image(systemName: "flame.fill")
                    .foregroundColor(.orange)
                    .font(.system(size: 12))
                
                Text(String(format: LocalizedString("streakSentence"), UserStatsManager.shared.streak))
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.orange)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(
                Capsule()
                    .fill(themeManager.isDark ? Color.orange.opacity(0.2) : Color.orange.opacity(0.1))
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var encouragementView: some View {
        Button(action: {
            showCalendar = true
        }) {
            HStack(spacing: 8) {
                Image(systemName: "sparkles")
                    .foregroundColor(.purple)
                    .font(.system(size: 12))
                
                Text(LocalizedString("encouragementMessage"))
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.purple)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(
                Capsule()
                    .fill(themeManager.isDark ? Color.purple.opacity(0.2) : Color.purple.opacity(0.1))
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var clockView: some View {
        VStack(spacing: 16) {
            // 圆形时钟
            ZStack {
                // 背景圆
                Circle()
                    .fill(themeManager.isDark ? Color.gray.opacity(0.3) : Color.white)
                    .frame(width: 260, height: 260)
                    .shadow(color: .black.opacity(0.1), radius: 20, x: 0, y: 10)
                
                // 秒针进度环
                Circle()
                    .trim(from: 0, to: CGFloat(Calendar.current.component(.second, from: currentTime)) / 60.0)
                    .stroke(
                        LinearGradient(
                            colors: [Color(hex: "6366F1"), Color(hex: "A855F7")],
                            startPoint: .leading,
                            endPoint: .trailing
                        ),
                        style: StrokeStyle(lineWidth: 4, lineCap: .round)
                    )
                    .frame(width: 240, height: 240)
                    .rotationEffect(.degrees(-90))
                
                // 时间显示
                VStack(spacing: 4) {
                    Text(timeString)
                        .font(.system(size: 56, weight: .bold, design: .rounded))
                        .foregroundColor(themeManager.isDark ? .white : .primary)
                    
                    // 倒计时提示
                    if let countdown = alarmManager.getCountdownText() {
                        HStack(spacing: 4) {
                            Image(systemName: "bell.fill")
                                .font(.system(size: 10))
                            Text(countdown)
                                .font(.system(size: 12, weight: .semibold))
                        }
                        .foregroundColor(Color(hex: "6366F1"))
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(
                            Capsule()
                                .fill(Color(hex: "6366F1").opacity(0.1))
                        )
                    }
                }
            }
        }
    }
    
    private var alarmListView: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(LocalizedString("myAlarms"))
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(themeManager.isDark ? .white : .primary)
                .padding(.horizontal)
            
            if alarms.isEmpty {
                emptyStateView
            } else {
                ForEach(alarms) { alarm in
                    AlarmRowView(alarm: alarm)
                        .environmentObject(alarmManager)
                        .environmentObject(themeManager)
                }
            }
        }
    }
    
    private var emptyStateView: some View {
        VStack(spacing: 12) {
            Image(systemName: "bell.slash")
                .font(.system(size: 48))
                .foregroundColor(.gray)
            
            Text(LocalizedString("noAlarms"))
                .font(.system(size: 14))
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
    }
    
    private var addAlarmButton: some View {
        VStack {
            Spacer()
            HStack {
                Spacer()
                Button(action: {
                    showAddAlarm = true
                }) {
                    Image(systemName: "plus")
                        .font(.system(size: 24, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 64, height: 64)
                        .background(
                            LinearGradient(
                                colors: [Color(hex: "6366F1"), Color(hex: "A855F7")],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .clipShape(Circle())
                        .shadow(color: Color(hex: "6366F1").opacity(0.4), radius: 20, x: 0, y: 10)
                }
                .padding(.trailing, 24)
                .padding(.bottom, 24)
            }
        }
    }
    
    // MARK: - 计算属性
    
    private var timeString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: currentTime)
    }
    
    // MARK: - 方法
    
    private func startTimer() {
        Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [self] _ in
            currentTime = Date()
        }
    }
}
