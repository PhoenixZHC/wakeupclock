//
//  CalendarView.swift
//  WakeupClock
//
//  日历视图：显示打卡记录和闹钟类型图标
//

import SwiftUI
import SwiftData

struct CalendarView: View {
    @Query private var records: [WakeUpRecord]
    @EnvironmentObject var themeManager: ThemeManager
    
    @State private var selectedMonth = Date()
    
    var body: some View {
        VStack(spacing: 0) {
            // 标题栏
            HStack {
                Text(LocalizedString("calendar"))
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(themeManager.isDark ? .white : .primary)
                
                Spacer()
                
                // 月份切换按钮
                HStack(spacing: 16) {
                    Button(action: {
                        withAnimation {
                            selectedMonth = Calendar.current.date(byAdding: .month, value: -1, to: selectedMonth) ?? selectedMonth
                        }
                    }) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(themeManager.isDark ? .white : .primary)
                    }
                    
                    Text(monthYearString)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(themeManager.isDark ? .white : .primary)
                        .frame(minWidth: 120)
                    
                    Button(action: {
                        withAnimation {
                            selectedMonth = Calendar.current.date(byAdding: .month, value: 1, to: selectedMonth) ?? selectedMonth
                        }
                    }) {
                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(themeManager.isDark ? .white : .primary)
                    }
                }
            }
            .padding()
            
            Divider()
            
            // 星期标题
            HStack(spacing: 0) {
                ForEach(["日", "一", "二", "三", "四", "五", "六"], id: \.self) { day in
                    Text(day)
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(themeManager.isDark ? .gray : .secondary)
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.vertical, 8)
            .padding(.horizontal)
            
            Divider()
            
            // 日历网格
            calendarGrid
                .padding()
        }
        .background(
            RoundedRectangle(cornerRadius: 20)
                .fill(themeManager.isDark ? Color.gray.opacity(0.2) : Color.white)
        )
    }
    
    private var monthYearString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy年MM月"
        return formatter.string(from: selectedMonth)
    }
    
    private var calendarGrid: some View {
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month], from: selectedMonth)
        
        if let firstDayOfMonth = calendar.date(from: components) {
            let firstWeekday = calendar.component(.weekday, from: firstDayOfMonth)
            let daysInMonth = calendar.range(of: .day, in: .month, for: selectedMonth)?.count ?? 0
            
            // 创建日期数组（在计算属性中完成）
            let days = buildDaysArray(calendar: calendar, firstDayOfMonth: firstDayOfMonth, firstWeekday: firstWeekday, daysInMonth: daysInMonth)
            
            return AnyView(
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 7), spacing: 8) {
                    ForEach(0..<days.count, id: \.self) { index in
                        if let date = days[index] {
                            calendarDayCell(date: date)
                        } else {
                            Color.clear
                                .frame(height: 50)
                        }
                    }
                }
            )
        } else {
            return AnyView(
                Text("无法加载日历")
                    .foregroundColor(.secondary)
                    .padding()
            )
        }
    }
    
    // 辅助方法：构建日期数组
    private func buildDaysArray(calendar: Calendar, firstDayOfMonth: Date, firstWeekday: Int, daysInMonth: Int) -> [Date?] {
        var days: [Date?] = []
        
        // 填充月初的空格
        for _ in 1..<firstWeekday {
            days.append(nil)
        }
        
        // 添加当月的所有日期
        for day in 1...daysInMonth {
            if let date = calendar.date(byAdding: .day, value: day - 1, to: firstDayOfMonth) {
                days.append(date)
            }
        }
        
        return days
    }
    
    private func calendarDayCell(date: Date) -> some View {
        let calendar = Calendar.current
        let day = calendar.component(.day, from: date)
        let dateStr = dateString(from: date)
        let record = records.first { $0.date == dateStr }
        let hasRecord = record != nil
        let isToday = calendar.isDateInToday(date)
        
        return VStack(spacing: 4) {
            Text("\(day)")
                .font(.system(size: 14, weight: isToday ? .bold : .regular))
                .foregroundColor(
                    isToday ? .white : (themeManager.isDark ? .white : .primary)
                )
            
            if hasRecord {
                // 显示闹钟类型图标
                if let alarmLabel = record?.alarmLabel {
                    Image(systemName: categoryIconName(for: alarmLabel))
                        .font(.system(size: 12))
                        .foregroundColor(categoryColor(for: alarmLabel))
                } else {
                    // 没有类型信息，显示圆点
                    Circle()
                        .fill(Color.green)
                        .frame(width: 6, height: 6)
                }
            }
        }
        .frame(width: 50, height: 50)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(
                    isToday ? Color(hex: "6366F1") :
                    (hasRecord ? (themeManager.isDark ? Color.green.opacity(0.2) : Color.green.opacity(0.1)) : Color.clear)
                )
        )
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(isToday ? Color(hex: "6366F1") : Color.clear, lineWidth: 2)
        )
    }
    
    private func dateString(from date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: date)
    }
    
    private func categoryIconName(for label: String) -> String {
        switch label {
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
    
    private func categoryColor(for label: String) -> Color {
        switch label {
        case "work": return .blue
        case "date": return .pink
        case "flight": return .cyan
        case "train": return .orange
        case "meeting": return .purple
        case "doctor": return .red
        case "interview": return .green
        case "exam": return .yellow
        default: return .gray
        }
    }
}
