package com.ninezero.halo.feature.profile.presentation.section_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.section.PostThumbnailListItem
import com.ninezero.halo.core.ui.components.section.ProductThumbnail
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.store.domain.model.Product
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
internal fun PostsList(
    posts: List<Post>,
    isLoading: Boolean,
    listState: LazyListState,
    headerHeight: Dp,
    hazeState: HazeState,
    onPostClick: (Post) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            top = headerHeight + 8.dp,
            bottom = 16.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
    ) {
        items(
            items = posts,
            key = { "post_${it.id}" }
        ) { post ->
            PostThumbnailListItem(
                post = post,
                onClick = { onPostClick(post) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        if (isLoading && posts.isNotEmpty()) {
            item(key = "loading") {
                ListLoadMoreIndicator()
            }
        }
    }
}

@Composable
internal fun ProductsGrid(
    products: List<Product>,
    isLoading: Boolean,
    gridState: LazyGridState,
    headerHeight: Dp,
    hazeState: HazeState,
    onProductClick: (Product) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = headerHeight + 8.dp,
            bottom = 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
    ) {
        items(
            items = products,
            key = { "product_${it.id}" }
        ) { product ->
            ProductThumbnail(
                product = product,
                onClick = { onProductClick(product) },
                width = null
            )
        }

        if (isLoading && products.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                ListLoadMoreIndicator()
            }
        }
    }
}
