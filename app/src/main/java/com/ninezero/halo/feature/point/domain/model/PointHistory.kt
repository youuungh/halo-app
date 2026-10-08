package com.ninezero.halo.feature.point.domain.model

data class PointHistory(
    val id: Int,
    val type: PointType,
    val amount: Int,
    val description: String,
    val createdAt: Long
)

data class PointHistoryPage(
    val histories: List<PointHistory>,
    val page: Int,
    val hasMore: Boolean
)
