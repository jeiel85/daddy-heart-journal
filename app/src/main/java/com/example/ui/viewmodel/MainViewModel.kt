package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.PapaApplication
import com.example.data.model.AppLockSetting
import com.example.data.model.FamilyMember
import com.example.data.model.MemoryEntry
import com.example.data.repository.AppRepository
import com.example.util.VoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository = (application as PapaApplication).repository
    private val voiceManager = VoiceManager(application)

    // Room flows
    val allMembers: StateFlow<List<FamilyMember>> = repository.allMembersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEntries: StateFlow<List<MemoryEntry>> = repository.allEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeCapsules: StateFlow<List<MemoryEntry>> = repository.timeCapsulesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lockSetting: StateFlow<AppLockSetting?> = repository.lockSettingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI state for unlock verification
    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun checkLockRequirement() {
        viewModelScope.launch {
            val setting = repository.getLockSetting()
            if (setting != null && setting.isEnabled) {
                _isUnlocked.value = false
            } else {
                _isUnlocked.value = true
            }
        }
    }

    fun verifyPin(pin: String): Boolean {
        val setting = lockSetting.value
        return if (setting != null && setting.isEnabled) {
            val matches = setting.pinHash == pin
            if (matches) {
                _isUnlocked.value = true
            }
            matches
        } else {
            _isUnlocked.value = true
            true
        }
    }

    fun setPinLock(enabled: Boolean, pin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLockSetting(AppLockSetting(id = 1, isEnabled = enabled, pinHash = pin))
        }
    }

    // Dynamic Filter state for Timeline search & filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTargetId = MutableStateFlow<Int?>(null)
    val selectedTargetId = _selectedTargetId.asStateFlow()

    private val _selectedEmotion = MutableStateFlow<String?>(null)
    val selectedEmotion = _selectedEmotion.asStateFlow()

    val filteredEntries: StateFlow<List<MemoryEntry>> = combine(
        allEntries, _searchQuery, _selectedTargetId, _selectedEmotion
    ) { entries, query, targetId, emotion ->
        entries.filter { entry ->
            val matchesQuery = query.isEmpty() || entry.title.contains(query, ignoreCase = true) || entry.body.contains(query, ignoreCase = true)
            val matchesTarget = targetId == null || entry.targetMemberId == targetId
            val matchesEmotion = emotion == null || entry.emotionTag == emotion
            matchesQuery && matchesTarget && matchesEmotion
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTargetFilter(memberId: Int?) {
        _selectedTargetId.value = memberId
    }

    fun selectEmotionFilter(emotion: String?) {
        _selectedEmotion.value = emotion
    }

    // Audio recording state
    private val _isRecordingNow = MutableStateFlow(false)
    val isRecordingNow: StateFlow<Boolean> = _isRecordingNow.asStateFlow()

    private val _isPlayingNow = MutableStateFlow(false)
    val isPlayingNow: StateFlow<Boolean> = _isPlayingNow.asStateFlow()

    private val _recordingTimerSeconds = MutableStateFlow(0)
    val recordingTimerSeconds: StateFlow<Int> = _recordingTimerSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var tempAudioFile: File? = null

    private val _currentRecordAudioPath = MutableStateFlow<String?>(null)
    val currentRecordAudioPath: StateFlow<String?> = _currentRecordAudioPath.asStateFlow()

    fun startVoiceRecording() {
        val fileName = "爸爸_voice_${System.currentTimeMillis()}.m4a"
        val file = File(getApplication<Application>().filesDir, fileName)
        tempAudioFile = file

        val success = voiceManager.startRecording(file)
        if (success) {
            _isRecordingNow.value = true
            _recordingTimerSeconds.value = 0
            _currentRecordAudioPath.value = null
            startTimer()
        }
    }

    fun stopVoiceRecording() {
        voiceManager.stopRecording()
        stopTimer()
        _isRecordingNow.value = false
        tempAudioFile?.let {
            _currentRecordAudioPath.value = it.absolutePath
        }
    }

    fun deleteTempVoice() {
        stopVoiceRecording()
        stopAudioPlayback()
        _currentRecordAudioPath.value?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        _currentRecordAudioPath.value = null
        tempAudioFile = null
    }

    fun playAudio(path: String) {
        val file = File(path)
        _isPlayingNow.value = true
        val success = voiceManager.startPlayback(file) {
            _isPlayingNow.value = false
        }
        if (!success) {
            _isPlayingNow.value = false
        }
    }

    fun stopAudioPlayback() {
        voiceManager.stopPlayback()
        _isPlayingNow.value = false
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _recordingTimerSeconds.value += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // Add entry helper
    fun saveEntry(
        title: String,
        body: String,
        targetMemberId: Int?,
        emotionTag: String,
        audioPath: String?,
        isTimeCapsule: Boolean,
        openDate: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = MemoryEntry(
                title = title.ifBlank { "아빠의 소중한 기록" },
                body = body,
                targetMemberId = targetMemberId,
                emotionTag = emotionTag,
                audioFilePath = audioPath,
                isTimeCapsule = isTimeCapsule,
                openDate = openDate,
                createdDate = System.currentTimeMillis()
            )
            repository.insertEntry(entry)
            
            // clear state
            _currentRecordAudioPath.value = null
            tempAudioFile = null
        }
    }

    fun deleteEntry(entry: MemoryEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete voice file if exists
            entry.audioFilePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            repository.deleteEntry(entry)
        }
    }

    // Family management
    fun updateFamilyMemberNames(members: List<FamilyMember>) {
        viewModelScope.launch(Dispatchers.IO) {
            members.forEach { member ->
                repository.updateMember(member)
            }
        }
    }

    fun addFamilyMember(name: String, relation: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMember(FamilyMember(name = name, relation = relation))
        }
    }

    // Today's warm daily encouragement questions for fathers
    val dailyPrompts = listOf(
        "오늘, 아이에게 말로 차마 못 전했던 고마운 사랑의 메시지를 남겨보세요.",
        "수고한 나의 가족, 아내의 지친 어깨를 보았을 때 느꼈던 고마움을 담아보세요.",
        "우리 아이가 훗날 청년이 되었을 때, 아빠로서 꼭 해주고 싶은 조언은 무엇인가요?",
        "오늘 아이와 나눈 소소한 대화 중, 가슴에 깊이 남은 행복했던 순간을 말해보세요.",
        "결혼기념일 혹은 결혼 첫날을 되돌아보며, 아내를 위한 진솔한 응원을 전해봅니다.",
        "살아가며 힘든 시기를 겪고 있을 우리 아이에게 아빠가 보내는 뜨거운 격려.",
        "나의 어린 시절 아빠를 추억하고, 지금의 내가 아이에게 어떤 아빠가 투영되는지 적어보세요."
    )

    private val _currentPrompt = mutableStateOf(dailyPrompts.random())
    val currentPrompt: State<String> = _currentPrompt

    fun refreshPrompt() {
        _currentPrompt.value = dailyPrompts.random()
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.stopRecording()
        voiceManager.stopPlayback()
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
