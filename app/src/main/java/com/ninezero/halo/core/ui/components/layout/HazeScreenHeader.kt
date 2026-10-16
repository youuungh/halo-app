package com.ninezero.halo.core.ui.components.layout

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState

/** Haze 화면 헤더 */
@Composable
fun HazeScreenHeader(
    title: String,
    hazeState: HazeState,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,  // onNavigateBack이 우선
    startIntensity: Float = 0.5f,  // 리스트 0.5, 시트 1.0
    onHeightMeasured: ((Dp) -> Unit)? = null,  // 리스트 contentPadding용
    actions: @Composable BoxScope.() -> Unit = {},
    filterContent: @Composable () -> Unit = {}
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = backgroundColor,
                    tint = HaloBlurTint(backgroundColor),
                    noiseFactor = 0f
                )
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = startIntensity,
                    endIntensity = 0f
                )
            }
            .pointerInput(Unit) {
                // 헤더 영역 터치를 소비해 뒤 리스트로 전달되지 않게 함
                detectTapGestures { }
            }
            .then(
                if (onHeightMeasured != null) {
                    Modifier.onGloballyPositioned { onHeightMeasured(with(density) { it.size.height.toDp() }) }
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPaddingIgnoringVisibility()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val leadingClick = onNavigateBack ?: onClose
            if (leadingClick != null) {
                GlassIconButton(
                    onClick = leadingClick,
                    icon = painterResource(
                        id = if (onNavigateBack != null) R.drawable.ic_chevron_left else R.drawable.ic_close
                    ),
                    contentDescription = if (onNavigateBack != null) "뒤로가기" else "닫기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )

            actions()
        }

        filterContent()
    }
}
