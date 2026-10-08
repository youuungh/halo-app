package com.ninezero.halo.feature.notification.presentation.creator_notifications

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.profile.domain.model.FollowedCreator

private val previewCreators = listOf(
    FollowedCreator(
        id = 1,
        username = "tester_1",
        displayName = "테스터1",
        avatarUrl = null,
        avatarThumbUrl = null,
        isNotifyEnabled = true
    ),
    FollowedCreator(
        id = 2,
        username = "tester_2",
        displayName = "테스터2",
        avatarUrl = null,
        avatarThumbUrl = null,
        isNotifyEnabled = false
    ),
    FollowedCreator(
        id = 3,
        username = "tester_3",
        displayName = "테스터3",
        avatarUrl = null,
        avatarThumbUrl = null,
        isNotifyEnabled = true
    )
)

@HaloLightDarkPreview
@Composable
private fun CreatorNotificationsPreview() {
    HaloScreenPreview {
        CreatorNotificationsScreenContent(
            state = CreatorNotificationsState(
                creators = previewCreators,
                isLoading = false
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorNotificationsEmptyPreview() {
    HaloScreenPreview {
        CreatorNotificationsScreenContent(
            state = CreatorNotificationsState(
                creators = emptyList(),
                isLoading = false
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatorNotificationsErrorPreview() {
    HaloScreenPreview {
        CreatorNotificationsScreenContent(
            state = CreatorNotificationsState(
                isLoading = false,
                error = "크리에이터 목록을 불러올 수 없습니다"
            )
        )
    }
}
