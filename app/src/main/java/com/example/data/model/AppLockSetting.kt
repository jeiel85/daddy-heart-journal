package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_lock_settings")
data class AppLockSetting(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = false,
    val pinHash: String = "" // PIN string
)
