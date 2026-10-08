package com.ninezero.halo.feature.store.domain.model

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.tag.domain.model.Tag

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    // 실제 판매가
    val price: String,
    val originalPrice: String?,
    val discountRate: Int?,
    // 타임딜
    val dealPrice: String? = null,
    val dealStartAt: Long? = null,
    val dealEndAt: Long? = null,
    val isDealActive: Boolean = false,
    // 딜 적용 전 원본값
    val basePrice: String = "",
    val baseOriginalPrice: String? = null,
    val imageUrls: List<String>,
    val detailContent: List<DetailContentBlock> = emptyList(),
    val brandName: String?,
    val tags: List<String>,
    val sectionTag: Tag? = null,
    val stock: Int,
    val status: String,
    val isActive: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val salesCount: Int,
    val creator: UserSummary,
    val requiredTier: String,
    val canAccess: Boolean?,
    val shippingFee: String = "3000",
)

fun Product.hasLiveDeal(nowMillis: Long = System.currentTimeMillis()): Boolean =
    dealPrice != null && (dealEndAt ?: 0L) > nowMillis
