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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ninezero.halo.core.ui.components.badge.DealTag
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.ui.theme.SoldOutBgDark
import com.ninezero.halo.ui.theme.SoldOutBgLight
import com.ninezero.halo.ui.theme.SoldOutTextDark
import com.ninezero.halo.ui.theme.SoldOutTextLight
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
    val isDiscontinued = product.status == "DISCONTINUED"
    val isSoldOut = product.stock == 0 || product.status == "SOLD_OUT"
    val isMembershipOnly = product.requiredTier != "FREE"

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
                .clip(RoundedCornerShape(8.dp))
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
                    painter = painterResource(R.drawable.ic_img_placeholder),
                    contentDescription = "이미지 없음",
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    contentScale = ContentScale.Crop
                )
            }

            if (product.isDealActive) {
                DealTag(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isMembershipOnly) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = HaloGradient,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 5.dp)
                ) {
                    Text(
                        text = "멤버십",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 할인율 + 가격
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (product.discountRate != null && product.discountRate > 0) {
                Text(
                    text = "${product.discountRate}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Text(
                text = "${product.price.toBigDecimalOrNull()?.let { numberFormat.format(it) } ?: product.price}원",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (product.isDealActive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alignByBaseline()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 태그 또는 품절/판매종료
        if (isDiscontinued) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isDarkTheme) SoldOutBgDark else SoldOutBgLight,
                        shape = RoundedCornerShape(4.dp)
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
                        shape = RoundedCornerShape(4.dp)
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
                                shape = RoundedCornerShape(4.dp)
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
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

// 프리뷰

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
    price = "29000.00",
    originalPrice = null,
    discountRate = null,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("인기", "추천"),
    stock = 100,
    status = "ACTIVE",
    isActive = true,
    viewCount = 100,
    likeCount = 10,
    salesCount = 5,
    creator = previewCreator,
    requiredTier = "FREE",
    canAccess = true,
)

private val previewDiscountProduct = Product(
    id = 2,
    name = "할인 상품",
    description = "설명",
    price = "19000.00",
    originalPrice = "29000.00",
    discountRate = 34,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("세일"),
    stock = 50,
    status = "ACTIVE",
    isActive = true,
    viewCount = 200,
    likeCount = 20,
    salesCount = 15,
    creator = previewCreator,
    requiredTier = "FREE",
    canAccess = true,
)

private val previewDealProduct = Product(
    id = 6,
    name = "타임딜 상품",
    description = "설명",
    price = "15000.00",
    originalPrice = "25000.00",
    discountRate = 40,
    dealPrice = "15000.00",
    dealStartAt = System.currentTimeMillis() - 3_600_000L,
    dealEndAt = System.currentTimeMillis() + 183_165_000L,
    isDealActive = true,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("세일"),
    stock = 30,
    status = "ACTIVE",
    isActive = true,
    viewCount = 300,
    likeCount = 40,
    salesCount = 25,
    creator = previewCreator,
    requiredTier = "FREE",
    canAccess = true,
)

private val previewMembershipProduct = Product(
    id = 3,
    name = "멤버십 전용 상품",
    description = "설명",
    price = "50000.00",
    originalPrice = null,
    discountRate = null,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("프리미엄"),
    stock = 10,
    status = "ACTIVE",
    isActive = true,
    viewCount = 50,
    likeCount = 5,
    salesCount = 2,
    creator = previewCreator,
    requiredTier = "TIER2",
    canAccess = false,
)

private val previewSoldOutProduct = Product(
    id = 4,
    name = "품절 상품",
    description = "설명",
    price = "35000.00",
    originalPrice = null,
    discountRate = null,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("인기"),
    stock = 0,
    status = "ACTIVE",
    isActive = true,
    viewCount = 500,
    likeCount = 100,
    salesCount = 200,
    creator = previewCreator,
    requiredTier = "FREE",
    canAccess = true,
)

private val previewDiscontinuedProduct = Product(
    id = 5,
    name = "판매종료 상품",
    description = "설명",
    price = "45000.00",
    originalPrice = null,
    discountRate = null,
    imageUrls = emptyList(),
    brandName = null,
    tags = listOf("한정판"),
    stock = 0,
    status = "DISCONTINUED",
    isActive = true,
    viewCount = 1000,
    likeCount = 200,
    salesCount = 500,
    creator = previewCreator,
    requiredTier = "FREE",
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
