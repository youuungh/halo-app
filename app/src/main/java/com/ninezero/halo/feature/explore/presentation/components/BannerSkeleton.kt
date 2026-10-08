package com.ninezero.halo.feature.explore.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun BannerSkeleton(
    modifier: Modifier = Modifier
) {
    // 스켈레톤 ↔ 실제 배너 전환 시 높이 점프가 없게 실제와 맞춤
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmer()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BannerSkeletonPreview() {
    HaloTheme {
        BannerSkeleton()
    }
}
