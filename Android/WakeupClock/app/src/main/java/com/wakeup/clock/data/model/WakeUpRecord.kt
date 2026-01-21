package com.wakeup.clock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 起床记录模型
 */
@Entity(tableName = "wakeup_records")
data class WakeUpRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** 日期 (格式: "yyyy-MM-dd") */
    val date: String,
    
    /** 起床时间 (格式: "HH:mm") */
    val time: String,
    
    /** 闹钟类型标签 (work, date, flight等) */
    val alarmLabel: String? = null,
    
    /** 关联的闹钟ID */
    val alarmId: String? = null,
    
    /** 记录时间戳 */
    val timestamp: Long = System.currentTimeMillis()
)
