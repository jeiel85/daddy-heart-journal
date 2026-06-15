package com.example.ui.screen

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.FamilyMember
import com.example.ui.theme.PrimaryEspresso
import com.example.ui.theme.SecondaryTerracotta
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.WarmDarkText
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    viewModel: MainViewModel,
    initialTargetMemberId: Int?,
    initialTargetRelationName: String?,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val allMembers by viewModel.allMembers.collectAsState()
    val isRecordingNow by viewModel.isRecordingNow.collectAsState()
    val isPlayingNow by viewModel.isPlayingNow.collectAsState()
    val recordingTimerSeconds by viewModel.recordingTimerSeconds.collectAsState()
    val currentAudioPath by viewModel.currentRecordAudioPath.collectAsState()

    // Form states
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedTargetId by remember { mutableStateOf<Int?>(initialTargetMemberId) }
    var selectedEmotion by remember { mutableStateOf("고마움") }

    // Time Capsule states
    var isTimeCapsule by remember { mutableStateOf(false) }
    var openDateCalendar by remember { 
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.YEAR, 1) }) 
    }

    // Assign fallback selected target id if list loaded
    LaunchedEffect(allMembers) {
        if (selectedTargetId == null && allMembers.isNotEmpty()) {
            val idx = allMembers.find { it.relation.contains(initialTargetRelationName ?: "무관") }?.id
            if (idx != null) {
                selectedTargetId = idx
            } else if (initialTargetMemberId == null) {
                selectedTargetId = allMembers.firstOrNull()?.id
            }
        }
    }

    // Runtime Permission for microphone request
    var recordAudioPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        recordAudioPermissionGranted = isGranted
        if (isGranted) {
            viewModel.startVoiceRecording()
        } else {
            Log.e("WriteScreen", "Audio record permission denied")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "📝 오늘의 조각 남기기",
            style = MaterialTheme.typography.headlineLarge,
            color = PrimaryEspresso,
            fontWeight = FontWeight.Bold
        )

        // 1. Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("제목 (예: 우리 딸의 입학식을 축하하며)", fontSize = 16.sp) },
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryEspresso,
                focusedLabelColor = PrimaryEspresso
            )
        )

        // 2. Target Selection (대상 선택)
        Column {
            Text(
                text = "👨 누구에게 남기는 기록인가요?",
                style = MaterialTheme.typography.titleMedium,
                color = WarmDarkText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allMembers) { member ->
                    val isSelected = selectedTargetId == member.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTargetId = member.id },
                        label = {
                            Text(
                                text = "To. ${member.name} (${member.relation})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryEspresso,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 3. Emotion Tag Selection (감정 태그)
        Column {
            Text(
                text = "💝 이 순간 아빠의 마음은 어떠한가요?",
                style = MaterialTheme.typography.titleMedium,
                color = WarmDarkText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val emotions = listOf(
                "고마움" to "❤️",
                "미안함" to "🙌",
                "기쁨" to "😊",
                "걱정" to "🙏",
                "응원" to "🌟",
                "추억" to "🎞️"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(emotions) { (emotion, emoji) ->
                    val isSelected = selectedEmotion == emotion
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedEmotion = emotion },
                        label = {
                            Text(
                                text = "$emoji $emotion",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryTerracotta,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 4. Body Content Input
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("속에 담아두었던 따스한 이야기를 가득 적어주세요.", fontSize = 16.sp) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryEspresso,
                focusedLabelColor = PrimaryEspresso
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
        )

        // 5. Audio Recorder widget block (음성 녹음기 통합)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryEspresso.copy(alpha = 0.05f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "음성 목소리 녹음",
                    tint = PrimaryEspresso,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎙️ 아빠의 음성 메시지 남기기 (선택)",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryEspresso,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer display if recording or saved
            if (isRecordingNow) {
                val minState = recordingTimerSeconds / 60
                val secState = recordingTimerSeconds % 60
                Text(
                    text = String.format(Locale.US, "🔴 녹음 중... %02d:%02d", minState, secState),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else if (currentAudioPath != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "녹음 완료",
                        tint = SlateNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "목소리가 따뜻하게 담겼습니다",
                        color = SlateNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            } else {
                Text(
                    text = "녹음 버튼을 눌러 아빠의 진짜 목소리를 남겨주세요",
                    color = WarmDarkText.copy(alpha = 0.6f),
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Record buttons trigger
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isRecordingNow && currentAudioPath == null) {
                    // Start record button (large and red/clay)
                    Button(
                        onClick = {
                            if (recordAudioPermissionGranted) {
                                viewModel.startVoiceRecording()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "녹음 시작")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("녹음 시작", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isRecordingNow) {
                    // Stop Recording button
                    Button(
                        onClick = { viewModel.stopVoiceRecording() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEspresso),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "녹음 완료")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("녹음 완료", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (currentAudioPath != null) {
                    // We have recorded audio -> Play / Delete triggers
                    Button(
                        onClick = {
                            if (isPlayingNow) {
                                viewModel.stopAudioPlayback()
                            } else {
                                viewModel.playAudio(currentAudioPath!!)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateNavy),
                        modifier = Modifier
                            .height(50.dp)
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isPlayingNow) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "재생"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlayingNow) "듣기 종료" else "내가 녹음한 말 듣기",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { viewModel.deleteTempVoice() },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTerracotta),
                        modifier = Modifier
                            .height(50.dp)
                            .weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "다시 녹음")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("삭제/다시 녹음", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Time Capsule layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SecondaryTerracotta.copy(alpha = 0.04f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔒 타임캡슐로 묻어두기",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecondaryTerracotta,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "지정한 기념일이나 특정 날짜가 되기 전까지 보지 못하도록 잠급니다.",
                        fontSize = 14.sp,
                        color = WarmDarkText.copy(alpha = 0.7f),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Switch(
                    checked = isTimeCapsule,
                    onCheckedChange = { isTimeCapsule = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SecondaryTerracotta,
                        checkedTrackColor = SecondaryTerracotta.copy(alpha = 0.4f)
                    )
                )
            }

            AnimatedVisibility(visible = isTimeCapsule) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "🔓 언제 이 기록을 개봉할까요?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = WarmDarkText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN)
                    val formattedDate = dateFormat.format(openDateCalendar.time)

                    Button(
                        onClick = {
                            val dialog = DatePickerDialog(
                                context,
                                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                    val selected = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    openDateCalendar = selected
                                },
                                openDateCalendar.get(Calendar.YEAR),
                                openDateCalendar.get(Calendar.MONTH),
                                openDateCalendar.get(Calendar.DAY_OF_MONTH)
                            )
                            dialog.datePicker.minDate = System.currentTimeMillis() // open in the future
                            dialog.show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "지정된 날짜: $formattedDate (목표)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryEspresso
                            )
                            Icon(Icons.Default.DateRange, contentDescription = "날짜 선택", tint = PrimaryEspresso)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "결혼기념일 (1년)" to Calendar.getInstance().apply { add(Calendar.YEAR, 1) },
                            "아이 생일 (6개월)" to Calendar.getInstance().apply { add(Calendar.MONTH, 6) },
                            "100일 뒤" to Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 100) }
                        ).forEach { (label, cal) ->
                            Button(
                                onClick = { openDateCalendar = cal },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.05f)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = label, fontSize = 12.sp, color = WarmDarkText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save entry button
        Button(
            onClick = {
                if (title.isBlank() && body.isBlank() && currentAudioPath == null) {
                    // Empty entries alert
                    return@Button
                }
                viewModel.saveEntry(
                    title = title,
                    body = body,
                    targetMemberId = selectedTargetId,
                    emotionTag = selectedEmotion,
                    audioPath = currentAudioPath,
                    isTimeCapsule = isTimeCapsule,
                    openDate = if (isTimeCapsule) openDateCalendar.timeInMillis else System.currentTimeMillis()
                )
                onSaveSuccess()
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEspresso),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = "저장하기", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("소중한 기록 마음속에 저장하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
