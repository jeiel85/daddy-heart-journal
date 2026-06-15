package com.jeiel.daddyheartjournal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_entries")
data class MemoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetMemberId: Int?, // References FamilyMember.id, null means all/other
    val body: String,
    val emotionTag: String, // "고마움", "미안함", "기쁨", "걱정", "응원", "추억"
    val audioFilePath: String?, // audio record audio path
    val imageFilePath: String? = null,
    val createdDate: Long = System.currentTimeMillis(),
    val openDate: Long = System.currentTimeMillis(), // Unlock target date for time capsules
    val isTimeCapsule: Boolean = false
)

