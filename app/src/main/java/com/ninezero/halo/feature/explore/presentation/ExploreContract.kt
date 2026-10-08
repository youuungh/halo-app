package com.ninezero.halo.feature.explore.presentation

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.explore.domain.model.Banner
import com.ninezero.halo.feature.feed.domain.model.Post

data class ExploreState(
    // 배너
    val banners: List<Banner> = emptyList(),
    val isBannersLoading: Boolean = false,

    // 탭
    val selectedTab: ExploreTab = ExploreTab.TRENDING,

    // 실시간 탭
    val trendingPosts: List<Post> = emptyList(),
    val isTrendingLoading: Boolean = false,
    val trendingHasMore: Boolean = true,
    val trendingPage: Int = 1,
    val trendingError: String? = null,

    // 인기 탭
    val popularPosts: List<Post> = emptyList(),
    val isPopularLoading: Boolean = false,
    val popularHasMore: Boolean = true,
    val popularPage: Int = 1,
    val popularError: String? = null,

    // 공통
    val isRefreshing: Boolean = false,
    val snackbarState: SnackbarState? = null,

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

enum class ExploreTab {
    TRENDING,  // 실시간
    POPULAR    // 인기
}

sealed interface ExploreIntent {
    // 데이터 로딩
    data object LoadTrendingPosts : ExploreIntent
    data object LoadPopularPosts : ExploreIntent
    data object LoadMoreTrending : ExploreIntent
    data object LoadMorePopular : ExploreIntent
    data object RefreshCurrentTab : ExploreIntent

    // 탭 선택
    data class SelectTab(val tab: ExploreTab) : ExploreIntent

    // 포스트 상호작용
    data class LikePost(val postId: Int) : ExploreIntent
    data class UnlikePost(val postId: Int) : ExploreIntent
    data class BookmarkPost(val postId: Int) : ExploreIntent
    data class HidePost(val postId: Int) : ExploreIntent
    data class BlockUser(val userId: Int) : ExploreIntent

    // 팔로우
    data class FollowUser(val userId: Int) : ExploreIntent
    data class UnfollowUser(val userId: Int) : ExploreIntent

    // 포스트 옵션 시트
    data class ShowPostOptions(val post: Post) : ExploreIntent
    data object DismissPostOptions : ExploreIntent
    data object EditPost : ExploreIntent
    data object DeletePost : ExploreIntent
    data object PinPost : ExploreIntent

    // 공유
    data class SharePost(val postId: Int) : ExploreIntent
    data object DismissShareSheet : ExploreIntent
    data class ShareCompleted(val sharedRoomIds: List<Int>, val recipientCount: Int) : ExploreIntent
    data class NavigateToChatRoom(val roomId: Int) : ExploreIntent

    // 네비게이션
    data class NavigateToPostDetail(val postId: Int) : ExploreIntent
    data class NavigateToUserProfile(val userId: Int) : ExploreIntent
    data class NavigateToBannerLink(val banner: Banner) : ExploreIntent

    data object SnackbarDismissed : ExploreIntent
}

sealed interface ExploreSideEffect {
    data class NavigateToPostDetail(val postId: Int) : ExploreSideEffect
    data class NavigateToUserProfile(val userId: Int) : ExploreSideEffect
    data class NavigateToBannerLink(val link: String) : ExploreSideEffect
    data class NavigateToEditPost(val postId: Int) : ExploreSideEffect
    data class NavigateToChatRoom(val roomId: Int) : ExploreSideEffect
}
