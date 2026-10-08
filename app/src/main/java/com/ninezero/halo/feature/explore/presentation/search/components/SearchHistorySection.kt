package com.ninezero.halo.feature.explore.presentation.search.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.explore.domain.model.SearchHistory
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun SearchHistorySection(
    history: List<SearchHistory>,
    isLoading: Boolean,
    isEditing: Boolean,
    hazeState: HazeState,
    onHistoryClick: (String) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onEditClick: () -> Unit,
    onClearAll: () -> Unit,
    topPadding: Dp = 0.dp
) {
    // 화면 배경색을 깔아 hazeSource가 투명해지지 않게 함
    if (history.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    if (isLoading && history.isEmpty()) {
        SearchHistorySkeleton(
            modifier = Modifier.hazeSource(hazeState)
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
            .padding(top = topPadding)
            .padding(bottom = LocalBottomBarPadding.current)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 검색",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    Text(
                        text = "모두 지우기",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClearAll)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = if (isEditing) "완료" else "수정",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isEditing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditClick)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            history.forEach { item ->
                SearchHistoryChip(
                    keyword = item.keyword,
                    isEditing = isEditing,
                    onClick = { onHistoryClick(item.keyword) },
                    onDeleteClick = { onDeleteClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun SearchHistoryChip(
    keyword: String,
    isEditing: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = !isEditing, onClick = onClick)
            .padding(start = 14.dp, end = if (isEditing) 10.dp else 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = keyword,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        AnimatedVisibility(
            visible = isEditing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "삭제",
                        modifier = Modifier.size(8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchHistorySectionPreview() {
    HaloTheme {
        Surface {
            SearchHistorySection(
                history = listOf(
                    SearchHistory(1, "크리에이터"),
                    SearchHistory(2, "디자인"),
                    SearchHistory(3, "일러스트"),
                    SearchHistory(4, "포토그래퍼"),
                    SearchHistory(5, "영상편집")
                ),
                isLoading = false,
                isEditing = false,
                hazeState = remember { HazeState() },
                onHistoryClick = {},
                onDeleteClick = {},
                onEditClick = {},
                onClearAll = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchHistorySectionEditingPreview() {
    HaloTheme {
        Surface {
            SearchHistorySection(
                history = listOf(
                    SearchHistory(1, "크리에이터"),
                    SearchHistory(2, "디자인"),
                    SearchHistory(3, "일러스트")
                ),
                isLoading = false,
                isEditing = true,
                hazeState = remember { HazeState() },
                onHistoryClick = {},
                onDeleteClick = {},
                onEditClick = {},
                onClearAll = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SearchHistorySectionLoadingPreview() {
    HaloTheme {
        Surface {
            SearchHistorySection(
                history = emptyList(),
                isLoading = true,
                isEditing = false,
                hazeState = remember { HazeState() },
                onHistoryClick = {},
                onDeleteClick = {},
                onEditClick = {},
                onClearAll = {}
            )
        }
    }
}
