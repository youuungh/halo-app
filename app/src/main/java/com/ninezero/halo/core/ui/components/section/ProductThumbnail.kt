package com.ninezero.halo.core.ui.components.section

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.DealCountdownImageOverlay
import com.ninezero.halo.core.ui.components.badge.MembershipBadge
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.core.ui.theme.SoldOutBgDark
import com.ninezero.halo.core.ui.theme.SoldOutBgLight
import com.ninezero.halo.core.ui.theme.SoldOutTextDark
import com.ninezero.halo.core.ui.theme.SoldOutTextLight
import com.ninezero.halo.core.ui.theme.StarYellow
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductStatus
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/** 상품 썸네일 */
@Composable
fun ProductThumbnail(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = 140.dp
) {
    val isDarkTheme = LocalIsDarkTheme.current
    val numberFormat = rememberKoreanNumberFormat()
    val hazeState = remember { HazeState() }
    val thumbnailUrl = product.imageUrls.firstOrNull()
    val isDiscontinued = product.status == ProductStatus.DISCONTINUED
    val isSoldOut = product.stock == 0 || product.status == ProductStatus.SOLD_OUT
    val isMembershipOnly = product.requiredTier != SubscriptionPlanTier.FREE

    Column(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .bounceClick()
            // 하단 라운드는 chip과 일치
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) { onClick() }
    ) {
        // 이미지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnailUrl != null) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = "상품 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_image_placeholder),
                    contentDescription = "이미지 없음",
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    contentScale = ContentScale.Crop
                )
            }

            // 좌측 상단 뱃지
            val isLargeTag = width == null
            if (product.isDealActive || isMembershipOnly) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.isDealActive) {
                        ThumbnailTag(
                            text = "타임딜",
                            large = isLargeTag,
                            modifier = Modifier.background(MaterialTheme.colorScheme.error, ThumbnailTagShape)
                        )
                    }
                    if (isMembershipOnly) {
                        MembershipBadge(onPhoto = true, large = isLargeTag)
                    }
                }
            }

            if (product.isDealActive && product.dealEndAt != null) {
                DealCountdownImageOverlay(
                    dealEndAtMillis = product.dealEndAt,
                    hazeState = hazeState,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 상품명
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 할인율 + 가격
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (product.discountRate != null && product.discountRate > 0) {
                Text(
                    text = "${product.discountRate}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (product.isDealActive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Text(
                text = "${numberFormat.format(product.price)}원",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (product.isDealActive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alignByBaseline()
            )
        }

        // 별점, 리뷰 없으면 생략
        if (product.reviewCount > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_filled),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = StarYellow
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "%.1f".format(product.rating),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " (${product.reviewCount})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 태그 또는 품절/판매종료
        if (isDiscontinued) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isDarkTheme) SoldOutBgDark else SoldOutBgLight,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "판매종료",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkTheme) SoldOutTextDark else SoldOutTextLight,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (isSoldOut) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isDarkTheme) SoldOutBgDark else SoldOutBgLight,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "품절",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkTheme) SoldOutTextDark else SoldOutTextLight,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (product.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                product.tags.take(2).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** 상품 썸네일 스켈레톤 */
@Composable
fun ProductThumbnailSkeleton(
    modifier: Modifier = Modifier,
    width: Dp = 140.dp
) {
    Column(
        modifier = modifier.width(width)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(16.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(20.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .height(14.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(2.dp))
    }
}

/** 타임딜 뱃지 */
@Composable
private fun ThumbnailTag(text: String, large: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = if (large) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = modifier.padding(horizontal = if (large) 7.dp else 6.dp, vertical = if (large) 3.dp else 2.dp)
    )
}

private val ThumbnailTagShape = RoundedCornerShape(5.dp)

private val previewCreator = UserSummary(
    id = 1,
    username = "creator",
    displayName = "크리에이터",
    avatarUrl = null
)

private val previewProduct = Product(
    id = 1,
    name = "테스트 상품입니다 아주 긴 이름의 상품",
    description = "설명",
    price = 29000L,
    originalPrice = null,
    discountRate = null,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("인기", "추천"),
    stock = 100,
    status = ProductStatus.ACTIVE,
    isActive = true,
    viewCount = 100,
    likeCount = 10,
    salesCount = 5,
    creator = previewCreator,
    requiredTier = SubscriptionPlanTier.FREE,
    canAccess = true,
)

private val previewDiscountProduct = Product(
    id = 2,
    name = "할인 상품",
    description = "설명",
    price = 19000L,
    originalPrice = 29000L,
    discountRate = 34,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("세일"),
    stock = 50,
    status = ProductStatus.ACTIVE,
    isActive = true,
    viewCount = 200,
    likeCount = 20,
    salesCount = 15,
    creator = previewCreator,
    requiredTier = SubscriptionPlanTier.FREE,
    canAccess = true,
)

private val previewDealProduct = Product(
    id = 6,
    name = "타임딜 상품",
    description = "설명",
    price = 15000L,
    originalPrice = 25000L,
    discountRate = 40,
    dealPrice = 15000L,
    dealStartAt = System.currentTimeMillis() - 3_600_000L,
    dealEndAt = System.currentTimeMillis() + 183_165_000L,
    isDealActive = true,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("세일"),
    stock = 30,
    status = ProductStatus.ACTIVE,
    isActive = true,
    viewCount = 300,
    likeCount = 40,
    salesCount = 25,
    creator = previewCreator,
    requiredTier = SubscriptionPlanTier.FREE,
    canAccess = true,
)

private val previewMembershipProduct = Product(
    id = 3,
    name = "멤버십 전용 상품",
    description = "설명",
    price = 50000L,
    originalPrice = null,
    discountRate = null,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("프리미엄"),
    stock = 10,
    status = ProductStatus.ACTIVE,
    isActive = true,
    viewCount = 50,
    likeCount = 5,
    salesCount = 2,
    creator = previewCreator,
    requiredTier = SubscriptionPlanTier.TIER2,
    canAccess = false,
)

private val previewSoldOutProduct = Product(
    id = 4,
    name = "품절 상품",
    description = "설명",
    price = 35000L,
    originalPrice = null,
    discountRate = null,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("인기"),
    stock = 0,
    status = ProductStatus.ACTIVE,
    isActive = true,
    viewCount = 500,
    likeCount = 100,
    salesCount = 200,
    creator = previewCreator,
    requiredTier = SubscriptionPlanTier.FREE,
    canAccess = true,
)

private val previewDiscontinuedProduct = Product(
    id = 5,
    name = "판매종료 상품",
    description = "설명",
    price = 45000L,
    originalPrice = null,
    discountRate = null,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("한정판"),
    stock = 0,
    status = ProductStatus.DISCONTINUED,
    isActive = true,
    viewCount = 1000,
    likeCount = 200,
    salesCount = 500,
    creator = previewCreator,
    requiredTier = SubscriptionPlanTier.FREE,
    canAccess = true,
)

@HaloLightDarkPreview
@Composable
private fun ProductThumbnailPreview() {
    HaloTheme {
        Surface {
            ProductThumbnail(
                product = previewProduct,
                onClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductThumbnailDiscountPreview() {
    HaloTheme {
        Surface {
            ProductThumbnail(
                product = previewDiscountProduct,
                onClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductThumbnailDealPreview() {
    HaloTheme {
        Surface {
            ProductThumbnail(
                product = previewDealProduct,
                onClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductThumbnailMembershipPreview() {
    HaloTheme {
        Surface {
            ProductThumbnail(
                product = previewMembershipProduct,
                onClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductThumbnailSoldOutPreview() {
    HaloTheme {
        Surface {
            ProductThumbnail(
                product = previewSoldOutProduct,
                onClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductThumbnailDiscontinuedPreview() {
    HaloTheme {
        Surface {
            ProductThumbnail(
                product = previewDiscontinuedProduct,
                onClick = {}
            )
        }
    }
}
