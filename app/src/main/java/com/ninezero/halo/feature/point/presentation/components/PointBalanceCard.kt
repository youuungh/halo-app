package com.ninezero.halo.feature.point.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.core.util.toEpochMillis
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.point.domain.model.PointBalance
import java.time.LocalDateTime

@Composable
fun PointBalanceCard(
    balance: PointBalance,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = LocalIsDarkTheme.current
    val numberFormat = rememberKoreanNumberFormat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MembershipColors.cardBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "보유 포인트",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${numberFormat.format(balance.totalPoints)}P",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (balance.expiringPoints > 0 && balance.expiresAt != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = if (isDarkTheme) 0.2f else 0.1f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${balance.expiresAt.toFormattedDate("yyyy.MM.dd")}까지 ${numberFormat.format(balance.expiringPoints)}P 소멸 예정",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PointBalanceCardPreview() {
    HaloTheme {
        PointBalanceCard(
            balance = PointBalance(
                totalPoints = 12500,
                expiringPoints = 500,
                expiresAt = LocalDateTime.of(2024, 2, 28, 0, 0).toEpochMillis()
            )
        )
    }
}
