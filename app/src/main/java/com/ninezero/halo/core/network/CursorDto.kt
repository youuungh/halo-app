package com.ninezero.halo.core.network

import kotlinx.serialization.Serializable

/** 커서 목록 응답 래퍼 */
@Serializable
data class CursorDto<T>(  // 목록 응답은 typealias로 선언
    val items: List<T>,
    val nextCursor: String? = null,
    val hasNext: Boolean = false
)
