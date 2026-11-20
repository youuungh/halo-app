package com.ninezero.halo.feature.store.presentation.product_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.DealBand
import com.ninezero.halo.core.ui.components.badge.MembershipBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.coupon.domain.model.Coupon
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.domain.model.UserCouponStatus
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.feature.store.domain.model.ReviewSummary
import com.ninezero.halo.feature.store.domain.model.earnRatePercent
import com.ninezero.halo.feature.store.presentation.components.StarRating
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.subscription.domain.model.UserTierInfo

internal fun UserTierInfo?.earnRatePercent(): Int = earnRatePercent(this?.tier)

@Composable
fun ProductInfoSection(
    product: Product,
    reviewSummary: ReviewSummary?,
    bestCoupon: Coupon?,
    couponAppliedPrice: Int?,
    isCouponDetailExpanded: Boolean,
    userTierInfo: UserTierInfo?,
    onCreatorClick: () -> Unit,
    onReviewClick: () -> Unit,
    onCouponDetailToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = rememberKoreanNumberFormat()
    val locale = LocalLocale.current.platformLocale

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 타임딜 띠
        if (product.isDealActive && product.dealEndAt != null) {
            DealBand(dealEndAtMillis = product.dealEndAt)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 판매자 정보 + 찜·조회 수
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onCreatorClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    model = product.creator.avatarThumbUrl ?: product.creator.avatarUrl,
                    size = 24.dp,
                    contentDescription = "${product.creator.displayName} 프로필"
                )
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = product.creator.displayName ?: product.creator.username,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    fontWeight = FontWeight.Medium
                )
                VerifiedBadge(role = product.creator.role, size = 14.dp)
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(R.drawable.ic_chart_column),
                contentDescription = "조회수",
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = numberFormat.format(product.viewCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 멤버십 · 품절임박 태그는 이름 위 한 줄
        val isMembershipOnly = product.requiredTier != SubscriptionPlanTier.FREE
        val isLowStock = product.stock in 1..10
        if (isMembershipOnly || isLowStock) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isMembershipOnly) {
                    MembershipBadge()
                }
                if (isLowStock) {
                    Text(
                        text = "${product.stock}개 남음",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name,
            style = MaterialTheme.typography.titleLarge.copy(lineHeight = 26.sp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        // 별점 + 리뷰 수
        if (reviewSummary != null && reviewSummary.totalReviews > 0) {
            val ratingTextStyle = MaterialTheme.typography.bodySmall.copy(
                lineHeight = MaterialTheme.typography.bodySmall.fontSize
            )
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onReviewClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarRating(rating = reviewSummary.avgRating, starSize = 14.dp)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = String.format(locale, "%.1f", reviewSummary.avgRating),
                    style = ratingTextStyle,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${numberFormat.format(reviewSummary.totalReviews)})",
                    style = ratingTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 가격 영역
        val hasDiscount = product.originalPrice != null && product.discountRate != null && product.discountRate > 0
        val hasCouponPrice = bestCoupon != null && couponAppliedPrice != null && couponAppliedPrice > 0
        val priceStyle = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, lineHeight = 30.sp)

        if (hasDiscount) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${product.discountRate}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = "${numberFormat.format(product.originalPrice)}원",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }

        // 판매가
        Text(
            text = "${numberFormat.format(product.price)}원",
            style = priceStyle,
            fontWeight = FontWeight.Bold,
            color = if (product.isDealActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )

        // 쿠폰 적용가
        if (bestCoupon != null && hasCouponPrice) {
            val arrowRotation by animateFloatAsState(
                targetValue = if (isCouponDetailExpanded) 180f else 0f,
                label = "couponArrow"
            )
            Row(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onCouponDetailToggle() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${numberFormat.format(couponAppliedPrice)}원",
                    style = priceStyle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "쿠폰적용가",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down_small),
                    contentDescription = if (isCouponDetailExpanded) "쿠폰 내역 접기" else "쿠폰 내역 펼치기",
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = arrowRotation },
                    tint = MaterialTheme.colorScheme.error
                )
            }

            AnimatedVisibility(
                visible = isCouponDetailExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                CouponPriceDetail(
                    originalPrice = product.originalPrice?.toInt(),
                    price = product.price.toInt(),
                    coupon = bestCoupon,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        ProductBenefits(product = product, userTierInfo = userTierInfo)

        // 태그
        if (product.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                product.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
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

@Composable
private fun ProductBenefits(
    product: Product,
    userTierInfo: UserTierInfo?
) {
    val price = product.price.toInt()
    if (price / 100 <= 0) return

    if (userTierInfo?.isSubscribed == true) {
        MembershipBenefits(product = product, price = price, userTierInfo = userTierInfo)
    } else {
        DefaultBenefits(product = product, price = price)
    }
}

@Composable
private fun DefaultBenefits(product: Product, price: Int) {
    val numberFormat = rememberKoreanNumberFormat()
    val shippingFee = product.shippingFee.toInt()

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        BenefitRow(iconRes = R.drawable.ic_point) {
            Text(
                text = "구매 시 ${numberFormat.format(price / 100)}P 적립 (1%)",
                style = MaterialTheme.typography.bodySmall
            )
        }
        BenefitRow(iconRes = R.drawable.ic_shipping) {
            Text(
                text = if (shippingFee > 0) {
                    "배송비 ${numberFormat.format(shippingFee)}원 (${numberFormat.format(Constants.Commerce.FREE_SHIPPING_THRESHOLD)}원 이상 무료배송)"
                } else {
                    "무료배송"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MembershipBenefits(
    product: Product,
    price: Int,
    userTierInfo: UserTierInfo
) {
    val numberFormat = rememberKoreanNumberFormat()
    val earnRate = userTierInfo.earnRatePercent()
    val shippingFee = product.shippingFee.toInt()
    val highlightBrush = MembershipColors.textBrush
    val highlight = SpanStyle(brush = highlightBrush, fontWeight = FontWeight.Bold)
    val labelColor = MembershipColors.label

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = MembershipColors.cardBrush,
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_sparkle),
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = labelColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${userTierInfo.planName ?: "멤버십"} 혜택",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    brush = highlightBrush
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = labelColor
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "적용 중",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = labelColor
            )
        }

        BenefitRow(iconRes = R.drawable.ic_point) {
            Text(
                text = buildAnnotatedString {
                    append("구매 시 ")
                    withStyle(highlight) {
                        append("${numberFormat.format(price * earnRate / 100)}P")
                    }
                    append(" 적립 (${earnRate}%)")
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
        BenefitRow(iconRes = R.drawable.ic_shipping) {
            Text(
                text = buildAnnotatedString {
                    if (shippingFee > 0) {
                        withStyle(
                            SpanStyle(
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            append("${numberFormat.format(shippingFee)}원")
                        }
                        append(" ")
                    }
                    withStyle(highlight) {
                        append("무료배송")
                    }
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun BenefitRow(
    iconRes: Int,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}

private fun previewProduct(
    requiredTier: SubscriptionPlanTier = SubscriptionPlanTier.FREE,
    canAccess: Boolean = true,
    dealPrice: Long? = null
) = Product(
    id = 1,
    name = "프리미엄 무선 이어폰 Pro Max",
    description = "",
    price = dealPrice ?: 89000L,
    originalPrice = 129000L,
    discountRate = if (dealPrice != null) 46 else 31,
    dealPrice = dealPrice,
    dealEndAt = dealPrice?.let { System.currentTimeMillis() + 3_600_000L },
    isDealActive = dealPrice != null,
    imageUrls = emptyList(),
    brandName = "SoundWave",
    tags = listOf("이어폰", "무선"),
    stock = 50,
    status = ProductStatus.ACTIVE,
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
    discountValue = 15L,
    minOrderAmount = 50000L,
    maxDiscountAmount = 20000L,
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
                    bestCoupon = null,
                    couponAppliedPrice = null,
                    isCouponDetailExpanded = false,
                    userTierInfo = null,
                    onCreatorClick = {},
                    onReviewClick = {},
                    onCouponDetailToggle = {}
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
                    bestCoupon = previewCoupon,
                    couponAppliedPrice = 75650,
                    isCouponDetailExpanded = true,
                    userTierInfo = null,
                    onCreatorClick = {},
                    onReviewClick = {},
                    onCouponDetailToggle = {}
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
                    product = previewProduct(dealPrice = 69000L),
                    reviewSummary = null,
                    bestCoupon = null,
                    couponAppliedPrice = null,
                    isCouponDetailExpanded = false,
                    userTierInfo = null,
                    onCreatorClick = {},
                    onReviewClick = {},
                    onCouponDetailToggle = {}
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
                    product = previewProduct(requiredTier = SubscriptionPlanTier.TIER2, canAccess = false),
                    reviewSummary = previewSummary,
                    bestCoupon = null,
                    couponAppliedPrice = null,
                    isCouponDetailExpanded = false,
                    userTierInfo = null,
                    onCreatorClick = {},
                    onReviewClick = {},
                    onCouponDetailToggle = {}
                )
            }
        }
    }
}
