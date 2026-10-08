package com.ninezero.halo.feature.point.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.point.domain.model.PointHistory
import com.ninezero.halo.feature.point.domain.model.PointType
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.LocalIsDarkTheme

@Composable
fun PointHistoryItem(
    history: PointHistory,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalIsDarkTheme.current
    val isPositive = history.type in listOf(PointType.EARN, PointType.REFUND)
    val amountColor = when {
        isPositive -> if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF1976D2)
        history.type == PointType.USE -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant  // EXPIRE
    }
    val formattedAmount = rememberKoreanNumberFormat().format(history.amount)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 설명 및 날짜
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = history.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = history.createdAt.toFormattedDate("yyyy.MM.dd"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 포인트 금액 및 타입
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "${if (isPositive) "+ " else "- "}${formattedAmount}P",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
            Text(
                text = history.type.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PointHistoryItemEarnPreview() {
    HaloTheme {
        PointHistoryItem(
            history = PointHistory(
                id = 1,
                type = PointType.EARN,
                amount = 500,
                description = "구매 적립 (주문 #12345)",
                createdAt = System.currentTimeMillis()
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PointHistoryItemUsePreview() {
    HaloTheme {
        PointHistoryItem(
            history = PointHistory(
                id = 2,
                type = PointType.USE,
                amount = 1000,
                description = "포인트 사용",
                createdAt = System.currentTimeMillis()
            )
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun PointHistoryItemExpirePreview() {
    HaloTheme {
        PointHistoryItem(
            history = PointHistory(
                id = 3,
                type = PointType.EXPIRE,
                amount = 200,
                description = "포인트 만료",
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
