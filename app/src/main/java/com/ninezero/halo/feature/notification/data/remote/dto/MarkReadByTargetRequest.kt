package com.ninezero.halo.feature.notification.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MarkReadByTargetRequest(
    val type: String,
    val targetId: Int
)
