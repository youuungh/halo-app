package com.ninezero.halo.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_posts")
data class PendingPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientRequestId: String,  // 서버 멱등 키
    val content: String,
    val postType: String,
    val mediaUris: String?,
    val mediaDimensions: String? = null,
    val authorId: Int,
    val authorUsername: String,
    val authorDisplayName: String?,
    val authorAvatarUrl: String?,
    val tagIds: String? = null,
    val sectionTagId: Int? = null,
    val requiredTier: String = "FREE",
    val creatorId: Int? = null,
    val isSecret: Boolean = false,
    val status: UploadStatus = UploadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val workerId: String? = null
)
