package com.jeiel.daddyheartjournal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel.daddyheartjournal.ui.theme.PrimaryEspresso
import com.jeiel.daddyheartjournal.ui.theme.SoftCreamBg
import com.jeiel.daddyheartjournal.ui.theme.WarmDarkText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLockScreen(
    onUnlockSuccess: () -> Unit,
    correctPin: String
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCreamBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "비밀번호 잠금",
            tint = PrimaryEspresso,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "아빠의 기록장",
            style = MaterialTheme.typography.headlineLarge,
            color = WarmDarkText,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "소중한 가족의 추억 보호를 위해\n비밀번호 4자리를 입력해주세요.",
            style = MaterialTheme.typography.bodyLarge,
            color = WarmDarkText.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // PIN dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            for (i in 0 until 4) {
                val active = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (active) PrimaryEspresso else PrimaryEspresso.copy(alpha = 0.2f)
                        )
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Custom Numeric Keyboard for 40s-50s dads (large buttons!)
        val numColumns = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("지우기", "0", "")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            numColumns.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    row.forEach { char ->
                        if (char.isEmpty()) {
                            // Empty space spacer to keep layout grid aligned
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1.2f))
                        } else {
                            Button(
                                onClick = {
                                    if (char == "지우기") {
                                        if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                            errorMessage = ""
                                        }
                                    } else {
                                        if (enteredPin.length < 4) {
                                            enteredPin += char
                                            errorMessage = ""
                                            if (enteredPin.length == 4) {
                                                if (enteredPin == correctPin) {
                                                    onUnlockSuccess()
                                                } else {
                                                    errorMessage = "비밀번호가 일치하지 않습니다. 다시 시도해주세요."
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (char == "지우기") Color.Transparent else MaterialTheme.colorScheme.surface,
                                    contentColor = if (char == "지우기") ErrorSienna else WarmDarkText
                                ),
                                elevation = if (char == "지우기") null else ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.3f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = char,
                                    fontSize = if (char == "지우기") 16.sp else 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

val ErrorSienna = Color(0xFFBC4749)

