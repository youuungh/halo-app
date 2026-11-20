package com.ninezero.halo.feature.store.presentation.sales_stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer

fun LazyListScope.salesStatsSkeleton() {
    item { SalesStatsSkeletonContent() }
}

@Composable
private fun SalesStatsSkeletonContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardFrame(height = 188.dp)  // hero
        CardFrame(height = 150.dp)  // 매출 추이
        CardFrame(height = 168.dp)  // 핵심 지표
    }
}

@Composable
private fun CardFrame(height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .shimmer()
    )
}
