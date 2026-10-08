package com.ninezero.halo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ninezero.halo.core.database.entity.PendingChatMessageEntity
import com.ninezero.halo.core.database.entity.UploadStatus

@Dao
interface PendingChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: PendingChatMessageEntity): Long

    @Update
    suspend fun update(message: PendingChatMessageEntity)

    @Query("SELECT * FROM pending_chat_messages WHERE id = :id")
    suspend fun getById(id: Long): PendingChatMessageEntity?

    @Query("SELECT id FROM pending_chat_messages")
    suspend fun getAllIds(): List<Long>

    @Query("UPDATE pending_chat_messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: UploadStatus)

    @Query("DELETE FROM pending_chat_messages WHERE id = :id")
    suspend fun deleteById(id: Long)
}
