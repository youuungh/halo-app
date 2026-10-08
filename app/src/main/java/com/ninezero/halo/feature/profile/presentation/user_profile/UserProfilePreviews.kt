package com.ninezero.halo.feature.profile.presentation.user_profile

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.core.ui.preview.previewUserProfile
import com.ninezero.halo.core.ui.preview.previewUserSummary
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.PostSummaryData
import com.ninezero.halo.feature.profile.domain.model.PostTagSection
import com.ninezero.halo.feature.profile.domain.model.ProductTagSection
import com.ninezero.halo.feature.profile.domain.model.SectionTag
import com.ninezero.halo.feature.profile.domain.model.ThreadCommentData
import com.ninezero.halo.feature.profile.domain.model.UserSummaryData
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.store.domain.model.Product

private val previewAuthor = previewAuthor(2, "creator_user", "크리에이터")

private val previewPosts = List(10) { index ->
    previewPost(
        id = index + 1,
        content = "이것은 테스트 포스트 #${index + 1}입니다. 스크롤 테스트를 위한 더미 컨텐츠입니다. 좋은 하루 되세요!",
        author = previewAuthor,
        likeCount = (10..500).random(),
        commentCount = (0..50).random(),
        viewCount = (100..5000).random(),
        isLiked = index % 3 == 0,
        isBookmarked = index % 5 == 0,
        createdAtOffset = index * 3600000L
    )
}

private val previewProfile = previewUserProfile(
    userId = 2,
    role = "CREATOR",
    displayName = "크리에이터",
    username = "creator_user",
    bio = "안녕하세요! 콘텐츠 크리에이터입니다.",
    location = "서울",
    website = "https://example.com",
    followerCount = 1234,
    followingCount = 567,
    postCount = 89,
    subscriberCount = 100,
    hasSubscriptionPlans = true,
    isCreator = true
)

private val previewPinnedPosts = previewPosts.take(5)

private val previewCreator = previewUserSummary(
    id = 2, username = "creator_user", displayName = "크리에이터",
    role = "CREATOR"
)

private val previewPostSections = listOf(
    PostTagSection(
        tag = SectionTag(
            id = 1,
            name = "일상 브이로그"
        ),
        posts = previewPosts.take(8),
        hasMore = true
    ),
    PostTagSection(
        tag = SectionTag(
            id = 2,
            name = "여행"
        ),
        posts = previewPosts.drop(2).take(6),
        hasMore = true
    )
)

private val previewProducts = List(6) { index ->
    Product(
        id = index + 1,
        name = "상품 ${index + 1}",
        description = "상품 설명입니다.",
        price = "${10000 + index * 5000}.00",
        originalPrice = if (index % 2 == 0) "${15000 + index * 5000}.00" else null,
        discountRate = if (index % 2 == 0) 20 else null,
        imageUrls = emptyList(),
        brandName = "브랜드",
        tags = listOf("태그1", "태그2"),
        stock = 100,
        status = "ACTIVE",
        isActive = true,
        viewCount = 100,
        likeCount = 10,
        salesCount = 5,
        creator = previewCreator,
        requiredTier = "FREE",
        canAccess = true,
    )
}

private val previewProductSections = listOf(
    ProductTagSection(
        tag = SectionTag(
            id = 3,
            name = "인기 상품"
        ),
        products = previewProducts,
        hasMore = true
    )
)

