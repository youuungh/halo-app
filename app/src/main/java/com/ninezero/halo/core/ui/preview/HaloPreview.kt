package com.ninezero.halo.core.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ninezero.halo.ui.theme.HaloTheme

/** 프리뷰 테마 래퍼 */
@Composable
fun HaloPreview(
    content: @Composable () -> Unit
) {
    HaloTheme {
        content()
    }
}

/** Surface 프리뷰 래퍼 */
@Composable
fun HaloSurfacePreview(
    content: @Composable () -> Unit
) {
    HaloPreview {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

/** 화면 프리뷰 래퍼 */
@Composable
fun HaloScreenPreview(
    content: @Composable () -> Unit
) {
    HaloPreview {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
