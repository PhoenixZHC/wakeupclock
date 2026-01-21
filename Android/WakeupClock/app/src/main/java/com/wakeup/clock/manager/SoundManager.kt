package com.wakeup.clock.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.wakeup.clock.R

/**
 * 音量级别
 */
enum class VolumeLevel {
    NORMAL,  // 普通音量
    LOUD,    // 大声
    SUPER_LOUD  // 超大声
}

/**
 * 声音管理器
 * 负责播放闹钟声音和震动
 */
class SoundManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SoundManager"
        
        // 闹钟声音资源列表
        private val ALARM_SOUNDS = listOf(
            R.raw.alarm1,
            R.raw.alarm2,
            R.raw.alarm3,
            R.raw.alarm4,
            R.raw.alarm5,
            R.raw.alarm6,
            R.raw.alarm7
        )
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator: Vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private var currentSoundIndex: Int = -1
    
    /**
     * 播放闹钟声音
     */
    fun playAlarmSound(level: VolumeLevel = VolumeLevel.NORMAL) {
        stopAlarmSound()
        
        // 随机选择一个声音（避免连续重复）
        var newIndex: Int
        do {
            newIndex = ALARM_SOUNDS.indices.random()
        } while (newIndex == currentSoundIndex && ALARM_SOUNDS.size > 1)
        currentSoundIndex = newIndex
        
        val soundResId = ALARM_SOUNDS[currentSoundIndex]
        
        try {
            mediaPlayer = MediaPlayer.create(context, soundResId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                
                // 根据音量级别设置音量
                val volume = when (level) {
                    VolumeLevel.NORMAL -> 0.6f
                    VolumeLevel.LOUD -> 0.85f
                    VolumeLevel.SUPER_LOUD -> 1.0f
                }
                setVolume(volume, volume)
                
                start()
            }
            
            // 设置系统音量为最大
            setSystemVolumeToMax()
            
            // 开始震动
            startVibration(level)
            
            Log.d(TAG, "Playing alarm sound at level: $level")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alarm sound: ${e.message}")
        }
    }
    
    /**
     * 更新音量级别
     */
    fun updateVolumeLevel(level: VolumeLevel) {
        mediaPlayer?.let { player ->
            val volume = when (level) {
                VolumeLevel.NORMAL -> 0.6f
                VolumeLevel.LOUD -> 0.85f
                VolumeLevel.SUPER_LOUD -> 1.0f
            }
            player.setVolume(volume, volume)
            
            // 更新震动模式
            startVibration(level)
            
            Log.d(TAG, "Updated volume level to: $level")
        }
    }
    
    /**
     * 停止闹钟声音
     */
    fun stopAlarmSound() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping media player: ${e.message}")
            }
        }
        mediaPlayer = null
        
        // 停止震动
        stopVibration()
        
        Log.d(TAG, "Stopped alarm sound")
    }
    
    /**
     * 设置系统音量为最大
     */
    private fun setSystemVolumeToMax() {
        try {
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                maxVolume,
                0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set system volume: ${e.message}")
        }
    }
    
    /**
     * 开始震动
     */
    private fun startVibration(level: VolumeLevel) {
        if (!vibrator.hasVibrator()) return
        
        val pattern = when (level) {
            VolumeLevel.NORMAL -> longArrayOf(0, 500, 500)
            VolumeLevel.LOUD -> longArrayOf(0, 300, 200)
            VolumeLevel.SUPER_LOUD -> longArrayOf(0, 200, 100)
        }
        
        val effect = VibrationEffect.createWaveform(pattern, 0)
        vibrator.vibrate(effect)
    }
    
    /**
     * 停止震动
     */
    private fun stopVibration() {
        vibrator.cancel()
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stopAlarmSound()
    }
}
