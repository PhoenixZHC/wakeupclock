package com.wakeup.clock.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.clock.R
import com.wakeup.clock.data.model.AlarmModel
import com.wakeup.clock.data.model.RepeatMode
import com.wakeup.clock.data.model.ThemeMode
import com.wakeup.clock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 主界面
 */
@Composable
fun DashboardScreen(
    alarms: List<AlarmModel>,
    streak: Int,
    countdownText: String?,
    themeMode: ThemeMode,
    onAddAlarm: () -> Unit,
    onToggleAlarm: (AlarmModel) -> Unit,
    onDeleteAlarm: (AlarmModel) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCalendar: () -> Unit,
    getCountdownText: (AlarmModel) -> String?
) {
    val isDark = when (themeMode) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    // 当前时间
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            kotlinx.coroutines.delay(1000)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBackground else LightBackground)
            .systemBarsPadding() // 添加系统栏内边距
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题栏
            item {
                HeaderSection(
                    isDark = isDark,
                    onOpenSettings = onOpenSettings
                )
            }
            
            // 连续天数或鼓励语
            item {
                StreakSection(
                    streak = streak,
                    isDark = isDark,
                    onClick = onOpenCalendar
                )
            }
            
            // 时钟
            item {
                ClockSection(
                    currentTime = currentTime,
                    countdownText = countdownText,
                    isDark = isDark
                )
            }
            
            // 闹钟列表标题
            item {
                Text(
                    text = stringResource(R.string.my_alarms),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            }
            
            // 闹钟列表
            if (alarms.isEmpty()) {
                item {
                    EmptyStateSection(isDark = isDark)
                }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmRowItem(
                        alarm = alarm,
                        isDark = isDark,
                        onToggle = { onToggleAlarm(alarm) },
                        onDelete = { onDeleteAlarm(alarm) }
                    )
                }
            }
            
            // 底部间距
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // 添加按钮
        FloatingActionButton(
            onClick = onAddAlarm,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp),
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alarm",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(
    isDark: Boolean,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color.Black
            )
            Text(
                text = stringResource(R.string.slogan),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.Gray else Color.Gray
            )
        }
        
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isDark) Color.Gray.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.6f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = if (isDark) Color.Gray else Color.Black
            )
        }
    }
}

@Composable
private fun StreakSection(
    streak: Int,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.clickable { onClick() },
            shape = RoundedCornerShape(50),
            color = if (streak > 0) {
                if (isDark) Orange.copy(alpha = 0.2f) else Orange.copy(alpha = 0.1f)
            } else {
                if (isDark) Purple500.copy(alpha = 0.2f) else Purple500.copy(alpha = 0.1f)
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (streak > 0) Icons.Default.LocalFireDepartment else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (streak > 0) Orange else Purple500,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (streak > 0) {
                        stringResource(R.string.streak_sentence, streak)
                    } else {
                        stringResource(R.string.encouragement_message)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (streak > 0) Orange else Purple500
                )
            }
        }
    }
}

@Composable
private fun ClockSection(
    currentTime: Date,
    countdownText: String?,
    isDark: Boolean
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val calendar = remember(currentTime) { Calendar.getInstance().apply { time = currentTime } }
    val seconds = calendar.get(Calendar.SECOND)
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // 背景圆
        Box(
            modifier = Modifier
                .size(260.dp)
                .shadow(20.dp, CircleShape)
                .background(
                    color = if (isDark) Color.Gray.copy(alpha = 0.3f) else Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // 秒针进度环
            CircularProgressIndicator(
                progress = { seconds / 60f },
                modifier = Modifier.size(240.dp),
                color = Purple500,
                strokeWidth = 4.dp,
                trackColor = Color.Transparent
            )
            
            // 时间显示
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = timeFormat.format(currentTime),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
                
                // 倒计时
                countdownText?.let { countdown ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Purple500.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Purple500,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = countdown,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Purple500
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateSection(isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(R.string.no_alarms),
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun AlarmRowItem(
    alarm: AlarmModel,
    isDark: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = if (isDark) DarkSurface else Color.White,
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 第一行：图标、时间、开关、删除
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 图标
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Purple500.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAlarmIcon(alarm.label),
                            contentDescription = null,
                            tint = Purple500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // 时间
                    Text(
                        text = alarm.time,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light,
                        color = if (alarm.enabled) {
                            if (isDark) Color.White else Color.Black
                        } else {
                            Color.Gray
                        }
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 开关
                    Switch(
                        checked = alarm.enabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple500,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                    
                    // 删除按钮
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // 第二行：重复信息
            Spacer(modifier = Modifier.height(8.dp))
            RepeatInfoSection(alarm = alarm, isDark = isDark)
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_alarm_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.delete), color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RepeatInfoSection(alarm: AlarmModel, isDark: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (alarm.repeatMode) {
            RepeatMode.ONCE -> {
                // 响一次
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDark) Color.Blue.copy(alpha = 0.25f) else Color.Blue.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(R.string.repeat_once),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.Blue.copy(alpha = 0.9f) else Color.Blue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            RepeatMode.WORKDAYS -> {
                // 工作日
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDark) Green.copy(alpha = 0.25f) else Green.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(R.string.repeat_workdays),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Green.copy(alpha = 0.9f) else Green,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            RepeatMode.CUSTOM -> {
                // 自定义
                if (alarm.customDays.size == 7) {
                    // 每天
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isDark) Purple500.copy(alpha = 0.25f) else Purple500.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(R.string.everyday),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Purple500.copy(alpha = 0.9f) else Purple500,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else if (alarm.customDays.isNotEmpty()) {
                    // 显示具体日期
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val sortedDays = alarm.customDays.sortedBy { if (it == 0) 7 else it }
                        sortedDays.forEach { day ->
                            Surface(
                                shape = CircleShape,
                                color = if (isDark) Purple500.copy(alpha = 0.3f) else Purple500.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = getDayLabel(day),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Purple500.copy(alpha = 0.9f) else Purple500,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }
                
                // 跳过节假日标签
                if (alarm.skipHolidays) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Orange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(R.string.skip_holidays_tag),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Orange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getDayLabel(day: Int): String {
    return when (day) {
        0 -> stringResource(R.string.day_0)
        1 -> stringResource(R.string.day_1)
        2 -> stringResource(R.string.day_2)
        3 -> stringResource(R.string.day_3)
        4 -> stringResource(R.string.day_4)
        5 -> stringResource(R.string.day_5)
        6 -> stringResource(R.string.day_6)
        else -> ""
    }
}

@Composable
private fun getAlarmIcon(label: String) = when (label) {
    "work" -> Icons.Default.Work
    "date" -> Icons.Default.Favorite
    "flight" -> Icons.Default.Flight
    "train" -> Icons.Default.Train
    "meeting" -> Icons.Default.Groups
    "doctor" -> Icons.Default.MedicalServices
    "interview" -> Icons.Default.PersonAdd
    "exam" -> Icons.Default.School
    else -> Icons.Default.Alarm
}
