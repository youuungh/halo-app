package com.ninezero.halo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ninezero.halo.core.database.entity.PostDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDraftDao {

    @Query("SELECT * FROM drafts ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<PostDraftEntity>>

    @Query("SELECT id FROM drafts")
    suspend fun getAllIds(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(draft: PostDraftEntity): Long

    @Update
    suspend fun update(draft: PostDraftEntity)

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
