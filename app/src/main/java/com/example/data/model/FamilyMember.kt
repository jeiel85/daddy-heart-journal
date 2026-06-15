package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relation: String, // e.g., "첫째", "둘째", "아내", "가족 모두", "나 자신"
    val createdAt: Long = System.currentTimeMillis()
)
