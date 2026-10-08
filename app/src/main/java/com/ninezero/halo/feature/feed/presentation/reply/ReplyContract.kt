package com.ninezero.halo.feature.feed.presentation.reply

import android.net.Uri
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.profile.domain.model.UserProfile

data class ReplyState(
    val post: Post? = null,
    val parentComment: Comment? = null,  // CommentDetail에서 이동 시 부모 댓글
    val myProfile: UserProfile? = null,
    val replyContent: String = "",
    val selectedMediaUris: List<Uri> = emptyList(),
    val isSubmitting: Boolean = false,
    val snackbarState: SnackbarState? = null
)

sealed interface ReplyIntent {
    data class UpdateReplyContent(val content: String) : ReplyIntent
    data class AddMedia(val uris: List<Uri>) : ReplyIntent
    data class RemoveMedia(val uri: Uri) : ReplyIntent
    data object ClearAllMedia : ReplyIntent
    data object SubmitReply : ReplyIntent
    data object NavigateBack : ReplyIntent
    data object SnackbarDismissed : ReplyIntent
    data class ToggleAuthorFollow(val authorId: Int, val shouldFollow: Boolean) : ReplyIntent
}

sealed interface ReplySideEffect {
    data object NavigateBack : ReplySideEffect
    data object ReplySubmitted : ReplySideEffect  // 게시 요청 완료
}
