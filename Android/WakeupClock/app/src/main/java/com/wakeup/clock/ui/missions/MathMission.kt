package com.wakeup.clock.ui.missions

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.clock.R
import com.wakeup.clock.data.model.Difficulty
import com.wakeup.clock.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 数学任务 - 与iOS逻辑一致
 * Easy: 1道加法题
 * Medium: 3道乘加题
 * Hard: 5道复杂题
 */
@Composable
fun MathMission(
    difficulty: Difficulty,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    
    val config = remember(difficulty) {
        when (difficulty) {
            Difficulty.EASY -> MissionConfig(questions = 1, range = 20, operation = MathOperation.ADD)
            Difficulty.MEDIUM -> MissionConfig(questions = 3, range = 15, operation = MathOperation.MULTIPLY_ADD)
            Difficulty.HARD -> MissionConfig(questions = 5, range = 50, operation = MathOperation.COMPLEX)
        }
    }
    
    var problems by remember { mutableStateOf(generateProblems(config)) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var solvedCount by remember { mutableIntStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    val currentProblem = problems.getOrNull(currentIndex)
    
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
                    text = stringResource(R.string.mission_math),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Purple500
                )
                
                // 进度点
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(config.questions) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (index < solvedCount) Green else Color.Gray.copy(alpha = 0.3f))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 题目区域
            if (currentProblem != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isError) Red.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 题目 - 单行显示，自动调整字体大小
                        Text(
                            text = currentProblem.text,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 换题按钮
                        TextButton(onClick = {
                            problems = problems.toMutableList().apply {
                                set(currentIndex, generateProblem(config))
                            }
                            userInput = ""
                            isError = false
                        }) {
                            Text(
                                text = "换一题",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 输入显示
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Gray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = userInput.ifEmpty { "0" },
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple500,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        
                        // 错误提示
                        if (isError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.mission_failed),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Red
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 数字键盘
            NumberPad(
                onNumberClick = { num ->
                    if (userInput.length < 5) {
                        userInput += num.toString()
                        isError = false
                    }
                },
                onClear = {
                    userInput = ""
                    isError = false
                },
                onConfirm = {
                    if (currentProblem != null) {
                        val answer = userInput.toIntOrNull()
                        if (answer == currentProblem.answer) {
                            solvedCount++
                            if (solvedCount >= config.questions) {
                                showSuccess = true
                            } else {
                                currentIndex++
                                userInput = ""
                                isError = false
                            }
                        } else {
                            isError = true
                            userInput = ""
                            vibrate(context)
                        }
                    }
                }
            )
            
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
            text = { Text("全部答对！") },
            confirmButton = { }
        )
    }
}

@Composable
private fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onClear: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1-9
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (col in 1..3) {
                    val num = row * 3 + col
                    NumberButton(
                        num = num,
                        onClick = { onNumberClick(num) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // 清除、0、确认
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 清除
            Button(
                onClick = onClear,
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("清除", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Red)
            }
            
            // 0
            NumberButton(num = 0, onClick = { onNumberClick(0) }, modifier = Modifier.weight(1f))
            
            // 确认
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("确认", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NumberButton(
    num: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = num.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

private data class MathProblem(val text: String, val answer: Int)

private enum class MathOperation {
    ADD,
    MULTIPLY_ADD,
    COMPLEX
}

private data class MissionConfig(
    val questions: Int,
    val range: Int,
    val operation: MathOperation
)

private fun generateProblems(config: MissionConfig): List<MathProblem> {
    return (0 until config.questions).map { generateProblem(config) }
}

private fun generateProblem(config: MissionConfig): MathProblem {
    val a = Random.nextInt(2, config.range + 1)
    val b = Random.nextInt(2, config.range + 1)
    val c = Random.nextInt(1, 11)
    
    return when (config.operation) {
        MathOperation.ADD -> MathProblem("$a + $b = ?", a + b)
        MathOperation.MULTIPLY_ADD -> MathProblem("$a × $b + $c = ?", a * b + c)
        MathOperation.COMPLEX -> MathProblem("($a + $b) × $c = ?", (a + b) * c)
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
