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
 * 记忆任务 - 与iOS逻辑一致
 * - 固定3x3网格
 * - 3轮制，全部完成才算通过
 * - Easy=3格, Medium=5格, Hard=7格
 * - 错误后自动重置当前轮
 */
@Composable
fun MemoryMission(
    difficulty: Difficulty,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val gridSize = 3 // 固定3x3网格，与iOS一致
    val totalRounds = 3
    
    val tilesToMemorize = when (difficulty) {
        Difficulty.EASY -> 3
        Difficulty.MEDIUM -> 5
        Difficulty.HARD -> 7
    }
    
    var currentRound by remember { mutableIntStateOf(1) }
    var pattern by remember { mutableStateOf(generatePattern(gridSize, tilesToMemorize)) }
    var userPattern by remember { mutableStateOf(listOf<Int>()) }
    var gameState by remember { mutableStateOf(GameState.SHOWING) }
    var showSuccess by remember { mutableStateOf(false) }
    
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
                    text = stringResource(R.string.mission_memory),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Cyan
                )
                
                // 进度点 (3轮)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(totalRounds) { index ->
                        val dotColor = when {
                            index < currentRound - 1 -> Green
                            index == currentRound - 1 -> Color.Yellow
                            else -> Color.Gray.copy(alpha = 0.3f)
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 状态提示
            Text(
                text = when (gameState) {
                    GameState.SHOWING -> stringResource(R.string.remember_pattern)
                    GameState.RECALL -> stringResource(R.string.tap_remembered)
                    GameState.WAITING -> ""
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 网格
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (row in 0 until gridSize) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (col in 0 until gridSize) {
                                val index = row * gridSize + col
                                val isActive = when (gameState) {
                                    GameState.SHOWING -> pattern.contains(index)
                                    GameState.RECALL -> userPattern.contains(index)
                                    GameState.WAITING -> false
                                }
                                
                                val backgroundColor by animateColorAsState(
                                    targetValue = if (isActive) Color.Cyan else Color.Gray.copy(alpha = 0.3f),
                                    label = "tileColor"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(backgroundColor)
                                        .clickable(enabled = gameState == GameState.RECALL) {
                                            handleTileClick(
                                                index = index,
                                                pattern = pattern,
                                                userPattern = userPattern,
                                                onUserPatternChange = { userPattern = it },
                                                currentRound = currentRound,
                                                totalRounds = totalRounds,
                                                tilesToMemorize = tilesToMemorize,
                                                gridSize = gridSize,
                                                onRoundComplete = {
                                                    if (currentRound >= totalRounds) {
                                                        showSuccess = true
                                                    } else {
                                                        gameState = GameState.WAITING
                                                        currentRound++
                                                    }
                                                },
                                                onError = {
                                                    vibrate(context)
                                                    userPattern = emptyList()
                                                    gameState = GameState.WAITING
                                                },
                                                onGameStateChange = { gameState = it },
                                                onPatternChange = { pattern = it }
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 准备好了按钮 (只在SHOWING状态显示)
            if (gameState == GameState.SHOWING) {
                Button(
                    onClick = { gameState = GameState.RECALL },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ready),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // 等待状态后生成新图案
    LaunchedEffect(gameState) {
        if (gameState == GameState.WAITING) {
            delay(500)
            pattern = generatePattern(gridSize, tilesToMemorize)
            userPattern = emptyList()
            gameState = GameState.SHOWING
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
            text = { Text("全部完成！") },
            confirmButton = { }
        )
    }
}

private enum class GameState {
    WAITING,
    SHOWING,
    RECALL
}

private fun generatePattern(gridSize: Int, count: Int): List<Int> {
    return (0 until gridSize * gridSize).shuffled().take(count)
}

private fun handleTileClick(
    index: Int,
    pattern: List<Int>,
    userPattern: List<Int>,
    onUserPatternChange: (List<Int>) -> Unit,
    currentRound: Int,
    totalRounds: Int,
    tilesToMemorize: Int,
    gridSize: Int,
    onRoundComplete: () -> Unit,
    onError: () -> Unit,
    onGameStateChange: (GameState) -> Unit,
    onPatternChange: (List<Int>) -> Unit
) {
    if (pattern.contains(index)) {
        // 正确
        if (!userPattern.contains(index)) {
            val newPattern = userPattern + index
            onUserPatternChange(newPattern)
            
            // 检查是否完成当前轮
            if (newPattern.size == pattern.size) {
                onRoundComplete()
            }
        }
    } else {
        // 错误 - 自动重置当前轮
        onError()
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
