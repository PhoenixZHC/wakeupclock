package com.wakeup.clock.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wakeup.clock.data.model.AlarmModel
import com.wakeup.clock.data.model.AppSettings
import com.wakeup.clock.data.model.WakeUpRecord

/**
 * 应用数据库
 */
@Database(
    entities = [
        AlarmModel::class,
        WakeUpRecord::class,
        AppSettings::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun alarmDao(): AlarmDao
    abstract fun wakeUpRecordDao(): WakeUpRecordDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wakeup_clock_database"
                )
                    .fallbackToDestructiveMigration() // 开发阶段：schema变更时删除旧数据重建
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
