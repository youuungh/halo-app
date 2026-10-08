package com.ninezero.halo.feature.store.domain.model

data class CreateProductRequest(
    val name: String,
    val description: String,
    val price: String,
    val originalPrice: String? = null,
    val stock: Int,
    val categoryId: Int? = null,
    val brandName: String? = null,
    val imageDraftIds: List<Int> = emptyList(),  // 대표 이미지
    val detailContent: List<DetailContentBlock>? = null,
    val tags: List<String>? = null,
    val requiredTier: String = "FREE",
    val tagIds: List<Int>? = null,
    val sectionTagId: Int? = null
)
