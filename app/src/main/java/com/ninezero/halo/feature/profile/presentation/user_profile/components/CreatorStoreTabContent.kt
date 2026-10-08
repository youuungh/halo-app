package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.section.ProductThumbnail
import com.ninezero.halo.core.ui.components.section.ProductThumbnailRow
import com.ninezero.halo.core.ui.components.section.ProductThumbnailSkeleton
import com.ninezero.halo.core.ui.components.section.SectionHeader
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.profile.domain.model.ProductTagSection
import com.ninezero.halo.feature.profile.domain.model.SectionTag
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.ProductSortChip
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileIntent
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileState
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun CreatorStoreTabContent(
    state: UserProfileState,
    listState: LazyListState,
    onIntent: (UserProfileIntent) -> Unit,
    onProductClick: (Int) -> Unit
) {
    val productSections = state.storeProductSections
    val products = state.storeProducts
    val isLoading = state.isStoreLoading

    // 차단한 사용자
    if (state.profile?.isBlocked == true) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
        ) {
            item(key = "blocked_message") { BlockedUserPlaceholder() }
        }
        return
    }

    LaunchedEffect(Unit) {
        if (products.isEmpty() && productSections.isEmpty() && !isLoading) {
            onIntent(UserProfileIntent.LoadStoreProducts)
        }
    }

    if (isLoading && products.isEmpty() && productSections.isEmpty()) {
        // 초기 로딩 스켈레톤
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight + 16.dp, bottom = 16.dp)
        ) {
            // 상품 그리드 스켈레톤
            items(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductThumbnailSkeleton(modifier = Modifier.weight(1f))
                    ProductThumbnailSkeleton(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    } else if (products.isEmpty() && productSections.isEmpty()) {
        // 빈 상태
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
        ) {
            item(key = "empty_store") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 상품이 없어요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
        ) {
            // 태그 섹션, 최대 3개
            if (productSections.isNotEmpty()) {
                productSections.forEach { section ->
                    item(key = "section_header_${section.tag.id}") {
                        SectionHeader(
                            title = section.tag.name,
                            showViewAll = section.hasMore,
                            onViewAllClick = {
                                onIntent(
                                    UserProfileIntent.NavigateToSectionDetail(
                                        sectionType = SectionType.PRODUCT_TAG,
                                        tagId = section.tag.id,
                                        title = section.tag.name
                                    )
                                )
                            }
                        )
                    }

                    item(key = "section_products_${section.tag.id}") {
                        ProductThumbnailRow(
                            products = section.products,
                            onProductClick = { product ->
                                if (product.canAccess == false) {
                                    onIntent(
                                        UserProfileIntent.ShowRestrictedContentSnackbar(
                                            "${product.requiredTier} 멤버십 전용 상품입니다"
                                        )
                                    )
                                } else {
                                    onProductClick(product.id)
                                }
                            }
                        )
                    }

                    item(key = "section_spacer_${section.tag.id}") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // 하단: 전체 상품 목록
            if (products.isNotEmpty() || isLoading) {
                // 전체 상품 헤더 + 정렬
                item(key = "all_products_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "전체 상품",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        ProductSortChip(
                            currentSort = state.storeSortType,
                            onSortChange = { sortType ->
                                onIntent(UserProfileIntent.ChangeStoreSortType(sortType))
                            }
                        )
                    }
                }

                // 정렬 변경 중 스켈레톤
                if (isLoading && products.isEmpty()) {
                    items(3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProductThumbnailSkeleton(modifier = Modifier.weight(1f))
                            ProductThumbnailSkeleton(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else {
                    // 2열 그리드
                    val chunkedProducts = products.chunked(2)
                    items(
                        items = chunkedProducts,
                        key = { "row_${it.first().id}" }
                    ) { rowProducts ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowProducts.forEach { product ->
                                ProductThumbnail(
                                    product = product,
                                    onClick = {
                                        if (product.canAccess == false) {
                                            onIntent(
                                                UserProfileIntent.ShowRestrictedContentSnackbar(
                                                    "${product.requiredTier} 멤버십 전용 상품입니다"
                                                )
                                            )
                                        } else {
                                            onProductClick(product.id)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    width = null
                                )
                            }
                            if (rowProducts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 추가 로딩
                    if (state.storeHasMore) {
                        item(key = "load_more_trigger") {
                            // 목록 수당 한 번, InfiniteScrollTrigger와 같은 규칙
                            var firedAtCount by remember { mutableStateOf<Int?>(null) }
                            LaunchedEffect(state.isStoreLoading, state.storeProducts.size) {
                                if (!state.isStoreLoading && firedAtCount != state.storeProducts.size) {
                                    firedAtCount = state.storeProducts.size
                                    onIntent(UserProfileIntent.LoadMoreStoreProducts)
                                }
                            }
                        }
                        items(3) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProductThumbnailSkeleton(modifier = Modifier.weight(1f))
                                ProductThumbnailSkeleton(modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

private val previewCreator = UserSummary(
    id = 1,
    username = "creator",
    displayName = "크리에이터",
    avatarUrl = null,
    role = "CREATOR",
)

private val previewProducts = List(6) { index ->
    Product(
        id = index + 1,
        name = "상품 ${index + 1}",
        description = "상품 설명입니다.",
        price = "${10000 + index * 5000}.00",
        originalPrice = if (index % 2 == 0) "${15000 + index * 5000}.00" else null,
        discountRate = if (index % 2 == 0) 20 else null,
        imageUrls = emptyList(),
        brandName = "브랜드",
        tags = listOf("태그1", "태그2"),
        stock = if (index == 2) 0 else 100,
        status = if (index == 2) "SOLD_OUT" else "ACTIVE",
        isActive = true,
        viewCount = 100,
        likeCount = 10,
        salesCount = 5,
        creator = previewCreator,
        requiredTier = if (index == 3) "TIER2" else "FREE",
        canAccess = index != 3,
    )
}

private val previewProductSections = listOf(
    ProductTagSection(
        tag = SectionTag(
            id = 1,
            name = "인기 상품"
        ),
        products = previewProducts.take(4),
        hasMore = true
    ),
    ProductTagSection(
        tag = SectionTag(
            id = 2,
            name = "신상품"
        ),
        products = previewProducts.drop(2).take(4),
        hasMore = false
    )
)

@HaloLightDarkPreview
@Composable
private fun CreatorStoreTabContentPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CreatorStoreTabContent(
                state = UserProfileState(
                    storeProductSections = previewProductSections,
                    storeProducts = previewProducts
                ),
                listState = rememberLazyListState(),
                onIntent = {},
                onProductClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorStoreTabContentGridPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CreatorStoreTabContent(
                state = UserProfileState(
                    storeProductSections = emptyList(),
                    storeProducts = previewProducts
                ),
                listState = rememberLazyListState(),
                onIntent = {},
                onProductClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorStoreTabContentLoadingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CreatorStoreTabContent(
                state = UserProfileState(
                    isStoreLoading = true,
                    storeProducts = emptyList()
                ),
                listState = rememberLazyListState(),
                onIntent = {},
                onProductClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorStoreTabContentEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CreatorStoreTabContent(
                state = UserProfileState(
                    storeProductSections = emptyList(),
                    storeProducts = emptyList()
                ),
                listState = rememberLazyListState(),
                onIntent = {},
                onProductClick = {}
            )
        }
    }
}
