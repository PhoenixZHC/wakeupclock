package com.wakeup.clock.ui.missions

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.clock.R
import com.wakeup.clock.data.model.Difficulty
import com.wakeup.clock.ui.theme.*
import kotlinx.coroutines.delay

/**
 * 打字任务 - 与iOS逻辑一致
 * - 实时校验输入前缀
 * - 输入错误时立即显示错误状态
 * - 使用与iOS相同的短语
 */
@Composable
fun TypingMission(
    difficulty: Difficulty,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val targetText = remember { generateRandomText() }
    var userInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 标题
            Text(
                text = stringResource(R.string.mission_typing),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 提示
            Text(
                text = "请输入上方显示的文字",
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            // 目标文本
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = targetText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
            
            // 输入框
            OutlinedTextField(
                value = userInput,
                onValueChange = { newValue ->
                    userInput = newValue
                    // 实时校验：检查输入是否是目标文本的前缀
                    if (newValue.isNotEmpty() && !targetText.lowercase().startsWith(newValue.lowercase())) {
                        showError = true
                        vibrate(context)
                    } else {
                        showError = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("在此输入...", color = Color.Gray) },
                isError = showError,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        checkComplete(
                            userInput = userInput,
                            targetText = targetText,
                            onSuccess = { showSuccess = true },
                            onError = {
                                showError = true
                                vibrate(context)
                            }
                        )
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple500,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Purple500,
                    errorBorderColor = Red
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
            
            // 错误提示
            if (showError) {
                Text(
                    text = "输入错误，请重新检查！",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Red
                )
            }
            
            // 确认按钮
            Button(
                onClick = {
                    checkComplete(
                        userInput = userInput,
                        targetText = targetText,
                        onSuccess = { showSuccess = true },
                        onError = {
                            showError = true
                            vibrate(context)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)), // Pink
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "确认",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
            text = { Text("输入正确！") },
            confirmButton = { }
        )
    }
}

private fun checkComplete(
    userInput: String,
    targetText: String,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    if (userInput.trim().equals(targetText, ignoreCase = true)) {
        onSuccess()
    } else {
        onError()
    }
}

/**
 * 生成随机文本 - 与iOS使用相同的短语
 */
private fun generateRandomText(): String {
    val phrases = listOf(
        "早起的鸟儿有虫吃",
        "新的一天开始了",
        "加油，你可以的",
        "Wake up and shine",
        "Good morning sunshine",
        "Time to start your day"
    )
    return phrases.random()
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
