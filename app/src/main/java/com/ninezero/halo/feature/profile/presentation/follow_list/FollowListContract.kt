package com.ninezero.halo.feature.profile.presentation.follow_list

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState

enum class FollowTab(val displayName: String, val emptyText: String) {
    FOLLOWERS("팔로워", "팔로워가 없어요"),
    FOLLOWING("팔로잉", "팔로잉이 없어요"),
    SUBSCRIBERS("구독자", "구독자가 없어요")
}

data class FollowListState(
    val displayName: String = "",
    val isMyProfile: Boolean = false,
    val selectedTab: FollowTab = FollowTab.FOLLOWERS,
    // 탭 자리가 검색창으로 바뀐 상태
    val isSearchMode: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val subscriberCount: Int = 0,
    // 구독자 탭 노출 여부
    val showSubscribers: Boolean = false,
    // 팔로워 목록
    val followers: List<UserSummary> = emptyList(),
    val isFollowersLoading: Boolean = false,
    val hasMoreFollowers: Boolean = true,
    val followersNextCursor: String? = null,
    // 팔로잉 목록
    val following: List<UserSummary> = emptyList(),
    val isFollowingLoading: Boolean = false,
    val hasMoreFollowing: Boolean = true,
    val followingNextCursor: String? = null,
    // 구독자 목록
    val subscribers: List<UserSummary> = emptyList(),
    val isSubscribersLoading: Boolean = false,
    val hasMoreSubscribers: Boolean = true,
    val subscribersPage: Int = 1,
    // 검색(탭별)
    val followersSearchQuery: String = "",
    val followingSearchQuery: String = "",
    val subscribersSearchQuery: String = "",
    // 로딩 상태
    val isInitialLoading: Boolean = true,
    // 첫 응답을 받은 탭, 그 전엔 빈 목록 문구 대신 로딩
    val loadedTabs: Set<FollowTab> = emptySet(),
    // 팔로우 요청 중인 사람, 두 번 누름 막기
    val followInProgressIds: Set<Int> = emptySet(),
    // 팔로우 취소 확인 다이얼로그
    val showUnfollowConfirmDialog: Boolean = false,
    val unfollowTargetUser: UserSummary? = null,
    // 오류(탭별)
    val followersError: String? = null,
    val followingError: String? = null,
    val subscribersError: String? = null,
    val toastMessage: String? = null,
    val snackbarState: SnackbarState? = null
) {
    fun usersOf(tab: FollowTab): List<UserSummary> = when (tab) {
        FollowTab.FOLLOWERS -> followers
        FollowTab.FOLLOWING -> following
        FollowTab.SUBSCRIBERS -> subscribers
    }

    // 첫 응답 전엔 빈 목록 문구 대신 로딩
    fun isLoadingOf(tab: FollowTab): Boolean = tab !in loadedTabs || when (tab) {
        FollowTab.FOLLOWERS -> isFollowersLoading
        FollowTab.FOLLOWING -> isFollowingLoading
        FollowTab.SUBSCRIBERS -> isSubscribersLoading
    }

    fun errorOf(tab: FollowTab): String? = when (tab) {
        FollowTab.FOLLOWERS -> followersError
        FollowTab.FOLLOWING -> followingError
        FollowTab.SUBSCRIBERS -> subscribersError
    }

    fun searchQueryOf(tab: FollowTab): String = when (tab) {
        FollowTab.FOLLOWERS -> followersSearchQuery
        FollowTab.FOLLOWING -> followingSearchQuery
        FollowTab.SUBSCRIBERS -> subscribersSearchQuery
    }

    // 세 탭에서 그 사람의 팔로우 상태만 바꿈
    fun withFollowing(userId: Int, isFollowing: Boolean): FollowListState {
        fun List<UserSummary>.mark() = map { if (it.id == userId) it.copy(isFollowing = isFollowing) else it }
        return copy(followers = followers.mark(), following = following.mark(), subscribers = subscribers.mark())
    }
}

sealed interface FollowListIntent {
    data object NavigateBack : FollowListIntent
    data class SelectTab(val tab: FollowTab) : FollowListIntent
    data class UpdateSearchQuery(val query: String) : FollowListIntent
    data object Search : FollowListIntent
    data object OpenSearch : FollowListIntent
    data object CloseSearch : FollowListIntent
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
    data class NavigateToNewChat(
        val targetUserId: Int,
        val username: String = "",
        val displayName: String = "",
        val avatarUrl: String = "",
        val role: String = "USER"
    ) : FollowListSideEffect
}
