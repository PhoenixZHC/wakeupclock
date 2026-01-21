package com.wakeup.clock.ui.screens

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.wakeup.clock.R
import com.wakeup.clock.data.model.Difficulty
import com.wakeup.clock.data.model.MissionType
import com.wakeup.clock.manager.VolumeLevel
import com.wakeup.clock.ui.missions.*
import com.wakeup.clock.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 上次选择的任务类型（用于避免连续重复）
 */
private var lastMissionType: MissionType? = null

/**
 * 选择随机任务（避免与上次重复）
 */
private fun selectRandomMission(): MissionType {
    val missions = MissionType.entries.toMutableList()
    
    // 如果有上次的任务，从列表中移除以避免连续重复
    lastMissionType?.let { last ->
        if (missions.size > 1) {
            missions.remove(last)
        }
    }
    
    val selected = missions.random()
    lastMissionType = selected
    return selected
}

/**
 * 闹钟锁屏界面
 */
@Composable
fun AlarmLockdownScreen(
    alarmLabel: String,
    difficulty: Difficulty,
    isAntiSnooze: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // 当前时间
    var currentTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }
    
    // 音量级别
    var volumeLevel by remember { mutableStateOf(VolumeLevel.NORMAL) }
    LaunchedEffect(Unit) {
        delay(15000)
        volumeLevel = VolumeLevel.LOUD
        delay(15000)
        volumeLevel = VolumeLevel.SUPER_LOUD
    }
    
    // 闪烁动画
    val infiniteTransition = rememberInfiniteTransition(label = "flash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flashAlpha"
    )
    
    // 是否显示任务
    var showMission by remember { mutableStateOf(false) }
    
    // 随机选择任务类型（避免与上次重复）
    var missionType by remember { mutableStateOf(selectRandomMission()) }
    
    // 视频播放器
    val videoUri = remember(alarmLabel) {
        getVideoUri(context, alarmLabel)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 视频背景
        if (videoUri != null) {
            VideoBackground(videoUri = videoUri)
        } else {
            // 渐变背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black,
                                Color(0xFF1A1A2E),
                                Color.Black
                            )
                        )
                    )
            )
        }
        
        // 黑色遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        // 红色闪烁边框（超大音量时）
        if (volumeLevel == VolumeLevel.SUPER_LOUD) {
            RedFlashBorder(alpha = flashAlpha)
        }
        
        // 内容
        if (showMission) {
            MissionContent(
                missionType = missionType,
                difficulty = difficulty,
                onComplete = onDismiss
            )
        } else {
            AlarmDisplayContent(
                currentTime = currentTime,
                alarmLabel = alarmLabel,
                volumeLevel = volumeLevel,
                onStartMission = { showMission = true }
            )
        }
    }
}

@Composable
private fun VideoBackground(videoUri: Uri) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f // 静音
            prepare()
            play()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun RedFlashBorder(alpha: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 顶部
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .alpha(alpha)
                .background(Red)
                .align(Alignment.TopCenter)
        )
        // 底部
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .alpha(alpha)
                .background(Red)
                .align(Alignment.BottomCenter)
        )
        // 左侧
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(8.dp)
                .alpha(alpha)
                .background(Red)
                .align(Alignment.CenterStart)
        )
        // 右侧
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(8.dp)
                .alpha(alpha)
                .background(Red)
                .align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun AlarmDisplayContent(
    currentTime: Date,
    alarmLabel: String,
    volumeLevel: VolumeLevel,
    onStartMission: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 时间
        Text(
            text = timeFormat.format(currentTime),
            fontSize = 100.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 图标
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(
                    color = if (volumeLevel == VolumeLevel.SUPER_LOUD) {
                        Red.copy(alpha = 0.3f)
                    } else {
                        Color.White.copy(alpha = 0.1f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (volumeLevel == VolumeLevel.SUPER_LOUD) {
                    Icons.Default.Warning
                } else {
                    getAlarmIcon(alarmLabel)
                },
                contentDescription = null,
                tint = if (volumeLevel == VolumeLevel.SUPER_LOUD) Red else Color.White,
                modifier = Modifier.size(56.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 消息
        Text(
            text = when (volumeLevel) {
                VolumeLevel.NORMAL -> getAlarmMessage(alarmLabel)
                VolumeLevel.LOUD -> stringResource(R.string.get_up_now)
                VolumeLevel.SUPER_LOUD -> stringResource(R.string.emergency)
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (volumeLevel == VolumeLevel.SUPER_LOUD) Red else Color.White,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = when (volumeLevel) {
                VolumeLevel.NORMAL -> stringResource(R.string.early_bird)
                VolumeLevel.LOUD -> getAlarmMessage(alarmLabel)
                VolumeLevel.SUPER_LOUD -> stringResource(R.string.noise_bombing)
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 开始任务按钮
        Button(
            onClick = onStartMission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.9f),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.start_mission),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.complete_mission),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun MissionContent(
    missionType: MissionType,
    difficulty: Difficulty,
    onComplete: () -> Unit
) {
    when (missionType) {
        MissionType.MATH -> MathMission(difficulty = difficulty, onComplete = onComplete)
        MissionType.MEMORY -> MemoryMission(difficulty = difficulty, onComplete = onComplete)
        MissionType.ORDER -> OrderMission(difficulty = difficulty, onComplete = onComplete)
        MissionType.SHAKE -> ShakeMission(difficulty = difficulty, onComplete = onComplete)
        MissionType.TYPING -> TypingMission(difficulty = difficulty, onComplete = onComplete)
    }
}

private fun getVideoUri(context: android.content.Context, label: String): Uri? {
    val videoName = when (label) {
        "work" -> "work"
        "date" -> "date"
        "flight" -> "flight"
        "train" -> "train"
        "meeting" -> "meeting"
        "doctor" -> "doctor"
        "interview" -> "interview"
        "exam" -> "exam"
        else -> return null
    }
    
    val resId = context.resources.getIdentifier(videoName, "raw", context.packageName)
    return if (resId != 0) {
        Uri.parse("android.resource://${context.packageName}/$resId")
    } else {
        null
    }
}

@Composable
private fun getAlarmIcon(label: String): ImageVector = when (label) {
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

@Composable
private fun getAlarmMessage(label: String): String = when (label) {
    "work" -> stringResource(R.string.alarm_msg_work)
    "date" -> stringResource(R.string.alarm_msg_date)
    "flight" -> stringResource(R.string.alarm_msg_flight)
    "train" -> stringResource(R.string.alarm_msg_train)
    "meeting" -> stringResource(R.string.alarm_msg_meeting)
    "doctor" -> stringResource(R.string.alarm_msg_doctor)
    "interview" -> stringResource(R.string.alarm_msg_interview)
    "exam" -> stringResource(R.string.alarm_msg_exam)
    else -> stringResource(R.string.alarm_msg_other)
}
