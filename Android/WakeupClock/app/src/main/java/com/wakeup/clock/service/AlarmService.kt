package com.wakeup.clock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wakeup.clock.R
import com.wakeup.clock.manager.AlarmScheduler
import com.wakeup.clock.manager.SoundManager
import com.wakeup.clock.manager.VolumeLevel
import com.wakeup.clock.ui.screens.AlarmLockdownActivity
import java.util.Timer
import java.util.TimerTask

/**
 * 闹钟前台服务
 * 负责播放闹钟声音、显示通知和启动锁屏界面
 */
class AlarmService : Service() {
    
    companion object {
        private const val TAG = "AlarmService"
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_ALARM = "com.wakeup.clock.START_ALARM"
        const val ACTION_STOP_ALARM = "com.wakeup.clock.STOP_ALARM"
        
        // 音量升级间隔（毫秒）
        private const val VOLUME_UPGRADE_INTERVAL = 15000L
    }
    
    private var soundManager: SoundManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var volumeTimer: Timer? = null
    private var currentVolumeLevel = VolumeLevel.NORMAL
    
    private var currentAlarmId: String? = null
    private var currentAlarmLabel: String? = null
    private var currentAlarmDifficulty: Int = 2
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        soundManager = SoundManager(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ALARM -> {
                val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: ""
                val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "other"
                val alarmTime = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TIME) ?: ""
                val alarmDifficulty = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, 2) // 默认 MEDIUM
                val isAntiSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_ANTI_SNOOZE, false)
                
                startAlarm(alarmId, alarmLabel, alarmTime, alarmDifficulty, isAntiSnooze)
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        soundManager?.release()
        soundManager = null
    }
    
    /**
     * 启动闹钟
     */
    private fun startAlarm(alarmId: String, alarmLabel: String, alarmTime: String, alarmDifficulty: Int, isAntiSnooze: Boolean) {
        Log.d(TAG, "Starting alarm: id=$alarmId, label=$alarmLabel, difficulty=$alarmDifficulty")
        
        currentAlarmId = alarmId
        currentAlarmLabel = alarmLabel
        currentAlarmDifficulty = alarmDifficulty
        
        // 获取 WakeLock 保持 CPU 唤醒并点亮屏幕
        acquireWakeLock()
        
        // 创建通知（包含 Full-Screen Intent 作为后备方案）
        val notification = createNotification(alarmId, alarmLabel, alarmDifficulty, isAntiSnooze)
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
        
        // 播放闹钟声音
        currentVolumeLevel = VolumeLevel.NORMAL
        soundManager?.playAlarmSound(currentVolumeLevel)
        
        // 启动音量递增定时器
        startVolumeUpgradeTimer()
        
        // 注意：Activity 已经在 AlarmReceiver 中启动
        // Full-Screen Intent 在通知中作为后备方案
    }
    
    /**
     * 停止闹钟
     */
    private fun stopAlarm() {
        Log.d(TAG, "Stopping alarm")
        
        // 停止音量定时器
        volumeTimer?.cancel()
        volumeTimer = null
        
        // 停止声音
        soundManager?.stopAlarmSound()
        
        // 释放 WakeLock
        releaseWakeLock()
        
        // 停止前台服务
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        currentAlarmId = null
        currentAlarmLabel = null
    }
    
    /**
     * 启动音量递增定时器
     */
    private fun startVolumeUpgradeTimer() {
        volumeTimer?.cancel()
        volumeTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    when (currentVolumeLevel) {
                        VolumeLevel.NORMAL -> {
                            currentVolumeLevel = VolumeLevel.LOUD
                            soundManager?.updateVolumeLevel(currentVolumeLevel)
                            Log.d(TAG, "Volume upgraded to LOUD")
                        }
                        VolumeLevel.LOUD -> {
                            currentVolumeLevel = VolumeLevel.SUPER_LOUD
                            soundManager?.updateVolumeLevel(currentVolumeLevel)
                            Log.d(TAG, "Volume upgraded to SUPER_LOUD")
                            // 已经是最大音量，取消定时器
                            volumeTimer?.cancel()
                        }
                        VolumeLevel.SUPER_LOUD -> {
                            // 已经是最大音量
                        }
                    }
                }
            }, VOLUME_UPGRADE_INTERVAL, VOLUME_UPGRADE_INTERVAL)
        }
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.alarm_channel_desc)
            setBypassDnd(true) // 绕过勿扰模式
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null) // 声音由 SoundManager 处理
            enableVibration(false) // 振动由 SoundManager 处理
            enableLights(true)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * 创建通知（使用 Full-Screen Intent 作为后备方案）
     */
    private fun createNotification(alarmId: String, label: String, difficulty: Int, isAntiSnooze: Boolean): Notification {
        // 创建启动 AlarmLockdownActivity 的 Intent
        val intent = Intent(this, AlarmLockdownActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, difficulty)
            putExtra(AlarmScheduler.EXTRA_IS_ANTI_SNOOZE, isAntiSnooze)
        }
        
        // 点击通知的 PendingIntent
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Full-Screen Intent - 作为后备方案
        val fullScreenIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = getAlarmMessage(label)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(getString(R.string.alarm_notification_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
    
    /**
     * 获取闹钟消息
     */
    private fun getAlarmMessage(label: String): String {
        return when (label) {
            "work" -> getString(R.string.alarm_msg_work)
            "date" -> getString(R.string.alarm_msg_date)
            "flight" -> getString(R.string.alarm_msg_flight)
            "train" -> getString(R.string.alarm_msg_train)
            "meeting" -> getString(R.string.alarm_msg_meeting)
            "doctor" -> getString(R.string.alarm_msg_doctor)
            "interview" -> getString(R.string.alarm_msg_interview)
            "exam" -> getString(R.string.alarm_msg_exam)
            else -> getString(R.string.alarm_msg_other)
        }
    }
    
    /**
     * 获取 WakeLock 并点亮屏幕
     */
    @Suppress("DEPRECATION")
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        
        // 使用 FULL_WAKE_LOCK 点亮屏幕（虽然已弃用，但对闹钟应用仍然有效）
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or 
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "WakeupClock:AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 最多持有10分钟
        }
        Log.d(TAG, "WakeLock acquired")
    }
    
    /**
     * 释放 WakeLock
     */
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "WakeLock released")
            }
        }
        wakeLock = null
    }
}
