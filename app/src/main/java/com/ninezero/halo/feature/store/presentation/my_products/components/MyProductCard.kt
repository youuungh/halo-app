package com.ninezero.halo.feature.store.presentation.my_products.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.MembershipBadge
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import java.text.NumberFormat

private const val LOW_STOCK_THRESHOLD = 5

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
    val price = product.price
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
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 가격
                    if (hasDiscount) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${product.discountRate}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
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
                                text = "${numberFormat.format(product.originalPrice)}원",
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

                    // 상품 상태는 가격 아래, 분류 태그는 아래 줄
                    val isMembershipOnly = product.requiredTier != SubscriptionPlanTier.FREE
                    if (product.isDealActive || isMembershipOnly) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (product.isDealActive) {
                                StatusTagBadge(text = "타임딜", color = MaterialTheme.colorScheme.error)
                            }
                            if (isMembershipOnly) {
                                MembershipBadge(text = "멤버십 전용")
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onMoreOptions() }
                        .bounceClick(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                        contentDescription = "옵션",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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

            // 재고 · 판매 · 조회 한 줄
            Text(
                text = buildStatsLine(product),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            )
        }
    }
}

/** 재고 5개 이하 · 품절은 빨강으로 강조 */
@Composable
private fun buildStatsLine(product: Product): AnnotatedString {
    val isSoldOut = product.stock <= 0 || product.status == ProductStatus.SOLD_OUT
    val isLowStock = !isSoldOut && product.stock <= LOW_STOCK_THRESHOLD
    val warnColor = if (isSoldOut || isLowStock) MaterialTheme.colorScheme.error else null
    return buildAnnotatedString {
        val stockText = if (isSoldOut) "품절" else "재고 ${product.stock}"
        if (warnColor != null) {
            withStyle(SpanStyle(color = warnColor, fontWeight = FontWeight.SemiBold)) { append(stockText) }
        } else {
            append(stockText)
        }
        append(" · 판매 ${product.salesCount} · 조회 ${product.viewCount}")
    }
}

/** 상품 상태 태그, 분류 태그와 같은 옅은 바탕 모양 */
@Composable
private fun StatusTagBadge(text: String, color: Color) {
    // 다크에선 바탕을 조금 진하게, 멤버십 뱃지와 같음
    val isDark = LocalIsDarkTheme.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier
            .background(
                color = color.copy(alpha = if (isDark) 0.14f else 0.1f),
                shape = RoundedCornerShape(5.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
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
                shape = MaterialTheme.shapes.extraSmall
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private fun previewProduct(
    id: Int,
    name: String,
    stock: Int = 50,
    status: ProductStatus = ProductStatus.ACTIVE,
    dealPrice: Long? = null
) = Product(
    id = id,
    name = name,
    description = "",
    price = 89000L,
    originalPrice = 129000L,
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
    requiredTier = SubscriptionPlanTier.FREE,
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
                    product = previewProduct(4, "타임딜 진행 중인 상품", dealPrice = 69000L),
                    onClick = {},
                    onMoreOptions = {}
                )
            }
        }
    }
}
