package com.ninezero.halo.core.ui.components.list

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

/** 불러오기 실패 상태, 제목 · 이유 · 다시 시도 */
@Composable
fun ListErrorState(
    title: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    reason: String? = null
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (!reason.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .bounceClick()
                .height(36.dp)
                .clip(CircleShape)
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clickable(onClick = onRetry)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "다시 시도",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ListErrorStatePreview() {
    HaloSurfacePreview {
        ListErrorState(
            title = "목록을 불러오지 못했습니다",
            onRetry = {},
            reason = "네트워크 연결을 확인해 주세요",
            modifier = Modifier.padding(16.dp)
        )
    }
}
