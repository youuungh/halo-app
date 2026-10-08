package com.ninezero.halo.feature.store.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun StarRating(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
    filledColor: Color = Color(0xFFFFC107),
    emptyColor: Color = MaterialTheme.colorScheme.outlineVariant,
    maxStars: Int = 5
) {
    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            val icon = when {
                rating >= i -> painterResource(R.drawable.ic_star_filled)
                rating >= i - 0.5 -> painterResource(R.drawable.ic_star_half)
                else -> painterResource(R.drawable.ic_star_empty)
            }
            val tint = when {
                rating >= i - 0.5 -> filledColor
                else -> emptyColor
            }
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint = tint
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun StarRatingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StarRating(rating = 5.0)
                StarRating(rating = 4.5)   // 반 별
                StarRating(rating = 3.2)   // 내림 확인
                StarRating(rating = 0.0)
                StarRating(rating = 4.0, starSize = 24.dp)
            }
        }
    }
}
