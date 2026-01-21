package com.wakeup.clock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.clock.R
import com.wakeup.clock.data.model.*
import com.wakeup.clock.ui.theme.*
import com.wakeup.clock.util.HolidayChecker
import kotlinx.coroutines.launch
import java.util.*

/**
 * 添加闹钟界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmScreen(
    themeMode: ThemeMode,
    onSave: (AlarmModel) -> Unit,
    onCancel: () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    var selectedHour by remember { mutableIntStateOf(7) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedLabel by remember { mutableStateOf("work") }
    var repeatMode by remember { mutableStateOf(RepeatMode.WORKDAYS) }
    var customDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) } // 默认周一到周五
    var skipHolidays by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        is24Hour = true
    )
    
    // 更新选中时间
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        selectedHour = timePickerState.hour
        selectedMinute = timePickerState.minute
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_alarm)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val alarm = AlarmModel(
                            time = String.format("%02d:%02d", selectedHour, selectedMinute),
                            enabled = true,
                            label = selectedLabel,
                            missionType = MissionType.MATH,
                            difficulty = Difficulty.MEDIUM,
                            repeatMode = repeatMode,
                            customDays = if (repeatMode == RepeatMode.CUSTOM) customDays.toList() else emptyList(),
                            skipHolidays = if (repeatMode == RepeatMode.CUSTOM) skipHolidays else false
                        )
                        onSave(alarm)
                    }) {
                        Text(
                            text = stringResource(R.string.save_alarm),
                            fontWeight = FontWeight.SemiBold,
                            color = Purple500
                        )
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 时间选择器
            TimePickerSection(
                timePickerState = timePickerState,
                isDark = isDark
            )
            
            // 标签选择
            LabelSection(
                selectedLabel = selectedLabel,
                isDark = isDark,
                onLabelSelected = { selectedLabel = it }
            )
            
            // 重复模式
            RepeatSection(
                repeatMode = repeatMode,
                customDays = customDays,
                skipHolidays = skipHolidays,
                isDark = isDark,
                onRepeatModeChanged = { repeatMode = it },
                onCustomDaysChanged = { customDays = it },
                onSkipHolidaysChanged = { newValue ->
                    skipHolidays = newValue
                    if (newValue) {
                        scope.launch {
                            HolidayChecker.preloadHolidays(forceRefresh = true)
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerSection(
    timePickerState: TimePickerState,
    isDark: Boolean
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.time_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = if (isDark) Color.Gray.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                    selectorColor = Purple500,
                    containerColor = if (isDark) DarkSurface else Color.White,
                    periodSelectorSelectedContainerColor = Purple500,
                    timeSelectorSelectedContainerColor = Purple500.copy(alpha = 0.2f),
                    timeSelectorSelectedContentColor = Purple500
                )
            )
        }
    }
}

@Composable
private fun LabelSection(
    selectedLabel: String,
    isDark: Boolean,
    onLabelSelected: (String) -> Unit
) {
    val categories = listOf(
        Triple("work", Icons.Default.Work, R.string.label_work),
        Triple("date", Icons.Default.Favorite, R.string.label_date),
        Triple("flight", Icons.Default.Flight, R.string.label_flight),
        Triple("train", Icons.Default.Train, R.string.label_train),
        Triple("meeting", Icons.Default.Groups, R.string.label_meeting),
        Triple("doctor", Icons.Default.MedicalServices, R.string.label_doctor),
        Triple("interview", Icons.Default.PersonAdd, R.string.label_interview),
        Triple("exam", Icons.Default.School, R.string.label_exam),
        Triple("other", Icons.Default.Tag, R.string.label_other)
    )
    
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
                text = stringResource(R.string.label_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categories) { (id, icon, labelRes) ->
                    CategoryItem(
                        id = id,
                        icon = icon,
                        label = stringResource(labelRes),
                        isSelected = selectedLabel == id,
                        isDark = isDark,
                        onClick = { onLabelSelected(id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    id: String,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                    } else {
                        Brush.linearGradient(listOf(
                            Color.Gray.copy(alpha = 0.1f),
                            Color.Gray.copy(alpha = 0.1f)
                        ))
                    }
                )
                .then(
                    if (!isSelected) {
                        Modifier.border(1.dp, Color.Gray.copy(alpha = 0.2f), CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else if (isDark) Color.White else Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Purple500 else Color.Gray
        )
    }
}

@Composable
private fun RepeatSection(
    repeatMode: RepeatMode,
    customDays: Set<Int>,
    skipHolidays: Boolean,
    isDark: Boolean,
    onRepeatModeChanged: (RepeatMode) -> Unit,
    onCustomDaysChanged: (Set<Int>) -> Unit,
    onSkipHolidaysChanged: (Boolean) -> Unit
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
                text = stringResource(R.string.repeat_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // 重复模式选择
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = repeatMode == RepeatMode.ONCE,
                    onClick = { onRepeatModeChanged(RepeatMode.ONCE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Purple500,
                        activeContentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.repeat_once), fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = repeatMode == RepeatMode.WORKDAYS,
                    onClick = { onRepeatModeChanged(RepeatMode.WORKDAYS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Purple500,
                        activeContentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.repeat_workdays), fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = repeatMode == RepeatMode.CUSTOM,
                    onClick = { onRepeatModeChanged(RepeatMode.CUSTOM) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = Purple500,
                        activeContentColor = Color.White
                    )
                ) {
                    Text(stringResource(R.string.repeat_custom), fontSize = 12.sp)
                }
            }
            
            // 自定义日期选择
            if (repeatMode == RepeatMode.CUSTOM) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.select_days_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 星期选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val days = listOf(
                        1 to R.string.day_1,
                        2 to R.string.day_2,
                        3 to R.string.day_3,
                        4 to R.string.day_4,
                        5 to R.string.day_5,
                        6 to R.string.day_6,
                        0 to R.string.day_0
                    )
                    
                    days.forEach { (dayIndex, labelRes) ->
                        DayButton(
                            label = stringResource(labelRes),
                            isSelected = customDays.contains(dayIndex),
                            isDark = isDark,
                            onClick = {
                                onCustomDaysChanged(
                                    if (customDays.contains(dayIndex)) {
                                        customDays - dayIndex
                                    } else {
                                        customDays + dayIndex
                                    }
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 跳过节假日
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.skip_holidays_label),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            text = stringResource(R.string.skip_holidays_desc),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = skipHolidays,
                        onCheckedChange = onSkipHolidaysChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple500
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DayButton(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                color = if (isSelected) Purple500 else Color.Gray.copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else if (isDark) Color.White else Color.Black
        )
    }
}
