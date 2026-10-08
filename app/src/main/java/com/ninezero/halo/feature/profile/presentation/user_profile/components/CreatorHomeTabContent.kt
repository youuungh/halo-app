package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.section.PostThumbnailRow
import com.ninezero.halo.core.ui.components.section.PostThumbnailRowSkeleton
import com.ninezero.halo.core.ui.components.section.ProductThumbnailRow
import com.ninezero.halo.core.ui.components.section.SectionHeader
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.profile.domain.model.PostTagSection
import com.ninezero.halo.feature.profile.domain.model.ProductTagSection
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileState
import com.ninezero.halo.feature.store.domain.model.Product

@Composable
fun CreatorHomeTabContent(
    state: UserProfileState,
    listState: LazyListState,
    onPostClick: (Post) -> Unit,
    onProductClick: (Product) -> Unit,
    onViewAllPostSectionClick: (PostTagSection) -> Unit,
    onViewAllProductSectionClick: (ProductTagSection) -> Unit
) {
    val isEmpty = state.pinnedPosts.isEmpty() &&
            state.postSections.isEmpty() &&
            state.productSections.isEmpty()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
    ) {
        // 차단한 사용자
        if (state.profile?.isBlocked == true) {
            item(key = "blocked_message") { BlockedUserPlaceholder() }
            return@LazyColumn
        }

        // 로딩 중에도 기존 콘텐츠 유지
        if (state.isHomeSectionsLoading && isEmpty) {
            item(key = "loading_skeleton") {
                Column {
                    PostThumbnailRowSkeleton()
                    Spacer(modifier = Modifier.height(16.dp))
                    PostThumbnailRowSkeleton()
                }
            }
        }
        // 빈 상태
        else if (isEmpty) {
            item(key = "empty_home") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 콘텐츠가 없어요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // 실제 콘텐츠
        else {
            // 고정 포스트 섹션, 최대 10개
            if (state.pinnedPosts.isNotEmpty()) {
                item(key = "pinned_header") {
                    SectionHeader(
                        title = "고정 포스트",
                        showViewAll = false
                    )
                }
                item(key = "pinned_posts") {
                    PostThumbnailRow(
                        posts = state.pinnedPosts,
                        onPostClick = onPostClick
                    )
                }
                item(key = "pinned_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 포스트 태그 섹션들
            state.postSections.forEachIndexed { index, section ->
                item(key = "post_section_header_$index") {
                    SectionHeader(
                        title = section.tag.name,
                        onViewAllClick = { onViewAllPostSectionClick(section) },
                        showViewAll = section.hasMore
                    )
                }
                item(key = "post_section_posts_$index") {
                    PostThumbnailRow(
                        posts = section.posts,
                        onPostClick = onPostClick
                    )
                }
                item(key = "post_section_spacer_$index") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 상품 태그 섹션들
            state.productSections.forEachIndexed { index, section ->
                item(key = "product_section_header_$index") {
                    SectionHeader(
                        title = section.tag.name,
                        onViewAllClick = { onViewAllProductSectionClick(section) },
                        showViewAll = section.hasMore
                    )
                }
                item(key = "product_section_products_$index") {
                    ProductThumbnailRow(
                        products = section.products,
                        onProductClick = onProductClick
                    )
                }
                item(key = "product_section_spacer_$index") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
