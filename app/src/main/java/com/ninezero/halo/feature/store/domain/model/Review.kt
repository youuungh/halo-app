package com.ninezero.halo.feature.store.domain.model

import com.ninezero.halo.core.model.UserSummary

data class Review(
    val id: Int,
    val productId: Int,
    val orderId: Int,
    val user: UserSummary,
    val rating: Int,
    val content: String,
    val images: List<String>,
    val productName: String? = null,
    val productImageUrl: String? = null,
    val canAccess: Boolean = true,
    val createdAt: Long,
)

data class ReviewSummary(
    val avgRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>
)
