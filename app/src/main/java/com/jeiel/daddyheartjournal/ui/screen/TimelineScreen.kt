package com.jeiel.daddyheartjournal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.jeiel.daddyheartjournal.data.model.FamilyMember
import com.jeiel.daddyheartjournal.data.model.MemoryEntry
import com.jeiel.daddyheartjournal.ui.theme.PrimaryEspresso
import com.jeiel.daddyheartjournal.ui.theme.SecondaryTerracotta
import com.jeiel.daddyheartjournal.ui.theme.SlateNavy
import com.jeiel.daddyheartjournal.ui.theme.WarmDarkText
import com.jeiel.daddyheartjournal.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: MainViewModel,
    onPlayAudio: (String) -> Unit,
    isPlaying: Boolean,
    onStopAudio: () -> Unit
) {
    val allMembers by viewModel.allMembers.collectAsState()
    val filteredEntries by viewModel.filteredEntries.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTargetId by viewModel.selectedTargetId.collectAsState()
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()

    var showDeleteDialogForEntry by remember { mutableStateOf<MemoryEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "📁 아빠의 역사 보관소",
            style = MaterialTheme.typography.headlineLarge,
            color = PrimaryEspresso,
            fontWeight = FontWeight.Bold
        )

        // 1. Live Text Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("기록한 내용이나 제목 검색", fontSize = 16.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색", tint = PrimaryEspresso) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "지우기", tint = PrimaryEspresso)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryEspresso,
                unfocusedBorderColor = PrimaryEspresso.copy(alpha = 0.3f)
            ),
            singleLine = true
        )

        // 2. Target Filters row (가족 대상 필터)
        Column {
            Text(
                text = "👨 대상별 필터",
                style = MaterialTheme.typography.titleMedium,
                color = WarmDarkText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // "전체" option
                item {
                    FilterChip(
                        selected = selectedTargetId == null,
                        onClick = { viewModel.selectTargetFilter(null) },
                        label = { Text("가족 전체", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryEspresso,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                items(allMembers) { member ->
                    val isSelected = selectedTargetId == member.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectTargetFilter(member.id) },
                        label = { Text(member.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryEspresso,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 3. Emotion Filters row (감정 필터)
        Column {
            Text(
                text = "💝 감정 태그별 필터",
                style = MaterialTheme.typography.titleMedium,
                color = WarmDarkText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            val emotionsList = listOf("고마움", "미안함", "기쁨", "걱정", "응원", "추억")

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedEmotion == null,
                        onClick = { viewModel.selectEmotionFilter(null) },
                        label = { Text("전체 마음", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryTerracotta,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                items(emotionsList) { emotion ->
                    val isSelected = selectedEmotion == emotion
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectEmotionFilter(emotion) },
                        label = { Text(emotion, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryTerracotta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Divider(color = PrimaryEspresso.copy(alpha = 0.1f))

        // 4. Entries list
        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "기록 없음",
                        tint = PrimaryEspresso.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "해당하는 필터조건의 기록을 찾지 못했습니다.\n다양한 마음을 다시 검색해보세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WarmDarkText.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
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
                                    // Target recipient label
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

                                // Emotion tag chip preview
                                val emotionIcon = when (entry.emotionTag) {
                                    "고마움" -> "❤️"
                                    "미안함" -> "🙌"
                                    "기쁨" -> "😊"
                                    "걱정" -> "🙏"
                                    "응원" -> "🌟"
                                    "추억" -> "🎞️"
                                    else -> "✍"
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

                            // Main body text
                            Text(
                                text = entry.body,
                                style = MaterialTheme.typography.bodyLarge,
                                color = WarmDarkText.copy(alpha = 0.8f),
                                lineHeight = 26.sp
                            )

                            // Play Voice recorder bar if exists
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
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                        contentDescription = "음성 재생",
                                        tint = PrimaryEspresso,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPlaying) "음성 듣기 종료" else "아빠의 목소리로 채워진 대답 듣기",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryEspresso
                                    )
                                }
                            }

                            // Time capsule padlock indication if closed
                            if (entry.isTimeCapsule) {
                                val isLocked = entry.openDate > System.currentTimeMillis()
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isLocked) SecondaryTerracotta.copy(alpha = 0.1f) else Color.Green.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                                        contentDescription = "잠금 상황",
                                        tint = if (isLocked) SecondaryTerracotta else PrimaryEspresso,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val dateStr = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(Date(entry.openDate))
                                    Text(
                                        text = if (isLocked) "캡슐 잠김 (개봉일: $dateStr)" else "캡슐 잠금 해제됨 !",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLocked) SecondaryTerracotta else PrimaryEspresso
                                    )
                                }
                            }

                            // Delete button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(
                                    onClick = { showDeleteDialogForEntry = entry },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "기록 영구 삭제",
                                        tint = ErrorSienna
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialogForEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { showDeleteDialogForEntry = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEntry(entry)
                        showDeleteDialogForEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorSienna)
                ) {
                    Text("네, 삭제합니다", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogForEntry = null }) {
                    Text("취소", color = WarmDarkText, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("기록 지우기", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("이 소중한 이야기를 영구히 삭제하시겠습니까?\n삭제된 목소리와 글은 복구할 수 없습니다.", fontSize = 16.sp)
            }
        )
    }
}

