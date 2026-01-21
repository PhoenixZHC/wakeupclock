package com.wakeup.clock.data.database

import androidx.room.*
import com.wakeup.clock.data.model.AlarmModel
import kotlinx.coroutines.flow.Flow

/**
 * 闹钟数据访问对象
 */
@Dao
interface AlarmDao {
    
    @Query("SELECT * FROM alarms ORDER BY time ASC")
    fun getAllAlarms(): Flow<List<AlarmModel>>
    
    @Query("SELECT * FROM alarms WHERE enabled = 1 ORDER BY time ASC")
    fun getEnabledAlarms(): Flow<List<AlarmModel>>
    
    @Query("SELECT * FROM alarms WHERE enabled = 1 ORDER BY time ASC")
    suspend fun getEnabledAlarmsOnce(): List<AlarmModel>
    
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: String): AlarmModel?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmModel)
    
    @Update
    suspend fun updateAlarm(alarm: AlarmModel)
    
    @Delete
    suspend fun deleteAlarm(alarm: AlarmModel)
    
    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: String)
    
    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: String, enabled: Boolean)
    
    @Query("DELETE FROM alarms")
    suspend fun deleteAllAlarms()
}
