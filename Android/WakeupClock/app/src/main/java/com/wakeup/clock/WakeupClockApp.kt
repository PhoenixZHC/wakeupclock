package com.wakeup.clock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.wakeup.clock.data.database.AppDatabase
import com.wakeup.clock.service.AlarmService

/**
 * 应用入口类
 */
class WakeupClockApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化数据库
        AppDatabase.getDatabase(this)
        
        // 创建通知渠道
        createNotificationChannel()
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
