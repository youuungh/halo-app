package com.ninezero.halo.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drafts")
data class PostDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val mediaUris: String?,
    val authorId: Int,
    val authorUsername: String,
    val authorDisplayName: String?,
    val authorAvatarUrl: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
