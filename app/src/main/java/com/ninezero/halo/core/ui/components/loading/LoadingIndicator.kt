package com.ninezero.halo.core.ui.components.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class LoadingIndicatorSize(val size: Dp, val strokeWidth: Dp) {
    Small(16.dp, 2.dp),
    Medium(24.dp, 2.dp),
    Large(36.dp, 2.dp),
    ExtraLarge(40.dp, 2.dp)
}

/** 로딩 인디케이터 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: LoadingIndicatorSize = LoadingIndicatorSize.Medium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.size),
        strokeWidth = size.strokeWidth,
        color = color
    )
}

/** 가운데 로딩 인디케이터 */
@Composable
fun CenteredLoadingIndicator(
    modifier: Modifier = Modifier,
    size: LoadingIndicatorSize = LoadingIndicatorSize.Medium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(size = size, color = color)
    }
}

/** 목록 추가 로딩 인디케이터 */
@Composable
fun ListLoadMoreIndicator(
    modifier: Modifier = Modifier,
    size: LoadingIndicatorSize = LoadingIndicatorSize.Medium,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(size = size, color = color)
    }
}
