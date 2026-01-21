package com.wakeup.clock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.wakeup.clock.manager.AlarmScheduler
import com.wakeup.clock.ui.screens.AlarmLockdownActivity
import com.wakeup.clock.ui.screens.AntiSnoozeActivity

/**
 * 闹钟广播接收器
 * 接收闹钟触发事件并启动前台服务和闹钟界面
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received: ${intent.action}")
        
        if (intent.action == AlarmScheduler.ACTION_ALARM_TRIGGER) {
            val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
            val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "other"
            val alarmTime = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TIME) ?: ""
            val alarmDifficulty = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, 2) // 默认 MEDIUM
            val isAntiSnooze = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_ANTI_SNOOZE, false)
            val reminderIndex = intent.getIntExtra(AlarmScheduler.EXTRA_REMINDER_INDEX, 1)
            val totalReminders = intent.getIntExtra(AlarmScheduler.EXTRA_TOTAL_REMINDERS, 2)
            
            Log.d(TAG, "Triggering alarm: id=$alarmId, label=$alarmLabel, difficulty=$alarmDifficulty, isAntiSnooze=$isAntiSnooze")
            
            // 获取 WakeLock 确保设备保持唤醒
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or 
                PowerManager.ACQUIRE_CAUSES_WAKEUP or 
                PowerManager.ON_AFTER_RELEASE,
                "WakeupClock:AlarmReceiverWakeLock"
            )
            wakeLock.acquire(10000) // 持有 10 秒，足够启动 Activity 和 Service
            
            // 1. 启动前台服务（处理声音和通知）
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = AlarmService.ACTION_START_ALARM
                putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
                putExtra(AlarmScheduler.EXTRA_ALARM_TIME, alarmTime)
                putExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, alarmDifficulty)
                putExtra(AlarmScheduler.EXTRA_IS_ANTI_SNOOZE, isAntiSnooze)
            }
            context.startForegroundService(serviceIntent)
            
            // 2. 根据是否为防赖床提醒，启动不同的 Activity
            try {
                val activityIntent = if (isAntiSnooze) {
                    // 防赖床提醒 -> 启动确认界面（带超时触发完整闹钟）
                    Intent(context, AntiSnoozeActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                        putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
                        putExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, alarmDifficulty)
                        putExtra(AntiSnoozeActivity.EXTRA_REMINDER_INDEX, reminderIndex)
                        putExtra(AntiSnoozeActivity.EXTRA_TOTAL_REMINDERS, totalReminders)
                    }
                } else {
                    // 普通闹钟 -> 启动任务解锁界面
                    Intent(context, AlarmLockdownActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                        putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
                        putExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, alarmDifficulty)
                        putExtra(AlarmScheduler.EXTRA_IS_ANTI_SNOOZE, false)
                    }
                }
                context.startActivity(activityIntent)
                Log.d(TAG, "Successfully started ${if (isAntiSnooze) "AntiSnoozeActivity" else "AlarmLockdownActivity"} from receiver")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start activity from receiver: ${e.message}")
                // 如果失败，Service 中的 Full-Screen Intent 会作为后备
            }
            
            // 释放 WakeLock（如果还持有的话）
            try {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing wake lock: ${e.message}")
            }
        }
    }
}
