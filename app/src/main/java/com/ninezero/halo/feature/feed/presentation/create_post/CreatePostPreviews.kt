package com.ninezero.halo.feature.feed.presentation.create_post

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserProfile
import com.ninezero.halo.feature.tag.domain.model.Tag

internal fun previewCreatePostDefaultState(): CreatePostState {
    return CreatePostState(
        myProfile = previewUserProfile()
    )
}

internal fun previewCreatePostWithSelectedImagesState(): CreatePostState {
    return CreatePostState(
        myProfile = previewUserProfile(),
        content = "오늘 새로운 프로젝트를 시작했습니다!",
        selectedMediaUris = listOf(
            "https://picsum.photos/200/200".toUri(),
            "https://picsum.photos/201/200".toUri(),
            "https://picsum.photos/202/200".toUri()
        ),
        tags = listOf("프로젝트", "개발", "일상")
    )
}

internal fun previewCreatePostWithSelectedVideoState(): CreatePostState {
    return CreatePostState(
        myProfile = previewUserProfile(
            role = "CREATOR",
            displayName = "크리에이터",
            followerCount = 100,
            followingCount = 50,
            postCount = 30,
            subscriberCount = 500,
            hasSubscriptionPlans = true,
            isCreator = true,
            username = "creator"
        ),
        content = "새로운 영상을 공유합니다!",
        selectedMediaUris = listOf("content://video.mp4".toUri()),
        tags = listOf("음악", "신곡"),
        sectionTags = previewCreatePostSectionTags,
        selectedSectionTagId = 1
    )
}

internal fun previewCreatePostWithContentState(): CreatePostState {
    return CreatePostState(
        myProfile = previewUserProfile(),
        content = "오늘 새로운 프로젝트를 시작했습니다!"
    )
}

private val previewCreatePostSectionTags = listOf(
    Tag(id = 1, name = "음악 이야기", isSectionEnabled = true),
    Tag(id = 2, name = "일상", isSectionEnabled = true)
)

@HaloLightDarkPreview
@Composable
private fun CreatePostScreenDefaultPreview() {
    HaloScreenPreview {
        CreatePostScreenContent(
            state = previewCreatePostDefaultState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatePostScreenWithSelectedImagesPreview() {
    HaloScreenPreview {
        CreatePostScreenContent(
            state = previewCreatePostWithSelectedImagesState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatePostScreenWithSelectedVideoPreview() {
    HaloScreenPreview {
        CreatePostScreenContent(
            state = previewCreatePostWithSelectedVideoState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatePostScreenWithContentPreview() {
    HaloScreenPreview {
        CreatePostScreenContent(
            state = previewCreatePostWithContentState(),
            onIntent = {}
        )
    }
}
