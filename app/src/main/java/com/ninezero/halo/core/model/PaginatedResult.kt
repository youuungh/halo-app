package com.ninezero.halo.core.model

data class PaginatedResult<T>(
    val items: List<T>,
    val hasNext: Boolean,
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val totalPages: Int = 1
)
