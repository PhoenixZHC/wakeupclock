package com.wakeup.clock.ui.screens

import android.app.AlarmManager
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.LocaleList
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.clock.BuildConfig
import com.wakeup.clock.R
import com.wakeup.clock.data.model.AppSettings
import com.wakeup.clock.data.model.ThemeMode
import com.wakeup.clock.ui.theme.*

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onResetData: () -> Unit,
    onBack: () -> Unit
) {
    val isDark = when (settings.themeMode) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    var showResetDialog by remember { mutableStateOf(false) }
    var showUsageGuide by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    var selectedLanguage by remember { 
        mutableStateOf(
            getCurrentLanguage(context)
        ) 
    }
    
    // 权限状态
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var canScheduleExactAlarms by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else true
        )
    }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .isIgnoringBatteryOptimizations(context.packageName)
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) DarkBackground else LightBackground
                )
            )
        },
        containerColor = if (isDark) DarkBackground else LightBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 权限设置（如果有权限未开启，显示警告）
            if (!canDrawOverlays || !canScheduleExactAlarms || !isIgnoringBatteryOptimizations) {
                PermissionsSection(
                    isDark = isDark,
                    canDrawOverlays = canDrawOverlays,
                    canScheduleExactAlarms = canScheduleExactAlarms,
                    isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                    onRequestOverlayPermission = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    },
                    onRequestExactAlarmPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    },
                    onRequestBatteryOptimization = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
            }
            
            // 主题设置
            SettingsSection(title = stringResource(R.string.theme_mode), isDark = isDark) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = settings.themeMode == ThemeMode.AUTO,
                        onClick = { onSettingsChanged(settings.copy(themeMode = ThemeMode.AUTO)) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Purple500,
                            activeContentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.theme_auto))
                    }
                    SegmentedButton(
                        selected = settings.themeMode == ThemeMode.LIGHT,
                        onClick = { onSettingsChanged(settings.copy(themeMode = ThemeMode.LIGHT)) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Purple500,
                            activeContentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.theme_light))
                    }
                    SegmentedButton(
                        selected = settings.themeMode == ThemeMode.DARK,
                        onClick = { onSettingsChanged(settings.copy(themeMode = ThemeMode.DARK)) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Purple500,
                            activeContentColor = Color.White
                        )
                    ) {
                        Text(stringResource(R.string.theme_dark))
                    }
                }
            }
            
            // 语言设置
            SettingsSection(title = stringResource(R.string.language), isDark = isDark) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedLanguage == "zh",
                        onClick = { 
                            selectedLanguage = "zh"
                            setAppLanguage(context, "zh")
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Purple500,
                            activeContentColor = Color.White
                        )
                    ) {
                        Text("中文")
                    }
                    SegmentedButton(
                        selected = selectedLanguage == "en",
                        onClick = { 
                            selectedLanguage = "en"
                            setAppLanguage(context, "en")
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Purple500,
                            activeContentColor = Color.White
                        )
                    ) {
                        Text("English")
                    }
                }
            }
            
            // 防赖床设置
            SettingsSection(
                title = stringResource(R.string.anti_snooze_title),
                subtitle = stringResource(R.string.anti_snooze_desc),
                isDark = isDark
            ) {
                // 启用开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.enable_anti_snooze),
                        color = if (isDark) Color.White else Color.Black
                    )
                    Switch(
                        checked = settings.enableAntiSnooze,
                        onCheckedChange = { onSettingsChanged(settings.copy(enableAntiSnooze = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple500
                        )
                    )
                }
                
                if (settings.enableAntiSnooze) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 间隔选择
                    Text(
                        text = stringResource(R.string.anti_snooze_interval),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(3, 5, 10).forEachIndexed { index, interval ->
                            SegmentedButton(
                                selected = settings.antiSnoozeInterval == interval,
                                onClick = { onSettingsChanged(settings.copy(antiSnoozeInterval = interval)) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                            ) {
                                Text("$interval ${stringResource(R.string.minutes)}")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 次数选择
                    Text(
                        text = stringResource(R.string.anti_snooze_count),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(1, 2, 3).forEachIndexed { index, count ->
                            SegmentedButton(
                                selected = settings.antiSnoozeCount == count,
                                onClick = { onSettingsChanged(settings.copy(antiSnoozeCount = count)) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                            ) {
                                Text("$count ${stringResource(R.string.times)}")
                            }
                        }
                    }
                }
            }
            
            // 音量提醒设置
            SettingsSection(
                title = stringResource(R.string.volume_reminder_section_title),
                subtitle = stringResource(R.string.volume_reminder_desc),
                isDark = isDark
            ) {
                // 启用开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.enable_volume_reminder),
                        color = if (isDark) Color.White else Color.Black
                    )
                    Switch(
                        checked = settings.enableVolumeReminder,
                        onCheckedChange = { 
                            val newSettings = settings.copy(enableVolumeReminder = it)
                            onSettingsChanged(newSettings)
                            // 更新音量管理器
                            val volumeManager = com.wakeup.clock.manager.VolumeCheckManager.getInstance(context)
                            if (it) {
                                volumeManager.ensureVolumeReminderChannel() // 前台创建渠道，避免 OPPO/ColorOS 后台创建时降级
                                volumeManager.startMonitoring()
                                volumeManager.scheduleDailyCheck(newSettings)
                            } else {
                                volumeManager.stopMonitoring()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple500
                        )
                    )
                }
                
                if (settings.enableVolumeReminder) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 提醒时间设置（使用时间选择器）
                    var showTimePicker by remember { mutableStateOf(false) }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.volume_reminder_time),
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            text = String.format("%02d:%02d", settings.volumeReminderHour, settings.volumeReminderMinute),
                            color = Purple500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (showTimePicker) {
                        TimePickerDialog(
                            initialHour = settings.volumeReminderHour,
                            initialMinute = settings.volumeReminderMinute,
                            onDismiss = { showTimePicker = false },
                            onConfirm = { hour, minute ->
                                val newSettings = settings.copy(
                                    volumeReminderHour = hour,
                                    volumeReminderMinute = minute
                                )
                                onSettingsChanged(newSettings)
                                // 重新调度检查
                                val volumeManager = com.wakeup.clock.manager.VolumeCheckManager.getInstance(context)
                                volumeManager.scheduleDailyCheck(newSettings)
                                showTimePicker = false
                            }
                        )
                    }
                    
                    // 部分机型需在系统通知里单独开启「睡前提醒」的声音与锁屏
                    Text(
                        text = stringResource(R.string.volume_reminder_open_notification_settings),
                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(
                        onClick = {
                            val intent = Intent().apply {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                } else {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try { context.startActivity(intent) } catch (_: Exception) { }
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(stringResource(R.string.open_notification_settings), color = Purple500)
                    }
                }
            }
            
            // 数据管理
            SettingsSection(title = stringResource(R.string.data_management), isDark = isDark) {
                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.reset_data),
                            color = Red
                        )
                    }
                }
            }
            
            // 后台保护设置
            BackgroundProtectionSection(
                isDark = isDark,
                context = context
            )
            
            // 帮助
            SettingsSection(title = stringResource(R.string.help), isDark = isDark) {
                TextButton(
                    onClick = { showUsageGuide = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Help,
                                contentDescription = null,
                                tint = if (isDark) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.usage_guide),
                                color = if (isDark) Color.White else Color.Black
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }
            
            // 关于
            SettingsSection(title = stringResource(R.string.about), isDark = isDark) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.version),
                        color = if (isDark) Color.White else Color.Black
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        color = Color.Gray
                    )
                }
            }
        }
    }
    
    // 重置确认对话框
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.confirm_reset)) },
            text = { Text(stringResource(R.string.reset_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    onResetData()
                    showResetDialog = false
                }) {
                    Text(stringResource(R.string.reset), color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // 使用指南
    if (showUsageGuide) {
        UsageGuideDialog(
            isDark = isDark,
            onDismiss = { showUsageGuide = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String? = null,
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkSurface else Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun UsageGuideDialog(
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Purple500
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.usage_guide))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.guide_subtitle),
                    color = Color.Gray
                )
                
                GuideSection(
                    title = stringResource(R.string.recommended_practices),
                    color = Purple500,
                    items = listOf(
                        GuideItem(Icons.Default.BatteryAlert, stringResource(R.string.guide_tip1_title), stringResource(R.string.guide_tip1_desc)),
                        GuideItem(Icons.Default.PlayArrow, stringResource(R.string.guide_tip2_title), stringResource(R.string.guide_tip2_desc)),
                        GuideItem(Icons.Default.Notifications, stringResource(R.string.guide_tip3_title), stringResource(R.string.guide_tip3_desc))
                    )
                )
                
                GuideSection(
                    title = stringResource(R.string.not_recommended_practices),
                    color = Orange,
                    items = listOf(
                        GuideItem(Icons.Default.Block, stringResource(R.string.guide_tip4_title), stringResource(R.string.guide_tip4_desc)),
                        GuideItem(Icons.Default.BatteryFull, stringResource(R.string.guide_tip5_title), stringResource(R.string.guide_tip5_desc)),
                        GuideItem(Icons.Default.Science, stringResource(R.string.guide_tip6_title), stringResource(R.string.guide_tip6_desc))
                    )
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Purple500.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.system_limitations),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.system_limitations_desc),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        }
    )
}

