package com.ninezero.halo.feature.share.presentation

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.share.domain.model.ShareResult

data class ShareState(
    val searchQuery: String = "",
    val followingUsers: List<UserSummary> = emptyList(),
    val filteredUsers: List<UserSummary> = emptyList(),
    val selectedUserIds: Set<Int> = emptySet(),
    val message: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    // 재전송돼도 서버가 같은 공유로 인식하도록 유지
    val shareTempId: String? = null,
    val shareTempTarget: String? = null,  // shareTempId로 보낸 대상, 바뀌면 새 공유
    // 한 마디는 따로 식별, 같은 한 마디로 재시도면 같은 id
    val messageTempId: String? = null,
    val shareTempMessage: String? = null  // messageTempId로 보낸 한 마디
)

sealed interface ShareIntent {
    data object LoadFollowing : ShareIntent
    data class SearchUsers(val query: String) : ShareIntent
    data class ToggleUser(val userId: Int) : ShareIntent
    data class UpdateMessage(val text: String) : ShareIntent
    data class SharePost(val postId: Int) : ShareIntent
    data class ShareProduct(val productId: Int) : ShareIntent
    data object Reset : ShareIntent
}

sealed interface ShareSideEffect {
    data class ShareSuccess(val result: ShareResult) : ShareSideEffect
    data class ShareError(val message: String) : ShareSideEffect
}
