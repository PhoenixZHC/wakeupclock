package com.wakeup.clock.data.repository

import com.wakeup.clock.data.database.AlarmDao
import com.wakeup.clock.data.model.AlarmModel
import kotlinx.coroutines.flow.Flow

/**
 * 闹钟数据仓库
 */
class AlarmRepository(private val alarmDao: AlarmDao) {
    
    val allAlarms: Flow<List<AlarmModel>> = alarmDao.getAllAlarms()
    
    val enabledAlarms: Flow<List<AlarmModel>> = alarmDao.getEnabledAlarms()
    
    suspend fun getEnabledAlarmsOnce(): List<AlarmModel> = alarmDao.getEnabledAlarmsOnce()
    
    suspend fun getAlarmById(id: String): AlarmModel? = alarmDao.getAlarmById(id)
    
    suspend fun insertAlarm(alarm: AlarmModel) = alarmDao.insertAlarm(alarm)
    
    suspend fun updateAlarm(alarm: AlarmModel) = alarmDao.updateAlarm(alarm)
    
    suspend fun deleteAlarm(alarm: AlarmModel) = alarmDao.deleteAlarm(alarm)
    
    suspend fun deleteAlarmById(id: String) = alarmDao.deleteAlarmById(id)
    
    suspend fun setAlarmEnabled(id: String, enabled: Boolean) = alarmDao.setAlarmEnabled(id, enabled)
    
    suspend fun deleteAllAlarms() = alarmDao.deleteAllAlarms()
}
