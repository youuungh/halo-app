package com.ninezero.halo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ninezero.halo.core.database.entity.CachedChatRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedChatRoomDao {

    @Query("""
        SELECT * FROM cached_chat_rooms
        WHERE isArchived = 0
        ORDER BY COALESCE(lastMessageTimestamp, createdAt) DESC, id DESC
    """)
    fun observeActiveChatRooms(): Flow<List<CachedChatRoomEntity>>

    @Query("SELECT * FROM cached_chat_rooms WHERE id = :roomId")
    fun observeChatRoom(roomId: Int): Flow<CachedChatRoomEntity?>

    @Query("SELECT * FROM cached_chat_rooms WHERE id = :roomId")
    suspend fun getChatRoom(roomId: Int): CachedChatRoomEntity?

    @Query("SELECT isHistoryComplete FROM cached_chat_rooms WHERE id = :roomId")
    suspend fun isHistoryComplete(roomId: Int): Boolean?

    @Query("SELECT id FROM cached_chat_rooms WHERE isHistoryComplete = 1 AND id IN (:roomIds)")
    suspend fun getHistoryCompleteIds(roomIds: List<Int>): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceAll(rooms: List<CachedChatRoomEntity>)

    // 서버 행으로 갈아끼워도 처음까지 받음 표시는 유지
    @Transaction
    suspend fun upsertAll(rooms: List<CachedChatRoomEntity>) {
        val completeIds = getHistoryCompleteIds(rooms.map { it.id }).toSet()
        replaceAll(rooms.map { if (it.id in completeIds) it.copy(isHistoryComplete = true) else it })
    }

    @Transaction
    suspend fun upsert(room: CachedChatRoomEntity) = upsertAll(listOf(room))

    @Query("""
        UPDATE cached_chat_rooms
        SET lastMessagePreview = :preview,
            lastMessageTimestamp = :timestamp
        WHERE id = :roomId
    """)
    suspend fun updateLastMessage(
        roomId: Int,
        preview: String,
        timestamp: Long
    )

    @Query("UPDATE cached_chat_rooms SET isArchived = :archived WHERE id = :roomId")
    suspend fun setArchived(roomId: Int, archived: Boolean)

    @Query("UPDATE cached_chat_rooms SET isBlockedByMe = :blocked WHERE otherUserId = :otherUserId")
    suspend fun setBlockedByMe(otherUserId: Int, blocked: Boolean)

    @Query("""
        UPDATE cached_chat_rooms
        SET isBlockedByMe = :blockedByMe,
            isBlockedByOther = :blockedByOther
        WHERE otherUserId = :otherUserId
    """)
    suspend fun setBlockRelation(otherUserId: Int, blockedByMe: Boolean, blockedByOther: Boolean)

    @Query("UPDATE cached_chat_rooms SET unreadCount = :count WHERE id = :roomId")
    suspend fun updateUnreadCount(roomId: Int, count: Int)

    @Query("UPDATE cached_chat_rooms SET isHistoryComplete = :complete WHERE id = :roomId")
    suspend fun setHistoryComplete(roomId: Int, complete: Boolean)

    @Query("DELETE FROM cached_chat_rooms WHERE id = :roomId")
    suspend fun delete(roomId: Int)
}
