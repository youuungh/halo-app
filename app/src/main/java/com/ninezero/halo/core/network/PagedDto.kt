package com.ninezero.halo.core.network

import kotlinx.serialization.Serializable

/** 페이지네이션 응답 래퍼 */
@Serializable
data class PagedDto<T>(  // 목록 응답은 typealias로 선언
    val items: List<T>,
    val totalCount: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
)
