package com.ninezero.halo.feature.store.presentation.order_list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.store.domain.model.OrderItem
import com.ninezero.halo.ui.theme.HaloTheme
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

@Composable
fun OrderExpandedItem(
    item: OrderItem,
    canWriteReview: Boolean,
    reviewDeadlineLabel: String?,
    numberFormat: NumberFormat,
    onClick: () -> Unit,
    onWriteReview: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = item.productImageUrl,
            contentDescription = item.productName,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_img_placeholder),
            error = painterResource(R.drawable.ic_img_placeholder),
            fallback = painterResource(R.drawable.ic_img_placeholder)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.quantity}개 · ${numberFormat.format(item.subtotal.toLongOrNull() ?: 0)}원",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 액션 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canWriteReview) {
                    OutlinedButton(
                        onClick = onWriteReview,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .bounceClick()
                    ) {
                        Text(
                            text = "리뷰 작성",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        reviewDeadlineLabel?.let { deadlineLabel ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = deadlineLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .bounceClick()
                ) {
                    Text(
                        text = "장바구니 담기",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (!canWriteReview) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

internal fun reviewDeadlineLabel(deadlineMillis: Long): String {
    val now = System.currentTimeMillis()
    val elapsedDays = TimeUnit.MILLISECONDS.toDays(now - deadlineMillis).toInt()
    val daysLeft = Constants.Commerce.REVIEW_WRITE_DEADLINE_DAYS - elapsedDays

    return when {
        daysLeft < 0 -> "만료"
        daysLeft == 0 -> "D-Day"
        else -> "D-$daysLeft"
    }
}

private fun previewItem(name: String) = OrderItem(
    id = 1,
    productId = 1,
    productName = name,
    productImageUrl = null,
    price = "89000",
    quantity = 1,
    subtotal = "89000"
)

@HaloLightDarkPreview
@Composable
private fun OrderExpandedItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                OrderExpandedItem(
                    item = previewItem("프리미엄 무선 이어폰 Pro Max"),
                    canWriteReview = true,
                    reviewDeadlineLabel = "D-25",
                    numberFormat = rememberKoreanNumberFormat(),
                    onClick = {},
                    onWriteReview = {},
                    onAddToCart = {}
                )
                OrderExpandedItem(
                    item = previewItem("리뷰 작성 불가 상품"),
                    canWriteReview = false,
                    reviewDeadlineLabel = null,
                    numberFormat = rememberKoreanNumberFormat(),
                    onClick = {},
                    onWriteReview = {},
                    onAddToCart = {}
                )
            }
        }
    }
}
