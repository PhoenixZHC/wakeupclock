//
//  WakeUpRecord.swift
//  WakeupClock
//
//  起床打卡记录数据模型
//

import Foundation
import SwiftData

/// 起床打卡记录
@Model
final class WakeUpRecord {
    /// 唯一标识符
    @Attribute(.unique) var id: String
    
    /// 日期 (格式: YYYY-MM-DD)
    var date: String
    
    /// 时间 (格式: HH:mm)
    var time: String
    
    /// 创建时间戳
    var createdAt: Date
    
    /// 闹钟类型标签（用于显示图标）
    var alarmLabel: String?
    
    init(
        id: String = UUID().uuidString,
        date: String,
        time: String,
        createdAt: Date = Date(),
        alarmLabel: String? = nil
    ) {
        self.id = id
        self.date = date
        self.time = time
        self.createdAt = createdAt
        self.alarmLabel = alarmLabel
    }
    
    /// 从当前时间创建记录
    static func createFromNow(alarmLabel: String? = nil) -> WakeUpRecord {
        let now = Date()
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let dateStr = formatter.string(from: now)
        
        formatter.dateFormat = "HH:mm"
        let timeStr = formatter.string(from: now)
        
        return WakeUpRecord(date: dateStr, time: timeStr, createdAt: now, alarmLabel: alarmLabel)
    }
}
