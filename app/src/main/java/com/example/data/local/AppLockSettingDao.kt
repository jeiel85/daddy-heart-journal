package com.example.data.local

import androidx.room.*
import com.example.data.model.AppLockSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLockSettingDao {
    @Query("SELECT * FROM app_lock_settings WHERE id = 1")
    fun getLockSettingFlow(): Flow<AppLockSetting?>

    @Query("SELECT * FROM app_lock_settings WHERE id = 1")
    suspend fun getLockSetting(): AppLockSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockSetting(setting: AppLockSetting)
}
