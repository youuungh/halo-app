package com.ninezero.halo.feature.auth.presentation.social

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick

@Composable
fun SocialIconButton(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    loadingColor: Color = Color(0xFF191919)
) {
    val isEnabled = enabled && !isLoading

    Button(
        onClick = { if (!isLoading) onClick() },
        modifier = modifier
            .height(48.dp)
            .then(if (isEnabled) Modifier.bounceClick() else Modifier),
        enabled = isEnabled,
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (isLoading) {
            LoadingIndicator(
                size = LoadingIndicatorSize.Small,
                color = loadingColor
            )
        } else {
            Image(
                painter = icon,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(24.dp)
                    .alpha(if (isEnabled) 1f else 0.5f)
            )
        }
    }
}