private data class GuideItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

/**
 * 权限设置区域
 */
@Composable
private fun PermissionsSection(
    isDark: Boolean,
    canDrawOverlays: Boolean,
    canScheduleExactAlarms: Boolean,
    isIgnoringBatteryOptimizations: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onRequestExactAlarmPermission: () -> Unit,
    onRequestBatteryOptimization: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Orange.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Orange
                )
                Text(
                    text = "需要开启以下权限以确保闹钟正常工作",
                    fontWeight = FontWeight.Bold,
                    color = Orange
                )
            }
            
            // 显示在其他应用上层
            if (!canDrawOverlays) {
                PermissionItem(
                    title = "显示在其他应用上层",
                    description = "允许闹钟在任何情况下弹出全屏界面",
                    onClick = onRequestOverlayPermission
                )
            }
            
            // 精确闹钟权限
            if (!canScheduleExactAlarms) {
                PermissionItem(
                    title = "精确闹钟",
                    description = "允许闹钟在准确的时间触发",
                    onClick = onRequestExactAlarmPermission
                )
            }
            
            // 电池优化
            if (!isIgnoringBatteryOptimizations) {
                PermissionItem(
                    title = "忽略电池优化",
                    description = "防止系统在后台终止闹钟服务",
                    onClick = onRequestBatteryOptimization
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        TextButton(onClick = onClick) {
            Text("去开启", color = Purple500)
        }
    }
}

