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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wakeup.clock.R
import com.wakeup.clock.manager.AlarmScheduler
import com.wakeup.clock.service.AlarmService
import com.wakeup.clock.ui.theme.*
import com.wakeup.clock.ui.viewmodel.AlarmViewModel
import kotlinx.coroutines.delay

/**
 * 防赖床确认Activity
 * 显示确认界面，用户需要在1分钟内点击确认
 * 如果超时未确认，则重新触发完整闹钟（需要做任务）
 */
class AntiSnoozeActivity : ComponentActivity() {
    
    private lateinit var alarmScheduler: AlarmScheduler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        alarmScheduler = AlarmScheduler(this)
        
        // 设置锁屏显示
        setupLockScreenFlags()
        
        enableEdgeToEdge()
        
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: ""
        val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "other"
        val alarmDifficulty = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, 2)
        val reminderIndex = intent.getIntExtra(EXTRA_REMINDER_INDEX, 1)
        val totalReminders = intent.getIntExtra(EXTRA_TOTAL_REMINDERS, 2)
        
        setContent {
            val viewModel: AlarmViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            
            WakeupClockTheme(themeMode = settings.themeMode) {
                AntiSnoozeScreen(
                    reminderIndex = reminderIndex,
                    totalReminders = totalReminders,
                    onConfirm = {
                        // 用户确认醒了，停止声音并取消剩余提醒
                        stopAlarmService()
                        alarmScheduler.cancelAntiSnoozeAlarms(alarmId, settings.antiSnoozeCount)
                        finish()
                    },
                    onTimeout = {
                        // 超时未确认，触发完整闹钟（需要做任务）
                        triggerFullAlarm(alarmId, alarmLabel, alarmDifficulty)
                        finish()
                    }
                )
            }
        }
    }
    
    /**
     * 触发完整闹钟（需要做任务）
     */
    private fun triggerFullAlarm(alarmId: String, alarmLabel: String, difficulty: Int) {
        // 启动 AlarmLockdownActivity（带任务的完整闹钟界面）
        val intent = Intent(this, AlarmLockdownActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_ALARM_DIFFICULTY, difficulty)
            putExtra(AlarmScheduler.EXTRA_IS_ANTI_SNOOZE, true) // 标记为防赖床触发的闹钟
        }
        startActivity(intent)
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
    
    companion object {
        const val EXTRA_REMINDER_INDEX = "reminder_index"
        const val EXTRA_TOTAL_REMINDERS = "total_reminders"
        const val TIMEOUT_SECONDS = 60 // 1分钟超时
    }
}

/**
 * 防赖床确认界面
 */
@Composable
private fun AntiSnoozeScreen(
    reminderIndex: Int,
    totalReminders: Int,
    onConfirm: () -> Unit,
    onTimeout: () -> Unit
) {
    // 倒计时（60秒）
    var remainingSeconds by remember { mutableIntStateOf(AntiSnoozeActivity.TIMEOUT_SECONDS) }
    
    // 倒计时逻辑
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        // 超时，触发完整闹钟
        onTimeout()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Purple500, Purple700)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 倒计时显示
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = stringResource(R.string.anti_snooze_countdown, remainingSeconds),
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 标题
            Text(
                text = when {
                    reminderIndex == 1 -> stringResource(R.string.anti_snooze_reminder1)
                    reminderIndex >= totalReminders -> stringResource(R.string.anti_snooze_reminder_last)
                    else -> stringResource(R.string.anti_snooze_reminder)
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            // 副标题
            Text(
                text = stringResource(R.string.anti_snooze_subtitle),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            // 超时警告
            Text(
                text = stringResource(R.string.anti_snooze_timeout_warning),
                fontSize = 14.sp,
                color = Orange,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 确认按钮
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Purple500,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.confirm_awake),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple500
                )
            }
            
            // 提示
            Text(
                text = stringResource(R.string.anti_snooze_hint, reminderIndex, totalReminders),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
