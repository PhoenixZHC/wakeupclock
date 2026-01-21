package com.wakeup.clock.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wakeup.clock.data.model.AlarmModel
import com.wakeup.clock.data.model.RepeatMode
import com.wakeup.clock.service.AlarmReceiver
import com.wakeup.clock.util.HolidayChecker
import java.util.Calendar

/**
 * 闹钟调度管理器
 * 负责使用 AlarmManager 调度和取消闹钟
 */
class AlarmScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        private const val TAG = "AlarmScheduler"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_ALARM_TIME = "alarm_time"
        const val EXTRA_ALARM_DIFFICULTY = "alarm_difficulty"
        const val EXTRA_IS_ANTI_SNOOZE = "is_anti_snooze"
        const val EXTRA_REMINDER_INDEX = "reminder_index"
        const val EXTRA_TOTAL_REMINDERS = "total_reminders"
        const val ACTION_ALARM_TRIGGER = "com.wakeup.clock.ALARM_TRIGGER"
    }
    
    /**
     * 调度闹钟
     */
    fun scheduleAlarm(alarm: AlarmModel) {
        if (!alarm.enabled) {
            Log.d(TAG, "Alarm ${alarm.id} is disabled, skipping schedule")
            return
        }
        
        val triggerTime = calculateNextTriggerTime(alarm)
        if (triggerTime == null) {
            Log.d(TAG, "No valid trigger time for alarm ${alarm.id}")
            return
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_ALARM_TIME, alarm.time)
            putExtra(EXTRA_ALARM_DIFFICULTY, alarm.difficulty.value)
            putExtra(EXTRA_IS_ANTI_SNOOZE, false)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 使用精确闹钟
        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
            Log.d(TAG, "Scheduled alarm ${alarm.id} for ${formatTime(triggerTime)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule exact alarm: ${e.message}")
            // 降级使用非精确闹钟
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * 调度防赖床提醒
     */
    fun scheduleAntiSnoozeAlarm(
        alarmId: String, 
        label: String, 
        difficulty: Int,
        delayMinutes: Int, 
        index: Int,
        totalCount: Int
    ) {
        val triggerTime = System.currentTimeMillis() + delayMinutes * 60 * 1000L
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_ALARM_DIFFICULTY, difficulty)
            putExtra(EXTRA_IS_ANTI_SNOOZE, true)
            putExtra(EXTRA_REMINDER_INDEX, index)
            putExtra(EXTRA_TOTAL_REMINDERS, totalCount)
        }
        
        val requestCode = "${alarmId}_anti_$index".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
            Log.d(TAG, "Scheduled anti-snooze alarm #$index/$totalCount for ${formatTime(triggerTime)}, delay=${delayMinutes}min")
        } catch (e: SecurityException) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * 取消闹钟
     */
    fun cancelAlarm(alarm: AlarmModel) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm ${alarm.id}")
    }
    
    /**
     * 取消防赖床提醒
     */
    fun cancelAntiSnoozeAlarms(alarmId: String, count: Int) {
        for (i in 1..count) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_ALARM_TRIGGER
            }
            
            val requestCode = "${alarmId}_anti_$i".hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
        }
        Log.d(TAG, "Cancelled anti-snooze alarms for $alarmId")
    }
    
    /**
     * 计算下一次触发时间
     */
    fun calculateNextTriggerTime(alarm: AlarmModel): Long? {
        val (hour, minute) = alarm.timeComponents ?: return null
        
        val now = Calendar.getInstance()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // 如果今天的时间已过，从明天开始找
        if (alarmTime.timeInMillis <= now.timeInMillis) {
            alarmTime.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // 根据重复模式找到下一个有效日期
        when (alarm.repeatMode) {
            RepeatMode.ONCE -> {
                // 一次性闹钟，直接返回
                return alarmTime.timeInMillis
            }
            
            RepeatMode.WORKDAYS -> {
                // 工作日：周一到周五
                for (i in 0 until 7) {
                    val dayOfWeek = alarmTime.get(Calendar.DAY_OF_WEEK)
                    val isWorkday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
                    
                    if (isWorkday) {
                        // 检查是否跳过节假日
                        if (alarm.skipHolidays && HolidayChecker.shouldSkipAlarm(alarmTime.time)) {
                            alarmTime.add(Calendar.DAY_OF_MONTH, 1)
                            continue
                        }
                        return alarmTime.timeInMillis
                    }
                    alarmTime.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            RepeatMode.CUSTOM -> {
                // 自定义日期
                if (alarm.customDays.isEmpty()) return null
                
                for (i in 0 until 7) {
                    val dayOfWeek = alarmTime.get(Calendar.DAY_OF_WEEK)
                    // Calendar: 1=周日, 2=周一, ..., 7=周六
                    // 我们的格式: 0=周日, 1=周一, ..., 6=周六
                    val dayIndex = if (dayOfWeek == Calendar.SUNDAY) 0 else dayOfWeek - 1
                    
                    if (alarm.customDays.contains(dayIndex)) {
                        // 检查是否跳过节假日
                        if (alarm.skipHolidays && HolidayChecker.shouldSkipAlarm(alarmTime.time)) {
                            alarmTime.add(Calendar.DAY_OF_MONTH, 1)
                            continue
                        }
                        return alarmTime.timeInMillis
                    }
                    alarmTime.add(Calendar.DAY_OF_MONTH, 1)
                }
            }
        }
        
        return null
    }
    
    /**
     * 获取倒计时文本
     */
    fun getCountdownText(alarm: AlarmModel): String? {
        val triggerTime = calculateNextTriggerTime(alarm) ?: return null
        val now = System.currentTimeMillis()
        val diff = triggerTime - now
        
        if (diff <= 0) return null
        
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}小时${minutes}分钟"
            minutes > 0 -> "${minutes}分钟"
            else -> "即将响铃"
        }
    }
    
    /**
     * 检查是否有精确闹钟权限
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    private fun formatTime(timeMillis: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
    }
}
