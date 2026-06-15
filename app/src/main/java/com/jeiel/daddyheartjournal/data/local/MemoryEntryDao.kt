package com.jeiel.daddyheartjournal.data.local

import androidx.room.*
import com.jeiel.daddyheartjournal.data.model.MemoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryEntryDao {
    @Query("SELECT * FROM memory_entries ORDER BY createdDate DESC")
    fun getAllEntriesFlow(): Flow<List<MemoryEntry>>

    @Query("SELECT * FROM memory_entries ORDER BY createdDate DESC")
    suspend fun getAllEntries(): List<MemoryEntry>

    @Query("SELECT * FROM memory_entries WHERE id = :id")
    suspend fun getEntryById(id: Int): MemoryEntry?

    @Query("SELECT * FROM memory_entries WHERE isTimeCapsule = 1 ORDER BY openDate ASC")
    fun getTimeCapsulesFlow(): Flow<List<MemoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MemoryEntry): Long

    @Update
    suspend fun updateEntry(entry: MemoryEntry)

    @Delete
    suspend fun deleteEntry(entry: MemoryEntry)
}

