//
//  NotificationDebugView.swift
//  WakeupClock
//
//  通知调试视图：用于查看和管理待处理的通知
//

import SwiftUI
import UserNotifications

struct NotificationDebugView: View {
    @State private var notifications: [UNNotificationRequest] = []
    @State private var isLoading = false
    
    var body: some View {
        List {
            Section(header: sectionHeader) {
                if isLoading {
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                } else if notifications.isEmpty {
                    Text("暂无待处理通知")
                        .foregroundColor(.secondary)
                        .font(.subheadline)
                } else {
                    ForEach(notifications, id: \.identifier) { request in
                        notificationRow(for: request)
                    }
                }
            }
            
            Section {
                Button(action: clearAllNotifications) {
                    HStack {
                        Image(systemName: "trash")
                        Text("清除所有通知")
                        Spacer()
                    }
                    .foregroundColor(.red)
                }
            }
        }
        .navigationTitle(LocalizedString("notificationDebug"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(LocalizedString("refresh")) {
                    loadNotifications()
                }
            }
        }
        .onAppear {
            loadNotifications()
        }
    }
    
    private var sectionHeader: some View {
        HStack {
            Text("\(LocalizedString("pendingNotifications")) (\(notifications.count)/64)")
            Spacer()
        }
    }
    
    private func notificationRow(for request: UNNotificationRequest) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            // 通知标识符
            Text(request.identifier)
                .font(.caption)
                .foregroundColor(.primary)
            
            // 触发时间
            if let trigger = request.trigger as? UNCalendarNotificationTrigger,
               let date = trigger.nextTriggerDate() {
                HStack(spacing: 4) {
                    Image(systemName: "clock")
                        .font(.caption2)
                    Text("\(LocalizedString("triggerTime")): \(formatDate(date))")
                        .font(.caption2)
                }
                .foregroundColor(.secondary)
            }
            
            // 通知类型标签
            if let userInfo = request.content.userInfo as? [String: Any],
               let type = userInfo["notificationType"] as? String {
                Text(notificationTypeLabel(type))
                    .font(.caption2)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(notificationTypeColor(type))
                    .foregroundColor(.white)
                    .cornerRadius(4)
            }
        }
        .padding(.vertical, 4)
    }
    
    private func notificationTypeLabel(_ type: String) -> String {
        if type == "main" {
            return "主通知"
        } else if type.hasPrefix("backup") {
            return "备份\(type.replacingOccurrences(of: "backup", with: ""))"
        }
        return type
    }
    
    private func notificationTypeColor(_ type: String) -> Color {
        if type == "main" {
            return .blue
        } else {
            return .orange
        }
    }
    
    private func loadNotifications() {
        isLoading = true
        UNUserNotificationCenter.current().getPendingNotificationRequests { requests in
            DispatchQueue.main.async {
                self.notifications = requests.sorted {
                    let date1 = ($0.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() ?? Date.distantFuture
                    let date2 = ($1.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() ?? Date.distantFuture
                    return date1 < date2
                }
                self.isLoading = false
            }
        }
    }
    
    private func clearAllNotifications() {
        UNUserNotificationCenter.current().removeAllPendingNotificationRequests()
        loadNotifications()
    }
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MM-dd HH:mm:ss"
        return formatter.string(from: date)
    }
}
