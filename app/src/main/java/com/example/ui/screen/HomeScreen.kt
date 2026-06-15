package com.example.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToWrite: (targetId: Int?, targetRelation: String) -> Unit,
    onNavigateToTimeline: () -> Unit,
    onPlayAudio: (String) -> Unit,
    isPlaying: Boolean,
    onStopAudio: () -> Unit
) {
    val allEntries by viewModel.allEntries.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val currentPrompt by viewModel.currentPrompt

    val recentEntries = remember(allEntries) {
        allEntries.take(3)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Title Welcomer
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "아빠의 기록장",
                    style = MaterialTheme.typography.headlineLarge,
                    color = PrimaryEspresso,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "가장 따스한 한마디를 마음과 음성으로 담습니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmDarkText.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 1. One-liner prompt Card (오늘 남길 한마디 카드)
        item {
            Card(
                onClick = {},
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✍️ 오늘의 생각",
                                style = MaterialTheme.typography.titleMedium,
                                color = PrimaryEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Refresh button
                        IconButton(
                            onClick = { viewModel.refreshPrompt() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "다른 생각 보기",
                                tint = PrimaryEspresso
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentPrompt,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                        color = WarmDarkText,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onNavigateToWrite(null, "가족 모두")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryEspresso
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "지금 기록하기",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. Family Quick Nav (가족별 기록 바로가기)
        item {
            Column {
                Text(
                    text = "👨‍👩‍👧‍👦 바로 기록 남기기",
                    style = MaterialTheme.typography.titleLarge,
                    color = WarmDarkText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickLinks = listOf(
                        Triple("아이", "아이에게", SlateNavy),
                        Triple("아내", "아내에게", SecondaryTerracotta),
                        Triple("나 자신", "삶의 기록", PrimaryEspresso)
                    )

                    quickLinks.forEach { (relationPattern, label, color) ->
                        // find first matching member or null
                        val matchingMember = allMembers.find { it.relation.contains(relationPattern) }

                        Card(
                            onClick = {
                                onNavigateToWrite(matchingMember?.id, matchingMember?.name ?: relationPattern)
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = color.copy(alpha = 0.08f),
                                contentColor = color
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            border = CardDefaults.outlinedCardBorder(enabled = true).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.3f))
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val icon = when (relationPattern) {
                                    "아이" -> Icons.Default.Face
                                    "아내" -> Icons.Default.Favorite
                                    else -> Icons.Default.Person
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(32.dp),
                                    tint = color
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Recent Records Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📜 최근 남긴 기록",
                    style = MaterialTheme.typography.titleLarge,
                    color = WarmDarkText,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onNavigateToTimeline) {
                    Text(
                        text = "전체보기 >",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryEspresso
                    )
                }
            }
        }

        // Recent records layout list
        if (recentEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 기록된 추억이 없습니다.\n첫 소중한 이야기를 남겨보세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WarmDarkText.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        } else {
            items(recentEntries, key = { it.id }) { entry ->
                val targetMember = allMembers.find { it.id == entry.targetMemberId }
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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Relation/Target tag + Date
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val tagBgColor = when (targetMember?.relation) {
                                        "아내" -> SecondaryTerracotta.copy(alpha = 0.15f)
                                        "나 자신" -> PrimaryEspresso.copy(alpha = 0.15f)
                                        else -> SlateNavy.copy(alpha = 0.15f)
                                    }
                                    val tagTextColor = when (targetMember?.relation) {
                                        "아내" -> SecondaryTerracotta
                                        "나 자신" -> PrimaryEspresso
                                        else -> SlateNavy
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(tagBgColor)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "To. $targetName",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = tagTextColor
                                        )
                                    }

                                    Text(
                                        text = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN).format(Date(entry.createdDate)),
                                        fontSize = 13.sp,
                                        color = WarmDarkText.copy(alpha = 0.5f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = entry.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmDarkText
                                )
                            }

                            // Emotion icon
                            val emotionIcon = when (entry.emotionTag) {
                                "고마움" -> "❤️"
                                "미안함" -> "🙌"
                                "기쁨" -> "😊"
                                "걱정" -> "🙏"
                                "응원" -> "🌟"
                                "추억" -> "🎞️"
                                else -> "✍️"
                            }
                            Text(
                                text = "$emotionIcon ${entry.emotionTag}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.04f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Body Text preview
                        Text(
                            text = entry.body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = WarmDarkText.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 24.sp
                        )

                        // Play Voice bar if exists
                        entry.audioFilePath?.let { audioPath ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryEspresso.copy(alpha = 0.05f))
                                    .clickable {
                                        if (isPlaying) onStopAudio() else onPlayAudio(audioPath)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = "음성 목소리 재생",
                                    tint = PrimaryEspresso,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPlaying) "음성 종료하기" else "아빠의 소중한 목소리 들어보기",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryEspresso
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
