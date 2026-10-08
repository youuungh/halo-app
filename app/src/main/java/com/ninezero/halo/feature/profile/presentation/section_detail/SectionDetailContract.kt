package com.ninezero.halo.feature.profile.presentation.section_detail

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.store.domain.model.Product

data class SectionDetailState(
    val sectionType: SectionType = SectionType.PINNED_POST,
    val creatorId: Int = 0,
    val tagId: Int? = null,
    val title: String = "",
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val products: List<Product> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface SectionDetailIntent {
    data class Initialize(
        val sectionType: SectionType,
        val creatorId: Int,
        val tagId: Int?,
        val title: String
    ) : SectionDetailIntent
    data object LoadMore : SectionDetailIntent
    data object Refresh : SectionDetailIntent
    data class NavigateToPostDetail(val postId: Int) : SectionDetailIntent
    data class NavigateToProductDetail(val productId: Int) : SectionDetailIntent
    data class ShowRestrictedContentSnackbar(val message: String) : SectionDetailIntent
    data object SnackbarDismissed : SectionDetailIntent
}

sealed interface SectionDetailSideEffect {
    data class NavigateToPostDetail(val postId: Int) : SectionDetailSideEffect
    data class NavigateToProductDetail(val productId: Int) : SectionDetailSideEffect
}
