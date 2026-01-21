package com.wakeup.clock.ui.missions

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.clock.R
import com.wakeup.clock.data.model.Difficulty
import com.wakeup.clock.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 点击任务 - 与iOS逻辑一致
 * Easy: 15次
 * Medium: 25次
 * Hard: 40次
 */
@Composable
fun ShakeMission(
    difficulty: Difficulty,
    onComplete: () -> Unit
) {
    val targetCount = remember(difficulty) {
        when (difficulty) {
            Difficulty.EASY -> 15
            Difficulty.MEDIUM -> 25
            Difficulty.HARD -> 40
        }
    }
    
    var tapCount by remember { mutableIntStateOf(0) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // 按钮缩放动画
    val scale by animateFloatAsState(
        targetValue = if (tapCount % 2 == 0) 1f else 0.95f,
        animationSpec = tween(100),
        label = "buttonScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 标题
            Text(
                text = "疯狂点击!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Orange
            )
            
            // 提示
            Text(
                text = "连续点击按钮 $targetCount 次即可关闭闹钟",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 点击按钮
            Button(
                onClick = {
                    tapCount++
                    if (tapCount >= targetCount) {
                        showSuccess = true
                    }
                },
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Red, Orange)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "点我!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 剩余次数
            Text(
                text = "还剩 ${targetCount - tapCount} 次",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Orange
            )
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
            text = { Text("点击完成！") },
            confirmButton = { }
        )
    }
}
