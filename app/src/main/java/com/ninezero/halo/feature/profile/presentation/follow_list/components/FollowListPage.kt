package com.ninezero.halo.feature.profile.presentation.follow_list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import dev.chrisbanes.haze.HazeState

@Composable
internal fun FollowListPage(
    hazeState: HazeState?,
    users: List<UserSummary>,
    isLoading: Boolean,
    error: String?,
    errorTitle: String,
    emptyText: String,
    isMyProfile: Boolean,
    isFollowingTab: Boolean,
    listState: LazyListState,
    headerHeight: Dp,
    onRetry: () -> Unit,
    onUserClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit,
    onMessageClick: (Int) -> Unit,
    onUnfollowClick: (UserSummary) -> Unit
) {
    // 비었을 땐 다른 목록 화면처럼 화면 정가운데
    if (users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                error != null -> ListErrorState(
                    title = errorTitle,
                    reason = error,
                    onRetry = onRetry
                )
                isLoading -> LoadingIndicator(size = LoadingIndicatorSize.Large)
                else -> Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = headerHeight, bottom = imeBottom)
    ) {
        items(
            items = users,
            key = { it.id }
        ) { user ->
            FollowListItem(
                hazeState = hazeState,
                user = user,
                isMyProfile = isMyProfile,
                isFollowingTab = isFollowingTab,
                onUserClick = { onUserClick(user.id) },
                onFollowClick = { onFollowClick(user.id) },
                onMessageClick = { onMessageClick(user.id) },
                onUnfollowClick = { onUnfollowClick(user) }
            )
        }

        if (isLoading) {
            item {
                ListLoadMoreIndicator()
            }
        }
    }
}
