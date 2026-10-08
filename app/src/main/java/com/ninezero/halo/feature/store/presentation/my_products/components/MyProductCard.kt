package com.ninezero.halo.feature.store.presentation.my_products.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.DealTag
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.ui.theme.HaloTheme
import java.text.NumberFormat

@Composable
fun MyProductCard(
    product: Product,
    onClick: () -> Unit,
    onMoreOptions: () -> Unit,
    modifier: Modifier = Modifier,
    numberFormat: NumberFormat = rememberKoreanNumberFormat(),
    groupColor: Color? = null
) {
    val isActive = product.status != ProductStatus.DISCONTINUED
    val contentAlpha = if (isActive) 1f else 0.5f
    val hasDiscount = product.originalPrice != null && product.discountRate != null && product.discountRate > 0
    val price = product.price.toLongOrNull() ?: 0
    val hasTags = product.sectionTag != null || product.tags.isNotEmpty()
    val priceColor = if (product.isDealActive) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        // 좌측 색상 바
        if (groupColor != null) {
            val barColor = barColorFor(groupColor)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // 상품 이미지
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (product.imageUrls.isNotEmpty()) {
                        AsyncImage(
                            model = product.imageUrls.first(),
                            contentDescription = product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }

                    if (product.status == ProductStatus.SOLD_OUT) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "품절",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // 상품 정보
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Top) {
                        if (product.isDealActive) {
                            DealTag(modifier = Modifier.offset(y = 1.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 가격
                    if (hasDiscount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${product.discountRate}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.alignByBaseline()
                            )
                            Text(
                                text = "${numberFormat.format(price)}원",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = priceColor,
                                modifier = Modifier.alignByBaseline()
                            )
                            Text(
                                text = "${product.originalPrice.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: product.originalPrice}원",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                    } else {
                        Text(
                            text = "${numberFormat.format(price)}원",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = priceColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(y = (-8).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onMoreOptions() }
                        .bounceClick(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                        contentDescription = "옵션",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 태그 뱃지
            if (hasTags) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // sectionTag 항상 먼저
                    product.sectionTag?.let { tag ->
                        TagBadge(
                            text = tag.name,
                            color = MaterialTheme.colorScheme.primary,
                            isSection = true
                        )
                    }
                    // 일반 tags
                    product.tags.forEach { tag ->
                        TagBadge(
                            text = tag,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            isSection = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 재고 프로그레스 바
            val maxStock = 100
            val targetProgress = (product.stock.toFloat() / maxStock).coerceIn(0f, 1f)
            var animatedTarget by rememberSaveable { mutableFloatStateOf(0f) }
            LaunchedEffect(targetProgress) { animatedTarget = targetProgress }
            val stockProgress by animateFloatAsState(
                targetValue = animatedTarget,
                animationSpec = tween(durationMillis = 800),
                label = "stockProgress"
            )
            val isLowStock = product.stock <= 5

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { stockProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isLowStock) MaterialTheme.colorScheme.error
                    else if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "재고 ${product.stock}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLowStock) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 하단: 판매·조회 + 수정·삭제
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                // 판매수
                Icon(
                    painter = painterResource(R.drawable.ic_chart_no_axes_column),
                    contentDescription = "판매",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${product.salesCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 조회수
                Icon(
                    painter = painterResource(R.drawable.ic_eye),
                    contentDescription = "조회",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${product.viewCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TagBadge(
    text: String,
    color: Color,
    isSection: Boolean
) {
    Text(
        text = if (isSection) text else "#$text",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSection) FontWeight.SemiBold else FontWeight.Normal,
        color = color,
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private fun previewProduct(
    id: Int,
    name: String,
    stock: Int = 50,
    status: String = ProductStatus.ACTIVE,
    dealPrice: String? = null
) = Product(
    id = id,
    name = name,
    description = "",
    price = "89000",
    originalPrice = "129000",
    discountRate = 31,
    dealPrice = dealPrice,
    dealEndAt = dealPrice?.let { System.currentTimeMillis() + 3_600_000L },
    isDealActive = dealPrice != null,
    imageUrls = emptyList(),
    brandName = "SoundWave",
    tags = listOf("이어폰"),
    stock = stock,
    status = status,
    isActive = status == ProductStatus.ACTIVE,
    viewCount = 1520,
    likeCount = 89,
    salesCount = 45,
    creator = previewUserSummary(displayName = "크리에이터 샵"),
    requiredTier = "FREE",
    canAccess = true
)

@HaloLightDarkPreview
@Composable
private fun MyProductCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                MyProductCard(
                    product = previewProduct(1, "프리미엄 무선 이어폰 Pro Max"),
                    onClick = {},
                    onMoreOptions = {}
                )
                MyProductCard(
                    product = previewProduct(2, "품절된 기계식 키보드", stock = 0),
                    onClick = {},
                    onMoreOptions = {}
                )
                MyProductCard(
                    product = previewProduct(3, "판매종료된 충전기", status = ProductStatus.DISCONTINUED),
                    onClick = {},
                    onMoreOptions = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProductCardDealPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                MyProductCard(
                    product = previewProduct(4, "타임딜 진행 중인 상품", dealPrice = "69000"),
                    onClick = {},
                    onMoreOptions = {}
                )
            }
        }
    }
}
