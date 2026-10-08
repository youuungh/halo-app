package com.ninezero.halo.feature.store.data.remote.dto

import com.ninezero.halo.core.network.CursorDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: Int,
    val user: UserSummaryDto,
    val productId: Int,
    val orderId: Int,
    val rating: Int,
    val content: String,
    val images: List<String> = emptyList(),
    val productName: String? = null,
    val productImageUrl: String? = null,
    val canAccess: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

typealias ReviewListResponseDto = CursorDto<ReviewDto>

@Serializable
data class ReviewSummaryResponseDto(
    val productId: Int,
    val avgRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>
)

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    productId = productId,
    orderId = orderId,
    user = user.toDomain(),
    rating = rating,
    content = content,
    images = images,
    productName = productName,
    productImageUrl = productImageUrl,
    canAccess = canAccess,
    createdAt = createdAt.parseTimestamp(),
)

fun ReviewSummaryResponseDto.toDomain(): ReviewSummary = ReviewSummary(
    avgRating = avgRating,
    totalReviews = totalReviews,
    ratingDistribution = ratingDistribution
)
