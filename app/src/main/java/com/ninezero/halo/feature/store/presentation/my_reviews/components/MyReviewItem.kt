package com.ninezero.halo.feature.store.presentation.my_reviews.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.presentation.components.ReviewImageRow
import com.ninezero.halo.feature.store.presentation.components.StarRating
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
internal fun MyReviewItem(
    review: Review,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    onImageClick: (images: List<String>, index: Int) -> Unit = { _, _ -> }
) {
    val horizontalPadding = 16.dp
    val hasImages = review.images.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        // 텍스트 컨텐츠 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(bottom = if (hasImages) 8.dp else 16.dp)
        ) {
            // 상품 정보
            if (review.productName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 0.25.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable(onClick = onClick)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    AsyncImage(
                        model = review.productImageUrl,
                        contentDescription = review.productName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_img_placeholder),
                        error = painterResource(R.drawable.ic_img_placeholder),
                        fallback = painterResource(R.drawable.ic_img_placeholder)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = review.productName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 별점 + 평점 + 날짜 + 더보기
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarRating(rating = review.rating.toDouble(), starSize = 14.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${review.rating}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " · ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = review.createdAt.toFormattedDate("yyyy.MM.dd"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                    contentDescription = "더보기",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onMoreClick
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 이미지
        ReviewImageRow(
            images = review.images,
            startPadding = horizontalPadding,
            endPadding = horizontalPadding,
            onImageClick = onImageClick
        )
    }
}

private fun previewMyReview(
    id: Int,
    rating: Int,
    content: String,
    images: List<String> = emptyList(),
    productName: String? = "프리미엄 무선 이어폰 Pro Max"
) = Review(
    id = id,
    productId = id,
    orderId = id,
    user = previewUserSummary(displayName = "나"),
    rating = rating,
    content = content,
    images = images,
    productName = productName,
    productImageUrl = null,
    createdAt = System.currentTimeMillis() - id * 86_400_000L
)

@HaloLightDarkPreview
@Composable
private fun MyReviewItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                MyReviewItem(
                    review = previewMyReview(
                        id = 1,
                        rating = 5,
                        content = "음질이 정말 뛰어납니다! 노이즈 캔슬링도 완벽해요.",
                        images = List(2) { "https://picsum.photos/200/200?random=$it" }
                    ),
                    onClick = {},
                    onMoreClick = {},
                    onImageClick = { _, _ -> }
                )
                MyReviewItem(
                    review = previewMyReview(2, 3, "무난합니다.", productName = "기계식 키보드 청축"),
                    onClick = {},
                    onMoreClick = {}
                )
            }
        }
    }
}
