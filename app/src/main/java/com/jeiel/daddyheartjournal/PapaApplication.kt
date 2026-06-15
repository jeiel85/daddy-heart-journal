package com.jeiel.daddyheartjournal

import android.app.Application
import com.jeiel.daddyheartjournal.data.local.AppDatabase
import com.jeiel.daddyheartjournal.data.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PapaApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { 
        AppRepository(
            database.familyMemberDao(),
            database.memoryEntryDao(),
            database.appLockSettingDao()
        )
    }
}

