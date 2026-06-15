package com.jeiel.daddyheartjournal.data.repository

import com.jeiel.daddyheartjournal.data.local.AppLockSettingDao
import com.jeiel.daddyheartjournal.data.local.FamilyMemberDao
import com.jeiel.daddyheartjournal.data.local.MemoryEntryDao
import com.jeiel.daddyheartjournal.data.model.AppLockSetting
import com.jeiel.daddyheartjournal.data.model.FamilyMember
import com.jeiel.daddyheartjournal.data.model.MemoryEntry
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val familyMemberDao: FamilyMemberDao,
    private val memoryEntryDao: MemoryEntryDao,
    private val appLockSettingDao: AppLockSettingDao
) {
    val allMembersFlow: Flow<List<FamilyMember>> = familyMemberDao.getAllMembersFlow()
    val allEntriesFlow: Flow<List<MemoryEntry>> = memoryEntryDao.getAllEntriesFlow()
    val timeCapsulesFlow: Flow<List<MemoryEntry>> = memoryEntryDao.getTimeCapsulesFlow()
    val lockSettingFlow: Flow<AppLockSetting?> = appLockSettingDao.getLockSettingFlow()

    suspend fun getAllMembers(): List<FamilyMember> = familyMemberDao.getAllMembers()
    suspend fun getMemberById(id: Int): FamilyMember? = familyMemberDao.getMemberById(id)
    suspend fun insertMember(member: FamilyMember): Long = familyMemberDao.insertMember(member)
    suspend fun updateMember(member: FamilyMember) = familyMemberDao.updateMember(member)
    suspend fun deleteMember(member: FamilyMember) = familyMemberDao.deleteMember(member)

    suspend fun getAllEntries(): List<MemoryEntry> = memoryEntryDao.getAllEntries()
    suspend fun getEntryById(id: Int): MemoryEntry? = memoryEntryDao.getEntryById(id)
    suspend fun insertEntry(entry: MemoryEntry): Long = memoryEntryDao.insertEntry(entry)
    suspend fun updateEntry(entry: MemoryEntry) = memoryEntryDao.updateEntry(entry)
    suspend fun deleteEntry(entry: MemoryEntry) = memoryEntryDao.deleteEntry(entry)

    suspend fun getLockSetting(): AppLockSetting? = appLockSettingDao.getLockSetting()
    suspend fun updateLockSetting(setting: AppLockSetting) = appLockSettingDao.insertLockSetting(setting)
}

