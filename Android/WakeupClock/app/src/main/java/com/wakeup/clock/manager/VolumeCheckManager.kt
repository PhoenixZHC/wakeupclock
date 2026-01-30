package com.wakeup.clock.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wakeup.clock.R
import com.wakeup.clock.data.model.AppSettings
import com.wakeup.clock.service.VolumeCheckReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    
    // 是否需要显示应用内弹窗（应用在前台时检测到音量过低）
    private val _showVolumeWarningDialog = MutableStateFlow(false)
    val showVolumeWarningDialog: StateFlow<Boolean> = _showVolumeWarningDialog.asStateFlow()
    
    // 弹窗信息
    private val _volumeWarningInfo = MutableStateFlow<VolumeWarningInfo?>(null)
    val volumeWarningInfo: StateFlow<VolumeWarningInfo?> = _volumeWarningInfo.asStateFlow()
    
    /**
     * 音量警告信息
     */
    data class VolumeWarningInfo(
        val currentVolumePercent: Int,
        val thresholdPercent: Int
    )
    
    /**
     * 关闭音量警告弹窗
     */
    fun dismissVolumeWarningDialog() {
        _showVolumeWarningDialog.value = false
        _volumeWarningInfo.value = null
    }
    
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
     * 执行音量检查（后台检查，发送系统通知）
     */
    fun performVolumeCheck(settings: AppSettings) {
        if (!settings.enableVolumeReminder) return
        
        val volume = getCurrentVolume()
        val threshold = settings.volumeReminderThreshold
        
        Log.d(TAG, "后台检查音量: $volume, 阈值: $threshold")
        
        if (volume < threshold) {
            // 音量过低，发送通知提醒（带声音）
            sendVolumeReminderNotification(volume, threshold)
        }
    }
    
    /**
     * 应用内检查音量（前台检查，显示弹窗）
     * @return true 如果音量过低需要提醒，false 如果音量正常
     */
    fun checkVolumeInApp(settings: AppSettings): Boolean {
        if (!settings.enableVolumeReminder) return false
        
        val volume = getCurrentVolume()
        val threshold = settings.volumeReminderThreshold
        
        Log.d(TAG, "应用内检查音量: $volume, 阈值: $threshold")
        
        if (volume < threshold) {
            val volumePercent = (volume * 100).toInt()
            val thresholdPercent = (threshold * 100).toInt()
            
            _volumeWarningInfo.value = VolumeWarningInfo(volumePercent, thresholdPercent)
            _showVolumeWarningDialog.value = true
            
            Log.d(TAG, "音量过低，显示应用内弹窗提醒")
            return true
        }
        
        return false
    }
    
    /**
     * 发送音量提醒通知（带声音，用于后台提醒）
     */
    private fun sendVolumeReminderNotification(currentVolume: Float, threshold: Float) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // 获取默认通知铃声
            val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            // 创建通知渠道（如果还没有创建）
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // 先删除旧的渠道（如果存在），以确保更新生效
                systemNotificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
                
                // 获取通知声音的音频属性
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                
                val channel = android.app.NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.volume_reminder_channel_name),
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.volume_reminder_channel_desc)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    setBypassDnd(true) // 绕过勿扰模式
                    setSound(defaultSoundUri, audioAttributes) // 设置通知声音
                }
                systemNotificationManager.createNotificationChannel(channel)
            }
            
            // 检查通知权限
            if (!notificationManager.areNotificationsEnabled()) {
                Log.w(TAG, "通知权限未开启，无法发送音量提醒通知")
                return
            }
            
            // 计算当前音量百分比
            val volumePercent = (currentVolume * 100).toInt()
            val thresholdPercent = (threshold * 100).toInt()
            
            // 创建点击通知时打开App的Intent
            val contentIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                context.packageManager.getLaunchIntentForPackage(context.packageName),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            // 创建全屏Intent（用于在锁屏时显示）
            val fullScreenIntent = android.app.PendingIntent.getActivity(
                context,
                1,
                context.packageManager.getLaunchIntentForPackage(context.packageName),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(context.getString(R.string.volume_reminder_title))
                .setContentText(context.getString(R.string.volume_reminder_body_detail, volumePercent, thresholdPercent))
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.volume_reminder_body_detail, volumePercent, thresholdPercent)))
                .setPriority(NotificationCompat.PRIORITY_MAX) // 使用最高优先级
                .setCategory(NotificationCompat.CATEGORY_REMINDER) // 设为提醒类别
                .setSound(defaultSoundUri) // 设置通知声音
                .setVibrate(longArrayOf(0, 500, 200, 500)) // 震动模式
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setFullScreenIntent(fullScreenIntent, true) // 高优先级全屏Intent，确保弹出
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 在锁屏上显示完整内容
                .setOngoing(false)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            Log.d(TAG, "已发送音量提醒通知（带声音），当前音量: $volumePercent%, 阈值: $thresholdPercent%")
        } catch (e: SecurityException) {
            Log.e(TAG, "发送音量提醒通知失败，缺少通知权限", e)
        } catch (e: Exception) {
            Log.e(TAG, "发送音量提醒通知失败", e)
        }
    }
    
    /**
     * 立即检查音量并发送通知（如果过低）- 用于后台
     */
    fun checkVolumeNow(settings: AppSettings) {
        performVolumeCheck(settings)
    }
}
