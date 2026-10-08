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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 스크롤 연동 FAB */
@Composable
private fun ScrollAwareFAB(
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val hapticFeedback: HapticFeedback = LocalHapticFeedback.current
    val view: View = LocalView.current

    val scale = progress.coerceIn(0f, 1f)

    if (scale > 0.1f) {
        Surface(
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            },
            modifier = modifier
                .scale(scale)
                .bounceClick(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 6.dp
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
            contentDescription = "Add",
            onClick = {}
        )
    }
}
