package com.ninezero.halo.feature.chat.domain.model

data class MediaUploadHandle(
    val clientTempId: String,
    val mediaUris: List<String>  // 스테이징된 URI, 복사 실패 시 원본
)
