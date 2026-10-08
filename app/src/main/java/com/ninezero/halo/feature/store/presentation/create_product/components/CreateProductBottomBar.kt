package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
fun CreateProductBottomBar(
    hazeState: HazeState,
    backgroundColor: Color,
    onImageClick: () -> Unit,
    onTextFormatClick: () -> Unit,
    onTagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState) {
                style = HazeStyle(
                    backgroundColor = backgroundColor,
                    tint = HazeTint(backgroundColor),
                    noiseFactor = 0f
                )
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = 0f,
                    endIntensity = 0.5f
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { /* 터치 이벤트 소비 */ }
            }
            // imePadding은 부모에서 한 번만
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GlassIconButton(
            onClick = onImageClick,
            icon = painterResource(R.drawable.ic_image),
            contentDescription = "이미지 삽입",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            size = 32.dp,
            iconSize = 18.dp
        )

        GlassIconButton(
            onClick = onTextFormatClick,
            icon = painterResource(R.drawable.ic_type),
            contentDescription = "텍스트 서식",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            size = 32.dp,
            iconSize = 18.dp
        )

        GlassIconButton(
            onClick = onTagClick,
            icon = painterResource(R.drawable.ic_tag),
            contentDescription = "태그",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            size = 32.dp,
            iconSize = 18.dp
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CreateProductBottomBarPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                CreateProductBottomBar(
                    hazeState = remember { HazeState() },
                    backgroundColor = MaterialTheme.colorScheme.background,
                    onImageClick = {},
                    onTextFormatClick = {},
                    onTagClick = {}
                )
            }
        }
    }
}
