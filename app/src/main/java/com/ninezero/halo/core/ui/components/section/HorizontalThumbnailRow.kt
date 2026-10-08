package com.ninezero.halo.core.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.modifiers.blockHorizontalScrollLeak
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.ui.theme.HaloTheme

/** 포스트 썸네일 가로 행 */
@Composable
fun PostThumbnailRow(
    posts: List<Post>,
    onPostClick: (Post) -> Unit,
    modifier: Modifier = Modifier,
    thumbnailWidth: Dp = 270.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp)
) {
    LazyRow(
        modifier = modifier.blockHorizontalScrollLeak(),
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement
    ) {
        items(
            items = posts,
            key = { post -> "post_thumb_${post.id}" }
        ) { post ->
            PostThumbnail(
                post = post,
                onClick = { onPostClick(post) },
                width = thumbnailWidth
            )
        }
    }
}

/** 상품 썸네일 가로 행 */
@Composable
fun ProductThumbnailRow(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
    thumbnailWidth: Dp = 140.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp)
) {
    LazyRow(
        modifier = modifier.blockHorizontalScrollLeak(),
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement
    ) {
        items(
            items = products,
            key = { product -> "product_thumb_${product.id}" }
        ) { product ->
            ProductThumbnail(
                product = product,
                onClick = { onProductClick(product) },
                width = thumbnailWidth
            )
        }
    }
}

/** 포스트 썸네일 행 스켈레톤 */
@Composable
fun PostThumbnailRowSkeleton(
    modifier: Modifier = Modifier,
    count: Int = 3,
    thumbnailWidth: Dp = 270.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp)
) {
    Column(modifier = modifier) {
        SectionHeaderSkeleton()
        LazyRow(
            modifier = Modifier.blockHorizontalScrollLeak(),
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement
        ) {
            items(count) {
                PostThumbnailSkeleton(width = thumbnailWidth)
            }
        }
    }
}

private val previewAuthor = Author(
    id = 1,
    username = "creator",
    displayName = "크리에이터",
    avatarUrl = null
)

private val previewPosts = List(5) { index ->
    Post(
        id = index + 1,
        content = "오늘의 일상을 공유합니다! 정말 좋은 하루였어요 #일상 #데일리",
        author = previewAuthor,
        mediaAttachments = emptyList(),  // Preview에서 placeholder로 테스트
        likeCount = (100 + index * 50),
        commentCount = (10 + index * 5),
        viewCount = (1000 + index * 500),
        createdAt = System.currentTimeMillis() - (index * 86400000L),
    )
}

private val previewCreator = UserSummary(
    id = 1,
    username = "creator",
    displayName = "크리에이터",
    avatarUrl = null
)

private val previewProducts = List(5) { index ->
    Product(
        id = index + 1,
        name = "테스트 상품 $index",
        description = "설명",
        price = "${10000 + index * 5000}.00",
        originalPrice = if (index % 2 == 0) "${15000 + index * 5000}.00" else null,
        discountRate = if (index % 2 == 0) 20 else null,
        imageUrls = if (index % 3 != 0) listOf("https://example.com/product$index.jpg") else emptyList(),
        brandName = "브랜드",
        tags = listOf("태그1"),
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
}

@HaloLightDarkPreview
@Composable
private fun PostThumbnailRowPreview() {
    HaloTheme {
        Column {
            SectionHeader(title = "고정 포스트", onViewAllClick = {})
            PostThumbnailRow(
                posts = previewPosts,
                onPostClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProductThumbnailRowPreview() {
    HaloTheme {
        Column {
            SectionHeader(title = "인기 상품", onViewAllClick = {})
            ProductThumbnailRow(
                products = previewProducts,
                onProductClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PostThumbnailRowSkeletonPreview() {
    HaloTheme {
        PostThumbnailRowSkeleton()
    }
}
