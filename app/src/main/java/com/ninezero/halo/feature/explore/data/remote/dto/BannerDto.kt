package com.ninezero.halo.feature.explore.data.remote.dto

import com.ninezero.halo.feature.explore.domain.model.Banner
import kotlinx.serialization.Serializable

@Serializable
data class BannerDto(
    val id: Int,
    val imageUrl: String,
    val title: String,
    val description: String? = null,
    val link: String,
    val type: String,
    val order: Int
)

@Serializable
data class BannerListResponseDto(
    val banners: List<BannerDto>,
    val totalCount: Int
)

fun BannerDto.toDomain(): Banner = Banner(
    imageUrl = imageUrl,
    title = title,
    description = description,
    link = link
)
