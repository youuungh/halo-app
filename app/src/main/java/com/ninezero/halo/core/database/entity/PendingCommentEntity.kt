package com.ninezero.halo.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_comments")
data class PendingCommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientRequestId: String,  // 서버 멱등 키
    val postId: Int,
    val content: String,
    val parentCommentId: Int?,
    val authorId: Int,
    val authorUsername: String,
    val authorDisplayName: String?,
    val authorAvatarUrl: String?,
    val mediaUris: String? = null,
    val mediaDimensions: String? = null,
    val status: UploadStatus = UploadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val workerId: String? = null
)

enum class UploadStatus {
    PENDING,
    UPLOADING,
    SUCCESS,
    FAILED
}
