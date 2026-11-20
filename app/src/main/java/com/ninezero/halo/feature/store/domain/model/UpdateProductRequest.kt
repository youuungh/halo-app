package com.ninezero.halo.feature.store.domain.model

import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: String? = null,
    val originalPrice: String? = null,
    val stock: Int? = null,
    val categoryId: Int? = null,
    val brandName: String? = null,
    val images: List<ProductImageRef>? = null,
    val detailContent: List<DetailContentBlock>? = null,
    val tags: List<String>? = null,
    val status: ProductStatus? = null,
    val requiredTier: SubscriptionPlanTier? = null,
    val tagIds: List<Int>? = null,
    val sectionTagId: Int? = null
)
