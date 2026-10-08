package com.ninezero.halo.core.ui.components.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Pretendard
import kotlinx.coroutines.delay

/** 화면 중앙 토스트 */
@Composable
fun CenterToast(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    durationMillis: Long = 1500L,
    onDismiss: () -> Unit = {}
) {
    // message도 키에 넣어 교체 시 재시작
    LaunchedEffect(visible, message) {
        if (visible) {
            delay(durationMillis)
            onDismiss()
        }
    }

    // message 비우면 exit 중 박스 축소
    // 마지막으로 표시한 비어있지 않은 메시지를 유지함
    var shownMessage by remember { mutableStateOf(message) }
    if (message.isNotEmpty()) shownMessage = message

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = shownMessage,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CenterToastPreview() {
    HaloTheme {
        CenterToast(visible = true, message = "삭제됨")
    }
}
