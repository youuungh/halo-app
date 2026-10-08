package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.DealBand
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun ProductInfoSection(
    product: Product,
    reviewSummary: ReviewSummary?,
    onCreatorClick: () -> Unit,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier,
    bestCoupon: Coupon? = null,
    couponAppliedPrice: Int? = null,
    showCouponDetailPopup: Boolean = false,
    onCouponDetailClick: () -> Unit = {},
    onDismissCouponDetail: () -> Unit = {}
) {
    val numberFormat = rememberKoreanNumberFormat()
    val locale = LocalLocale.current.platformLocale

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 타임딜 띠
        if (product.isDealActive && product.dealEndAt != null) {
            DealBand(dealEndAtMillis = product.dealEndAt)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 판매자 정보
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onCreatorClick() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.creator.avatarThumbUrl ?: product.creator.avatarUrl,
                contentDescription = "${product.creator.displayName} 프로필",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder),
                fallback = painterResource(R.drawable.ic_profile_placeholder)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = product.creator.displayName ?: product.creator.username,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize
                ),
                fontWeight = FontWeight.Medium
            )
            VerifiedBadge(role = product.creator.role, size = 14.dp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 품절임박 배지
        if (product.stock in 1..10) {
            Text(
                text = "${product.stock}개 남음",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
        }

        val membershipBadgeId = "membershipBadge"
        val nameText = buildAnnotatedString {
            if (product.requiredTier != "FREE") {
                appendInlineContent(membershipBadgeId, "멤버십 ")
                append(" ")
            }
            append(product.name)
        }
        val membershipInlineContent = mapOf(
            membershipBadgeId to InlineTextContent(
                placeholder = Placeholder(
                    width = 46.sp,
                    height = 18.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = HaloGradient,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "멤버십",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
        Text(
            text = nameText,
            inlineContent = membershipInlineContent,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        // 별점 + 리뷰 수 + 판매 수
        if (reviewSummary != null && reviewSummary.totalReviews > 0) {
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onReviewClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val ratingTextStyle = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = MaterialTheme.typography.bodySmall.fontSize
                )
                Icon(
                    painter = painterResource(R.drawable.ic_star_filled),
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .offset(y = (-0.5).dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = String.format(locale, "%.1f", reviewSummary.avgRating),
                    style = ratingTextStyle,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "리뷰 ${numberFormat.format(reviewSummary.totalReviews)}개",
                    style = ratingTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.Underline
                )
                if (product.salesCount > 0) {
                    Text(
                        text = " · ",
                        style = ratingTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "구매 ${numberFormat.format(product.salesCount)}",
                        style = ratingTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 가격 영역
        val hasDiscount = product.originalPrice != null && product.discountRate != null && product.discountRate > 0

        if (hasDiscount) {
            // 할인 가격 + 원래 가격
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "${product.discountRate}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Normal,
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
        }

        // 판매가
        Text(
            text = "${product.price.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: product.price}원",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (product.isDealActive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
            modifier = if (hasDiscount) Modifier.offset(y = (-4).dp) else Modifier
        )

        // 쿠폰 적용가
        if (bestCoupon != null && couponAppliedPrice != null && couponAppliedPrice > 0) {
            Column(
                modifier = Modifier.offset(y = if (hasDiscount) (-12).dp else (-8).dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onCouponDetailClick() }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${numberFormat.format(couponAppliedPrice)}원",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "쿠폰적용가",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.offset(y = 1.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_info_circle),
                        contentDescription = "쿠폰 상세 내역",
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = (-3).dp, y = 2.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    if (showCouponDetailPopup) {
                        CouponPriceDetailPopup(
                            originalPrice = product.originalPrice?.toIntOrNull(),
                            price = product.price.toIntOrNull() ?: 0,
                            coupon = bestCoupon,
                            onDismiss = onDismissCouponDetail
                        )
                    }
                }
            }
        }

        // 태그
        val hasCouponPrice = bestCoupon != null && couponAppliedPrice != null && couponAppliedPrice > 0
        if (product.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(if (hasCouponPrice) 0.dp else 6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                product.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun previewProduct(
    requiredTier: String = "FREE",
    canAccess: Boolean = true,
    dealPrice: String? = null
) = Product(
    id = 1,
    name = "프리미엄 무선 이어폰 Pro Max",
    description = "",
    price = dealPrice ?: "89000",
    originalPrice = "129000",
    discountRate = if (dealPrice != null) 46 else 31,
    dealPrice = dealPrice,
    dealEndAt = dealPrice?.let { System.currentTimeMillis() + 3_600_000L },
    isDealActive = dealPrice != null,
    imageUrls = emptyList(),
    brandName = "SoundWave",
    tags = listOf("이어폰", "무선"),
    stock = 50,
    status = "ACTIVE",
    isActive = true,
    viewCount = 1234,
    likeCount = 89,
    salesCount = 567,
    creator = previewUserSummary(displayName = "크리에이터 샵", role = "CREATOR"),
    requiredTier = requiredTier,
    canAccess = canAccess
)

private val previewSummary = ReviewSummary(
    avgRating = 4.5,
    totalReviews = 128,
    ratingDistribution = mapOf(5 to 72, 4 to 35, 3 to 12, 2 to 6, 1 to 3)
)

private val previewCoupon = Coupon(
    id = 1,
    creatorId = 1,
    creatorName = "크리에이터 샵",
    code = "WELCOME15",
    name = "신규 15% 할인",
    type = CouponType.PERCENTAGE,
    discountValue = "15",
    minOrderAmount = "50000",
    maxDiscountAmount = "20000",
    status = UserCouponStatus.AVAILABLE,
    expiresAt = System.currentTimeMillis() + 7 * 86_400_000L,
    daysRemaining = 7
)

@HaloLightDarkPreview
@Composable
private fun ProductInfoSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductInfoSection(
                    product = previewProduct(),
                    reviewSummary = previewSummary,
                    onCreatorClick = {},
                    onReviewClick = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductInfoSectionWithCouponPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductInfoSection(
                    product = previewProduct(),
                    reviewSummary = previewSummary,
                    onCreatorClick = {},
                    onReviewClick = {},
                    bestCoupon = previewCoupon,
                    couponAppliedPrice = 75650
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductInfoSectionDealPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductInfoSection(
                    product = previewProduct(dealPrice = "69000"),
                    reviewSummary = null,
                    onCreatorClick = {},
                    onReviewClick = {}
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductInfoSectionLockedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductInfoSection(
                    product = previewProduct(requiredTier = "TIER2", canAccess = false),
                    reviewSummary = previewSummary,
                    onCreatorClick = {},
                    onReviewClick = {}
                )
            }
        }
    }
}
