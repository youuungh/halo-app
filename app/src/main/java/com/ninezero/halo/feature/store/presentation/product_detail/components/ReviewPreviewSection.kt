package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.presentation.components.StarRating

private const val PREVIEW_REVIEW_COUNT = 3

@Composable
fun ReviewPreviewSection(
    recentReviews: List<Review>,
    totalReviews: Int,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (recentReviews.isEmpty()) return

    val reviews = recentReviews.take(PREVIEW_REVIEW_COUNT)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProductDetailSectionTitle(
                text = "리뷰",
                count = totalReviews,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onViewAllClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = "전체보기",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        reviews.forEachIndexed { index, review ->
            if (index > 0) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
            ReviewPreviewItem(review = review)
        }

        // 리뷰 전체로
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(48.dp)
                .bounceClick()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onViewAllClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "리뷰 ${totalReviews}개 모두 보기",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ReviewPreviewItem(review: Review) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            StarRating(rating = review.rating.toDouble(), starSize = 12.dp)

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = review.user.avatarThumbUrl ?: review.user.avatarUrl,
                    contentDescription = "${review.user.displayName} 프로필",
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_profile_placeholder),
                    error = painterResource(R.drawable.ic_profile_placeholder),
                    fallback = painterResource(R.drawable.ic_profile_placeholder)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${review.user.displayName ?: review.user.username} · ${review.createdAt.toFormattedDate("yyyy.MM.dd")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (review.images.isNotEmpty()) {
            AsyncImage(
                model = review.images.first(),
                contentDescription = "리뷰 이미지",
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_image_placeholder),
                error = painterResource(R.drawable.ic_image_placeholder),
                fallback = painterResource(R.drawable.ic_image_placeholder)
            )
        }
    }
}

private fun previewReview(id: Int, rating: Int, content: String, images: List<String> = emptyList()) = Review(
    id = id,
    productId = 1,
    orderId = id,
    user = previewUserSummary(id = id + 10, username = "user$id", displayName = "리뷰어$id"),
    rating = rating,
    content = content,
    images = images,
    createdAt = System.currentTimeMillis() - id * 86_400_000L
)

@HaloLightDarkPreview
@Composable
private fun ReviewPreviewSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ReviewPreviewSection(
                    recentReviews = listOf(
                        previewReview(
                            id = 1,
                            rating = 5,
                            content = "음질이 정말 뛰어납니다! 노이즈 캔슬링도 완벽하고 배터리도 오래가요.",
                            images = List(3) { "https://picsum.photos/200/200?random=$it" }
                        ),
                        previewReview(2, 4, "가성비 좋습니다. 착용감도 편하네요."),
                        previewReview(3, 3, "무난해요.")
                    ),
                    totalReviews = 3,
                    onViewAllClick = {}
                )
            }
        }
    }
}
