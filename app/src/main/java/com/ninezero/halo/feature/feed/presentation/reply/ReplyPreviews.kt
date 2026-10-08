package com.ninezero.halo.feature.feed.presentation.reply

import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewMediaItem
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.core.ui.preview.previewUserProfile

internal fun previewReplyDefaultState(): ReplyState {
    return ReplyState(
        myProfile = previewUserProfile(),
        post = previewPost(
            content = "오늘 새로운 프로젝트를 시작했습니다. Kotlin과 Jetpack Compose로 만드는 소셜 앱인데요, 정말 재미있어요!",
            author = previewAuthor(1, "developer", "개발자"),
            mediaAttachments = listOf(
                previewMediaItem(1, url = "https://picsum.photos/400/300"),
                previewMediaItem(2, url = "https://picsum.photos/400/301"),
                previewMediaItem(3, url = "https://picsum.photos/400/302")
            ),
            likeCount = 42,
            commentCount = 15,
            viewCount = 1240,
            isLiked = true,
            createdAtOffset = 3_600_000
        )
    )
}

internal fun previewReplyWithSelectedImagesState(): ReplyState {
    return ReplyState(
        myProfile = previewUserProfile(),
        post = previewPost(
            content = "오늘 새로운 프로젝트를 시작했습니다!",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 42,
            commentCount = 15,
            viewCount = 1240,
            isLiked = true,
            createdAtOffset = 3_600_000
        ),
        replyContent = "멋진 프로젝트네요!",
        selectedMediaUris = listOf(
            "https://picsum.photos/200/200".toUri(),
            "https://picsum.photos/201/200".toUri(),
            "https://picsum.photos/202/200".toUri()
        )
    )
}

internal fun previewReplyWithSelectedVideoState(): ReplyState {
    return ReplyState(
        myProfile = previewUserProfile(),
        post = previewPost(
            content = "오늘 새로운 프로젝트를 시작했습니다!",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 42,
            commentCount = 15,
            viewCount = 1240,
            isLiked = true,
            createdAtOffset = 3_600_000
        ),
        replyContent = "동영상 첨부 테스트!",
        selectedMediaUris = listOf("content://media/external/video/media/1000".toUri())
    )
}

internal fun previewReplyWithContentState(): ReplyState {
    return ReplyState(
        myProfile = previewUserProfile(),
        post = previewPost(
            content = "오늘 새로운 프로젝트를 시작했습니다!",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 42,
            commentCount = 15,
            viewCount = 1240,
            isLiked = true,
            createdAtOffset = 3_600_000
        ),
        replyContent = "멋진 프로젝트네요!"
    )
}

@HaloLightDarkPreview
@Composable
private fun ReplyScreenDefaultPreview() {
    HaloScreenPreview {
        ReplyScreenContent(
            state = previewReplyDefaultState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ReplyScreenWithSelectedImagesPreview() {
    HaloScreenPreview {
        ReplyScreenContent(
            state = previewReplyWithSelectedImagesState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ReplyScreenWithSelectedVideoPreview() {
    HaloScreenPreview {
        ReplyScreenContent(
            state = previewReplyWithSelectedVideoState(),
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ReplyScreenWithContentPreview() {
    HaloScreenPreview {
        ReplyScreenContent(
            state = previewReplyWithContentState(),
            onIntent = {}
        )
    }
}
