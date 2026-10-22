package com.ninezero.halo.feature.feed.presentation.post_detail

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.presentation.BlockTargetUser
import com.ninezero.halo.feature.feed.presentation.CommentSortType

data class PostDetailState(
    val myUserId: Int? = null,
    val isCurrentUserCreator: Boolean = false,
    // 포스트
    val post: Post? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isViewCountLoaded: Boolean = false,                     // API 응답 후 조회수 표시 여부
    val error: String? = null,
    // COMMENT 알림 진입
    val highlightCommentId: Int? = null,
    // 댓글
    val comments: List<Comment> = emptyList(),
    val pendingComments: List<Comment> = emptyList(),           // 업로드 중인 댓글
    val pendingReplies: Map<Int, List<Comment>> = emptyMap(),   // 업로드 중인 답글, 부모 id별
    val isCommentsLoading: Boolean = true,
    val commentsError: String? = null,
    val commentsPage: Int = 1,
    val commentsHasMore: Boolean = true,
    val commentSortBy: CommentSortType = CommentSortType.POPULAR,
    val isChangingSort: Boolean = false,  // 정렬 바꿔 다시 받는 중, 정렬·입력창 유지
    // 확장된 답글
    val expandedCommentReplies: Map<Int, List<Comment>> = emptyMap(),
    val loadingRepliesCommentIds: Set<Int> = emptySet(),
    val repliesPage: Map<Int, Int> = emptyMap(),
    val repliesHasMore: Map<Int, Boolean> = emptyMap(),
    // 옵션 시트
    val showPostOptionsSheet: Boolean = false,
    val showCommentOptionsSheet: Boolean = false,
    val selectedComment: Comment? = null,
    val replyToComment: Comment? = null,
    // 작성자
    val isFollowing: Boolean = false,
    // 차단 다이얼로그
    val showBlockConfirmDialog: Boolean = false,
    val blockTargetUser: BlockTargetUser? = null,
    val showBlockCompletedDialog: Boolean = false,
    // 공유
    val showShareSheet: Boolean = false,
    val navigateRoomId: Int? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface PostDetailIntent {
    data object RefreshPost : PostDetailIntent

    // 포스트 상호작용
    data object LikePost : PostDetailIntent
    data object UnlikePost : PostDetailIntent
    data object BookmarkPost : PostDetailIntent
    data object FollowAuthor : PostDetailIntent
    data object UnfollowAuthor : PostDetailIntent

    // 포스트 옵션
    data object ShowPostOptions : PostDetailIntent
    data object DismissPostOptions : PostDetailIntent
    data object DeletePost : PostDetailIntent
    data object EditPost : PostDetailIntent
    data object PinPost : PostDetailIntent

    // 댓글
    data object LoadMoreComments : PostDetailIntent
    data class ChangeSortType(val sortType: CommentSortType) : PostDetailIntent
    data class SetReplyTo(val comment: Comment?) : PostDetailIntent

    // 댓글 상호작용
    data class ToggleCommentLike(val commentId: Int) : PostDetailIntent
    data class ExpandReplies(val commentId: Int) : PostDetailIntent
    data class LoadMoreReplies(val commentId: Int) : PostDetailIntent

    // 댓글 옵션
    data class ShowCommentOptions(val commentId: Int) : PostDetailIntent
    data object DismissCommentOptions : PostDetailIntent
    data object DeleteComment : PostDetailIntent
    data object BookmarkComment : PostDetailIntent

    // 차단 다이얼로그
    data object BlockAuthor : PostDetailIntent
    data object BlockCommentAuthor : PostDetailIntent
    data object DismissBlockConfirmDialog : PostDetailIntent
    data object ConfirmBlockUser : PostDetailIntent
    data object DismissBlockCompletedDialog : PostDetailIntent

    // 공유
    data object SharePost : PostDetailIntent
    data object DismissShareSheet : PostDetailIntent
    data class ShareCompleted(val sharedRoomIds: List<Int>, val recipientCount: Int) : PostDetailIntent
    data class NavigateToChatRoom(val roomId: Int) : PostDetailIntent

    data class NavigateToUserProfile(val userId: Int) : PostDetailIntent
    data class NavigateToCommentDetail(val commentId: Int) : PostDetailIntent
    data object SnackbarDismissed : PostDetailIntent
}

sealed interface PostDetailSideEffect {
    data object NavigateBack : PostDetailSideEffect
    data class NavigateToUserProfile(val userId: Int) : PostDetailSideEffect
    data class NavigateToCommentDetail(val commentId: Int) : PostDetailSideEffect
    data class NavigateToEditPost(val postId: Int) : PostDetailSideEffect
    data object PostDeleted : PostDetailSideEffect
    data class NavigateToChatRoom(val roomId: Int) : PostDetailSideEffect
    data class ShowCenterToast(val message: String) : PostDetailSideEffect
}
