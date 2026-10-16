package com.ninezero.halo.feature.feed.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState

@Composable
fun CreatePostBottomBar(
    hazeState: HazeState,
    backgroundColor: Color,
    showSettingsButton: Boolean,
    onSettingsClick: () -> Unit,
    onTagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = backgroundColor,
                    tint = HaloBlurTint(backgroundColor),
                    noiseFactor = 0f
                )
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = 0.5f,
                    endIntensity = 0f
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { /* 터치 이벤트 소비 */ }
            }
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showSettingsButton) {
            GlassIconButton(
                onClick = onSettingsClick,
                icon = painterResource(R.drawable.ic_adjustments_horizontal),
                contentDescription = "설정",
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                size = 32.dp,
                iconSize = 18.dp
            )
        }

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
private fun CreatePostBottomBarWithSettingsPreview() {
    HaloTheme {
        val hazeState = remember { HazeState() }
        val backgroundColor = MaterialTheme.colorScheme.background

        Surface(color = backgroundColor) {
            CreatePostBottomBar(
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                showSettingsButton = true,
                onSettingsClick = {},
                onTagClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CreatePostBottomBarWithoutSettingsPreview() {
    HaloTheme {
        val hazeState = remember { HazeState() }
        val backgroundColor = MaterialTheme.colorScheme.background

        Surface(color = backgroundColor) {
            CreatePostBottomBar(
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                showSettingsButton = false,
                onSettingsClick = {},
                onTagClick = {}
            )
        }
    }
}
