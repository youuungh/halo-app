package com.ninezero.halo.feature.profile.presentation.my_profile.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.layout.collapsingGlassTopBarHeight
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

// 다 접히면 블러 끝을 탭 뒤로 숨김
private val TopBarBlurExtra = 24.dp

/** 내 프로필 톱바, collapseReveal 1 = 다 접힘 */
@Composable
fun MyProfileTopBar(
    title: String,
    hazeState: HazeState,
    backgroundColor: Color,
    collapseReveal: () -> Float,
    blurAlpha: () -> Float,
    onSettingsClick: () -> Unit
) {
    val topBarHeight = collapsingGlassTopBarHeight()
    val statusBarHeight = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val height = (topBarHeight.toPx() + TopBarBlurExtra.toPx() * collapseReveal()).roundToInt()
                    val placeable = measurable.measure(Constraints.fixed(constraints.maxWidth, height))
                    layout(constraints.maxWidth, height) { placeable.place(0, 0) }
                }
                .pointerInput(Unit) { detectTapGestures { /* 터치 이벤트 소비 */ } }
                .graphicsLayer { alpha = blurAlpha() }
                .haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPaddingIgnoringVisibility()
                .height(topBarHeight - statusBarHeight)
                .padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            GlassIconButton(
                onClick = onSettingsClick,
                icon = painterResource(R.drawable.ic_settings),
                contentDescription = "설정",
                hazeState = hazeState,
                backgroundColor = backgroundColor
            )
        }
    }
}
