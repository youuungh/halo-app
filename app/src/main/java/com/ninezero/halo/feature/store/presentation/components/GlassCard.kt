package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.ui.theme.Primary
import com.ninezero.halo.ui.theme.Success
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val haze = remember { HazeState() }
    val tintColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        // 그라데이션
        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeSource(haze)
                .background(baseGradient(isDark))
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 60.dp, y = (-60).dp)
                    .background(blobBrush(Primary, isDark), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 50.dp)
                    .background(blobBrush(Success, isDark), CircleShape)
            )
        }

        // 프로스트 글래스
        Box(
            modifier = Modifier
                .matchParentSize()
                .haloBlur(state = haze) {
                    style = HaloBlurStyle(
                        backgroundColor = tintColor,
                        tint = HaloBlurTint(tintColor.copy(alpha = if (isDark) 0.30f else 0.40f)),
                        noiseFactor = 0.05f
                    )
                }
        )

        // 콘텐츠
        content()
    }
}

private fun baseGradient(isDark: Boolean): Brush = if (isDark) {
    Brush.linearGradient(
        colors = listOf(Color(0xFF1F2A40), Color(0xFF241F38))
    )
} else {
    Brush.linearGradient(
        colors = listOf(Color(0xFFEDF4FF), Color(0xFFF1ECFF))
    )
}

private fun blobBrush(color: Color, isDark: Boolean): Brush = Brush.radialGradient(
    colors = listOf(color.copy(alpha = if (isDark) 0.35f else 0.30f), Color.Transparent)
)

@Composable
fun SummaryStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelTrailing: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (labelTrailing != null) {
                Spacer(modifier = Modifier.width(3.dp))
                labelTrailing()
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun GlassCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryStatCell(label = "이번 달 매출", value = "4,150,000원")
                        SummaryStatCell(label = "누적 매출", value = "28,900,000원")
                    }
                }
            }
        }
    }
}
