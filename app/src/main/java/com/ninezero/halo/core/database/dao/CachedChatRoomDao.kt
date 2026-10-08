package com.ninezero.halo.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rooms: List<CachedChatRoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(room: CachedChatRoomEntity)

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

    @Query("UPDATE cached_chat_rooms SET unreadCount = :count WHERE id = :roomId")
    suspend fun updateUnreadCount(roomId: Int, count: Int)

    @Query("DELETE FROM cached_chat_rooms WHERE id = :roomId")
    suspend fun delete(roomId: Int)
}
