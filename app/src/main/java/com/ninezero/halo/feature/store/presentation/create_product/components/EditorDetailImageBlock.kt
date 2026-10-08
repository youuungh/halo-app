package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R

// 긴 이미지는 접힘 시 비율 상한, 펼쳐보기
private const val COLLAPSED_MAX_HEIGHT_RATIO = 1.3f

@Composable
fun EditorDetailImageBlock(
    model: Any?,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
    initialAspectRatio: Float? = null,  // 화면 비율 캐시, 재생성 시 높이 점프 방지
    onAspectRatioResolved: (Float) -> Unit = {}  // 실제 비율을 화면 캐시로
) {
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    var aspectRatio by remember(model) { mutableStateOf(initialAspectRatio) }  // width / height
    var expanded by remember(model) { mutableStateOf(false) }

    val heightPerWidth = aspectRatio?.let { if (it > 0f) 1f / it else null }
    val isTall = heightPerWidth != null && heightPerWidth > COLLAPSED_MAX_HEIGHT_RATIO
    val showCollapsed = isTall && !expanded

    // 로드 전·접힘은 상한 비율, 펼침은 실제 비율
    val boxRatio = when {
        aspectRatio == null -> 1f / COLLAPSED_MAX_HEIGHT_RATIO
        showCollapsed -> 1f / COLLAPSED_MAX_HEIGHT_RATIO
        else -> aspectRatio!!
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .aspectRatio(boxRatio)
            .clip(RoundedCornerShape(12.dp))
            // 이미지 탭은 아래 글칸 생성·포커스
            .pointerInput(Unit) { detectTapGestures { onTap() } }
    ) {
        AsyncImage(
            model = model,
            contentDescription = "상세 이미지",
            modifier = Modifier.fillMaxSize(),
            contentScale = if (showCollapsed) ContentScale.Crop else ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
            onSuccess = { state ->
                val image = state.result.image
                if (image.width > 0 && image.height > 0) {
                    val ratio = image.width.toFloat() / image.height.toFloat()
                    aspectRatio = ratio
                    onAspectRatioResolved(ratio)
                }
            },
            placeholder = ColorPainter(placeholderColor),
            error = ColorPainter(placeholderColor)
        )

        // 세로로 긴 이미지에만 펼쳐보기 / 접기 오버레이
        if (isTall) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                        )
                    )
                    .clickable { expanded = !expanded },
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "접기" else "펼쳐보기",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(
                            if (expanded) R.drawable.ic_chevron_up_small
                            else R.drawable.ic_chevron_down_small
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }

        // 삭제
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "이미지 삭제",
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
        }
    }
}
