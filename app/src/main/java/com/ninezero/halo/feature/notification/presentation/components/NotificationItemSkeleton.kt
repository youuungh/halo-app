package com.ninezero.halo.feature.notification.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

@Composable
fun NotificationItemSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),  // 실제 줄의 읽음 점 자리만큼
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .shimmer()
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .shimmer()
            )
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .shimmer()
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationItemSkeletonPreview() {
    HaloTheme {
        Column {
            NotificationItemSkeleton()
            NotificationItemSkeleton()
            NotificationItemSkeleton()
        }
    }
}
