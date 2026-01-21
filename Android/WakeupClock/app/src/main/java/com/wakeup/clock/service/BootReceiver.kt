package com.wakeup.clock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wakeup.clock.data.database.AppDatabase
import com.wakeup.clock.data.repository.AlarmRepository
import com.wakeup.clock.manager.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 开机广播接收器
 * 设备重启后重新调度所有启用的闹钟
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Device booted, rescheduling alarms")
            
            // 在后台重新调度所有闹钟
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val repository = AlarmRepository(database.alarmDao())
                    val scheduler = AlarmScheduler(context)
                    
                    val enabledAlarms = repository.getEnabledAlarmsOnce()
                    enabledAlarms.forEach { alarm ->
                        scheduler.scheduleAlarm(alarm)
                    }
                    
                    Log.d(TAG, "Rescheduled ${enabledAlarms.size} alarms")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule alarms: ${e.message}")
                }
            }
        }
    }
}
