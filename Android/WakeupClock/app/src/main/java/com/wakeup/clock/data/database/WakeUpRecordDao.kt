package com.wakeup.clock.data.database

import androidx.room.*
import com.wakeup.clock.data.model.WakeUpRecord
import kotlinx.coroutines.flow.Flow

/**
 * 起床记录数据访问对象
 */
@Dao
interface WakeUpRecordDao {
    
    @Query("SELECT * FROM wakeup_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<WakeUpRecord>>
    
    @Query("SELECT * FROM wakeup_records WHERE date LIKE :yearMonth || '%' ORDER BY date ASC")
    fun getRecordsByMonth(yearMonth: String): Flow<List<WakeUpRecord>>
    
    @Query("SELECT * FROM wakeup_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): WakeUpRecord?
    
    @Query("SELECT COUNT(*) FROM wakeup_records")
    fun getTotalCount(): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WakeUpRecord)
    
    @Delete
    suspend fun deleteRecord(record: WakeUpRecord)
    
    @Query("DELETE FROM wakeup_records")
    suspend fun deleteAllRecords()
    
    /**
     * 获取所有起床记录（按日期降序）
     * 用于计算连续打卡天数
     */
    @Query("SELECT * FROM wakeup_records ORDER BY date DESC")
    suspend fun getAllRecordsForStreak(): List<WakeUpRecord>
}
