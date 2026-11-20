package com.ninezero.halo.feature.store.domain.model

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.tag.domain.model.Tag

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    // 실제 판매가
    val price: Long,
    val originalPrice: Long?,
    val discountRate: Int?,
    // 타임딜
    val dealPrice: Long? = null,
    val dealStartAt: Long? = null,
    val dealEndAt: Long? = null,
    val isDealActive: Boolean = false,
    // 딜 적용 전 원본값
    val basePrice: Long? = null,
    val baseOriginalPrice: Long? = null,
    val imageUrls: List<String>,
    val detailContent: List<DetailContentBlock> = emptyList(),
    val brandName: String?,
    val tags: List<String>,
    val sectionTag: Tag? = null,
    val stock: Int,
    val status: ProductStatus,
    val isActive: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val salesCount: Int,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val creator: UserSummary,
    val requiredTier: SubscriptionPlanTier,
    val canAccess: Boolean?,
    val shippingFee: Long = 3000L,
)

fun Product.hasLiveDeal(nowMillis: Long = System.currentTimeMillis()): Boolean =
    dealPrice != null && (dealEndAt ?: 0L) > nowMillis
