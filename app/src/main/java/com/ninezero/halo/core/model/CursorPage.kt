package com.ninezero.halo.core.model

data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: String?
)
