package com.wakeup.clock

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wakeup.clock.data.database.AppDatabase
import com.wakeup.clock.ui.screens.*
import com.wakeup.clock.ui.theme.*
import com.wakeup.clock.ui.viewmodel.AlarmViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 主Activity
 */
class MainActivity : ComponentActivity() {
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val viewModel: AlarmViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            
            // 检查是否需要显示首次启动引导
            var showOnboarding by remember { 
                mutableStateOf(!settings.hasAcceptedSafetyNotice) 
            }
            
            // 监听设置变化，当 hasAcceptedSafetyNotice 变为 true 时隐藏引导
            LaunchedEffect(settings.hasAcceptedSafetyNotice) {
                if (settings.hasAcceptedSafetyNotice) {
                    showOnboarding = false
                }
            }
            
            WakeupClockTheme(themeMode = settings.themeMode) {
                val context = LocalContext.current
                
                // 初始化音量检测
                LaunchedEffect(settings.enableVolumeReminder) {
                    val volumeManager = com.wakeup.clock.manager.VolumeCheckManager.getInstance(context)
                    if (settings.enableVolumeReminder) {
                        volumeManager.startMonitoring()
                        volumeManager.scheduleDailyCheck(settings)
                    } else {
                        volumeManager.stopMonitoring()
                    }
                }
                
                if (showOnboarding) {
                    OnboardingScreen(
                        onComplete = {
                            viewModel.updateSettings(settings.copy(hasAcceptedSafetyNotice = true))
                            showOnboarding = false
                        }
                    )
                } else {
                    MainContent(viewModel = viewModel)
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

/**
 * 首次启动引导页面
 */
@Composable
private fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(0) }
    
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
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    
    // 刷新权限状态
    fun refreshPermissions() {
        canDrawOverlays = Settings.canDrawOverlays(context)
        canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true
        isIgnoringBatteryOptimizations = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
    
    // 每次界面可见时刷新权限状态
    DisposableEffect(Unit) {
        onDispose { }
    }
    
    // 监听生命周期，从设置页面返回时刷新权限
    LaunchedEffect(currentStep) {
        refreshPermissions()
    }
    
    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                0 -> SafetyNoticeStep(
                    onNext = { currentStep = 1 }
                )
                1 -> PermissionsStep(
                    canDrawOverlays = canDrawOverlays,
                    canScheduleExactAlarms = canScheduleExactAlarms,
                    isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                    hasNotificationPermission = hasNotificationPermission,
                    onRefresh = { refreshPermissions() },
                    onComplete = onComplete
                )
            }
        }
    }
}

/**
 * 安全提示步骤
 */
@Composable
private fun SafetyNoticeStep(
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // 图标
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Orange,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 标题
        Text(
            text = stringResource(R.string.safety_notice_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 内容
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkCardBackground
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.safety_notice_message),
                modifier = Modifier.padding(20.dp),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                lineHeight = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 按钮
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple500
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.safety_notice_agree),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 权限设置步骤
 */
@Composable
private fun PermissionsStep(
    canDrawOverlays: Boolean,
    canScheduleExactAlarms: Boolean,
    isIgnoringBatteryOptimizations: Boolean,
    hasNotificationPermission: Boolean,
    onRefresh: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    
    // 所有必要权限是否都已开启
    val allPermissionsGranted = canDrawOverlays && canScheduleExactAlarms && 
            isIgnoringBatteryOptimizations && hasNotificationPermission
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // 图标
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = Purple500,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 标题
        Text(
            text = stringResource(R.string.permissions_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.permissions_subtitle),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 权限列表
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkCardBackground
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 显示在其他应用上层
                PermissionRow(
                    icon = Icons.Default.Layers,
                    title = stringResource(R.string.permission_overlay),
                    description = stringResource(R.string.permission_overlay_desc),
                    isGranted = canDrawOverlays,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
                
                // 精确闹钟
                PermissionRow(
                    icon = Icons.Default.Alarm,
                    title = stringResource(R.string.permission_exact_alarm),
                    description = stringResource(R.string.permission_exact_alarm_desc),
                    isGranted = canScheduleExactAlarms,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
                
                // 电池优化
                PermissionRow(
                    icon = Icons.Default.BatteryFull,
                    title = stringResource(R.string.permission_battery),
                    description = stringResource(R.string.permission_battery_desc),
                    isGranted = isIgnoringBatteryOptimizations,
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )
                
                // 通知权限
                PermissionRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.permission_notification),
                    description = stringResource(R.string.permission_notification_desc),
                    isGranted = hasNotificationPermission,
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 刷新按钮
        TextButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.refresh_permissions))
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 完成按钮
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allPermissionsGranted) Purple500 else Purple500.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (allPermissionsGranted) 
                    stringResource(R.string.start_using) 
                else 
                    stringResource(R.string.skip_for_now),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        if (!allPermissionsGranted) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.permission_warning),
                fontSize = 12.sp,
                color = Orange,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) Green else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(24.dp)
            )
        } else {
            TextButton(onClick = onClick) {
                Text(
                    text = stringResource(R.string.go_to_enable),
                    color = Purple500
                )
            }
        }
    }
}

@Composable
private fun MainContent(viewModel: AlarmViewModel) {
    val navController = rememberNavController()
    
    val alarms by viewModel.alarms.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val countdownText by viewModel.countdownText.collectAsState()
    
    // 起床记录（简化处理）
    val records by remember { mutableStateOf(emptyList<com.wakeup.clock.data.model.WakeUpRecord>()) }
    
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                alarms = alarms,
                streak = streak,
                countdownText = countdownText,
                themeMode = settings.themeMode,
                onAddAlarm = { navController.navigate("add_alarm") },
                onToggleAlarm = { viewModel.toggleAlarm(it) },
                onDeleteAlarm = { viewModel.deleteAlarm(it) },
                onOpenSettings = { navController.navigate("settings") },
                onOpenCalendar = { navController.navigate("calendar") },
                getCountdownText = { viewModel.getCountdownText(it) }
            )
        }
        
        composable("add_alarm") {
            AddAlarmScreen(
                themeMode = settings.themeMode,
                onSave = { alarm ->
                    viewModel.addAlarm(alarm)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                settings = settings,
                onSettingsChanged = { viewModel.updateSettings(it) },
                onResetData = { viewModel.resetAllData() },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("calendar") {
            CalendarScreen(
                records = records,
                streak = streak,
                themeMode = settings.themeMode,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
