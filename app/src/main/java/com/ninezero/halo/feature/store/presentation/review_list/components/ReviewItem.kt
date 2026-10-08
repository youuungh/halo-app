package com.ninezero.halo.feature.store.presentation.review_list.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
internal fun ReviewItem(
    review: Review,
    modifier: Modifier = Modifier,
    isMyReview: Boolean = false,
    onMoreClick: () -> Unit = {},
    onImageClick: (images: List<String>, index: Int) -> Unit = { _, _ -> }
) {
    val horizontalPadding = 8.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp
    val contentStartPadding = horizontalPadding + avatarSize + avatarSpacing
    val hasImages = review.images.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        // 텍스트 컨텐츠 섹션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = review.user.avatarThumbUrl ?: review.user.avatarUrl,
                contentDescription = "${review.user.displayName} 프로필",
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder),
                fallback = painterResource(R.drawable.ic_profile_placeholder)
            )

            Spacer(modifier = Modifier.width(avatarSpacing))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = if (hasImages) 8.dp else 20.dp)
            ) {
                // 별점 + 평점 + 더보기
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRating(rating = review.rating.toDouble(), starSize = 14.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${review.rating}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isMyReview) {
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
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 이름 + 날짜
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = review.user.displayName ?: review.user.username,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " · ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = review.createdAt.toFormattedDate("yyyy.MM.dd"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = review.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 이미지
        ReviewImageRow(
            images = review.images,
            startPadding = contentStartPadding,
            endPadding = horizontalPadding,
            onImageClick = onImageClick
        )
    }
}

private fun previewReview(
    id: Int,
    rating: Int,
    content: String,
    images: List<String> = emptyList()
) = Review(
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
private fun ReviewItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ReviewItem(
                    review = previewReview(
                        id = 1,
                        rating = 5,
                        content = "음질이 정말 뛰어납니다! 노이즈 캔슬링도 완벽하고 배터리도 오래가요.",
                        images = List(3) { "https://picsum.photos/200/200?random=$it" }
                    ),
                    onImageClick = { _, _ -> }
                )
                ReviewItem(
                    review = previewReview(2, 3, "가격 대비 무난합니다.")
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ReviewItemMyReviewPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ReviewItem(
                    review = previewReview(3, 4, "배송도 빠르고 포장도 꼼꼼했어요. 잘 쓰겠습니다."),
                    isMyReview = true,
                    onMoreClick = {}
                )
            }
        }
    }
}
