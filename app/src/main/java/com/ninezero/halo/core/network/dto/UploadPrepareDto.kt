package com.ninezero.halo.core.network.dto

import com.ninezero.halo.core.model.UploadTicket
import kotlinx.serialization.Serializable

@Serializable
data class UploadPrepareDto(
    val uploadId: Int,
    val uploadUrl: String,
    val contentType: String,
    val expiresAt: String
)

fun UploadPrepareDto.toDomain(): UploadTicket = UploadTicket(
    uploadId = uploadId,
    uploadUrl = uploadUrl,
    contentType = contentType
)
