package com.ninezero.halo.feature.feed.presentation.blocked_profiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.feed.presentation.blocked_profiles.components.BlockedUserItem
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun BlockedProfilesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Int) -> Unit = {},
    viewModel: BlockedProfilesViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is BlockedProfilesSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(BlockedProfilesIntent.SnackbarDismissed) }
    )

    BlockedProfilesScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun BlockedProfilesScreenContent(
    state: BlockedProfilesState,
    onNavigateBack: () -> Unit,
    onIntent: (BlockedProfilesIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    PagedListScreenShell(
        title = "차단된 프로필",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        isEmpty = state.blockedUsers.isEmpty(),
        error = state.error,
        emptyTitle = "차단된 프로필이 없어요",
        emptyDescription = "차단한 사용자가 여기에 표시돼요",
        onNavigateBack = onNavigateBack,
        onRefresh = { onIntent(BlockedProfilesIntent.RefreshBlockedUsers) },
        onRetry = { onIntent(BlockedProfilesIntent.LoadBlockedUsers) },
        onLoadMore = { onIntent(BlockedProfilesIntent.LoadMoreBlockedUsers) },
        snackbarHostState = snackbarHostState
    ) { listState, headerHeight ->
        LazyColumn(
            state = listState,
            modifier = Modifier.Companion,
            contentPadding = PaddingValues(top = headerHeight + 16.dp, bottom = 16.dp)
        ) {
            itemsIndexed(
                items = state.blockedUsers,
                key = { _, user -> user.id }
            ) { index, user ->
                Column(modifier = Modifier.animateItem()) {
                    BlockedUserItem(
                        user = user,
                        onClick = { onIntent(BlockedProfilesIntent.NavigateToUserProfile(user.id)) },
                        onUnblockClick = { onIntent(BlockedProfilesIntent.UnblockClick(user)) }
                    )

                    if (index < state.blockedUsers.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 64.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (state.isLoading && state.blockedUsers.isNotEmpty()) {
                item(key = "loading_more") {
                    ListLoadMoreIndicator()
                }
            }
        }
    }

    if (state.showUnblockDialog && state.selectedUser != null) {
        ConfirmDialog(
            profileImageUrl = state.selectedUser.avatarUrl,
            title = "${state.selectedUser.displayName ?: state.selectedUser.username}님의 차단을 해제하시겠어요?",
            message = "차단을 해제하면 서로의 포스트와 프로필을 다시 볼 수 있습니다.",
            confirmText = "차단 해제",
            dismissText = "취소",
            onConfirm = { onIntent(BlockedProfilesIntent.ConfirmUnblock) },
            onDismiss = { onIntent(BlockedProfilesIntent.DismissUnblockDialog) }
        )
    }
}
