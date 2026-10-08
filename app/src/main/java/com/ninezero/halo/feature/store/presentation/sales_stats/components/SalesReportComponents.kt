package com.ninezero.halo.feature.store.presentation.sales_stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.store.domain.model.SalesStatusBreakdown
import com.ninezero.halo.ui.theme.Error
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Primary
import com.ninezero.halo.ui.theme.Success
import com.ninezero.halo.ui.theme.Warning

@Composable
fun StatSection(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}

data class SalesKpiData(
    val label: String,
    val value: String,
    val sub: String? = null,
    val tint: Color? = null
)

@Composable
fun SalesKpiGrid(
    tiles: List<SalesKpiData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tiles.chunked(2).forEach { rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowTiles.forEach { tile ->
                    SalesKpiTile(data = tile, modifier = Modifier.weight(1f))
                }
                if (rowTiles.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private val KPI_TILE_HEIGHT = 92.dp

@Composable
private fun SalesKpiTile(
    data: SalesKpiData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(KPI_TILE_HEIGHT)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp)
    ) {
        Text(
            text = data.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = data.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = data.tint ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (data.sub != null) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = data.sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SalesStatusBar(
    breakdown: SalesStatusBreakdown,
    modifier: Modifier = Modifier
) {
    val segments = listOf(
        StatusSegment("대기", breakdown.pending, Warning),
        StatusSegment("확인", breakdown.confirmed, Primary.copy(alpha = 0.45f)),
        StatusSegment("배송중", breakdown.shipping, Primary),
        StatusSegment("완료", breakdown.delivered, Success),
        StatusSegment("취소", breakdown.cancelled, Error.copy(alpha = 0.7f))
    )
    val total = segments.sumOf { it.count }
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50)),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            if (total == 0) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )
            } else {
                segments.filter { it.count > 0 }.forEach { seg ->
                    Box(
                        modifier = Modifier
                            .weight(seg.count.toFloat())
                            .fillMaxHeight()
                            .background(seg.color)
                    )
                }
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEach { seg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(seg.color)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = buildAnnotatedString {
                            append(seg.label)
                            append(" ")
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    color = onSurface
                                )
                            ) {
                                append("${seg.count}")
                            }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class StatusSegment(val label: String, val count: Int, val color: Color)

@HaloLightDarkPreview
@Composable
private fun SalesKpiGridPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatSection(title = "이번 달 요약", trailing = "2026.08") {
                    SalesKpiGrid(
                        tiles = listOf(
                            SalesKpiData("총 매출", "4,150,000원", sub = "45건"),
                            SalesKpiData("환불", "320,000원", sub = "2건", tint = Error),
                            SalesKpiData("순매출", "3,830,000원", tint = Success),
                            SalesKpiData("평균 주문가", "92,222원")
                        )
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesStatusBarPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatSection(title = "주문 상태") {
                    SalesStatusBar(
                        breakdown = SalesStatusBreakdown(
                            pending = 3,
                            confirmed = 5,
                            shipping = 8,
                            delivered = 27,
                            cancelled = 2
                        )
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun SalesStatusBarEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatSection(title = "주문 상태") {
                    SalesStatusBar(breakdown = SalesStatusBreakdown(0, 0, 0, 0, 0))
                }
            }
        }
    }
}
