//
//  AlarmMetadata.swift
//  WakeupClock
//
//  闹钟元数据：符合 AlarmMetadata 协议
//

import Foundation
import AlarmKit

struct WakeupAlarmMetadata: AlarmMetadata {
    let createdAt: Date
    let alarmLabel: String
    
    init(alarmLabel: String = "other") {
        self.createdAt = Date.now
        self.alarmLabel = alarmLabel
    }
}
