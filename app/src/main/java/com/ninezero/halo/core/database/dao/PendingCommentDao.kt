package com.ninezero.halo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ninezero.halo.core.database.entity.PendingCommentEntity
import com.ninezero.halo.core.database.entity.UploadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingCommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: PendingCommentEntity): Long

    @Update
    suspend fun update(comment: PendingCommentEntity)

    @Query("SELECT * FROM pending_comments WHERE id = :id")
    suspend fun getById(id: Long): PendingCommentEntity?

    @Query("SELECT * FROM pending_comments WHERE postId = :postId ORDER BY createdAt DESC")
    fun getByPostId(postId: Int): Flow<List<PendingCommentEntity>>

    @Query("SELECT * FROM pending_comments WHERE parentCommentId = :parentCommentId ORDER BY createdAt DESC")
    fun getByParentCommentId(parentCommentId: Int): Flow<List<PendingCommentEntity>>

    @Query("UPDATE pending_comments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: UploadStatus)

    @Query("UPDATE pending_comments SET status = :status, workerId = :workerId WHERE id = :id")
    suspend fun updateStatusWithWorkerId(id: Long, status: UploadStatus, workerId: String)

    @Query("DELETE FROM pending_comments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT id FROM pending_comments")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT id FROM pending_comments WHERE workerId IS NULL")
    suspend fun getUnqueuedIds(): List<Long>
}
