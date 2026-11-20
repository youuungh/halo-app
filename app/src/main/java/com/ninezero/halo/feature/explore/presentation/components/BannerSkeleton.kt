package com.ninezero.halo.feature.explore.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.theme.HaloTheme

@Composable
fun BannerSkeleton(
    modifier: Modifier = Modifier
) {
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
                .clip(MaterialTheme.shapes.large)
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
