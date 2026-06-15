package com.example.data.local

import androidx.room.*
import com.example.data.model.FamilyMember
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY id ASC")
    fun getAllMembersFlow(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members ORDER BY id ASC")
    suspend fun getAllMembers(): List<FamilyMember>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberById(id: Int): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)
}
