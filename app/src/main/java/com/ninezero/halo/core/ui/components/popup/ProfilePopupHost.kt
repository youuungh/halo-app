package com.ninezero.halo.core.ui.components.popup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import com.ninezero.halo.core.ui.components.overlay.CenterToast

data class ProfilePopupData(
    val id: Int,
    val isFollowing: Boolean
)

class ProfilePopupHostState {
    var showPopup by mutableStateOf(false)
        internal set
    var authorId by mutableStateOf<Int?>(null)
        internal set
    var offset by mutableStateOf(IntOffset.Zero)
        internal set
    var showFollowToast by mutableStateOf(false)
        internal set
    var followToastMessage by mutableStateOf("")
        internal set

    fun show(authorId: Int, offset: IntOffset) {
        this.authorId = authorId
        this.offset = offset
        this.showPopup = true
    }

    fun dismiss() {
        showPopup = false
        authorId = null
    }

    fun showToast(message: String) {
        followToastMessage = message
        showFollowToast = true
    }

    fun dismissToast() {
        showFollowToast = false
    }
}

/** 프로필 팝업 호스트 상태 */
@Composable
fun rememberProfilePopupHostState(): ProfilePopupHostState {
    return remember { ProfilePopupHostState() }
}

/** 프로필 팝업 호스트 */
@Composable
fun ProfilePopupHost(
    state: ProfilePopupHostState,
    resolveAuthor: (Int) -> ProfilePopupData?,
    onNavigateToProfile: (Int) -> Unit,
    onFollow: (Int) -> Unit,
    onUnfollow: (Int) -> Unit
) {
    val author = state.authorId?.let { resolveAuthor(it) }

    if (state.showPopup && author != null) {
        ProfilePopupMenu(
            isFollowing = author.isFollowing,
            onDismiss = { state.dismiss() },
            onVisitProfile = {
                onNavigateToProfile(author.id)
                state.dismiss()
            },
            onToggleFollow = {
                if (author.isFollowing) {
                    onUnfollow(author.id)
                    state.showToast("팔로우를 취소했습니다")
                } else {
                    onFollow(author.id)
                    state.showToast("팔로우")
                }
            },
            offset = state.offset
        )
    }

    CenterToast(
        visible = state.showFollowToast,
        message = state.followToastMessage,
        onDismiss = { state.dismissToast() }
    )
}
