package com.wakeup.clock.data.database

import androidx.room.*
import com.wakeup.clock.data.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * 应用设置数据访问对象
 */
@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettings?>
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): AppSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
    
    @Update
    suspend fun updateSettings(settings: AppSettings)
    
    @Query("DELETE FROM app_settings")
    suspend fun deleteSettings()
}
