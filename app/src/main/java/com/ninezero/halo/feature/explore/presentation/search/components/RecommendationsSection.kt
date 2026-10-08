package com.ninezero.halo.feature.explore.presentation.search.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.explore.presentation.components.UserListItem
import com.ninezero.halo.feature.explore.presentation.components.UserListItemSkeleton
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun RecommendationsSection(
    recommendations: List<UserSummary>,
    isLoading: Boolean,
    hazeState: HazeState,
    onUserClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit,
    topPadding: Dp = 0.dp
) {
    LazyColumn(
        contentPadding = PaddingValues(top = topPadding, bottom = LocalBottomBarPadding.current),
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
    ) {
        if (isLoading && recommendations.isEmpty()) {
            items(5) { index ->
                if (index == 0) {
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                UserListItemSkeleton()
                HorizontalDivider(
                    thickness = 0.25.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        } else if (recommendations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "추천할 사용자가 없어요",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "관심 있는 크리에이터를 팔로우해보세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            itemsIndexed(
                items = recommendations,
                key = { _, user -> user.id }
            ) { index, user ->
                if (index == 0) {
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                UserListItem(
                    avatarUrl = user.avatarThumbUrl ?: user.avatarUrl,
                    displayName = user.displayName ?: user.username,
                    username = user.username,
                    followerCount = user.followerCount,
                    role = user.role,
                    isFollowing = user.isFollowing,
                    onClick = { onUserClick(user.id) },
                    onFollowClick = { onFollowClick(user.id) }
                )
                HorizontalDivider(
                    thickness = 0.25.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun RecommendationsSectionPreview() {
    HaloTheme {
        Surface {
            RecommendationsSection(
                recommendations = listOf(
                    UserSummary(
                        id = 1,
                        username = "creator1",
                        displayName = "크리에이터 1",
                        avatarUrl = null,
                        role = "CREATOR",
                        followerCount = 12340
                    ),
                    UserSummary(
                        id = 2,
                        username = "user2",
                        displayName = "사용자 2",
                        avatarUrl = null,
                        role = "USER",
                        followerCount = 567
                    )
                ),
                isLoading = false,
                hazeState = remember { HazeState() },
                onUserClick = {},
                onFollowClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun RecommendationsSectionLoadingPreview() {
    HaloTheme {
        Surface {
            RecommendationsSection(
                recommendations = emptyList(),
                isLoading = true,
                hazeState = remember { HazeState() },
                onUserClick = {},
                onFollowClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun RecommendationsSectionEmptyPreview() {
    HaloTheme {
        Surface {
            RecommendationsSection(
                recommendations = emptyList(),
                isLoading = false,
                hazeState = remember { HazeState() },
                onUserClick = {},
                onFollowClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun RecommendationsSectionWithFollowingPreview() {
    HaloTheme {
        Surface {
            RecommendationsSection(
                recommendations = listOf(
                    UserSummary(
                        id = 1,
                        username = "creator1",
                        displayName = "크리에이터 1",
                        avatarUrl = null,
                        role = "CREATOR",
                        followerCount = 12340,
                        isFollowing = true
                    ),
                    UserSummary(
                        id = 2,
                        username = "user2",
                        displayName = "사용자 2",
                        avatarUrl = null,
                        role = "USER",
                        followerCount = 567,
                        isFollowing = false
                    ),
                    UserSummary(
                        id = 3,
                        username = "creator3",
                        displayName = "크리에이터 3",
                        avatarUrl = null,
                        role = "CREATOR",
                        followerCount = 89012,
                        isFollowing = true
                    )
                ),
                isLoading = false,
                hazeState = remember { HazeState() },
                onUserClick = {},
                onFollowClick = {}
            )
        }
    }
}
