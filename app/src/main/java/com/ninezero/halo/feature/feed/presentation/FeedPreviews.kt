package com.ninezero.halo.feature.feed.presentation
import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewMediaItem
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.feature.explore.domain.model.Creator

@HaloLightDarkPreview
@Composable
private fun FeedScreenPreview() {
    FeedScreenPreviewContent(
        state = FeedState(
            recommendedCreators = listOf(
                Creator(
                    id = 1,
                    username = "username",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl",
                    role = "CREATOR",
                    isFollowing = false
                ),
                Creator(
                    id = 2,
                    username = "username",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl",
                    role = "CREATOR",
                    isFollowing = true
                ),
                Creator(
                    id = 3,
                    username = "username",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl",
                    role = "CREATOR",
                    isFollowing = false
                ),
                Creator(
                    id = 4,
                    username = "username",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl",
                    role = "CREATOR",
                    isFollowing = false
                ),
                Creator(
                    id = 5,
                    username = "username",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl",
                    role = "CREATOR",
                    isFollowing = true
                ),
                Creator(
                    id = 6,
                    username = "username",
                    displayName = "displayName",
                    avatarUrl = "avatarUrl",
                    role = "CREATOR",
                    isFollowing = false
                )
            ),
            posts = listOf(
                previewPost(
                    id = 1,
                    author = previewAuthor(1, "username", "displayName", "avatarUrl"),
                    content = "내용내용내용내용내용내용내용",
                    likeCount = 12,
                    commentCount = 3,
                    isLiked = true,
                    createdAtOffset = 3600000
                ),
                previewPost(
                    id = 2,
                    author = previewAuthor(2, "username", "displayName", "avatarUrl"),
                    content = "내용내용내용내용내용내용내용내용내용내용내용",
                    likeCount = 1234,
                    commentCount = 56,
                    isBookmarked = true,
                    mediaAttachments = listOf(previewMediaItem(1)),
                    createdAtOffset = 86400000
                ),
                previewPost(
                    id = 3,
                    author = previewAuthor(3, "username", "displayName", "avatarUrl"),
                    content = "내용내용내용내용내용내용내용내용내용내용내용내용내용",
                    likeCount = 123,
                    commentCount = 45,
                    isLiked = true,
                    mediaAttachments = listOf(
                        previewMediaItem(1),
                        previewMediaItem(2),
                        previewMediaItem(3)
                    ),
                    createdAtOffset = 5400000
                )
            )
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun FeedScreenLoadingPreview() {
    FeedScreenPreviewContent(state = FeedState(isLoading = true))
}

@HaloLightDarkPreview
@Composable
private fun FeedScreenEmptyPreview() {
    FeedScreenPreviewContent(
        state = FeedState(
            recommendedCreators = emptyList(),
            posts = emptyList()
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun FeedScreenErrorPreview() {
    FeedScreenPreviewContent(
        state = FeedState(
            error = "네트워크 연결을 확인해주세요",
            posts = emptyList()
        )
    )
}

@Composable
private fun FeedScreenPreviewContent(
    state: FeedState
) {
    HaloScreenPreview {
        FeedScreenContent(
            state = state,
            onIntent = {}
        )
    }
}
