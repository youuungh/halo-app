package com.ninezero.halo.feature.profile.presentation.my_profile

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.core.ui.preview.previewUserProfile
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.PostSummaryData
import com.ninezero.halo.feature.profile.domain.model.ThreadCommentData
import com.ninezero.halo.feature.profile.domain.model.UserSummaryData
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab

private val previewProfile = previewUserProfile(
    displayName = "홍길동",
    username = "honggildong",
    bio = "안녕하세요! 반갑습니다.",
    location = "서울",
    website = "https://example.com",
    followerCount = 345,
    followingCount = 6789,
    postCount = 12
)

private val previewAuthor = previewAuthor(
    username = "honggildong",
    displayName = "홍길동"
)

private val previewPosts = listOf(
    previewPost(
        id = 1,
        content = "오늘 새로운 프로젝트를 시작했습니다. 많은 관심 부탁드려요!",
        author = previewAuthor,
        likeCount = 24,
        commentCount = 5,
        isLiked = true,
        createdAtOffset = 3600000
    ),
    previewPost(
        id = 2,
        content = "주말에 카페에서 작업 중입니다.",
        author = previewAuthor,
        likeCount = 15,
        commentCount = 3,
        createdAtOffset = 86400000
    )
)

private val previewCommentThreads = listOf(
    CommentThreadData(
        comments = listOf(
            ThreadCommentData(
                id = 1,
                content = "와 정말 대단하시네요! 응원합니다.",
                author = UserSummaryData(id = 1, username = "honggildong", displayName = "홍길동", avatarUrl = null),
                replyTo = null,
                createdAt = System.currentTimeMillis() - 3600000,
                likeCount = 2, replyCount = 0, isLiked = false, isProfileUser = true
            )
        ),
        post = PostSummaryData(
            id = 101,
            excerpt = "드디어 첫 앱 출시했습니다! 많은 피드백 부탁드려요.",
            mediaAttachments = emptyList(),
            author = UserSummaryData(id = 5, username = "developer_lee", displayName = "개발자 이씨", avatarUrl = null)
        )
    ),
    CommentThreadData(
        comments = listOf(
            ThreadCommentData(
                id = 2,
                content = "이 부분 어떻게 구현하셨어요?",
                author = UserSummaryData(id = 6, username = "curious_dev", displayName = "궁금한개발자", avatarUrl = null),
                replyTo = null,
                createdAt = System.currentTimeMillis() - 7200000,
                likeCount = 3, replyCount = 1, isLiked = false, isProfileUser = false
            ),
            ThreadCommentData(
                id = 3,
                content = "Kotlin Coroutines랑 Flow 사용했어요! 나중에 블로그에 정리해볼게요.",
                author = UserSummaryData(id = 1, username = "honggildong", displayName = "홍길동", avatarUrl = null),
                replyTo = UserSummaryData(id = 6, username = "curious_dev", displayName = "궁금한개발자", avatarUrl = null),
                createdAt = System.currentTimeMillis() - 5400000,
                likeCount = 5, replyCount = 0, isLiked = true, isProfileUser = true
            )
        ),
        post = PostSummaryData(
            id = 102,
            excerpt = "안드로이드 비동기 처리 관련 질문입니다.",
            mediaAttachments = emptyList(),
            author = UserSummaryData(id = 7, username = "android_lover", displayName = "안드로이드러버", avatarUrl = null)
        )
    ),
    CommentThreadData(
        comments = listOf(
            ThreadCommentData(
                id = 4,
                content = "좋은 정보 감사합니다! 참고하겠습니다.",
                author = UserSummaryData(id = 1, username = "honggildong", displayName = "홍길동", avatarUrl = null),
                replyTo = null,
                createdAt = System.currentTimeMillis() - 172800000,
                likeCount = 1, replyCount = 0, isLiked = false, isProfileUser = true
            )
        ),
        post = PostSummaryData(
            id = 103,
            excerpt = "Jetpack Compose 팁 모음입니다.",
            mediaAttachments = emptyList(),
            author = UserSummaryData(id = 8, username = "compose_master", displayName = "컴포즈마스터", avatarUrl = null)
        )
    )
)

@HaloLightDarkPreview
@Composable
private fun MyProfileScreenPreview() {
    HaloScreenPreview {
        MyProfileScreenContent(
            state = MyProfileState(
                profile = previewProfile.copy(isCreator = false),
                selectedTab = ProfileTab.POSTS,
                myPosts = previewPosts,
                myCommentThreads = previewCommentThreads
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProfileScreenCreatorPreview() {
    HaloScreenPreview {
        MyProfileScreenContent(
            state = MyProfileState(
                profile = previewProfile.copy(
                    isCreator = true,
                    subscriberCount = 150,
                    hasSubscriptionPlans = true
                ),
                selectedTab = ProfileTab.HOME,
                myPosts = previewPosts,
                myCommentThreads = previewCommentThreads
            ),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProfileScreenLoadingPreview() {
    HaloScreenPreview {
        MyProfileScreenContent(
            state = MyProfileState(isLoading = true),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun MyProfileScreenErrorPreview() {
    HaloScreenPreview {
        MyProfileScreenContent(
            state = MyProfileState(
                isLoading = false,
                error = "프로필을 불러올 수 없습니다"
            ),
            onIntent = {}
        )
    }
}
