package com.wakeup.clock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import com.wakeup.clock.data.database.AppDatabase
import com.wakeup.clock.data.repository.AlarmRepository
import com.wakeup.clock.manager.AlarmScheduler
import com.wakeup.clock.manager.VolumeCheckManager
import com.wakeup.clock.service.AlarmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 应用入口类
 */
class WakeupClockApp : Application() {
    
    companion object {
        private const val TAG = "WakeupClockApp"
    }
    
    // 应用级别的协程作用域
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化数据库
        AppDatabase.getDatabase(this)
        
        // 创建通知渠道
        createNotificationChannel()
        
        // 应用启动时重新调度所有闹钟（防止被强制停止后闹钟丢失）
        rescheduleAllAlarms()
    }
    
    /**
     * 重新调度所有启用的闹钟
     * 这对于应用被强制停止后恢复非常重要
     */
    private fun rescheduleAllAlarms() {
        applicationScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@WakeupClockApp)
                val repository = AlarmRepository(database.alarmDao())
                val scheduler = AlarmScheduler(this@WakeupClockApp)
                
                // 重新调度所有启用的闹钟
                val enabledAlarms = repository.getEnabledAlarmsOnce()
                enabledAlarms.forEach { alarm ->
                    scheduler.scheduleAlarm(alarm)
                }
                Log.d(TAG, "应用启动：重新调度了 ${enabledAlarms.size} 个闹钟")
                
                // 重新调度音量提醒
                val settings = database.appSettingsDao().getSettingsOnce()
                if (settings != null && settings.enableVolumeReminder) {
                    val volumeManager = VolumeCheckManager.getInstance(this@WakeupClockApp)
                    volumeManager.scheduleDailyCheck(settings)
                    Log.d(TAG, "应用启动：重新调度音量提醒")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "重新调度闹钟失败: ${e.message}")
            }
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AlarmService.CHANNEL_ID,
            getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.alarm_channel_desc)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(null, null)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
