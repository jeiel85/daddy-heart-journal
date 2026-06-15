package com.jeiel.daddyheartjournal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel.daddyheartjournal.data.model.FamilyMember
import com.jeiel.daddyheartjournal.ui.theme.PrimaryEspresso
import com.jeiel.daddyheartjournal.ui.theme.SecondaryTerracotta
import com.jeiel.daddyheartjournal.ui.theme.SlateNavy
import com.jeiel.daddyheartjournal.ui.theme.WarmDarkText
import com.jeiel.daddyheartjournal.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val scrollState = rememberScrollState()

    val allMembers by viewModel.allMembers.collectAsState()
    val lockSetting by viewModel.lockSetting.collectAsState()

    var tempMembersList by remember { mutableStateOf<List<FamilyMember>>(emptyList()) }
    var loadedMembers by remember { mutableStateOf(false) }

    // PIN state
    var pinEnabled by remember { mutableStateOf(false) }
    var pinCode by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf("") }

    // Load initial states once Room flow emits
    LaunchedEffect(allMembers) {
        if (allMembers.isNotEmpty() && !loadedMembers) {
            tempMembersList = allMembers
            loadedMembers = true
        }
    }

    LaunchedEffect(lockSetting) {
        lockSetting?.let {
            pinEnabled = it.isEnabled
            pinCode = it.pinHash
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "⚙️ 설정 및 수첩 관리",
            style = MaterialTheme.typography.headlineLarge,
            color = PrimaryEspresso,
            fontWeight = FontWeight.Bold
        )

        // Privacy First Prominent Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = PrimaryEspresso.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "사생활 안전 보장",
                    tint = PrimaryEspresso,
                    modifier = Modifier.size(28.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "🔒 기기 내부 데이터 100% 보관 안내",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryEspresso,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "아빠의 기록장은 개인정보 보호를 위해 어떠한 클라우드 및 서버 전송 기능을 탑재하고 있지 않습니다. 남겨진 글과 목소리는 오직 이 단말기 기기 내에만 안전하게 암호화 보관됩니다.",
                        fontSize = 15.sp,
                        color = WarmDarkText.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // 1. Family Member Configuration Name
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = "가족", tint = PrimaryEspresso)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "가족 구성원 이름 조율하기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmDarkText
                    )
                }
                Text(
                    text = "기록의 대상이 되는 가족들의 진짜 별명이나 이름을 적어주세요.",
                    fontSize = 14.sp,
                    color = WarmDarkText.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Editable layout for each member
                tempMembersList.forEachIndexed { index, member ->
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Text(
                            text = "대상 관계: ${member.relation}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateNavy,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = member.name,
                            onValueChange = { newName ->
                                tempMembersList = tempMembersList.mapIndexed { idx, current ->
                                    if (idx == index) current.copy(name = newName) else current
                                }
                            },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("이름 또는 호칭") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEspresso,
                                unfocusedBorderColor = PrimaryEspresso.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.updateFamilyMemberNames(tempMembersList)
                        pinMessage = "가족 이름들이 안전하게 변경 완료되었습니다."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEspresso),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "저장")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("가족 이름들 일괄 적용하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Passcode PIN options
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = "잠금", tint = SecondaryTerracotta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "앱 잠금 및 사생활 잠금 (PIN)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmDarkText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "앱 비밀번호(4자리) 기능 사용하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmDarkText
                    )
                    Switch(
                        checked = pinEnabled,
                        onCheckedChange = {
                            pinEnabled = it
                            if (!it) {
                                pinCode = ""
                                viewModel.setPinLock(false, "")
                                pinMessage = "보호 잠금 비밀번호 기능이 비활성화되었습니다."
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SecondaryTerracotta,
                            checkedTrackColor = SecondaryTerracotta.copy(alpha = 0.4f)
                        )
                    )
                }

                if (pinEnabled) {
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { newVal ->
                            // restrict to digits & 4 characters
                            if (newVal.length <= 4 && newVal.all { it.isDigit() }) {
                                pinCode = newVal
                            }
                        },
                        label = { Text("비밀번호 4자리 숫자") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryTerracotta,
                            unfocusedBorderColor = SecondaryTerracotta.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (pinCode.length == 4) {
                                viewModel.setPinLock(true, pinCode)
                                pinMessage = "보호 비밀번호가 '$pinCode'로 정상 등록 및 갱신되었습니다."
                            } else {
                                pinMessage = "비밀번호는 반드시 숫자 4자리여야 합니다."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTerracotta),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("비밀번호 설정 완료하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (pinMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = pinMessage,
                        color = SlateNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Document Exports / Local backup
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = "공유", tint = SlateNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "기록 백업 및 소장하기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmDarkText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "기기 오작동에 대비해 모든 일기와 글귀를 하나의 기기 내부 폴더에 백업하거나, 종이 책 제작용 텍스트 형태로 안전하게 복사할 수 있습니다.",
                    fontSize = 15.sp,
                    color = WarmDarkText.copy(alpha = 0.7f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        pinMessage = "아빠의 전체 소중한 글들이 기기 클립보드에 무사히 백업되었습니다 !"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateNavy),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("전체 텍스트 백업본 복사하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Brand version details
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "아빠의 기록장 · v1.0.0 (출시 후보 MVP)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WarmDarkText.copy(alpha = 0.4f)
            )
            Text(
                text = "세상 모든 아빠의 사랑과 역사를 응원합니다.",
                fontSize = 12.sp,
                color = WarmDarkText.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

