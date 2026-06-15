package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.model.FamilyMember
import com.example.data.model.MemoryEntry
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SecondaryTerracotta
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.WarmDarkText
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCapsuleScreen(
    viewModel: MainViewModel,
    onPlayAudio: (String) -> Unit,
    isPlaying: Boolean,
    onStopAudio: () -> Unit
) {
    val timeCapsules by viewModel.timeCapsules.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()

    val currentTime = remember { System.currentTimeMillis() }

    val (lockedCapsules, unlockedCapsules) = remember(timeCapsules, currentTime) {
        timeCapsules.partition { it.openDate > currentTime }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "⏳ 진심이 개봉되는 타임캡슐",
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryEspresso,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "특별한 생일이나 결혼기념일, 미래의 특정 기일에 아빠의 이야기를 볼 수 있도록 설정해두세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmDarkText.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp),
                lineHeight = 22.sp
            )
        }

        // Notification guide block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateNavy.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "알림 설정 가이드",
                        tint = SlateNavy,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "💡 타임캡슐 알람",
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateNavy,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "설정된 날짜가 도래하면 캡슐 아이콘 옆 잠금이 해제되며 알람 배너가 제공됩니다.",
                            fontSize = 14.sp,
                            color = WarmDarkText.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 1. Unlocked Capsules Section
        item {
            Text(
                text = "🔓 지금 개봉된 편지 (${unlockedCapsules.size})",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryEspresso,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (unlockedCapsules.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "시간이 흘러 잠금 해제된 먼 훗날의 편지가 아직 없습니다.",
                        fontSize = 15.sp,
                        color = WarmDarkText.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(unlockedCapsules, key = { it.id }) { capsule ->
                val targetMember = allMembers.find { it.id == capsule.targetMemberId }
                val targetName = targetMember?.name ?: "가족 모두"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "To. $targetName",
                                fontWeight = FontWeight.Bold,
                                color = SlateNavy,
                                fontSize = 16.sp
                            )
                            val openDateStr = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(Date(capsule.openDate))
                            Text(
                                text = "$openDateStr 개봉 완료됨 🎉",
                                fontSize = 13.sp,
                                color = PrimaryEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = capsule.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarmDarkText
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = capsule.body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = WarmDarkText.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )

                        capsule.audioFilePath?.let { audioPath ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { if (isPlaying) onStopAudio() else onPlayAudio(audioPath) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEspresso),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "재생")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isPlaying) "음성 중지" else "해제된 목소리 고스란히 들어보기", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Locked Capsules Section
        item {
            Text(
                text = "🔒 비밀스럽게 잠긴 타임캡슐 (${lockedCapsules.size})",
                style = MaterialTheme.typography.titleLarge,
                color = SecondaryTerracotta,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        if (lockedCapsules.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 잠가놓은 타임캡슐 편지가 없습니다.",
                        fontSize = 15.sp,
                        color = WarmDarkText.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(lockedCapsules, key = { it.id }) { capsule ->
                val targetMember = allMembers.find { it.id == capsule.targetMemberId }
                val targetName = targetMember?.name ?: "가족 모두"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.03f)),
                    border = CardDefaults.outlinedCardBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SecondaryTerracotta.copy(alpha = 0.2f))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "To. $targetName 에게 바치는 약속",
                                fontWeight = FontWeight.Bold,
                                color = SecondaryTerracotta,
                                fontSize = 16.sp
                            )

                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "잠겨있음",
                                tint = SecondaryTerracotta,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🔒 ${capsule.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmDarkText.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Soft hint/mask body
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "“지정된 기일이 도래해야 개봉 가능합니다.”\n가장 설레는 순간에 아빠의 진심을 확인할 수 있습니다.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryTerracotta.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val openDateStr = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(Date(capsule.openDate))
                        Text(
                            text = "🔓 개봉 예정일: $openDateStr",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTerracotta,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