private val previewCommentThreads = listOf(
    CommentThreadData(
        comments = listOf(
            ThreadCommentData(
                id = 101,
                content = "이 글 정말 유익하네요! 저도 비슷한 경험이 있어서 공감됩니다.",
                author = UserSummaryData(
                    id = 2,
                    username = "creator_user",
                    displayName = "크리에이터",
                    avatarUrl = null
                ),
                replyTo = null,
                createdAt = System.currentTimeMillis() - 3600000,
                likeCount = 3,
                replyCount = 0,
                isLiked = false,
                isProfileUser = true
            )
        ),
        post = PostSummaryData(
            id = 201,
            excerpt = "오늘 처음으로 마라톤 완주했습니다!",
            mediaAttachments = emptyList(),
            author = UserSummaryData(
                id = 10,
                username = "runner_kim",
                displayName = "러너킴",
                avatarUrl = null
            )
        )
    ),
    CommentThreadData(
        comments = listOf(
            ThreadCommentData(
                id = 102,
                content = "이 영상 정말 재미있게 봤어요!",
                author = UserSummaryData(
                    id = 11,
                    username = "viewer1",
                    displayName = "시청자1",
                    avatarUrl = null
                ),
                replyTo = null,
                createdAt = System.currentTimeMillis() - 86400000,
                likeCount = 12,
                replyCount = 3,
                isLiked = false,
                isProfileUser = false
            ),
            ThreadCommentData(
                id = 103,
                content = "감사합니다! 다음 영상도 기대해주세요 😊",
                author = UserSummaryData(
                    id = 2,
                    username = "creator_user",
                    displayName = "크리에이터",
                    avatarUrl = null
                ),
                replyTo = UserSummaryData(
                    id = 11,
                    username = "viewer1",
                    displayName = "시청자1",
                    avatarUrl = null
                ),
                createdAt = System.currentTimeMillis() - 82800000,
                likeCount = 8,
                replyCount = 1,
                isLiked = true,
                isProfileUser = true
            )
        ),
        post = PostSummaryData(
            id = 202,
            excerpt = "새로운 브이로그가 업로드되었습니다! 오늘은 제주도 여행기예요.",
            mediaAttachments = emptyList(),
            author = UserSummaryData(
                id = 2,
                username = "creator_user",
                displayName = "크리에이터",
                avatarUrl = null
            )
        )
    ),
    CommentThreadData(
        comments = listOf(
            ThreadCommentData(
                id = 104,
                content = "좋은 정보 공유 감사합니다! 덕분에 많이 배웠어요.",
                author = UserSummaryData(
                    id = 2,
                    username = "creator_user",
                    displayName = "크리에이터",
                    avatarUrl = null
                ),
                replyTo = null,
                createdAt = System.currentTimeMillis() - 172800000,
                likeCount = 5,
                replyCount = 0,
                isLiked = false,
                isProfileUser = true
            )
        ),
        post = PostSummaryData(
            id = 203,
            excerpt = "커뮤니티에서 추천받은 맛집 리스트입니다.",
            mediaAttachments = emptyList(),
            author = UserSummaryData(
                id = 20,
                username = "foodie",
                displayName = "맛집탐험가",
                avatarUrl = null
            )
        )
    )
)

@HaloLightDarkPreview
@Composable
private fun UserProfileScreenPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = previewProfile.copy(
                displayName = "일반 사용자",
                username = "normal_user",
                isCreator = false,
                hasSubscriptionPlans = false,
                subscriberCount = null,
                isOwnProfile = false
            ),
            selectedTab = ProfileTab.POSTS,
            userPosts = previewPosts,
            userCommentThreads = previewCommentThreads
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun CreatorProfileScreenPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = previewProfile.copy(
                isOwnProfile = false
            ),
            selectedTab = ProfileTab.HOME,
            userPosts = previewPosts,
            userCommentThreads = previewCommentThreads,
            pinnedPosts = previewPinnedPosts,
            postSections = previewPostSections,
            productSections = previewProductSections
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun UserProfileScreenFollowingPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = previewProfile.copy(
                displayName = "일반 사용자",
                username = "normal_user",
                isCreator = false,
                hasSubscriptionPlans = false,
                subscriberCount = null,
                isFollowing = true,
                isOwnProfile = false
            ),
            selectedTab = ProfileTab.POSTS,
            userPosts = previewPosts,
            userCommentThreads = previewCommentThreads
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun CreatorProfileScreenFollowingPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = previewProfile.copy(
                isFollowing = true,
                isOwnProfile = false
            ),
            isSubscribed = true,
            selectedTab = ProfileTab.HOME,
            userPosts = previewPosts,
            userCommentThreads = previewCommentThreads,
            pinnedPosts = previewPinnedPosts,
            postSections = previewPostSections,
            productSections = previewProductSections
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun UserProfileScreenBlockedPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = previewProfile.copy(
                isCreator = false,
                isBlocked = true,
                bio = null,
                location = null,
                website = null
            ),
            userCommentThreads = emptyList()
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun CreatorHomeLoadingPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = previewProfile,
            selectedTab = ProfileTab.HOME,
            isHomeSectionsLoading = true
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun CreatorHomeEmptyPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = previewProfile,
            selectedTab = ProfileTab.HOME,
            pinnedPosts = emptyList(),
            postSections = emptyList(),
            productSections = emptyList()
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun UserProfileScreenBlockConfirmDialogPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = previewProfile.copy(
                isCreator = false,
                isOwnProfile = false,
                avatarUrl = "https://picsum.photos/100"
            ),
            selectedTab = ProfileTab.POSTS,
            userPosts = previewPosts,
            showBlockConfirmDialog = true
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun UserProfileScreenLoadingPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = null
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun UserProfileScreenErrorPreview() {
    UserProfilePreviewContent(
        state = UserProfileState(
            profile = null,
            snackbarState = SnackbarState(
                message = "연결에 실패했습니다",
                actionLabel = "재시도"
            )
        )
    )
}

@Composable
private fun UserProfilePreviewContent(
    state: UserProfileState
) {
    HaloScreenPreview {
        UserProfileScreenContent(
            state = state,
            onIntent = {}
        )
    }
}
