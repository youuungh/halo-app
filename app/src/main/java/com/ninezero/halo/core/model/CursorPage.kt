package com.ninezero.halo.core.model

data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: String?  // 마지막 페이지면 null, 다음 요청에 그대로 보냄
)
