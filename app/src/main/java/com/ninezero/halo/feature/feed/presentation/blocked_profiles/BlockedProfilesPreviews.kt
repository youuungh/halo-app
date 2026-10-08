package com.ninezero.halo.feature.feed.presentation.blocked_profiles

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewUserSummary

internal fun previewBlockedProfilesState(): BlockedProfilesState {
    return BlockedProfilesState(blockedUsers = previewBlockedUsers)
}

internal fun previewBlockedProfilesEmptyState(): BlockedProfilesState {
    return BlockedProfilesState()
}

internal fun previewBlockedProfilesLoadingState(): BlockedProfilesState {
    return BlockedProfilesState(isLoading = true)
}

internal fun previewBlockedProfilesErrorState(): BlockedProfilesState {
    return BlockedProfilesState(error = "네트워크 연결을 확인해주세요")
}

internal fun previewBlockedProfilesDialogState(): BlockedProfilesState {
    return BlockedProfilesState(
        blockedUsers = previewBlockedUsers,
        showUnblockDialog = true,
        selectedUser = previewBlockedUsers.first()
    )
}

private val previewBlockedUsers = listOf(
    previewUserSummary(id = 1, username = "blocked_user1", displayName = "차단된 유저 1"),
    previewUserSummary(id = 2, username = "spam_account", displayName = "스팸 계정"),
    previewUserSummary(id = 3, username = "unwanted_user", displayName = "원치 않는 유저"),
    previewUserSummary(id = 4, username = "another_blocked", displayName = null),
    previewUserSummary(id = 5, username = "blocked_creator", displayName = "차단된 크리에이터", role = "CREATOR")
)

@HaloLightDarkPreview
@Composable
private fun BlockedProfilesScreenPreview() {
    HaloScreenPreview {
        BlockedProfilesScreenContent(
            state = previewBlockedProfilesState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun BlockedProfilesScreenDialogPreview() {
    HaloScreenPreview {
        BlockedProfilesScreenContent(
            state = previewBlockedProfilesDialogState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun BlockedProfilesScreenLoadingPreview() {
    HaloScreenPreview {
        BlockedProfilesScreenContent(
            state = previewBlockedProfilesLoadingState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun BlockedProfilesScreenEmptyPreview() {
    HaloScreenPreview {
        BlockedProfilesScreenContent(
            state = previewBlockedProfilesEmptyState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun BlockedProfilesScreenErrorPreview() {
    HaloScreenPreview {
        BlockedProfilesScreenContent(
            state = previewBlockedProfilesErrorState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}
