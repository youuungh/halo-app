package com.ninezero.halo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ninezero.halo.core.database.entity.CachedMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedMessageDao {

    @Query("""
        SELECT * FROM cached_messages
        WHERE roomId = :roomId
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getMessages(roomId: Int, limit: Int): List<CachedMessageEntity>

    @Query("""
        SELECT * FROM cached_messages
        WHERE roomId = :roomId
        ORDER BY createdAt DESC
    """)
    fun observeMessages(roomId: Int): Flow<List<CachedMessageEntity>>  // 삭제 메시지 포함, placeholder 표시용

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<CachedMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CachedMessageEntity)

    @Query("UPDATE cached_messages SET isDeleted = 1 WHERE id = :messageId")
    suspend fun markAsDeleted(messageId: Int)

    @Query("""
        UPDATE cached_messages
        SET isRead = 1, readAt = :readAt
        WHERE roomId = :roomId AND id <= :lastReadMessageId AND isFromMe = 1 AND isRead = 0
    """)
    suspend fun markMyMessagesAsRead(roomId: Int, lastReadMessageId: Int, readAt: Long)  // lastReadMessageId까지 읽음 처리

    @Query("""
        UPDATE cached_messages
        SET isRead = 1, readAt = :readAt
        WHERE roomId = :roomId AND isFromMe = 0 AND isRead = 0
    """)
    suspend fun markIncomingMessagesAsRead(roomId: Int, readAt: Long)  // 상대 메시지 전체 읽음 처리

    @Query("DELETE FROM cached_messages WHERE roomId = :roomId")
    suspend fun deleteByRoomId(roomId: Int)

    @Query("DELETE FROM cached_messages WHERE cachedAt < :timestamp AND status = 'SENT'")
    suspend fun deleteSentOlderThan(timestamp: Long)  // SENDING·FAILED는 보존

    @Query("DELETE FROM cached_messages WHERE roomId = :roomId AND status = 'SENT' AND id < :messageId")
    suspend fun deleteSentBefore(roomId: Int, messageId: Int)  // SENDING·FAILED는 보존

    @Query("DELETE FROM cached_messages WHERE clientTempId = :clientTempId")
    suspend fun deleteByClientTempId(clientTempId: String)

    @Query("SELECT * FROM cached_messages WHERE clientTempId = :clientTempId LIMIT 1")
    suspend fun getByClientTempId(clientTempId: String): CachedMessageEntity?

    @Query("UPDATE cached_messages SET uploadProgress = :progress WHERE clientTempId = :clientTempId")
    suspend fun updateUploadProgressByClientTempId(clientTempId: String, progress: Float)

    @Query("UPDATE cached_messages SET status = :status WHERE clientTempId = :clientTempId")
    suspend fun updateStatusByClientTempId(clientTempId: String, status: String)

    @Query("DELETE FROM cached_messages WHERE clientTempId = :clientTempId AND status = 'FAILED'")
    suspend fun deleteFailedByClientTempId(clientTempId: String)

    @Query("""
        UPDATE cached_messages SET status = 'FAILED'
        WHERE roomId = :roomId AND status = 'SENDING' AND chatMessageType = 'TEXT'
            AND clientTempId NOT IN (:activeTempIds)
    """)
    suspend fun failStuckSendingText(roomId: Int, activeTempIds: List<String>)  // 미디어는 워커가 상태 관리

    @Transaction
    suspend fun replaceOptimisticMessage(clientTempId: String, message: CachedMessageEntity) {
        deleteByClientTempId(clientTempId)
        insertMessage(message)
    }
}
