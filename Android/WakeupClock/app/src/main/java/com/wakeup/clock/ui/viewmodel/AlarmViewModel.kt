package com.wakeup.clock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.clock.data.database.AppDatabase
import com.wakeup.clock.data.model.AlarmModel
import com.wakeup.clock.data.model.AppSettings
import com.wakeup.clock.data.model.WakeUpRecord
import com.wakeup.clock.data.repository.AlarmRepository
import com.wakeup.clock.data.repository.SettingsRepository
import com.wakeup.clock.data.repository.WakeUpRecordRepository
import com.wakeup.clock.manager.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 闹钟 ViewModel
 */
class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val alarmRepository = AlarmRepository(database.alarmDao())
    private val recordRepository = WakeUpRecordRepository(database.wakeUpRecordDao())
    private val settingsRepository = SettingsRepository(database.appSettingsDao())
    private val alarmScheduler = AlarmScheduler(application)
    
    // 所有闹钟
    val alarms: StateFlow<List<AlarmModel>> = alarmRepository.allAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 应用设置
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())
    
    // 连续天数
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()
    
    // 下一个闹钟倒计时
    private val _countdownText = MutableStateFlow<String?>(null)
    val countdownText: StateFlow<String?> = _countdownText.asStateFlow()
    
    init {
        // 初始化设置
        viewModelScope.launch {
            val existingSettings = settingsRepository.getSettingsOnce()
            if (existingSettings.id == 0) {
                settingsRepository.updateSettings(AppSettings())
            }
        }
        
        // 加载连续天数
        loadStreak()
        
        // 更新倒计时
        updateCountdown()
    }
    
    /**
     * 添加闹钟
     */
    fun addAlarm(alarm: AlarmModel) {
        viewModelScope.launch {
            alarmRepository.insertAlarm(alarm)
            alarmScheduler.scheduleAlarm(alarm)
        }
    }
    
    /**
     * 更新闹钟
     */
    fun updateAlarm(alarm: AlarmModel) {
        viewModelScope.launch {
            alarmRepository.updateAlarm(alarm)
            if (alarm.enabled) {
                alarmScheduler.scheduleAlarm(alarm)
            } else {
                alarmScheduler.cancelAlarm(alarm)
            }
        }
    }
    
    /**
     * 删除闹钟
     */
    fun deleteAlarm(alarm: AlarmModel) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(alarm)
            alarmRepository.deleteAlarm(alarm)
        }
    }
    
    /**
     * 切换闹钟启用状态
     */
    fun toggleAlarm(alarm: AlarmModel) {
        viewModelScope.launch {
            val newEnabled = !alarm.enabled
            alarmRepository.setAlarmEnabled(alarm.id, newEnabled)
            
            if (newEnabled) {
                alarmScheduler.scheduleAlarm(alarm.copy(enabled = true))
            } else {
                alarmScheduler.cancelAlarm(alarm)
            }
        }
    }
    
    /**
     * 记录起床
     */
    fun recordWakeUp(alarmId: String, alarmLabel: String) {
        viewModelScope.launch {
            val now = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            val record = WakeUpRecord(
                date = dateFormat.format(now),
                time = timeFormat.format(now),
                alarmLabel = alarmLabel,
                alarmId = alarmId
            )
            
            recordRepository.insertRecord(record)
            loadStreak()
        }
    }
    
    /**
     * 更新设置
     */
    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(settings)
        }
    }
    
    /**
     * 重置所有数据
     */
    fun resetAllData() {
        viewModelScope.launch {
            // 取消所有闹钟
            alarms.value.forEach { alarm ->
                alarmScheduler.cancelAlarm(alarm)
            }
            
            // 删除所有数据
            alarmRepository.deleteAllAlarms()
            recordRepository.deleteAllRecords()
            settingsRepository.resetSettings()
            
            _streak.value = 0
        }
    }
    
    /**
     * 获取闹钟倒计时文本
     */
    fun getCountdownText(alarm: AlarmModel): String? {
        return alarmScheduler.getCountdownText(alarm)
    }
    
    /**
     * 加载连续天数
     */
    private fun loadStreak() {
        viewModelScope.launch {
            _streak.value = recordRepository.getStreak()
        }
    }
    
    /**
     * 更新倒计时
     */
    private fun updateCountdown() {
        viewModelScope.launch {
            alarms.collect { alarmList ->
                val enabledAlarms = alarmList.filter { it.enabled }
                if (enabledAlarms.isEmpty()) {
                    _countdownText.value = null
                    return@collect
                }
                
                // 找到最近的闹钟
                var nearestAlarm: AlarmModel? = null
                var nearestTime: Long = Long.MAX_VALUE
                
                enabledAlarms.forEach { alarm ->
                    alarmScheduler.calculateNextTriggerTime(alarm)?.let { triggerTime ->
                        if (triggerTime < nearestTime) {
                            nearestTime = triggerTime
                            nearestAlarm = alarm
                        }
                    }
                }
                
                nearestAlarm?.let { alarm ->
                    _countdownText.value = alarmScheduler.getCountdownText(alarm)
                }
            }
        }
    }
    
    /**
     * 检查是否有精确闹钟权限
     */
    fun canScheduleExactAlarms(): Boolean = alarmScheduler.canScheduleExactAlarms()
    
    /**
     * 重新调度所有闹钟
     */
    fun rescheduleAllAlarms() {
        viewModelScope.launch {
            alarms.value.filter { it.enabled }.forEach { alarm ->
                alarmScheduler.scheduleAlarm(alarm)
            }
        }
    }
}
