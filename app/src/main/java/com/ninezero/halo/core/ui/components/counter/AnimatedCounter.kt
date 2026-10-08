package com.ninezero.halo.core.ui.components.counter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toFormattedParts
import com.ninezero.halo.ui.theme.HaloTheme

/** 자리수별 슬라이드 카운터 */
@Composable
fun AnimatedCounter(  // suffix 있으면 쉼표 포맷, 없으면 축약
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    color: Color = LocalContentColor.current,
    suffix: String? = null
) {
    val numberFormat = rememberKoreanNumberFormat()

    val (number, displaySuffix) = if (suffix != null) {
        remember(count) { numberFormat.format(count) } to suffix
    } else {
        val parts = remember(count) { count.toFormattedParts() }
        parts.number to parts.suffix
    }

    var previousNumber by remember { mutableStateOf(number) }
    // 숫자가 원보다 떠 보이는 baseline 보정
    val numberNudge = if (displaySuffix != null) 1.dp else 0.dp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        number.forEachIndexed { index, char ->
            if (char.isDigit()) {
                val oldChar = previousNumber.getOrNull(index) ?: char
                val isIncreasing = char > oldChar

                AnimatedContent(
                    targetState = char,
                    modifier = Modifier.offset(y = numberNudge),
                    transitionSpec = {
                        if (isIncreasing) {
                            slideInVertically { -it }
                                .togetherWith(slideOutVertically { it })
                        } else {
                            slideInVertically { it }
                                .togetherWith(slideOutVertically { -it })
                        }.using(SizeTransform(clip = true))
                    },
                    label = "digit_$index"
                ) { digit ->
                    Text(
                        text = digit.toString(),
                        style = style,
                        color = color
                    )
                }
            } else {
                Text(
                    text = char.toString(),
                    style = style,
                    color = color,
                    modifier = Modifier.offset(y = numberNudge)
                )
            }
        }

        displaySuffix?.let {
            Text(
                text = it,
                style = style,
                color = color
            )
        }
    }

    LaunchedEffect(number) { previousNumber = number }
}

@HaloLightDarkPreview
@Composable
private fun AnimatedCounterPreview() {
    HaloTheme {
        var count by remember { mutableIntStateOf(999) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedCounter(count = count)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { count-- }) { Text("-") }
                Button(onClick = { count++ }) { Text("+") }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun AnimatedCounterThousandPreview() {
    HaloTheme {
        var count by remember { mutableIntStateOf(1234) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedCounter(count = count)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { count -= 100 }) { Text("-100") }
                Button(onClick = { count += 100 }) { Text("+100") }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun AnimatedCounterTenThousandPreview() {
    HaloTheme {
        var count by remember { mutableIntStateOf(12345) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedCounter(count = count)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { count -= 1000 }) { Text("-1000") }
                Button(onClick = { count += 1000 }) { Text("+1000") }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun AnimatedCounterPricePreview() {
    HaloTheme {
        var price by remember { mutableIntStateOf(89000) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedCounter(
                count = price,
                style = MaterialTheme.typography.titleMedium,
                suffix = "원"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { price -= 89000 }) { Text("-89,000") }
                Button(onClick = { price += 89000 }) { Text("+89,000") }
            }
        }
    }
}
