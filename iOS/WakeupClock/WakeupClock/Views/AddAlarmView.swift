//
//  AddAlarmView.swift
//  WakeupClock
//
//  添加/编辑闹钟视图
//

import SwiftUI

struct AddAlarmView: View {
    @EnvironmentObject var alarmManager: AlarmManager
    @Environment(\.dismiss) var dismiss
    
    @State private var selectedTime = Date()
    @State private var selectedLabel = "work"
    @State private var repeatMode: RepeatMode = .workdays
    @State private var customDays: Set<Int> = [1, 2, 3, 4, 5]
    @State private var skipHolidays = false
    @State private var showCustomDaysRequiredAlert = false
    
    let categories = [
        ("work", "briefcase.fill", "label_work"),
        ("date", "heart.fill", "label_date"),
        ("flight", "airplane", "label_flight"),
        ("train", "tram.fill", "label_train"),
        ("meeting", "person.3.fill", "label_meeting"),
        ("doctor", "cross.case.fill", "label_doctor"),
        ("interview", "person.badge.plus", "label_interview"),
        ("exam", "graduationcap.fill", "label_exam"),
        ("other", "tag.fill", "label_other")
    ]
    
    var body: some View {
        NavigationView {
            Form {
                // 时间选择（强制 24 小时制，避免 12 小时制下「12 点」被当成午夜 12:00 AM）
                Section {
                    DatePicker(
                        LocalizedString("timeLabel"),
                        selection: $selectedTime,
                        displayedComponents: .hourAndMinute
                    )
                    .datePickerStyle(.compact)
                    .environment(\.locale, Locale(identifier: "en_GB")) // 使用 24 小时制，12:00 明确表示中午
                    .frame(maxWidth: 200)
                }
                
                // 标签选择
                Section(header: Text(LocalizedString("labelLabel"))) {
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 4), spacing: 16) {
                        ForEach(categories, id: \.0) { category in
                            categoryButton(category: category)
                        }
                    }
                    .padding(.vertical, 8)
                }
                
                // 重复模式
                Section(header: Text(LocalizedString("repeatLabel"))) {
                    Picker("", selection: $repeatMode) {
                        Text(LocalizedString("repeatOnce")).tag(RepeatMode.once)
                        Text(LocalizedString("repeatWorkdays")).tag(RepeatMode.workdays)
                        Text(LocalizedString("repeatCustom")).tag(RepeatMode.custom)
                    }
                    .pickerStyle(.segmented)
                    
                    if repeatMode == .custom {
                        // 自定义日期选择
                        VStack(alignment: .leading, spacing: 12) {
                            Text(LocalizedString("selectDaysLabel"))
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(.secondary)
                            
                            HStack {
                                ForEach([1, 2, 3, 4, 5, 6, 0], id: \.self) { day in
                                    dayButton(day: day)
                                }
                            }
                            
                            // 跳过节假日选项
                            Toggle(isOn: $skipHolidays) {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(LocalizedString("skipHolidaysLabel"))
                                        .font(.system(size: 14, weight: .medium))
                                    Text(LocalizedString("skipHolidaysDesc"))
                                        .font(.system(size: 12))
                                        .foregroundColor(.secondary)
                                }
                            }
                            .onChange(of: skipHolidays) { oldValue, newValue in
                                // 当用户开启节假日跳过功能时，强制获取最新节假日数据
                                if newValue && !oldValue {
                                    HolidayChecker.preloadHolidays(forceRefresh: true)
                                }
                            }
                        }
                        .padding(.vertical, 8)
                    }
                }
            }
            .navigationTitle(LocalizedString("newAlarm"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(LocalizedString("cancel")) {
                        dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(LocalizedString("saveAlarm")) {
                        saveAlarm()
                    }
                    .fontWeight(.semibold)
                }
            }
            .alert(LocalizedString("customDaysRequired"), isPresented: $showCustomDaysRequiredAlert) {
                Button(LocalizedString("ok")) { }
            } message: {
                Text(LocalizedString("customDaysRequiredMessage"))
            }
        }
    }
    
    private func categoryButton(category: (String, String, String)) -> some View {
        Button(action: {
            withAnimation {
                selectedLabel = category.0
            }
        }) {
            VStack(spacing: 8) {
                Image(systemName: category.1)
                    .font(.system(size: 24))
                    .foregroundColor(selectedLabel == category.0 ? .white : .primary)
                    .frame(width: 56, height: 56)
                    .background(
                        Circle()
                            .fill(selectedLabel == category.0 ?
                                  LinearGradient(
                                    colors: [Color(hex: "6366F1"), Color(hex: "A855F7")],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                  ) :
                                  LinearGradient(
                                    colors: [Color.gray.opacity(0.1), Color.gray.opacity(0.1)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                  )
                            )
                    )
                    .overlay(
                        Circle()
                            .stroke(selectedLabel == category.0 ? Color.clear : Color.gray.opacity(0.2), lineWidth: 1)
                    )
                
                Text(LocalizedString(category.2))
                    .font(.system(size: 10, weight: selectedLabel == category.0 ? .semibold : .regular))
                    .foregroundColor(selectedLabel == category.0 ? Color(hex: "6366F1") : .secondary)
            }
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private func dayButton(day: Int) -> some View {
        let isSelected = customDays.contains(day)
        return Button {
            withAnimation(.easeInOut(duration: 0.15)) {
                if isSelected {
                    customDays.remove(day)
                } else {
                    customDays.insert(day)
                }
            }
        } label: {
            Text(LocalizedString("day_\(day)"))
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(isSelected ? .white : .primary)
                .frame(width: 40, height: 40)
                .background(
                    Circle()
                        .fill(isSelected ?
                              Color(hex: "6366F1") :
                              Color.gray.opacity(0.1)
                        )
                )
        }
        .buttonStyle(.plain)
    }
    
    private func saveAlarm() {
        if repeatMode == .custom && customDays.isEmpty {
            showCustomDaysRequiredAlert = true
            return
        }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_GB")
        formatter.dateFormat = "HH:mm"
        let timeString = formatter.string(from: selectedTime)
        
        let alarm = AlarmModel(
            time: timeString,
            enabled: true,
            label: selectedLabel,
            missionType: .math,
            difficulty: .medium,
            repeatMode: repeatMode,
            customDays: repeatMode == .custom ? Array(customDays) : [],
            skipHolidays: repeatMode == .custom ? skipHolidays : false
        )
        
        alarmManager.addAlarm(alarm)
        dismiss()
    }
    
    /// 从 "HH:mm" 反解为当天该时刻的 Date（24 小时制，供编辑闹钟时预填时间使用）
    static func date(from timeString: String) -> Date? {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_GB")
        formatter.dateFormat = "HH:mm"
        guard let date = formatter.date(from: timeString) else { return nil }
        let cal = Calendar.current
        var comps = cal.dateComponents([.year, .month, .day], from: Date())
        comps.hour = cal.component(.hour, from: date)
        comps.minute = cal.component(.minute, from: date)
        return cal.date(from: comps)
    }
}
