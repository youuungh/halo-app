package com.ninezero.halo.feature.store.presentation.create_product.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme

/** 구역 머리: 제목 · 접으면 회색 요약 · 오른쪽 화살표 */
@Composable
fun CollapsibleSectionHeader(
    title: String,
    isCollapsed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        label = "chevron_rotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (isCollapsed) summary.orEmpty() else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_chevron_down),
            contentDescription = if (isCollapsed) "펼치기" else "접기",
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer { rotationZ = rotationAngle },
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 구역 사이 6dp 띠 */
@Composable
fun SectionBand() {
    HorizontalDivider(
        thickness = 6.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
}

@HaloLightDarkPreview
@Composable
private fun SectionComponentsPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                CollapsibleSectionHeader(title = "기본 정보", isCollapsed = false, onToggle = {})
                SectionBand()
                CollapsibleSectionHeader(title = "판매 정보", isCollapsed = true, onToggle = {}, summary = "35,000원 · 재고 20개 · 전체 공개")
                SectionBand()
                CollapsibleSectionHeader(title = "판매 설정", isCollapsed = false, onToggle = {})
            }
        }
    }
}
