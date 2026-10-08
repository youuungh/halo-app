package com.ninezero.halo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ninezero.halo.core.database.dao.CachedChatRoomDao
import com.ninezero.halo.core.database.dao.CachedMessageDao
import com.ninezero.halo.core.database.dao.PendingChatMessageDao
import com.ninezero.halo.core.database.dao.PendingCommentDao
import com.ninezero.halo.core.database.dao.PendingPostDao
import com.ninezero.halo.core.database.dao.PostDraftDao
import com.ninezero.halo.core.database.entity.CachedChatRoomEntity
import com.ninezero.halo.core.database.entity.CachedMessageEntity
import com.ninezero.halo.core.database.entity.PendingChatMessageEntity
import com.ninezero.halo.core.database.entity.PendingCommentEntity
import com.ninezero.halo.core.database.entity.PendingPostEntity
import com.ninezero.halo.core.database.entity.PostDraftEntity

@Database(
    entities = [
        PendingCommentEntity::class,
        PendingPostEntity::class,
        PostDraftEntity::class,
        PendingChatMessageEntity::class,
        CachedMessageEntity::class,
        CachedChatRoomEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HaloDatabase : RoomDatabase() {
    abstract fun pendingCommentDao(): PendingCommentDao
    abstract fun pendingPostDao(): PendingPostDao
    abstract fun postDraftDao(): PostDraftDao
    abstract fun pendingChatMessageDao(): PendingChatMessageDao
    abstract fun cachedMessageDao(): CachedMessageDao
    abstract fun cachedChatRoomDao(): CachedChatRoomDao
}
