package com.wakeup.clock.ui.missions

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.clock.R
import com.wakeup.clock.data.model.Difficulty
import com.wakeup.clock.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 顺序任务 - 与iOS逻辑一致
 * - Easy: 9个数字, 3列
 * - Medium: 12个数字, 3列
 * - Hard: 16个数字, 4列
 * - 错误后0.5秒自动重置
 */
@Composable
fun OrderMission(
    difficulty: Difficulty,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    
    val config = remember(difficulty) {
        when (difficulty) {
            Difficulty.EASY -> OrderConfig(count = 9, cols = 3)
            Difficulty.MEDIUM -> OrderConfig(count = 12, cols = 3)
            Difficulty.HARD -> OrderConfig(count = 16, cols = 4)
        }
    }
    
    var numbers by remember { mutableStateOf((1..config.count).shuffled()) }
    var nextNumber by remember { mutableIntStateOf(1) }
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // 错误后自动重置
    LaunchedEffect(showError) {
        if (showError) {
            delay(500)
            nextNumber = 1
            numbers = (1..config.count).shuffled()
            showError = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题和进度
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.mission_order),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Orange
                )
                
                Text(
                    text = "${nextNumber - 1} / ${config.count}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 提示
            Text(
                text = "请按顺序点击数字 (1-${config.count})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (showError) Red else Color.White
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 数字网格
            val rows = (config.count + config.cols - 1) / config.cols
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                for (row in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (col in 0 until config.cols) {
                            val index = row * config.cols + col
                            if (index < numbers.size) {
                                val number = numbers[index]
                                val isClicked = number < nextNumber
                                
                                DialButton(
                                    number = number,
                                    isClicked = isClicked,
                                    enabled = !isClicked && !showError,
                                    onClick = {
                                        if (number == nextNumber) {
                                            nextNumber++
                                            if (nextNumber > config.count) {
                                                showSuccess = true
                                            }
                                        } else {
                                            // 错误
                                            vibrate(context)
                                            showError = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 错误提示
            if (showError) {
                Text(
                    text = "顺序错误，重置!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Red
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // 成功
    if (showSuccess) {
        LaunchedEffect(Unit) {
            delay(500)
            onComplete()
        }
        
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.mission_complete)) },
            text = { Text("顺序正确！") },
            confirmButton = { }
        )
    }
}

private data class OrderConfig(val count: Int, val cols: Int)

@Composable
private fun DialButton(
    number: Int,
    isClicked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isClicked) Green else Color(0xFFF5F5F5),
        label = "dialColor"
    )
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                brush = if (isClicked) {
                    Brush.linearGradient(
                        colors = listOf(Green.copy(alpha = 0.7f), Green.copy(alpha = 0.9f))
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0))
                    )
                }
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isClicked) Color.White else Color(0xFF333333)
        )
    }
}

private fun vibrate(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(100)
    }
}
