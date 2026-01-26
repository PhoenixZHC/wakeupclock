package com.wakeup.clock.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wakeup.clock.R
import com.wakeup.clock.data.model.AppSettings
import com.wakeup.clock.service.VolumeCheckReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 音量检测管理器（单例）
 * 负责检测媒体音量并在音量过低时提醒用户
 */
class VolumeCheckManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "VolumeCheckManager"
        private const val NOTIFICATION_CHANNEL_ID = "volume_reminder_channel"
        private const val NOTIFICATION_ID = 1001
        private const val ALARM_REQUEST_CODE = 2001
        
        @Volatile
        private var INSTANCE: VolumeCheckManager? = null
        
        fun getInstance(context: Context): VolumeCheckManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VolumeCheckManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val audioManager: AudioManager = 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    private val _currentVolume = MutableStateFlow(0.0f)
    val currentVolume: StateFlow<Float> = _currentVolume.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main)
    
    /**
     * 获取当前媒体音量（0.0-1.0）
     */
    fun getCurrentVolume(): Float {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return if (maxVolume > 0) currentVolume.toFloat() / maxVolume.toFloat() else 0f
    }
    
    /**
     * 开始监听音量变化
     */
    fun startMonitoring() {
        if (_isMonitoring.value) return
        
        _isMonitoring.value = true
        _currentVolume.value = getCurrentVolume()
        
        Log.d(TAG, "开始监听音量变化，当前音量: ${_currentVolume.value}")
    }
    
    /**
     * 停止监听音量变化
     */
    fun stopMonitoring() {
        if (!_isMonitoring.value) return
        
        _isMonitoring.value = false
        cancelDailyCheck()
        
        Log.d(TAG, "停止监听音量变化")
    }
    
    /**
     * 设置每日定时检查（每天晚上指定时间检查一次）
     */
    fun scheduleDailyCheck(settings: AppSettings) {
        // 先取消之前的闹钟
        cancelDailyCheck()
        
        if (!settings.enableVolumeReminder) {
            Log.d(TAG, "音量提醒功能未启用")
            return
        }
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settings.volumeReminderHour)
            set(Calendar.MINUTE, settings.volumeReminderMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // 如果今天的时间已过，设置为明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, VolumeCheckReceiver::class.java).apply {
            putExtra("settings_enable", settings.enableVolumeReminder)
            putExtra("settings_threshold", settings.volumeReminderThreshold)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 使用精确闹钟（需要权限）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        
        Log.d(TAG, "已调度每日音量检查，时间: ${calendar.time}")
    }
    
    /**
     * 取消每日检查
     */
    private fun cancelDailyCheck() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, VolumeCheckReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * 执行音量检查
     */
    fun performVolumeCheck(settings: AppSettings) {
        if (!settings.enableVolumeReminder) return
        
        val volume = getCurrentVolume()
        val threshold = settings.volumeReminderThreshold
        
        Log.d(TAG, "检查音量: $volume, 阈值: $threshold")
        
        if (volume < threshold) {
            // 音量过低，发送通知提醒
            sendVolumeReminderNotification(volume, threshold)
        }
    }
    
    /**
     * 发送音量提醒通知
     */
    private fun sendVolumeReminderNotification(currentVolume: Float, threshold: Float) {
        scope.launch {
            try {
                val notificationManager = NotificationManagerCompat.from(context)
                
                // 创建通知渠道（如果还没有创建）
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val channel = android.app.NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.volume_reminder_channel_name),
                        android.app.NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = context.getString(R.string.volume_reminder_channel_desc)
                        enableVibration(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
                
                val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setContentTitle(context.getString(R.string.volume_reminder_title))
                    .setContentText(context.getString(R.string.volume_reminder_body))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                    .setAutoCancel(true)
                    .build()
                
                notificationManager.notify(NOTIFICATION_ID, notification)
                
                Log.d(TAG, "已发送音量提醒通知")
            } catch (e: Exception) {
                Log.e(TAG, "发送音量提醒通知失败", e)
            }
        }
    }
    
    /**
     * 立即检查音量并发送通知（如果过低）
     */
    fun checkVolumeNow(settings: AppSettings) {
        performVolumeCheck(settings)
    }
}
