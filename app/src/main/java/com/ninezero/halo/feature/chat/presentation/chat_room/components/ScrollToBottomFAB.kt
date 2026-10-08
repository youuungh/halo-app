package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeState

@Composable
fun ScrollToBottomFAB(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val hazeTintColor = if (LocalIsDarkTheme.current) {
                Color.White.copy(alpha = 0.08f)
            } else {
                Color.Black.copy(alpha = 0.08f)
            }

            GlassIconButton(
                onClick = onClick,
                icon = painterResource(R.drawable.ic_chevron_down),
                contentDescription = "맨 아래로 스크롤",
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                hazeTintColor = hazeTintColor,
                size = 40.dp,
                iconSize = 24.dp
            )
        }
    }
}
