package com.wakeup.clock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 主题模式
 */
enum class ThemeMode {
    AUTO,
    LIGHT,
    DARK
}

/**
 * 应用设置模型
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1, // 单例，只有一条记录
    
    /** 主题模式 */
    val themeMode: ThemeMode = ThemeMode.AUTO,
    
    /** 语言 (zh, en) */
    val language: String = "zh",
    
    /** 是否启用防赖床模式 */
    val enableAntiSnooze: Boolean = false,
    
    /** 防赖床提醒间隔（分钟） */
    val antiSnoozeInterval: Int = 5,
    
    /** 防赖床提醒次数 */
    val antiSnoozeCount: Int = 2,
    
    /** 是否已接受安全提示 */
    val hasAcceptedSafetyNotice: Boolean = false
)
