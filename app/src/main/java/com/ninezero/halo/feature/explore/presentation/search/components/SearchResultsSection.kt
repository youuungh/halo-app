package com.ninezero.halo.feature.explore.presentation.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.explore.domain.model.Creator
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun SearchResultsSection(
    results: List<Creator>,
    isLoading: Boolean,
    hazeState: HazeState,
    listState: LazyListState = rememberLazyListState(),
    topPadding: Dp = 0.dp,
    onCreatorClick: (Int) -> Unit
) {
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = topPadding, bottom = maxOf(imeBottom, LocalBottomBarPadding.current)),
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
    ) {
        if (isLoading && results.isEmpty()) {
            items(5) { index ->
                SearchResultItemSkeleton()
                if (index < 4) {
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        } else if (results.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "검색 결과가 없어요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            itemsIndexed(
                items = results,
                key = { _, creator -> creator.id }
            ) { index, creator ->
                SearchResultItem(
                    avatarUrl = creator.avatarThumbUrl ?: creator.avatarUrl,
                    displayName = creator.displayName ?: creator.username,
                    username = creator.username,
                    onClick = { onCreatorClick(creator.id) }
                )
                if (index < results.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            // 추가 로딩 스켈레톤
            if (isLoading && results.isNotEmpty()) {
                items(3) {
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SearchResultItemSkeleton()
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    avatarUrl: String?,
    displayName: String,
    username: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "$displayName 프로필",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_profile_placeholder),
            error = painterResource(R.drawable.ic_profile_placeholder),
            fallback = painterResource(R.drawable.ic_profile_placeholder)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = username,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchResultsSectionPreview() {
    HaloTheme {
        Surface {
            SearchResultsSection(
                results = listOf(
                    Creator(
                        id = 1,
                        username = "creator1",
                        displayName = "크리에이터 1",
                        avatarUrl = null,
                        role = "CREATOR",
                        isFollowing = false
                    ),
                    Creator(
                        id = 2,
                        username = "creator2",
                        displayName = "크리에이터 2",
                        avatarUrl = null,
                        role = "CREATOR",
                        isFollowing = true
                    )
                ),
                isLoading = false,
                hazeState = remember { HazeState() },
                onCreatorClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchResultsSectionLoadingPreview() {
    HaloTheme {
        Surface {
            SearchResultsSection(
                results = emptyList(),
                isLoading = true,
                hazeState = remember { HazeState() },
                onCreatorClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchResultsSectionEmptyPreview() {
    HaloTheme {
        Surface {
            SearchResultsSection(
                results = emptyList(),
                isLoading = false,
                hazeState = remember { HazeState() },
                onCreatorClick = {}
            )
        }
    }
}
