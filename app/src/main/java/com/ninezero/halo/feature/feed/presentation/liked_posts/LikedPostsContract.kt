package com.ninezero.halo.feature.feed.presentation.liked_posts

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.LikedItem
import com.ninezero.halo.feature.feed.domain.model.Post

data class LikedPostsState(
    val items: List<LikedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = true,
    val nextCursor: String? = null,
    val selectedTab: Int = 0,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,

    // 현재 로그인한 사용자 정보
    val currentUserId: Int? = null,
    val isCurrentUserCreator: Boolean = false,

    // 포스트 옵션 시트
    val selectedPost: Post? = null,
    val showPostOptionsSheet: Boolean = false,

    // 댓글 옵션 시트
    val selectedComment: Comment? = null,
    val showCommentOptionsSheet: Boolean = false,

    // 공유 시트
    val showShareSheet: Boolean = false,
    val sharePostId: Int? = null,
    val navigateRoomId: Int? = null
)

sealed interface LikedPostsIntent {
    // 데이터 로딩
    data object LoadItems : LikedPostsIntent
    data object RefreshItems : LikedPostsIntent
    data object LoadMoreItems : LikedPostsIntent
    data class SelectTab(val tabIndex: Int) : LikedPostsIntent

    // 포스트 상호작용
    data class LikePost(val postId: Int) : LikedPostsIntent
    data class UnlikePost(val postId: Int) : LikedPostsIntent
    data class BookmarkPost(val postId: Int) : LikedPostsIntent
    data class BlockUser(val userId: Int) : LikedPostsIntent
    data class FollowUser(val userId: Int) : LikedPostsIntent
    data class UnfollowUser(val userId: Int) : LikedPostsIntent

    // 포스트 옵션 시트
    data class ShowPostOptions(val post: Post) : LikedPostsIntent
    data object DismissPostOptions : LikedPostsIntent
    data object EditPost : LikedPostsIntent
    data object DeletePost : LikedPostsIntent
    data object PinPost : LikedPostsIntent

    // 댓글 상호작용
    data class LikeComment(val commentId: Int) : LikedPostsIntent
    data class UnlikeComment(val commentId: Int) : LikedPostsIntent

    // 댓글 옵션 시트
    data class ShowCommentOptions(val comment: Comment) : LikedPostsIntent
    data object DismissCommentOptions : LikedPostsIntent
    data object DeleteComment : LikedPostsIntent
    data object BookmarkComment : LikedPostsIntent
    data class BlockCommentAuthor(val userId: Int) : LikedPostsIntent

    // 공유
    data class SharePost(val postId: Int) : LikedPostsIntent
    data object DismissShareSheet : LikedPostsIntent
    data class ShareCompleted(val sharedRoomIds: List<Int>, val recipientCount: Int) : LikedPostsIntent

    data class NavigateToPostDetail(val postId: Int) : LikedPostsIntent
    data class NavigateToCommentDetail(val commentId: Int) : LikedPostsIntent
    data class NavigateToUserProfile(val userId: Int) : LikedPostsIntent
    data object SnackbarDismissed : LikedPostsIntent
}

sealed interface LikedPostsSideEffect {
    data class NavigateToPostDetail(val postId: Int) : LikedPostsSideEffect
    data class NavigateToCommentDetail(val commentId: Int) : LikedPostsSideEffect
    data class NavigateToUserProfile(val userId: Int) : LikedPostsSideEffect
    data class NavigateToEditPost(val postId: Int) : LikedPostsSideEffect
}
