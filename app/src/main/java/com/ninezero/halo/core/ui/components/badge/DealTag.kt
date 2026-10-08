package com.ninezero.halo.core.ui.components.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.util.format
import com.ninezero.halo.core.ui.components.util.rememberCountdown
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** 타임딜 태그 */
@Composable
fun DealTag(modifier: Modifier = Modifier) {
    Text(
        text = "타임딜",
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

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
                shape = RoundedCornerShape(8.dp)
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

/** 이미지 위 타임딜 카운트다운 오버레이 */
@Composable
fun DealCountdownImageOverlay(
    dealEndAtMillis: Long,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val remaining by rememberCountdown(dealEndAtMillis)
    if (remaining.isFinished) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState) {
                style = HazeStyle(
                    backgroundColor = Color.Black,
                    tint = HazeTint(Color.Black.copy(alpha = 0.35f)),
                    blurRadius = 10.dp,
                    noiseFactor = 0f
                )
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${remaining.format()} 남음",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1
        )
    }
}
