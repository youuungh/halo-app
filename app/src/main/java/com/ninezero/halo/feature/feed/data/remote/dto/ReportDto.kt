package com.ninezero.halo.feature.feed.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReportRequestDto(
    val targetType: String,
    val targetId: Int
)

@Serializable
data class ReportResponseDto(
    val reportCount: Int = 0,
    val blinded: Boolean = false
)
