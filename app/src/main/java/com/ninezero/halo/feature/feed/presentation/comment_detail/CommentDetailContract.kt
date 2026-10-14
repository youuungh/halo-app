package com.ninezero.halo.feature.feed.presentation.comment_detail

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.presentation.BlockTargetUser
import com.ninezero.halo.feature.feed.presentation.CommentSortType
import com.ninezero.halo.feature.feed.presentation.UnavailablePostReason

data class CommentDetailState(
    // 내 정보
    val myUserId: Int? = null,
    val isCurrentUserCreator: Boolean = false,

    // 알림 진입
    val highlightAnchor: Boolean = false,

    // REPLY 알림 진입, 일반은 null
    val highlightReplyId: Int? = null,

    // 조상 체인
    val parentChain: List<Comment> = emptyList(),

    // 탭한 댓글, 캐시에서 즉시
    val parentComment: Comment? = null,
    val isParentLoading: Boolean = true,  // 첫 프레임부터 자리를 잡아야 앵커가 그 칸에 걸린다

    // 원본 포스트, 나중에 로드
    val post: Post? = null,
    val isPostLoading: Boolean = true,
    val unavailablePost: UnavailablePostReason? = null,  // 원글을 못 받은 이유, 받았으면 null

    // 답글 목록
    val replies: List<Comment> = emptyList(),
    val pendingReplies: List<Comment> = emptyList(),  // 업로드 중인 답글
    val isRepliesLoading: Boolean = true,
    val repliesError: String? = null,
    val repliesPage: Int = 1,
    val repliesHasMore: Boolean = true,
    val isRefreshing: Boolean = false,
    val repliesSortBy: CommentSortType = CommentSortType.POPULAR,

    // 댓글 시트
    val showCommentOptionsSheet: Boolean = false,
    val selectedComment: Comment? = null,

    // 포스트 시트
    val showPostOptionsSheet: Boolean = false,

    // 차단 다이얼로그
    val showBlockConfirmDialog: Boolean = false,
    val blockTargetUser: BlockTargetUser? = null,
    val showBlockCompletedDialog: Boolean = false,

    val snackbarState: SnackbarState? = null
)

sealed interface CommentDetailIntent {
    data object RefreshReplies : CommentDetailIntent
    data object NavigateBack : CommentDetailIntent
    data object NavigateToReply : CommentDetailIntent

    // 포스트 상호작용
    data object LikePost : CommentDetailIntent
    data object UnlikePost : CommentDetailIntent

    // 부모 댓글 상호작용
    data object LikeParentComment : CommentDetailIntent
    data object UnlikeParentComment : CommentDetailIntent

    // 답글 관련
    data object LoadMoreReplies : CommentDetailIntent
    data class ChangeSortType(val sortType: CommentSortType) : CommentDetailIntent

    // 답글 상호작용
    data class LikeReply(val commentId: Int) : CommentDetailIntent
    data class UnlikeReply(val commentId: Int) : CommentDetailIntent
    data class FollowAuthor(val authorId: Int) : CommentDetailIntent
    data class UnfollowAuthor(val authorId: Int) : CommentDetailIntent

    // 댓글 옵션
    data class ShowCommentOptions(val comment: Comment) : CommentDetailIntent
    data object DismissCommentOptions : CommentDetailIntent
    data object DeleteComment : CommentDetailIntent
    data object BookmarkComment : CommentDetailIntent

    // 포스트 옵션
    data object ShowPostOptions : CommentDetailIntent
    data object DismissPostOptions : CommentDetailIntent
    data object EditPost : CommentDetailIntent
    data object PinPost : CommentDetailIntent
    data object BookmarkPost : CommentDetailIntent
    data object HidePost : CommentDetailIntent

    // 차단 다이얼로그
    data object BlockCommentAuthor : CommentDetailIntent
    data object BlockPostAuthor : CommentDetailIntent
    data object DismissBlockConfirmDialog : CommentDetailIntent
    data object ConfirmBlockUser : CommentDetailIntent
    data object DismissBlockCompletedDialog : CommentDetailIntent

    data class NavigateToUserProfile(val userId: Int) : CommentDetailIntent
    data class NavigateToCommentDetail(val commentId: Int) : CommentDetailIntent
    data object NavigateToPost : CommentDetailIntent
    data object SnackbarDismissed : CommentDetailIntent
}

sealed interface CommentDetailSideEffect {
    data object NavigateBack : CommentDetailSideEffect
    data class NavigateToUserProfile(val userId: Int) : CommentDetailSideEffect
    data class NavigateToCommentDetail(val commentId: Int) : CommentDetailSideEffect
    data class NavigateToPost(val postId: Int) : CommentDetailSideEffect
    data class NavigateToReply(val postId: Int, val parentCommentId: Int) : CommentDetailSideEffect
    data class NavigateToEditPost(val postId: Int) : CommentDetailSideEffect
}
