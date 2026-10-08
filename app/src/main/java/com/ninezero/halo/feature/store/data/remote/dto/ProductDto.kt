package com.ninezero.halo.feature.store.data.remote.dto

import com.ninezero.halo.core.network.PagedDto
import com.ninezero.halo.core.network.dto.UserSummaryDto
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.store.domain.model.CreateProductRequest
import com.ninezero.halo.feature.store.domain.model.DetailContentBlock
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.ProductImageRef
import com.ninezero.halo.feature.store.domain.model.RichTextAlignment
import com.ninezero.halo.feature.store.domain.model.RichTextBlockStyle
import com.ninezero.halo.feature.store.domain.model.UpdateProductRequest
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import com.ninezero.halo.feature.tag.data.remote.dto.TagDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.ninezero.halo.feature.tag.data.remote.dto.toDomain as tagToDomain

@Serializable
data class ProductDto(
    val id: Int,
    val creator: UserSummaryDto,
    val name: String,
    val description: String,
    val price: String,
    val originalPrice: String? = null,
    val discountRate: Int? = null,
    val dealPrice: String? = null,
    val dealStartAt: String? = null,
    val dealEndAt: String? = null,
    val isDealActive: Boolean = false,
    val basePrice: String? = null,
    val baseOriginalPrice: String? = null,
    val stock: Int,
    val categoryId: Int? = null,
    val brandName: String? = null,
    val imageUrls: List<String> = emptyList(),
    val detailContent: List<DetailContentBlockDto> = emptyList(),
    val tags: List<String> = emptyList(),
    val sectionTag: TagDto? = null,
    val status: String,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val salesCount: Int = 0,
    val rating: String = "0.0",
    val reviewCount: Int = 0,
    val isActive: Boolean = true,
    val shippingFee: String = "3000",
    val requiredTier: String = "FREE",
    val canAccess: Boolean? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class DetailContentBlockDto(
    val type: String,
    val blockId: String? = null,
    val content: String? = null,
    val url: String? = null,
    val draftId: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val textStyle: String? = null,
    val textAlign: String? = null,
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val underline: Boolean? = null,
    val strikethrough: Boolean? = null,
    val textColor: String? = null
)

fun DetailContentBlockDto.toDomain(): DetailContentBlock = DetailContentBlock(
    type = type,
    blockId = blockId,
    content = content,
    url = url,
    width = width,
    height = height,
    textStyle = RichTextBlockStyle.fromWire(textStyle),
    textAlignment = RichTextAlignment.fromWire(textAlign),
    bold = bold ?: false,
    italic = italic ?: false,
    underline = underline ?: false,
    strikethrough = strikethrough ?: false,
    textColor = textColor
)

fun DetailContentBlock.toDto(): DetailContentBlockDto = DetailContentBlockDto(
    type = type,
    blockId = blockId,
    content = content,
    url = url,
    draftId = draftId,
    width = width,
    height = height,
    textStyle = textStyle.wireValue,
    textAlign = textAlignment.wireValue,
    bold = bold.takeIf { it },
    italic = italic.takeIf { it },
    underline = underline.takeIf { it },
    strikethrough = strikethrough.takeIf { it },
    textColor = textColor
)

fun CreateProductRequest.toDto(): CreateProductRequestDto = CreateProductRequestDto(
    name = name,
    description = description,
    price = price,
    originalPrice = originalPrice,
    stock = stock,
    categoryId = categoryId,
    brandName = brandName,
    imageDraftIds = imageDraftIds,
    detailContent = detailContent?.map { it.toDto() },
    tags = tags,
    requiredTier = requiredTier,
    tagIds = tagIds,
    sectionTagId = sectionTagId
)

fun UpdateProductRequest.toDto(): UpdateProductRequestDto = UpdateProductRequestDto(
    name = name,
    description = description,
    price = price,
    originalPrice = originalPrice,
    stock = stock,
    categoryId = categoryId,
    brandName = brandName,
    images = images?.map { it.toDto() },
    detailContent = detailContent?.map { it.toDto() },
    tags = tags,
    status = status,
    requiredTier = requiredTier,
    tagIds = tagIds,
    sectionTagId = sectionTagId
)

typealias ProductListResponseDto = PagedDto<ProductDto>

@Serializable
data class CreatorStoreSectionsResponseDto(
    val tagSections: List<ProductTagSectionDto>,
    val allProducts: ProductListResponseDto
)

@Serializable
data class ProductTagSectionDto(
    val tag: ProductSectionTagDto,
    @SerialName("products") val items: List<ProductDto>,
    val hasMore: Boolean
)

@Serializable
data class ProductSectionTagDto(
    val id: Int,
    val name: String,
    val targetType: String? = null,
    val isSectionEnabled: Boolean = false,
    val postCount: Int? = null,
    val productCount: Int? = null,
    val createdAt: String? = null
)

fun ProductImageRef.toDto(): ProductImageRefDto = when (this) {
    is ProductImageRef.Existing -> ProductImageRefDto(url = url)
    is ProductImageRef.Draft -> ProductImageRefDto(draftId = draftId)
}

@Serializable
data class ProductImageRefDto(
    val url: String? = null,
    val draftId: Int? = null
)

@Serializable
data class CreateProductRequestDto(
    val name: String,
    val description: String,
    val price: String,
    val originalPrice: String? = null,
    val stock: Int,
    val categoryId: Int? = null,
    val brandName: String? = null,
    val imageDraftIds: List<Int> = emptyList(),
    val detailContent: List<DetailContentBlockDto>? = null,
    val tags: List<String>? = null,
    val requiredTier: String = SubscriptionPlanTier.FREE.name,
    val tagIds: List<Int>? = null,
    val sectionTagId: Int? = null
)

@Serializable
data class UpdateProductRequestDto(
    val name: String? = null,
    val description: String? = null,
    val price: String? = null,
    val originalPrice: String? = null,
    val stock: Int? = null,
    val categoryId: Int? = null,
    val brandName: String? = null,
    val images: List<ProductImageRefDto>? = null,
    val detailContent: List<DetailContentBlockDto>? = null,
    val tags: List<String>? = null,
    val status: String? = null,
    val requiredTier: String? = null,
    val tagIds: List<Int>? = null,
    val sectionTagId: Int? = null
)

@Serializable
data class SetProductDealRequestDto(
    val dealPrice: String? = null,
    val startAt: String? = null,
    val endAt: String? = null
)

fun ProductDto.toDomain(): Product = Product(
    id = id,
    name = name,
    description = description,
    price = price,
    originalPrice = originalPrice,
    discountRate = discountRate,
    dealPrice = dealPrice,
    dealStartAt = dealStartAt?.parseTimestamp(),
    dealEndAt = dealEndAt?.parseTimestamp(),
    isDealActive = isDealActive,
    basePrice = basePrice ?: price,
    baseOriginalPrice = baseOriginalPrice,
    imageUrls = imageUrls,
    detailContent = detailContent.map { it.toDomain() },
    brandName = brandName,
    tags = tags,
    sectionTag = sectionTag?.tagToDomain(),
    stock = stock,
    status = status,
    isActive = isActive,
    viewCount = viewCount,
    likeCount = likeCount,
    salesCount = salesCount,
    creator = creator.toDomain(),
    requiredTier = requiredTier,
    canAccess = canAccess,
    shippingFee = shippingFee,
)
