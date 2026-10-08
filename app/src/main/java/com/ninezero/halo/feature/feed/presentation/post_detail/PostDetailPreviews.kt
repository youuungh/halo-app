package com.ninezero.halo.feature.feed.presentation.post_detail

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewComment
import com.ninezero.halo.core.ui.preview.previewMediaItem
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.UserPreview

internal fun previewPostDetailState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 1,
            content = "오늘 새로운 프로젝트를 시작했습니다. Kotlin과 Jetpack Compose로 만드는 소셜 앱인데요, 정말 재미있어요! 많은 관심 부탁드려요.",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 42,
            commentCount = 15,
            viewCount = 1240,
            isLiked = true,
            createdAtOffset = 3_600_000
        )
    )
}

internal fun previewPostDetailWithImageState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 2,
            content = "오늘 찍은 사진입니다 📸",
            author = previewAuthor(2, "photographer", "사진작가"),
            mediaAttachments = listOf(
                previewMediaItem(id = 1, url = "https://example.com/image.jpg")
            ),
            likeCount = 1234,
            commentCount = 89,
            viewCount = 194405,
            isBookmarked = true,
            createdAtOffset = 86_400_000
        )
    )
}

internal fun previewPostDetailWithVideoState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 6,
            content = "새로운 영상을 공유합니다!",
            author = previewAuthor(5, "creator", "크리에이터"),
            mediaAttachments = listOf(
                previewMediaItem(
                    id = 1,
                    type = MediaType.VIDEO,
                    url = "https://example.com/video.mp4",
                    thumbnailUrl = "https://example.com/thumbnail.jpg"
                )
            ),
            likeCount = 1234,
            commentCount = 89,
            viewCount = 56789,
            createdAtOffset = 3_600_000
        )
    )
}

internal fun previewPostDetailFollowingState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 3,
            content = "팔로우 중인 유저의 포스트입니다.",
            author = previewAuthor(3, "friend", "친구"),
            likeCount = 56,
            commentCount = 23,
            viewCount = 5678,
            isLiked = true,
            createdAtOffset = 7_200_000
        ),
        isFollowing = true
    )
}

internal fun previewPostDetailLongContentState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 4,
            content = "이것은 매우 긴 포스트입니다. 여러 줄에 걸쳐서 내용이 표시됩니다. " +
                "Kotlin과 Jetpack Compose를 사용하여 안드로이드 앱을 개발하는 것은 정말 재미있습니다. " +
                "특히 선언형 UI 패러다임을 사용하면 코드가 더 직관적이고 유지보수하기 쉬워집니다. " +
                "이 앱에서는 MVI 아키텍처 패턴을 사용하고 있으며, Orbit MVI 라이브러리를 활용하고 있습니다. " +
                "또한 Hilt를 사용한 의존성 주입과 Coroutines를 사용한 비동기 처리도 적용되어 있습니다.",
            author = previewAuthor(4, "tech_writer", "기술 블로거"),
            likeCount = 789,
            commentCount = 156,
            viewCount = 45678,
            createdAtOffset = 172_800_000
        )
    )
}

internal fun previewPostDetailMultipleImagesState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 5,
            content = "여러 장의 사진을 공유합니다!",
            author = previewAuthor(5, "traveler", "여행자"),
            mediaAttachments = listOf(
                previewMediaItem(id = 1, url = "https://example.com/image1.jpg"),
                previewMediaItem(id = 2, url = "https://example.com/image2.jpg"),
                previewMediaItem(id = 3, url = "https://example.com/image3.jpg")
            ),
            likeCount = 2345,
            commentCount = 432,
            viewCount = 123456,
            isLiked = true,
            isBookmarked = true,
            createdAtOffset = 259_200_000
        )
    )
}

internal fun previewPostDetailLoadingState(): PostDetailState {
    return PostDetailState(isLoading = true)
}

internal fun previewPostDetailCommentsLoadingState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 1,
            content = "오늘 새로운 프로젝트를 시작했습니다!",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 42,
            commentCount = 15,
            viewCount = 1240,
            isLiked = true,
            createdAtOffset = 3_600_000
        ),
        isCommentsLoading = true,
        comments = emptyList()
    )
}

internal fun previewPostDetailWithCommentsState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 1,
            content = "오늘 새로운 프로젝트를 시작했습니다!",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 42,
            commentCount = 3,
            viewCount = 1240,
            isLiked = true,
            createdAtOffset = 3_600_000
        ),
        comments = listOf(
            previewComment(
                id = 1,
                author = previewAuthor(2, "user1", "사용자1"),
                content = "정말 좋은 글이네요! 많이 배워갑니다.",
                replyCount = 2,
                likeCount = 5,
                replyPreviewUsers = listOf(
                    UserPreview(id = 10, avatarUrl = null),
                    UserPreview(id = 11, avatarUrl = null)
                ),
                createdAtOffset = 1_800_000
            ),
            previewComment(
                id = 2,
                author = previewAuthor(3, "user2", "사용자2"),
                content = "저도 비슷한 프로젝트를 진행 중인데 참고가 많이 됩니다!",
                likeCount = 3,
                isLiked = true,
                createdAtOffset = 900_000
            ),
            previewComment(
                id = 3,
                author = previewAuthor(4, "user3", "사용자3"),
                content = "응원합니다! 화이팅!",
                likeCount = 1,
                createdAtOffset = 300_000
            )
        )
    )
}

internal fun previewPostDetailNoCommentsState(): PostDetailState {
    return PostDetailState(
        post = previewPost(
            id = 1,
            content = "새로 올린 포스트입니다. 아직 댓글이 없어요!",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 5,
            viewCount = 50,
            createdAtOffset = 600_000
        ),
        isCommentsLoading = false,
        comments = emptyList()
    )
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenWithImagePreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailWithImageState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenWithVideoPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailWithVideoState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenFollowingPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailFollowingState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenLongContentPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailLongContentState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenMultipleImagesPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailMultipleImagesState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenLoadingPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailLoadingState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenCommentsLoadingPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailCommentsLoadingState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenWithCommentsPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailWithCommentsState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PostDetailScreenNoCommentsPreview() {
    HaloScreenPreview {
        PostDetailScreenContent(
            state = previewPostDetailNoCommentsState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}
