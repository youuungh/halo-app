package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState

@Composable
fun CreateProductBottomBar(
    hazeState: HazeState,
    backgroundColor: Color,
    onImageClick: () -> Unit,
    onTextFormatClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFormatOpen: Boolean = false,
    formatBar: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .haloBlur(hazeState, HaloBlurs.fadeUp(backgroundColor))
            .pointerInput(Unit) {
                detectTapGestures { /* 터치 이벤트 소비 */ }
            }
            // imePadding은 부모에서 한 번만
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
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
            icon = painterResource(if (isFormatOpen) R.drawable.ic_close else R.drawable.ic_type),
            contentDescription = if (isFormatOpen) "서식 닫기" else "텍스트 서식",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            size = 32.dp,
            iconSize = 18.dp
        )

        formatBar()
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
                    onTextFormatClick = {}
                )
            }
        }
    }
}
