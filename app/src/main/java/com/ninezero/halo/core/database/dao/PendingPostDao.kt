package com.ninezero.halo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ninezero.halo.core.database.entity.PendingPostEntity
import com.ninezero.halo.core.database.entity.UploadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PendingPostEntity): Long

    @Update
    suspend fun update(post: PendingPostEntity)

    @Query("SELECT * FROM pending_posts WHERE id = :id")
    suspend fun getById(id: Long): PendingPostEntity?

    @Query("SELECT * FROM pending_posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getByAuthorId(authorId: Int): Flow<List<PendingPostEntity>>

    @Query("UPDATE pending_posts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: UploadStatus)

    @Query("UPDATE pending_posts SET status = :status, workerId = :workerId WHERE id = :id")
    suspend fun updateStatusWithWorkerId(id: Long, status: UploadStatus, workerId: String)

    @Query("DELETE FROM pending_posts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT id FROM pending_posts")
    suspend fun getAllPendingPostIds(): List<Long>

    @Query("SELECT id FROM pending_posts WHERE workerId IS NULL")
    suspend fun getUnqueuedIds(): List<Long>
}