/**
 * 后台保护设置区域
 * 引导用户去系统设置中保护应用不被清理
 */
@Composable
private fun BackgroundProtectionSection(
    isDark: Boolean,
    context: Context
) {
    var showDetailDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Purple500.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Purple500
                )
                Text(
                    text = stringResource(R.string.background_protection_title),
                    fontWeight = FontWeight.Bold,
                    color = Purple500
                )
            }
            
            Text(
                text = stringResource(R.string.background_protection_desc),
                fontSize = 14.sp,
                color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f),
                lineHeight = 20.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 打开应用设置按钮
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // 忽略
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.open_app_settings),
                        fontSize = 13.sp
                    )
                }
                
                // 查看详细指南
                Button(
                    onClick = { showDetailDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.view_guide),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
    
    // 详细指南对话框
    if (showDetailDialog) {
        BackgroundProtectionGuideDialog(
            onDismiss = { showDetailDialog = false }
        )
    }
}

/**
 * 后台保护详细指南对话框
 */
@Composable
private fun BackgroundProtectionGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Purple500
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.background_protection_guide_title))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.background_protection_guide_intro),
                    color = Color.Gray
                )
                
                // 华为/荣耀
                BrandGuideSection(
                    brand = "华为 / 荣耀",
                    steps = listOf(
                        stringResource(R.string.guide_huawei_step1),
                        stringResource(R.string.guide_huawei_step2),
                        stringResource(R.string.guide_huawei_step3)
                    )
                )
                
                // 小米/红米
                BrandGuideSection(
                    brand = "小米 / 红米",
                    steps = listOf(
                        stringResource(R.string.guide_xiaomi_step1),
                        stringResource(R.string.guide_xiaomi_step2),
                        stringResource(R.string.guide_xiaomi_step3)
                    )
                )
                
                // OPPO/一加/realme
                BrandGuideSection(
                    brand = "OPPO / 一加 / realme",
                    steps = listOf(
                        stringResource(R.string.guide_oppo_step1),
                        stringResource(R.string.guide_oppo_step2),
                        stringResource(R.string.guide_oppo_step3)
                    )
                )
                
                // vivo/iQOO
                BrandGuideSection(
                    brand = "vivo / iQOO",
                    steps = listOf(
                        stringResource(R.string.guide_vivo_step1),
                        stringResource(R.string.guide_vivo_step2),
                        stringResource(R.string.guide_vivo_step3)
                    )
                )
                
                // 三星
                BrandGuideSection(
                    brand = "三星",
                    steps = listOf(
                        stringResource(R.string.guide_samsung_step1),
                        stringResource(R.string.guide_samsung_step2),
                        stringResource(R.string.guide_samsung_step3)
                    )
                )
                
                // 通用方法
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Orange.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Orange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.general_tip_title),
                                fontWeight = FontWeight.SemiBold,
                                color = Orange
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.general_tip_content),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.got_it))
            }
        }
    )
}

/**
 * 品牌设置指南
 */
@Composable
private fun BrandGuideSection(
    brand: String,
    steps: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = brand,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Purple500
        )
        steps.forEachIndexed { index, step ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${index + 1}.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = step,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun GuideSection(
    title: String,
    color: Color,
    items: List<GuideItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = color
        )
        items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * 获取当前应用语言
 */
private fun getCurrentLanguage(context: Context): String {
    val localeManager = context.getSystemService(LocaleManager::class.java)
    val locales = localeManager.applicationLocales
    return if (locales.isEmpty) {
        // 跟随系统
        val systemLocale = context.resources.configuration.locales[0]
        if (systemLocale.language == "zh") "zh" else "en"
    } else {
        val locale = locales[0]
        if (locale?.language == "zh") "zh" else "en"
    }
}

/**
 * 设置应用语言
 */
private fun setAppLanguage(context: Context, languageCode: String) {
    val localeManager = context.getSystemService(LocaleManager::class.java)
    localeManager.applicationLocales = LocaleList.forLanguageTags(languageCode)
}

/**
 * 时间选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
