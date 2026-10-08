package com.ninezero.halo.feature.store.presentation.review_list

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.domain.model.ReviewSummary

private val previewReviewSummary = ReviewSummary(
    avgRating = 4.3,
    totalReviews = 86,
    ratingDistribution = mapOf(5 to 1232, 4 to 25, 3 to 10, 2 to 6, 1 to 3)
)

private val previewReviews = listOf(
    Review(
        id = 1,
        productId = 1,
        orderId = 1,
        user = previewUserSummary(id = 10, username = "user1", displayName = "음악러버"),
        rating = 5,
        content = "음질이 정말 뛰어납니다! 노이즈 캔슬링도 완벽하고 배터리도 오래가요. 이 가격에 이 퀄리티라니 정말 만족합니다.",
        images = emptyList(),
        createdAt = System.currentTimeMillis() - 86400000,
    ),
    Review(
        id = 2,
        productId = 1,
        orderId = 2,
        user = previewUserSummary(id = 11, username = "user2", displayName = "테크리뷰어"),
        rating = 4,
        content = "가성비 좋은 이어폰입니다. 착용감도 편하고 통화 품질도 괜찮네요.",
        images = listOf("https://example.com/review1.jpg", "https://example.com/review2.jpg"),
        createdAt = System.currentTimeMillis() - 172800000,
    ),
    Review(
        id = 3,
        productId = 1,
        orderId = 3,
        user = previewUserSummary(id = 12, username = "user3", displayName = "일상공유"),
        rating = 3,
        content = "보통입니다. 디자인은 예쁜데 노이즈 캔슬링이 기대보다는 약해요.",
        images = emptyList(),
        createdAt = System.currentTimeMillis() - 259200000,
    )
)

@HaloLightDarkPreview
@Composable
private fun ReviewListScreenPreview() {
    HaloScreenPreview {
        ReviewListScreenContent(
            state = ReviewListState(
                reviews = previewReviews,
                reviewSummary = previewReviewSummary,
                totalReviews = 86,
                currentUserId = 10
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewListDeleteDialogPreview() {
    HaloScreenPreview {
        ReviewListScreenContent(
            state = ReviewListState(
                isLoading = false,
                reviews = previewReviews,
                currentUserId = 10,
                showDeleteConfirm = true,
                selectedReview = previewReviews.first()
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewListLoadingPreview() {
    HaloScreenPreview {
        ReviewListScreenContent(
            state = ReviewListState(isLoading = true)
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewListEmptyPreview() {
    HaloScreenPreview {
        ReviewListScreenContent(
            state = ReviewListState(
                reviews = emptyList(),
                reviewSummary = previewReviewSummary,
                totalReviews = 0
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewListErrorPreview() {
    HaloScreenPreview {
        ReviewListScreenContent(
            state = ReviewListState(
                error = "네트워크 연결을 확인해주세요"
            )
        )
    }
}
