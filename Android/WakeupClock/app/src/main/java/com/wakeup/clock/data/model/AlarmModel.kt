package com.wakeup.clock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 任务类型枚举
 */
enum class MissionType {
    MATH,
    SHAKE,
    MEMORY,
    ORDER,
    TYPING
}

/**
 * 难度等级枚举
 */
enum class Difficulty(val value: Int) {
    EASY(1),
    MEDIUM(2),
    HARD(3)
}

/**
 * 重复模式枚举
 */
enum class RepeatMode {
    ONCE,
    WORKDAYS,
    CUSTOM
}

/**
 * 闹钟数据模型
 */
@Entity(tableName = "alarms")
data class AlarmModel(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    /** 闹钟时间 (格式: "HH:mm") */
    val time: String,
    
    /** 是否启用 */
    val enabled: Boolean = true,
    
    /** 标签分类 (work, date, flight, train, meeting, doctor, interview, exam, other) */
    val label: String = "other",
    
    /** 任务类型 */
    val missionType: MissionType = MissionType.MATH,
    
    /** 难度等级 */
    val difficulty: Difficulty = Difficulty.MEDIUM,
    
    /** 重复模式 */
    val repeatMode: RepeatMode = RepeatMode.WORKDAYS,
    
    /** 自定义重复日期 (0=周日, 1=周一, ..., 6=周六) */
    val customDays: List<Int> = emptyList(),
    
    /** 是否跳过节假日 */
    val skipHolidays: Boolean = false,
    
    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取时间的小时和分钟
     */
    val timeComponents: Pair<Int, Int>?
        get() {
            val parts = time.split(":")
            if (parts.size != 2) return null
            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null
            return Pair(hour, minute)
        }
    
    /**
     * 获取标签对应的图标名称
     */
    val iconName: String
        get() = when (label) {
            "work" -> "briefcase"
            "date" -> "favorite"
            "flight" -> "flight"
            "train" -> "train"
            "meeting" -> "groups"
            "doctor" -> "medical_services"
            "interview" -> "person_add"
            "exam" -> "school"
            else -> "alarm"
        }
    
    /**
     * 获取标签对应的视频名称
     */
    val videoName: String?
        get() = when (label) {
            "work" -> "work"
            "date" -> "date"
            "flight" -> "flight"
            "train" -> "train"
            "meeting" -> "meeting"
            "doctor" -> "doctor"
            "interview" -> "interview"
            "exam" -> "exam"
            else -> null
        }
}
