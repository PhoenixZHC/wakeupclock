package com.wakeup.clock.data.repository

import com.wakeup.clock.data.database.AppSettingsDao
import com.wakeup.clock.data.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * 应用设置数据仓库
 */
class SettingsRepository(private val settingsDao: AppSettingsDao) {
    
    val settings: Flow<AppSettings?> = settingsDao.getSettings()
    
    suspend fun getSettingsOnce(): AppSettings = 
        settingsDao.getSettingsOnce() ?: AppSettings()
    
    suspend fun updateSettings(settings: AppSettings) = settingsDao.insertSettings(settings)
    
    suspend fun resetSettings() {
        settingsDao.deleteSettings()
        settingsDao.insertSettings(AppSettings())
    }
}
