package com.ninezero.halo.feature.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.blockHorizontalScrollLeak
import com.ninezero.halo.feature.explore.domain.model.Creator

@Composable
fun RecommendedCreatorsSection(
    creators: List<Creator>,
    isLoading: Boolean,
    onCreatorClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 로딩 중이 아니고 크리에이터가 없으면 섹션 숨김
    if (!isLoading && creators.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // 섹션 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "추천 크리에이터",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 크리에이터 목록 또는 로딩
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        } else {
            LazyRow(
                modifier = Modifier.blockHorizontalScrollLeak(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = creators,
                    key = { it.id }
                ) { creator ->
                    CreatorCard(
                        creator = creator,
                        onClick = { onCreatorClick(creator.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
