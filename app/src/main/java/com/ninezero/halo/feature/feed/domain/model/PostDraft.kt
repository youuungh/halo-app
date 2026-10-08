package com.ninezero.halo.feature.feed.domain.model

import android.net.Uri

data class PostDraft(
    val id: Long = 0,
    val content: String,
    val mediaUris: List<Uri>,
    val authorUsername: String,
    val authorDisplayName: String?,
    val updatedAt: Long
)
