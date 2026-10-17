package com.ninezero.halo.feature.profile.presentation.follow_list

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

enum class FollowTab {
    FOLLOWERS,
    FOLLOWING,
    SUBSCRIBERS
}

data class FollowListState(
    val displayName: String = "",
    val isMyProfile: Boolean = false,
    val selectedTab: FollowTab = FollowTab.FOLLOWERS,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val subscriberCount: Int = 0,
    // 구독자 탭 노출 여부
    val showSubscribers: Boolean = false,
    // 팔로워 목록
    val followers: List<UserSummary> = emptyList(),
    val isFollowersLoading: Boolean = false,
    val followersHasMore: Boolean = true,
    val followersNextCursor: String? = null,
    // 팔로잉 목록
    val following: List<UserSummary> = emptyList(),
    val isFollowingLoading: Boolean = false,
    val followingHasMore: Boolean = true,
    val followingNextCursor: String? = null,
    // 구독자 목록
    val subscribers: List<UserSummary> = emptyList(),
    val isSubscribersLoading: Boolean = false,
    val subscribersHasMore: Boolean = true,
    val subscribersPage: Int = 1,
    // 검색, 탭별 독립
    val followersSearchQuery: String = "",
    val followingSearchQuery: String = "",
    val subscribersSearchQuery: String = "",
    // 로딩 상태
    val isInitialLoading: Boolean = true,
    // 첫 응답을 받은 탭, 그 전엔 빈 목록 문구 대신 로딩
    val loadedTabs: Set<FollowTab> = emptySet(),
    // 팔로우 토글 로딩 상태
    val toggleLoadingUserIds: Set<Int> = emptySet(),
    // 팔로우 취소 확인 다이얼로그
    val showUnfollowConfirmDialog: Boolean = false,
    val unfollowTargetUser: UserSummary? = null,
    // 오류, 탭별 독립
    val followersError: String? = null,
    val followingError: String? = null,
    val subscribersError: String? = null,
    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null
)

sealed interface FollowListIntent {
    data object NavigateBack : FollowListIntent
    data class SelectTab(val tab: FollowTab) : FollowListIntent
    data class UpdateSearchQuery(val query: String) : FollowListIntent
    data object Search : FollowListIntent
    data object ClearSearch : FollowListIntent
    data object LoadMoreFollowers : FollowListIntent
    data object LoadMoreFollowing : FollowListIntent
    data object LoadMoreSubscribers : FollowListIntent
    data class ToggleFollow(val userId: Int) : FollowListIntent
    data class NavigateToUserProfile(val userId: Int) : FollowListIntent
    data class NavigateToMessage(val userId: Int) : FollowListIntent
    data class ShowUnfollowConfirmDialog(val user: UserSummary) : FollowListIntent
    data object ConfirmUnfollow : FollowListIntent
    data object DismissUnfollowConfirmDialog : FollowListIntent
    data object Retry : FollowListIntent
    data object DismissToast : FollowListIntent
    data object SnackbarDismissed : FollowListIntent
}

sealed interface FollowListSideEffect {
    data object NavigateBack : FollowListSideEffect
    data class NavigateToUserProfile(val userId: Int) : FollowListSideEffect
    data class NavigateToNewChat(  // draft 진입, 첫 메시지 때 방 생성
        val targetUserId: Int,
        val username: String = "",
        val displayName: String = "",
        val avatarUrl: String = "",
        val role: String = "USER"
    ) : FollowListSideEffect
}
