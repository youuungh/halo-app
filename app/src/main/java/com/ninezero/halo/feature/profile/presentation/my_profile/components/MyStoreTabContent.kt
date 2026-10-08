package com.ninezero.halo.feature.profile.presentation.my_profile.components

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
import androidx.compose.material3.MaterialTheme
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
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.section.ProductThumbnail
import com.ninezero.halo.core.ui.components.section.ProductThumbnailRow
import com.ninezero.halo.core.ui.components.section.ProductThumbnailSkeleton
import com.ninezero.halo.core.ui.components.section.SectionHeader
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.ProductSortChip
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileIntent
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileState

@Composable
fun MyStoreTabContent(
    state: MyProfileState,
    listState: LazyListState,
    onIntent: (MyProfileIntent) -> Unit,
    onProductClick: (Int) -> Unit
) {
    val productSections = state.storeProductSections
    val products = state.storeProducts
    val isLoading = state.isStoreLoading

    LaunchedEffect(Unit) {
        if (products.isEmpty() && productSections.isEmpty() && !isLoading) {
            onIntent(MyProfileIntent.LoadStoreProducts)
        }
    }

    if (isLoading && products.isEmpty() && productSections.isEmpty()) {
        // 초기 로딩 스켈레톤
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight + 16.dp, bottom = LocalBottomBarPadding.current)
        ) {
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
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = LocalBottomBarPadding.current)
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
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = LocalBottomBarPadding.current)
        ) {
            // 상단: 태그 섹션
            if (productSections.isNotEmpty()) {
                productSections.forEach { section ->
                    item(key = "section_header_${section.tag.id}") {
                        SectionHeader(
                            title = section.tag.name,
                            showViewAll = section.hasMore,
                            onViewAllClick = {
                                onIntent(
                                    MyProfileIntent.NavigateToSectionDetail(
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
                            onProductClick = { product -> onProductClick(product.id) }
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
                                onIntent(MyProfileIntent.ChangeStoreSortType(sortType))
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
                                    onClick = { onProductClick(product.id) },
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
                                    onIntent(MyProfileIntent.LoadMoreStoreProducts)
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
