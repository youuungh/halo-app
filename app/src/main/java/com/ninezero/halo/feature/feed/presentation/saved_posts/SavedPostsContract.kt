package com.ninezero.halo.feature.feed.presentation.saved_posts

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.BookmarkItem
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.Post

data class SavedPostsState(
    val items: List<BookmarkItem> = emptyList(),
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

sealed interface SavedPostsIntent {
    // 데이터 로딩
    data object LoadItems : SavedPostsIntent
    data object RefreshItems : SavedPostsIntent
    data object LoadMoreItems : SavedPostsIntent
    data class SelectTab(val tabIndex: Int) : SavedPostsIntent

    // 포스트 상호작용
    data class LikePost(val postId: Int) : SavedPostsIntent
    data class UnlikePost(val postId: Int) : SavedPostsIntent
    data class BookmarkPost(val postId: Int) : SavedPostsIntent
    data class HidePost(val postId: Int) : SavedPostsIntent
    data class BlockUser(val userId: Int) : SavedPostsIntent
    data class FollowUser(val userId: Int) : SavedPostsIntent
    data class UnfollowUser(val userId: Int) : SavedPostsIntent

    // 포스트 옵션 시트
    data class ShowPostOptions(val post: Post) : SavedPostsIntent
    data object DismissPostOptions : SavedPostsIntent
    data object EditPost : SavedPostsIntent
    data object DeletePost : SavedPostsIntent
    data object PinPost : SavedPostsIntent

    // 댓글 상호작용
    data class LikeComment(val commentId: Int) : SavedPostsIntent
    data class UnlikeComment(val commentId: Int) : SavedPostsIntent

    // 댓글 옵션 시트
    data class ShowCommentOptions(val comment: Comment) : SavedPostsIntent
    data object DismissCommentOptions : SavedPostsIntent
    data object DeleteComment : SavedPostsIntent
    data object BookmarkComment : SavedPostsIntent
    data class BlockCommentAuthor(val userId: Int) : SavedPostsIntent

    // 공유
    data class SharePost(val postId: Int) : SavedPostsIntent
    data object DismissShareSheet : SavedPostsIntent
    data class ShareCompleted(val sharedRoomIds: List<Int>, val recipientCount: Int) : SavedPostsIntent

    data class NavigateToPostDetail(val postId: Int) : SavedPostsIntent
    data class NavigateToCommentDetail(val commentId: Int) : SavedPostsIntent
    data class NavigateToUserProfile(val userId: Int) : SavedPostsIntent
    data object SnackbarDismissed : SavedPostsIntent
}

sealed interface SavedPostsSideEffect {
    data class NavigateToPostDetail(val postId: Int) : SavedPostsSideEffect
    data class NavigateToCommentDetail(val commentId: Int) : SavedPostsSideEffect
    data class NavigateToUserProfile(val userId: Int) : SavedPostsSideEffect
    data class NavigateToEditPost(val postId: Int) : SavedPostsSideEffect
}
