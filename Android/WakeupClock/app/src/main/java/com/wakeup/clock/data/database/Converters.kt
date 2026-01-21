package com.wakeup.clock.data.database

import androidx.room.TypeConverter
import com.wakeup.clock.data.model.Difficulty
import com.wakeup.clock.data.model.MissionType
import com.wakeup.clock.data.model.RepeatMode
import com.wakeup.clock.data.model.ThemeMode

/**
 * Room 类型转换器
 */
class Converters {
    
    // List<Int> 转换
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }
    
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").mapNotNull { it.toIntOrNull() }
    }
    
    // MissionType 转换
    @TypeConverter
    fun fromMissionType(value: MissionType): String = value.name
    
    @TypeConverter
    fun toMissionType(value: String): MissionType = MissionType.valueOf(value)
    
    // Difficulty 转换
    @TypeConverter
    fun fromDifficulty(value: Difficulty): Int = value.value
    
    @TypeConverter
    fun toDifficulty(value: Int): Difficulty = Difficulty.entries.find { it.value == value } ?: Difficulty.MEDIUM
    
    // RepeatMode 转换
    @TypeConverter
    fun fromRepeatMode(value: RepeatMode): String = value.name
    
    @TypeConverter
    fun toRepeatMode(value: String): RepeatMode = RepeatMode.valueOf(value)
    
    // ThemeMode 转换
    @TypeConverter
    fun fromThemeMode(value: ThemeMode): String = value.name
    
    @TypeConverter
    fun toThemeMode(value: String): ThemeMode = ThemeMode.valueOf(value)
}
