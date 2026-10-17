package com.ninezero.halo.feature.profile.presentation.follow_list

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary

internal fun previewFollowListState(): FollowListState {
    return FollowListState(
        displayName = "사용자",
        isMyProfile = false,
        selectedTab = FollowTab.FOLLOWERS,
        followerCount = 12345,
        followingCount = 567,
        followers = previewFollowUsers,
        isInitialLoading = false,
        loadedTabs = FollowTab.entries.toSet()
    )
}

internal fun previewMyFollowersState(): FollowListState {
    return FollowListState(
        displayName = "나",
        isMyProfile = true,
        selectedTab = FollowTab.FOLLOWERS,
        followerCount = 100,
        followingCount = 50,
        followers = previewFollowUsers,
        isInitialLoading = false,
        loadedTabs = FollowTab.entries.toSet()
    )
}

internal fun previewMyFollowingState(): FollowListState {
    return FollowListState(
        displayName = "나",
        isMyProfile = true,
        selectedTab = FollowTab.FOLLOWING,
        followerCount = 100,
        followingCount = 50,
        following = previewFollowUsers,
        isInitialLoading = false,
        loadedTabs = FollowTab.entries.toSet()
    )
}

internal fun previewFollowSearchNoResultsState(): FollowListState {
    return FollowListState(
        displayName = "사용자",
        isMyProfile = false,
        selectedTab = FollowTab.FOLLOWERS,
        followerCount = 100,
        followingCount = 50,
        followers = emptyList(),
        followersSearchQuery = "검색어",
        isInitialLoading = false,
        loadedTabs = FollowTab.entries.toSet()
    )
}

internal fun previewFollowUnfollowDialogState(): FollowListState {
    return FollowListState(
        displayName = "나",
        isMyProfile = true,
        selectedTab = FollowTab.FOLLOWING,
        followerCount = 100,
        followingCount = 50,
        following = previewFollowUsers,
        isInitialLoading = false,
        loadedTabs = FollowTab.entries.toSet(),
        showUnfollowConfirmDialog = true,
        unfollowTargetUser = previewUserSummary(
            id = 1,
            username = "user1",
            displayName = "사용자 1",
            avatarUrl = "https://picsum.photos/100",
            isFollowing = true
        )
    )
}

internal fun previewFollowEmptyState(): FollowListState {
    return FollowListState(
        displayName = "사용자",
        isMyProfile = false,
        selectedTab = FollowTab.FOLLOWERS,
        followerCount = 0,
        followingCount = 0,
        followers = emptyList(),
        isInitialLoading = false,
        loadedTabs = FollowTab.entries.toSet()
    )
}

internal fun previewFollowErrorState(): FollowListState {
    return FollowListState(
        displayName = "사용자",
        isMyProfile = false,
        selectedTab = FollowTab.FOLLOWERS,
        isInitialLoading = false,
        loadedTabs = FollowTab.entries.toSet(),
        followersError = "네트워크 오류가 발생했습니다"
    )
}

private val previewFollowUsers = listOf(
    previewUserSummary(id = 1, username = "user1", displayName = "사용자 1", isFollowing = true),
    previewUserSummary(id = 2, username = "user2", displayName = "사용자 2"),
    previewUserSummary(id = 3, username = "creator", displayName = "크리에이터", isFollowing = true),
    previewUserSummary(id = 4, username = "designer_kim", displayName = "김디자이너", isFollowing = true),
    previewUserSummary(id = 5, username = "travel_lover", displayName = "여행러버"),
    previewUserSummary(id = 6, username = "foodie_park", displayName = "박푸디", isFollowing = true),
    previewUserSummary(id = 7, username = "photographer", displayName = "포토그래퍼", isFollowing = true),
    previewUserSummary(id = 8, username = "music_fan", displayName = "음악팬"),
    previewUserSummary(id = 9, username = "developer_lee", displayName = "이개발자", isFollowing = true),
    previewUserSummary(id = 10, username = "artist_choi", displayName = "최아티스트"),
    previewUserSummary(id = 11, username = "coffee_addict", displayName = "커피중독자", isFollowing = true),
    previewUserSummary(id = 12, username = "fitness_guru", displayName = "운동마니아"),
    previewUserSummary(id = 13, username = "bookworm_jung", displayName = "정북러", isFollowing = true),
    previewUserSummary(id = 14, username = "gamer_pro", displayName = "프로게이머"),
    previewUserSummary(id = 15, username = "cat_lover", displayName = "고양이집사", isFollowing = true),
    previewUserSummary(id = 16, username = "chef_hwang", displayName = "황셰프", isFollowing = true),
    previewUserSummary(id = 17, username = "yoga_master", displayName = "요가마스터"),
    previewUserSummary(id = 18, username = "movie_critic", displayName = "영화평론가", isFollowing = true),
    previewUserSummary(id = 19, username = "fashion_icon", displayName = "패션아이콘"),
    previewUserSummary(id = 20, username = "tech_reviewer", displayName = "테크리뷰어", isFollowing = true)
)

@HaloLightDarkPreview
@Composable
private fun FollowListScreenPreview() {
    HaloScreenPreview {
        FollowListScreenContent(
            state = previewFollowListState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun FollowListMyFollowersPreview() {
    HaloScreenPreview {
        FollowListScreenContent(
            state = previewMyFollowersState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun FollowListMyFollowingPreview() {
    HaloScreenPreview {
        FollowListScreenContent(
            state = previewMyFollowingState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun FollowListSearchNoResultsPreview() {
    HaloScreenPreview {
        FollowListScreenContent(
            state = previewFollowSearchNoResultsState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun FollowListUnfollowDialogPreview() {
    HaloScreenPreview {
        FollowListScreenContent(
            state = previewFollowUnfollowDialogState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun FollowListEmptyPreview() {
    HaloScreenPreview {
        FollowListScreenContent(
            state = previewFollowEmptyState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun FollowListErrorPreview() {
    HaloScreenPreview {
        FollowListScreenContent(
            state = previewFollowErrorState(),
            onIntent = {}
        )
    }
}
