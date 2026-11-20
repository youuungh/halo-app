package com.ninezero.halo.core.ui.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState

/** 타임딜 띠 */
@Composable
fun DealBand(
    dealEndAtMillis: Long,
    modifier: Modifier = Modifier
) {
    val remaining by rememberCountdown(dealEndAtMillis)
    if (remaining.isFinished) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_timer),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "타임딜",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${remaining.format()} 남음",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/** 타임딜 카운트다운 오버레이 */
@Composable
fun DealCountdownImageOverlay(
    dealEndAtMillis: Long,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val remaining by rememberCountdown(dealEndAtMillis)
    if (remaining.isFinished) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(DealOverlayHeight)
            .haloBlur(hazeState, DealOverlayBlur)
            .background(DealOverlayScrim),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = "${remaining.format()} 남음",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

private val DealOverlayHeight = 48.dp

private val DealOverlayBlur = HaloBlurStyle(
    backgroundColor = Color.Black,
    tint = Color.Black.copy(alpha = 0.1f),
    blurRadius = 12.dp,
    noiseFactor = 0f,
    progressive = HazeProgressive.verticalGradient(startIntensity = 0f, endIntensity = 1f)
)

private val DealOverlayScrim = Brush.verticalGradient(
    0f to Color.Transparent,
    0.35f to Color.Black.copy(alpha = 0.08f),
    1f to Color.Black.copy(alpha = 0.42f)
)

@HaloLightDarkPreview
@Composable
private fun DealBandPreview() {
    val endAt = remember { System.currentTimeMillis() + 3 * 60 * 60 * 1000L }
    HaloSurfacePreview {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DealBand(dealEndAtMillis = endAt)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DealCountdownImageOverlay(dealEndAtMillis = endAt, hazeState = remember { HazeState() })
            }
        }
    }
}
