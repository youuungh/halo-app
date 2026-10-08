package com.ninezero.halo.feature.feed.presentation

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.explore.domain.model.Creator
import com.ninezero.halo.feature.feed.domain.model.Post

data class FeedState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val pendingPosts: List<Post> = emptyList(),  // 업로드 중인 내 포스트
    val recommendedCreators: List<Creator> = emptyList(),
    val isCreatorsLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val nextCursor: String? = null,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,
    val centerToastMessage: String? = null,

    // 현재 로그인한 사용자 정보
    val currentUserId: Int? = null,
    val isCurrentUserCreator: Boolean = false,

    // 포스트 옵션 시트
    val selectedPost: Post? = null,
    val showPostOptionsSheet: Boolean = false,

    // 공유 시트
    val showShareSheet: Boolean = false,
    val sharePostId: Int? = null,
    val navigateRoomId: Int? = null
)

sealed interface FeedIntent {
    // 데이터 로딩
    data object LoadFeed : FeedIntent
    data object RefreshFeed : FeedIntent
    data object LoadMorePosts : FeedIntent

    // 포스트 상호작용
    data class LikePost(val postId: Int) : FeedIntent
    data class UnlikePost(val postId: Int) : FeedIntent
    data class BookmarkPost(val postId: Int) : FeedIntent
    data class HidePost(val postId: Int) : FeedIntent
    data class BlockUser(val userId: Int) : FeedIntent
    data class FollowUser(val userId: Int) : FeedIntent
    data class UnfollowUser(val userId: Int) : FeedIntent

    // 포스트 옵션 시트
    data class ShowPostOptions(val post: Post) : FeedIntent
    data object DismissPostOptions : FeedIntent
    data object EditPost : FeedIntent
    data object DeletePost : FeedIntent
    data object PinPost : FeedIntent

    // 공유
    data class SharePost(val postId: Int) : FeedIntent
    data object DismissShareSheet : FeedIntent
    data class ShareCompleted(val sharedRoomIds: List<Int>, val recipientCount: Int) : FeedIntent

    data class NavigateToPostDetail(val postId: Int) : FeedIntent
    data class NavigateToUserProfile(val userId: Int) : FeedIntent
    data class NavigateToCreatorProfile(val creatorId: Int) : FeedIntent
    data object SnackbarDismissed : FeedIntent
    data object DismissCenterToast : FeedIntent
}

sealed interface FeedSideEffect {
    data class NavigateToPostDetail(val postId: Int) : FeedSideEffect
    data class NavigateToUserProfile(val userId: Int) : FeedSideEffect
    data class NavigateToCreatorProfile(val creatorId: Int) : FeedSideEffect
    data class NavigateToEditPost(val postId: Int) : FeedSideEffect
}
