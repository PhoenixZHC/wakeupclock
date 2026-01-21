package com.wakeup.clock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.clock.R
import com.wakeup.clock.data.model.ThemeMode
import com.wakeup.clock.data.model.WakeUpRecord
import com.wakeup.clock.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日历打卡页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    records: List<WakeUpRecord>,
    streak: Int,
    themeMode: ThemeMode,
    onBack: () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar)) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 连续天数
            if (streak > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Orange.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Orange,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.streak_sentence, streak),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Orange
                        )
                    }
                }
            }
            
            // 月份导航
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
                    // 月份选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentMonth = (currentMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                        }
                        
                        Text(
                            text = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
                                .format(currentMonth.time),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        
                        IconButton(onClick = {
                            currentMonth = (currentMonth.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 星期标题
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 日历网格
                    CalendarGrid(
                        calendar = currentMonth,
                        records = records,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    calendar: Calendar,
    records: List<WakeUpRecord>,
    isDark: Boolean
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val recordMap = records.associateBy { it.date }
    
    val cal = (calendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = Calendar.getInstance()
    
    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (week in 0 until (totalCells / 7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (dayOfWeek in 0 until 7) {
                    val cellIndex = week * 7 + dayOfWeek
                    val dayOfMonth = cellIndex - firstDayOfWeek + 1
                    
                    if (dayOfMonth in 1..daysInMonth) {
                        val cellCal = (cal.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        }
                        val dateStr = dateFormat.format(cellCal.time)
                        val record = recordMap[dateStr]
                        val isToday = cellCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                cellCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                        
                        CalendarDay(
                            day = dayOfMonth,
                            record = record,
                            isToday = isToday,
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    record: WakeUpRecord?,
    isToday: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val hasRecord = record != null
    
    val backgroundColor = when {
        isToday -> Purple500
        hasRecord -> if (isDark) Green.copy(alpha = 0.2f) else Green.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isToday -> Color.White
        else -> if (isDark) Color.White else Color.Black
    }
    
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
        
        // 如果有打卡记录，显示闹钟类型图标
        if (hasRecord) {
            val label = record?.alarmLabel
            if (label != null) {
                Icon(
                    imageVector = getCategoryIcon(label),
                    contentDescription = null,
                    tint = getCategoryColor(label),
                    modifier = Modifier.size(12.dp)
                )
            } else {
                // 没有类型信息，显示圆点
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Green)
                )
            }
        }
    }
}

private fun getCategoryIcon(label: String): ImageVector = when (label) {
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

private fun getCategoryColor(label: String): Color = when (label) {
    "work" -> Color.Blue
    "date" -> Color(0xFFE91E63) // Pink
    "flight" -> Color.Cyan
    "train" -> Orange
    "meeting" -> Purple500
    "doctor" -> Red
    "interview" -> Green
    "exam" -> Color.Yellow
    else -> Color.Gray
}
