package com.ninezero.halo.feature.store.presentation.create_review.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun RatingSelector(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 32.dp,
    filledColor: Color = Color(0xFFFFC107),
    emptyColor: Color = MaterialTheme.colorScheme.outlineVariant,
    maxStars: Int = 5
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..maxStars) {
            val isFilled = i <= rating
            Icon(
                painter = painterResource(
                    if (isFilled) R.drawable.ic_star_filled else R.drawable.ic_star_empty
                ),
                contentDescription = "$i 점",
                modifier = Modifier
                    .size(starSize)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRatingChange(i) },
                tint = if (isFilled) filledColor else emptyColor
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun RatingSelectorEmptyPreview() {
    HaloTheme {
        RatingSelector(rating = 0, onRatingChange = {})
    }
}

@HaloLightDarkPreview
@Composable
private fun RatingSelectorFilledPreview() {
    HaloTheme {
        RatingSelector(rating = 4, onRatingChange = {})
    }
}
