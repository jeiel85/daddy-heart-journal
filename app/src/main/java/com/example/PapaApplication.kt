package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
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
