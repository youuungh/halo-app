package com.ninezero.halo.feature.share.data.remote.dto

import com.ninezero.halo.feature.share.domain.model.ShareResult
import kotlinx.serialization.Serializable

@Serializable
data class SharePostRequest(
    val postId: Int,
    val recipientUserIds: List<Int>,
    val message: String? = null,
    // 재전송 중복 방지 id
    val clientTempId: String? = null,
    val messageTempId: String? = null
)

@Serializable
data class ShareProductRequest(
    val productId: Int,
    val recipientUserIds: List<Int>,
    val message: String? = null,
    // 재전송 중복 방지 id
    val clientTempId: String? = null,
    val messageTempId: String? = null
)

@Serializable
data class ShareResponseDto(
    val sharedRoomIds: List<Int>,
    val recipientCount: Int
)

fun ShareResponseDto.toDomain() = ShareResult(
    sharedRoomIds = sharedRoomIds,
    recipientCount = recipientCount
)
