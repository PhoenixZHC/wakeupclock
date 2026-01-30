package com.wakeup.clock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wakeup.clock.data.database.AppDatabase
import com.wakeup.clock.data.repository.AlarmRepository
import com.wakeup.clock.manager.AlarmScheduler
import com.wakeup.clock.manager.VolumeCheckManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 开机广播接收器
 * 设备重启后重新调度所有启用的闹钟和音量提醒
 * 
 * 监听以下事件：
 * - BOOT_COMPLETED: 设备开机完成
 * - QUICKBOOT_POWERON: 快速启动（某些厂商）
 * - LOCKED_BOOT_COMPLETED: 直接启动模式下开机完成
 * - MY_PACKAGE_REPLACED: 应用更新后
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        
        // 处理各种需要重新调度闹钟的场景
        val shouldReschedule = when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> true
            else -> false
        }
        
        if (shouldReschedule) {
            Log.d(TAG, "Received action: $action, rescheduling alarms")
            
            // 使用 goAsync() 允许更长的执行时间
            val pendingResult = goAsync()
            
            // 在后台重新调度所有闹钟和音量提醒
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val repository = AlarmRepository(database.alarmDao())
                    val scheduler = AlarmScheduler(context)
                    
                    // 重新调度所有启用的闹钟
                    val enabledAlarms = repository.getEnabledAlarmsOnce()
                    enabledAlarms.forEach { alarm ->
                        scheduler.scheduleAlarm(alarm)
                    }
                    Log.d(TAG, "Rescheduled ${enabledAlarms.size} alarms")
                    
                    // 重新调度音量提醒
                    val settings = database.appSettingsDao().getSettingsOnce()
                    if (settings != null && settings.enableVolumeReminder) {
                        val volumeManager = VolumeCheckManager.getInstance(context)
                        volumeManager.scheduleDailyCheck(settings)
                        Log.d(TAG, "Rescheduled volume reminder")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule alarms: ${e.message}")
                } finally {
                    // 完成异步操作
                    pendingResult.finish()
                }
            }
        }
    }
}
