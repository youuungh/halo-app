package com.ninezero.halo.core.ui.components.fab

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeState
import androidx.compose.ui.graphics.shadow.Shadow as DropShadow

/** 스크롤 연동 FAB */
@Composable
private fun ScrollAwareFAB(
    progress: Float,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val hapticFeedback: HapticFeedback = LocalHapticFeedback.current
    val view: View = LocalView.current
    val isDark = LocalIsDarkTheme.current

    val scale = progress.coerceIn(0f, 1f)
    val shape = MaterialTheme.shapes.large
    val backing = MaterialTheme.colorScheme.surface  // 뒤 화면 배경과 같은 불투명 색
    val glassTint = if (isDark) Color(0xFF28282C).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.55f)
    // 위쪽 하이라이트와 가장자리 빛
    val highlight = if (isDark) {
        listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.03f))
    } else {
        listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.2f))
    }
    val edge = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)

    if (scale > 0.1f) {
        Box(
            modifier = modifier
                .scale(scale)
                .bounceClick()
                .dropShadow(
                    shape,
                    DropShadow(
                        radius = 14.dp,
                        color = Color.Black.copy(alpha = if (isDark) 0.4f else 0.14f),
                        offset = DpOffset(0.dp, 4.dp)
                    )
                )
                .clip(shape)
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = backing,
                        tint = HaloBlurTint(glassTint),
                        blurRadius = 20.dp,
                        noiseFactor = 0f,
                        fallbackTint = HaloBlurTint(glassTint.copy(alpha = 0.9f))
                    )
                }
                .background(Brush.verticalGradient(highlight))
                .border(0.5.dp, edge, shape)
                .clickable {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chat_bubbles),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .size(24.dp)
            )
        }
    }
}

/** 스크롤 연동 FAB 애니메이션 래퍼 */
@Composable
fun AnimatedScrollAwareFAB(
    visible: Boolean,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialScale = 0.5f
        ) + fadeIn(animationSpec = tween(durationMillis = 150)),
        exit = scaleOut(
            animationSpec = tween(durationMillis = 150),
            targetScale = 0.5f
        ) + fadeOut(animationSpec = tween(durationMillis = 150))
    ) {
        ScrollAwareFAB(
            progress = 1f,
            hazeState = hazeState,
            contentDescription = contentDescription,
            onClick = onClick
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ScrollAwareFABPreview() {
    HaloTheme {
        ScrollAwareFAB(
            progress = 1f,
            hazeState = remember { HazeState() },
            contentDescription = "Add",
            onClick = {}
        )
    }
}
