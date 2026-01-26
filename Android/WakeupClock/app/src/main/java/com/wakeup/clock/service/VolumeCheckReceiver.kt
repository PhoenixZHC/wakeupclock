package com.wakeup.clock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wakeup.clock.data.database.AppDatabase
import com.wakeup.clock.data.model.AppSettings
import com.wakeup.clock.manager.VolumeCheckManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 音量检查广播接收器
 * 接收定时检查的广播并执行音量检查
 */
class VolumeCheckReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "VolumeCheckReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Volume check receiver triggered")
        
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                // 从数据库获取设置
                val database = AppDatabase.getDatabase(context)
                val settings = database.appSettingsDao().getSettingsOnce()
                
                if (settings == null || !settings.enableVolumeReminder) {
                    Log.d(TAG, "音量提醒未启用，跳过检查")
                    return@launch
                }
                
                // 执行音量检查
                val volumeManager = VolumeCheckManager.getInstance(context)
                volumeManager.performVolumeCheck(settings)
                
                // 重新调度下一次检查（每天重复）
                volumeManager.scheduleDailyCheck(settings)
                
            } catch (e: Exception) {
                Log.e(TAG, "执行音量检查失败", e)
            }
        }
    }
}
