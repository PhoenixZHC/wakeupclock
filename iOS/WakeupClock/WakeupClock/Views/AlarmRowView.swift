//
//  AlarmRowView.swift
//  WakeupClock
//
//  闹钟列表项视图
//

import SwiftUI

struct AlarmRowView: View {
    @EnvironmentObject var alarmManager: AlarmManager
    @EnvironmentObject var themeManager: ThemeManager
    
    let alarm: AlarmModel
    @State private var offset: CGFloat = 0
    @State private var dragStart: CGFloat = 0
    
    var body: some View {
        ZStack(alignment: .trailing) {
            // 删除背景（更美观的设计）
            if offset < 0 {
                deleteBackground
            }
            
            // 主内容
            mainContent
                .offset(x: offset)
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            if value.translation.width < 0 {
                                offset = max(value.translation.width, -100)
                            }
                        }
                        .onEnded { value in
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                                if value.translation.width < -50 {
                                    offset = -100
                                } else {
                                    offset = 0
                                }
                            }
                        }
                )
        }
        .padding(.horizontal)
    }
    
    private var deleteBackground: some View {
        HStack {
            Spacer()
            Button(action: {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                    alarmManager.deleteAlarm(alarm)
                }
            }) {
                VStack(spacing: 6) {
                    Image(systemName: "trash.fill")
                        .font(.system(size: 22, weight: .semibold))
                    Text("删除")
                        .font(.system(size: 12, weight: .medium))
                }
                .foregroundColor(.white)
                .frame(width: 100, height: 100)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(
                            LinearGradient(
                                colors: [Color.red, Color.red.opacity(0.8)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .shadow(color: .red.opacity(0.3), radius: 8, x: 0, y: 4)
                )
            }
        }
        .padding(.trailing, 16)
    }
    
    private var mainContent: some View {
        VStack(spacing: 0) {
            // 第一行：分类图标、时间和开关
            HStack(spacing: 16) {
                // 分类图标和时间在同一行
                HStack(spacing: 12) {
                    categoryIcon
                    
                    Text(alarm.time)
                        .font(.system(size: 36, weight: .light, design: .rounded))
                        .foregroundColor(themeManager.isDark ? .white : .primary)
                }
                
                Spacer()
                
                // 开关和删除按钮
                HStack(spacing: 16) {
                    // 开关
                    Toggle("", isOn: Binding(
                        get: { alarm.enabled },
                        set: { _ in alarmManager.toggleAlarm(alarm) }
                    ))
                    .toggleStyle(SwitchToggleStyle(tint: Color(hex: "6366F1")))
                    .labelsHidden()
                    
                    // 删除按钮
                    Button(action: {
                        alarmManager.deleteAlarm(alarm)
                    }) {
                        Image(systemName: "trash")
                            .foregroundColor(themeManager.isDark ? .gray : .gray.opacity(0.6))
                            .font(.system(size: 18))
                    }
                }
            }
            .padding()
            
            // 第二行：重复信息（如果有）
            if shouldShowRepeatInfo {
                HStack {
                    repeatInfoView
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.bottom, 8)
            }
        }
        .background(
            RoundedRectangle(cornerRadius: 16)
                .fill(themeManager.isDark ? Color.gray.opacity(0.3) : Color.white)
        )
        .shadow(color: .black.opacity(0.05), radius: 10, x: 0, y: 5)
    }
    
    private var shouldShowRepeatInfo: Bool {
        switch alarm.repeatModeEnum {
        case .once, .workdays:
            return true
        case .custom:
            return !alarm.customDays.isEmpty || alarm.skipHolidays
        }
    }
    
    private var categoryIcon: some View {
        Image(systemName: categoryIconName)
            .font(.system(size: 20))
            .foregroundColor(Color(hex: "6366F1"))
            .frame(width: 40, height: 40)
            .background(
                Circle()
                    .fill(Color(hex: "6366F1").opacity(0.1))
            )
    }
    
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
        default: return "tag.fill"
        }
    }
    
    private var repeatInfoView: some View {
        VStack(alignment: .leading, spacing: 6) {
            switch alarm.repeatModeEnum {
            case .once:
                Text(LocalizedString("repeatOnce"))
                    .font(.system(size: 12, weight: .medium))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(
                        Capsule()
                            .fill(themeManager.isDark ? Color.blue.opacity(0.25) : Color.blue.opacity(0.15))
                    )
                    .foregroundColor(themeManager.isDark ? Color.blue.opacity(0.9) : Color.blue)
                
            case .workdays:
                Text(LocalizedString("repeatWorkdays"))
                    .font(.system(size: 12, weight: .medium))
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(
                        Capsule()
                            .fill(themeManager.isDark ? Color.green.opacity(0.25) : Color.green.opacity(0.15))
                    )
                    .foregroundColor(themeManager.isDark ? Color.green.opacity(0.9) : Color.green)
                
            case .custom:
                VStack(alignment: .leading, spacing: 6) {
                    if alarm.customDays.count == 7 {
                        Text(LocalizedString("everyday"))
                            .font(.system(size: 12, weight: .medium))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(
                                Capsule()
                                    .fill(themeManager.isDark ? Color.purple.opacity(0.25) : Color.purple.opacity(0.15))
                            )
                            .foregroundColor(themeManager.isDark ? Color.purple.opacity(0.9) : Color.purple)
                    } else if !alarm.customDays.isEmpty {
                        HStack(spacing: 4) {
                            ForEach(sortedDays, id: \.self) { day in
                                Text(dayLabel(for: day))
                                    .font(.system(size: 10, weight: .bold))
                                    .frame(width: 18, height: 18)
                                    .background(
                                        Circle()
                                            .fill(themeManager.isDark ? Color.indigo.opacity(0.3) : Color.indigo.opacity(0.2))
                                    )
                                    .foregroundColor(themeManager.isDark ? Color.indigo.opacity(0.9) : Color.indigo)
                            }
                        }
                    }
                    
                    // 跳过节假日显示在自定义日期下面
                    if alarm.skipHolidays {
                        Text(LocalizedString("skipHolidaysTag"))
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(.orange)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 3)
                            .background(
                                Capsule()
                                    .fill(Color.orange.opacity(0.15))
                                    .overlay(
                                        Capsule()
                                            .stroke(Color.orange.opacity(0.3), lineWidth: 0.5)
                                    )
                            )
                    }
                }
            }
        }
    }
    
    private var sortedDays: [Int] {
        alarm.customDays.sorted { day1, day2 in
            let d1 = day1 == 0 ? 7 : day1
            let d2 = day2 == 0 ? 7 : day2
            return d1 < d2
        }
    }
    
    private func dayLabel(for day: Int) -> String {
        return LocalizedString("day_\(day)")
    }
}
