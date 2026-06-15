package com.jeiel.daddyheartjournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jeiel.daddyheartjournal.data.model.AppLockSetting
import com.jeiel.daddyheartjournal.data.model.FamilyMember
import com.jeiel.daddyheartjournal.data.model.MemoryEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FamilyMember::class, MemoryEntry::class, AppLockSetting::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun memoryEntryDao(): MemoryEntryDao
    abstract fun appLockSettingDao(): AppLockSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "papa_diary_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                // Safe callback initiation
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val memberDao = database.familyMemberDao()
            val lockDao = database.appLockSettingDao()

            // Pre-populate typical target members of a dad's focus
            memberDao.insertMember(FamilyMember(name = "아이", relation = "아이 (첫째)"))
            memberDao.insertMember(FamilyMember(name = "우리 둘째", relation = "아이 (둘째)"))
            memberDao.insertMember(FamilyMember(name = "당신", relation = "아내"))
            memberDao.insertMember(FamilyMember(name = "우리 가족", relation = "가족 모두"))
            memberDao.insertMember(FamilyMember(name = "나의 삶", relation = "나 자신"))

            // Pre-populate settings
            lockDao.insertLockSetting(AppLockSetting(id = 1, isEnabled = false, pinHash = ""))
        }
    }
}

