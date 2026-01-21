package com.wakeup.clock.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wakeup.clock.data.model.Difficulty
import com.wakeup.clock.manager.AlarmScheduler
import com.wakeup.clock.service.AlarmService
import com.wakeup.clock.ui.theme.WakeupClockTheme
import com.wakeup.clock.ui.viewmodel.AlarmViewModel

/**
 * 闹钟锁屏Activity
 * 在锁屏状态下显示闹钟界面
 */
class AlarmLockdownActivity : ComponentActivity() {
    
    private lateinit var alarmScheduler: AlarmScheduler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化 AlarmScheduler
        alarmScheduler = AlarmScheduler(this)
        
        // 设置锁屏显示
        setupLockScreenFlags()
        
        enableEdgeToEdge()
        
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: ""
        val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "other"
        val alarmDifficultyValue = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, 2) // 默认 MEDIUM
        val alarmDifficulty = Difficulty.entries.find { it.value == alarmDifficultyValue } ?: Difficulty.MEDIUM
        // 是否是从防赖床超时触发的（这种情况下完成任务后不再调度新的防赖床提醒）
        val isFromAntiSnoozeTimeout = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_ANTI_SNOOZE, false)
        
        setContent {
            val viewModel: AlarmViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            
            WakeupClockTheme(themeMode = settings.themeMode) {
                AlarmLockdownScreen(
                    alarmLabel = alarmLabel,
                    difficulty = alarmDifficulty,
                    isAntiSnooze = isFromAntiSnoozeTimeout,
                    onDismiss = {
                        // 记录起床（保存闹钟类型）- 只有第一次闹钟才记录
                        if (!isFromAntiSnoozeTimeout) {
                            viewModel.recordWakeUp(
                                alarmId = alarmId,
                                alarmLabel = alarmLabel
                            )
                        }
                        
                        // 停止闹钟服务
                        stopAlarmService()
                        
                        // 只有普通闹钟才调度防赖床提醒
                        // 如果是从防赖床超时触发的，完成任务后不再调度新的防赖床
                        if (!isFromAntiSnoozeTimeout && settings.enableAntiSnooze) {
                            scheduleAntiSnoozeReminders(
                                alarmId = alarmId,
                                alarmLabel = alarmLabel,
                                alarmDifficulty = alarmDifficultyValue,
                                intervalMinutes = settings.antiSnoozeInterval,
                                count = settings.antiSnoozeCount
                            )
                        }
                        
                        // 关闭Activity
                        finish()
                    }
                )
            }
        }
    }
    
    /**
     * 调度防赖床提醒
     */
    private fun scheduleAntiSnoozeReminders(
        alarmId: String,
        alarmLabel: String,
        alarmDifficulty: Int,
        intervalMinutes: Int,
        count: Int
    ) {
        for (i in 1..count) {
            val delayMinutes = intervalMinutes * i
            alarmScheduler.scheduleAntiSnoozeAlarm(
                alarmId = alarmId,
                label = alarmLabel,
                difficulty = alarmDifficulty,
                delayMinutes = delayMinutes,
                index = i,
                totalCount = count
            )
        }
    }
    
    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    
    private fun stopAlarmService() {
        val intent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
        }
        startService(intent)
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 禁止返回键
    }
}
