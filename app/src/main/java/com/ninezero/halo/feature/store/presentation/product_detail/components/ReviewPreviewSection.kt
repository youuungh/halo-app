package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.presentation.components.StarRating
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ReviewPreviewSection(
    recentReviews: List<Review>,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (recentReviews.isEmpty()) return

    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .height(IntrinsicSize.Max)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        recentReviews.forEach { review ->
            ReviewPreviewItem(
                review = review,
                modifier = Modifier.width(280.dp)
            )
        }

        // 전체보기
        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onViewAllClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "전체보기",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReviewPreviewItem(
    review: Review,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        // 첫 이미지
        if (review.images.isNotEmpty()) {
            AsyncImage(
                model = review.images.first(),
                contentDescription = "리뷰 이미지",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_img_placeholder),
                error = painterResource(R.drawable.ic_img_placeholder),
                fallback = painterResource(R.drawable.ic_img_placeholder)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        // 리뷰 내용
        Column(
            modifier = Modifier.weight(1f).height(64.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = review.user.avatarThumbUrl ?: review.user.avatarUrl,
                    contentDescription = "${review.user.displayName} 프로필",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_profile_placeholder),
                    error = painterResource(R.drawable.ic_profile_placeholder),
                    fallback = painterResource(R.drawable.ic_profile_placeholder)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = review.user.displayName ?: review.user.username,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StarRating(rating = review.rating.toDouble(), starSize = 12.dp)
            }

            Text(
                text = review.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
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
                    onViewAllClick = {}
                )
            }
        }
    }
}
