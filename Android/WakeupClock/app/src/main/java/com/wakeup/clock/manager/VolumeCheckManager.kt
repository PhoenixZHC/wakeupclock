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
        // 前台创建渠道可避免 OPPO/ColorOS 在后台创建时降级；若仍被改静音可改为 v5
        private const val NOTIFICATION_CHANNEL_ID = "volume_reminder_channel_v4"
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
     * 将系统媒体音量设为指定比例（0.0–1.0），与 iOS 一致
     * 用于「调至 70%」等一键调节
     */
    fun setSystemVolume(targetRatio: Float) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (maxVolume <= 0) return
        val index = (maxVolume * targetRatio.coerceIn(0f, 1f)).toInt().coerceIn(0, maxVolume)
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
            _currentVolume.value = getCurrentVolume()
            Log.d(TAG, "已设置媒体音量至 ${(getCurrentVolume() * 100).toInt()}%")
        } catch (e: Exception) {
            Log.e(TAG, "设置媒体音量失败: ${e.message}")
        }
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
    
    /** 上次前台音量检测时间（用于 10 秒防抖） */
    @Volatile
    private var lastVolumeCheckTimeMs: Long = 0L
    
    /** 打开应用时音量检测的固定阈值（与「睡前提醒」设置无关） */
    private val inAppVolumeThreshold = 0.5f
    
    /**
     * 每次打开应用/回到前台时检测音量，低于 50% 则弹窗提醒。
     * 与「睡前音量提醒」设置无关，无需用户单独开启；带 10 秒防抖。
     */
    fun checkVolumeOnAppOpen(): Boolean {
        val now = System.currentTimeMillis()
        if (lastVolumeCheckTimeMs != 0L && now - lastVolumeCheckTimeMs < 10_000) return false
        lastVolumeCheckTimeMs = now
        val volume = getCurrentVolume()
        Log.d(TAG, "打开应用检测音量: $volume, 阈值: $inAppVolumeThreshold")
        if (volume < inAppVolumeThreshold) {
            val volumePercent = (volume * 100).toInt()
            val thresholdPercent = (inAppVolumeThreshold * 100).toInt()
            _volumeWarningInfo.value = VolumeWarningInfo(volumePercent, thresholdPercent)
            _showVolumeWarningDialog.value = true
            Log.d(TAG, "音量过低，显示应用内弹窗提醒")
            return true
        }
        return false
    }
    
    /**
     * 应用进入前台时检测音量（依赖设置：仅当开启「睡前音量提醒」时执行，带 10 秒防抖）
     * 用于与设置联动的场景；日常「打开应用就检测」请用 checkVolumeOnAppOpen()。
     */
    fun checkVolumeOnForeground(settings: AppSettings): Boolean {
        if (!settings.enableVolumeReminder) return false
        val now = System.currentTimeMillis()
        if (lastVolumeCheckTimeMs != 0L && now - lastVolumeCheckTimeMs < 10_000) return false
        lastVolumeCheckTimeMs = now
        return checkVolumeInApp(settings)
    }
    
    /**
     * 应用内检查音量（使用设置中的阈值，供睡前提醒等逻辑使用）
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
     * 在前台创建「睡前提醒」通知渠道（仅当渠道不存在时）。
     * 必须在用户开启睡前提醒或应用前台时调用，避免在后台创建被 OPPO/ColorOS 降级为静音。
     */
    fun ensureVolumeReminderChannel() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return
        val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (systemNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null) return
        val notificationSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = android.media.AudioAttributes.Builder()
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val channel = android.app.NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.volume_reminder_channel_name),
            android.app.NotificationManager.IMPORTANCE_MAX
        ).apply {
            description = context.getString(R.string.volume_reminder_channel_desc)
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            enableLights(true)
            lightColor = android.graphics.Color.GRAY
            setSound(notificationSoundUri, audioAttributes)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 200, 100, 200)
        }
        systemNotificationManager.createNotificationChannel(channel)
        Log.d(TAG, "已在前台创建睡前提醒通知渠道")
    }
    
    /**
     * 发送睡前音量提醒通知（使用短信/通知类提示音；通知栏+锁屏+提示音）
     */
    private fun sendVolumeReminderNotification(currentVolume: Float, threshold: Float) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // 若渠道尚未存在（例如未在前台创建过），后台仅作一次创建，但部分厂商会降级
                val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                if (systemNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                    ensureVolumeReminderChannel()
                }
            }
            
            if (!notificationManager.areNotificationsEnabled()) {
                Log.w(TAG, "通知权限未开启，无法发送音量提醒通知")
                return
            }
            
            val notificationSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val volumePercent = (currentVolume * 100).toInt()
            val thresholdPercent = (threshold * 100).toInt()
            
            val contentIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                context.packageManager.getLaunchIntentForPackage(context.packageName),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(context.getString(R.string.volume_reminder_title))
                .setContentText(context.getString(R.string.volume_reminder_body_detail, volumePercent, thresholdPercent))
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.volume_reminder_body_detail, volumePercent, thresholdPercent)))
                .setPriority(NotificationCompat.PRIORITY_MAX)  // 与 IMPORTANCE_MAX 一致，争取锁屏与提示音
                .setCategory(NotificationCompat.CATEGORY_REMINDER) // 提醒类，更易被系统当作需响铃+锁屏
                .setSound(notificationSoundUri)
                .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE) // 部分厂商会参考
                .setOnlyAlertOnce(false)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            Log.d(TAG, "已发送睡前音量提醒通知，当前音量: $volumePercent%, 阈值: $thresholdPercent%")
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
