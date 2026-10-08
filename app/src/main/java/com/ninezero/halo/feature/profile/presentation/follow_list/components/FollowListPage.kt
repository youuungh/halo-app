package com.ninezero.halo.feature.profile.presentation.follow_list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.modifiers.bounceClick

@Composable
internal fun FollowListPage(
    users: List<UserSummary>,
    isLoading: Boolean,
    error: String?,
    emptyText: String,
    isMyProfile: Boolean,
    isFollowingTab: Boolean,
    toggleLoadingUserIds: Set<Int>,
    listState: LazyListState,
    headerHeight: Dp,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onRetry: () -> Unit,
    onUserClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit,
    onMessageClick: (Int) -> Unit,
    onUnfollowClick: (UserSummary) -> Unit
) {
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = headerHeight, bottom = imeBottom)
    ) {
        // 검색바는 항상 첫 item
        item(key = "follow_search_bar") {
            FollowSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearch,
                onClear = onClearSearch,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        when {
            error != null && users.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onRetry,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }
            }
            users.isEmpty() && !isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                items(
                    items = users,
                    key = { it.id }
                ) { user ->
                    FollowListItem(
                        user = user,
                        isMyProfile = isMyProfile,
                        isFollowingTab = isFollowingTab,
                        isLoading = toggleLoadingUserIds.contains(user.id),
                        onUserClick = { onUserClick(user.id) },
                        onFollowClick = { onFollowClick(user.id) },
                        onMessageClick = { onMessageClick(user.id) },
                        onUnfollowClick = { onUnfollowClick(user) },
                        modifier = Modifier.animateItem()
                    )
                }

                if (isLoading && users.isNotEmpty()) {
                    item {
                        ListLoadMoreIndicator()
                    }
                }
            }
        }
    }
}
