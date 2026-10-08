package com.ninezero.halo.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_chat_messages")
data class PendingChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Int,
    val content: String?,
    val messageType: String,  // IMAGE, VIDEO, FILE
    val mediaUris: String?,
    val status: UploadStatus = UploadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val workerId: String? = null
)
