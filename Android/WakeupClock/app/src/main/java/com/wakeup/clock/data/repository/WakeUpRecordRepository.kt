package com.wakeup.clock.data.repository

import com.wakeup.clock.data.database.WakeUpRecordDao
import com.wakeup.clock.data.model.WakeUpRecord
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * 起床记录数据仓库
 */
class WakeUpRecordRepository(private val recordDao: WakeUpRecordDao) {
    
    val allRecords: Flow<List<WakeUpRecord>> = recordDao.getAllRecords()
    
    fun getRecordsByMonth(yearMonth: String): Flow<List<WakeUpRecord>> = 
        recordDao.getRecordsByMonth(yearMonth)
    
    val totalCount: Flow<Int> = recordDao.getTotalCount()
    
    suspend fun getRecordByDate(date: String): WakeUpRecord? = recordDao.getRecordByDate(date)
    
    suspend fun insertRecord(record: WakeUpRecord) = recordDao.insertRecord(record)
    
    suspend fun deleteRecord(record: WakeUpRecord) = recordDao.deleteRecord(record)
    
    suspend fun deleteAllRecords() = recordDao.deleteAllRecords()
    
    /**
     * 计算连续打卡天数
     * 与iOS逻辑一致：只要有打卡记录就算一天
     */
    suspend fun getStreak(): Int {
        val records = recordDao.getAllRecordsForStreak()
        if (records.isEmpty()) return 0
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val todayCal = Calendar.getInstance()
        
        var streak = 0
        var expectedDate = todayCal.clone() as Calendar
        
        // 检查今天是否有记录，如果没有则从昨天开始计算
        val todayRecord = records.find { it.date == today }
        if (todayRecord == null) {
            expectedDate.add(Calendar.DAY_OF_MONTH, -1)
        }
        
        // 按日期分组（同一天可能有多条记录，只算一天）
        val uniqueDates = records.map { it.date }.distinct()
        
        for (dateStr in uniqueDates) {
            val recordDate = try {
                dateFormat.parse(dateStr)
            } catch (e: Exception) {
                continue
            } ?: continue
            
            val recordCal = Calendar.getInstance().apply { time = recordDate }
            
            // 检查是否是预期的日期
            if (isSameDay(recordCal, expectedDate)) {
                streak++
                expectedDate.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                // 连续中断
                break
            }
        }
        
        return streak
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
