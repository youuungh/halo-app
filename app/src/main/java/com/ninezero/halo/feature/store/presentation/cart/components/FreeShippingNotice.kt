package com.ninezero.halo.feature.store.presentation.cart.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloGradientEnd
import com.ninezero.halo.core.ui.theme.HaloGradientMid1
import com.ninezero.halo.core.ui.theme.HaloGradientMid2
import com.ninezero.halo.core.ui.theme.HaloGradientStart
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.core.util.Constants
import java.text.NumberFormat

private const val SHINE_DURATION = 1100
private val TextShineColors = listOf(
    Color.Transparent,
    HaloGradientStart,
    HaloGradientMid1,
    HaloGradientMid2,
    Color.Transparent
)
private val BandShineColors = listOf(
    Color.Transparent,
    HaloGradientStart.copy(alpha = 0.18f),
    HaloGradientMid2.copy(alpha = 0.22f),
    HaloGradientEnd.copy(alpha = 0.16f),
    Color.Transparent
)

/** 무료배송까지 남은 금액, 넘기면 빛이 지나감 */
@Composable
fun FreeShippingNotice(
    subtotal: Long,
    numberFormat: NumberFormat,
    modifier: Modifier = Modifier
) {
    val threshold = Constants.Commerce.FREE_SHIPPING_THRESHOLD.toLong()
    val remaining = (threshold - subtotal).coerceAtLeast(0L)
    val isReached = remaining == 0L
    val progress by animateFloatAsState(
        targetValue = (subtotal.toFloat() / threshold).coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "freeShippingProgress"
    )
    val primary = MaterialTheme.colorScheme.primary
    val barStartColor = lerp(primary, Color.White, if (LocalIsDarkTheme.current) 0.3f else 0.45f)

    // 1이면 쉬는 상태, 처음부터 달성이면 재생 안 함
    val shine = remember { Animatable(1f) }
    var wasReached by remember { mutableStateOf(isReached) }
    LaunchedEffect(isReached) {
        if (isReached && !wasReached) {
            shine.snapTo(0f)
            shine.animateTo(1f, tween(SHINE_DURATION, easing = FastOutSlowInEasing))
        }
        wasReached = isReached
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val t = shine.value
                if (t < 1f) {
                    val bandWidth = size.width * 0.6f
                    val center = -bandWidth / 2 + (size.width + bandWidth) * t
                    drawRect(
                        Brush.horizontalGradient(
                            colors = BandShineColors,
                            startX = center - bandWidth / 2,
                            endX = center + bandWidth / 2
                        )
                    )
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                if (isReached) {
                    append("무료배송 조건 달성")
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("${numberFormat.format(remaining)}원")
                    }
                    append(" 더 담으면 ")
                    withStyle(SpanStyle(color = primary, fontWeight = FontWeight.SemiBold)) {
                        append("무료배송")
                    }
                }
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isReached) FontWeight.SemiBold else null,
            color = if (isReached) primary else MaterialTheme.colorScheme.onSurface,
            // 글자 모양에만 그라데이션 빛을 입힘
            modifier = Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    val t = shine.value
                    if (t < 1f) {
                        val bandWidth = 64.dp.toPx()
                        val center = -bandWidth / 2 + (size.width + bandWidth) * t
                        drawRect(
                            Brush.horizontalGradient(
                                colors = TextShineColors,
                                startX = center - bandWidth / 2,
                                endX = center + bandWidth / 2
                            ),
                            blendMode = BlendMode.SrcAtop
                        )
                    }
                }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // 채운 길이 안에서 옅은 파랑 → 파랑
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(barStartColor, primary)))
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_shipping),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isReached) primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun FreeShippingNoticePreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                FreeShippingNotice(subtotal = 14_000L, numberFormat = rememberKoreanNumberFormat())
                FreeShippingNotice(subtotal = 52_000L, numberFormat = rememberKoreanNumberFormat())
            }
        }
    }
}
