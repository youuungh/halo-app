package com.ninezero.halo.feature.store.presentation.payment_methods.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.presentation.components.cardIssuerName
import com.ninezero.halo.feature.store.presentation.components.cardLogoRes
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.HaloTheme
import kotlinx.coroutines.launch

private const val CARD_ASPECT_RATIO = 1.586f
private const val MAX_TILT = 14f

private fun groupedCardNumber(masked: String?): String {
    val raw = masked?.filter { !it.isWhitespace() && it != '-' }.orEmpty()
    if (raw.isBlank()) return "•••• - •••• - •••• - ••••"
    return raw.chunked(4).joinToString(" - ")
}

@Composable
fun RegisteredPaymentCard(
    cardCompany: String?,
    cardNumberMasked: String?,
    cardType: String?,
    modifier: Modifier = Modifier
) {
    val logoRes = cardLogoRes(cardCompany)
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CARD_ASPECT_RATIO)
            .graphicsLayer {
                rotationX = rotX.value
                rotationY = rotY.value
                cameraDistance = 16f
            }
            .pointerInput(Unit) {
                fun reset() {
                    scope.launch { rotX.animateTo(0f, spring()) }
                    scope.launch { rotY.animateTo(0f, spring()) }
                }
                detectDragGestures(
                    onDragEnd = { reset() },
                    onDragCancel = { reset() }
                ) { change, _ ->
                    change.consume()
                    val px = (change.position.x / size.width).coerceIn(0f, 1f)
                    val py = (change.position.y / size.height).coerceIn(0f, 1f)
                    scope.launch { rotY.snapTo((px - 0.5f) * 2f * MAX_TILT) }
                    scope.launch { rotX.snapTo((0.5f - py) * 2f * MAX_TILT) }
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .background(HaloGradient)
            // 기울임 광택
            .drawWithContent {
                drawContent()
                val cx = size.width * (0.5f + (rotY.value / MAX_TILT) * 0.5f)
                val cy = size.height * (0.5f - (rotX.value / MAX_TILT) * 0.5f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = size.maxDimension * 0.55f
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (logoRes != null) {
                Image(
                    painter = painterResource(logoRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(28.dp)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_credit_card),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Color.White
                )
            }

            Column {
                Text(
                    text = groupedCardNumber(cardNumberMasked),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cardIssuerName(cardCompany) ?: "카드",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    cardType?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddPaymentCardPlaceholder(
    isLoading: Boolean,  // 발급 대기 중 로딩, 다시 누르기 막음
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 14f))
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            LoadingIndicator(size = LoadingIndicatorSize.Medium)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "카드 추가하기",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun RegisteredPaymentCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                RegisteredPaymentCard(
                    cardCompany = "신한",
                    cardNumberMasked = "1234-****-****-5678",
                    cardType = "체크"
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun RegisteredPaymentCardUnknownPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                RegisteredPaymentCard(
                    cardCompany = null,
                    cardNumberMasked = null,
                    cardType = null
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun AddPaymentCardPlaceholderPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                AddPaymentCardPlaceholder(isLoading = false, onClick = {})
            }
        }
    }
}
