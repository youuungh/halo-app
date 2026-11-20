package com.ninezero.halo.core.ui.components.bottomsheet

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview
import dev.chrisbanes.haze.HazeState

private val HeaderButtonSize = 36.dp

/** 시트 맨 위 손잡이 · 제목 · 닫기 · 확인 */
@Composable
fun SheetHeader(
    title: String,
    onClose: () -> Unit,
    hazeState: HazeState,
    onHeightChange: (Dp) -> Unit,
    modifier: Modifier = Modifier,
    closeDescription: String = "닫기",
    onConfirm: (() -> Unit)? = null,
    confirmDescription: String = "확인",
    subtitle: String? = null,
    topInset: Dp = 0.dp,  // 맨 위로 펼친 시트는 상태바만큼 내림
    handleAlpha: Float = 1f
) {
    val surface = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .haloBlur(hazeState, HaloBlurs.fadeDown(surface))
            .pointerInput(Unit) { detectTapGestures { } }
            .onGloballyPositioned { onHeightChange(with(density) { it.size.height.toDp() }) }
    ) {
        Spacer(modifier = Modifier.height(topInset))
        Box(modifier = Modifier.alpha(handleAlpha)) {
            BottomSheetDragHandle()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                onClick = onClose,
                icon = painterResource(R.drawable.ic_close),
                contentDescription = closeDescription,
                hazeState = hazeState,
                backgroundColor = surface,
                size = HeaderButtonSize,
                iconSize = 20.dp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            if (onConfirm != null) {
                GlassIconButton(
                    onClick = onConfirm,
                    icon = painterResource(R.drawable.ic_check),
                    contentDescription = confirmDescription,
                    hazeState = hazeState,
                    backgroundColor = surface,
                    size = HeaderButtonSize,
                    iconSize = 20.dp
                )
            } else {
                Spacer(modifier = Modifier.size(HeaderButtonSize))
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SheetHeaderPreview() {
    HaloSurfacePreview {
        SheetHeader(
            title = "정렬",
            onClose = {},
            hazeState = remember { HazeState() },
            onHeightChange = {},
            onConfirm = {}
        )
    }
}
