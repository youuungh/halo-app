package com.ninezero.halo.feature.store.presentation.payment_methods.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.presentation.components.cardIssuerName

private val CardInfoShape = RoundedCornerShape(14.dp)

/** 등록 카드 칸 */
@Composable
internal fun PaymentCardInfo(
    card: BillingCard,
    hasAutoRenew: Boolean,
    modifier: Modifier = Modifier
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardBorder(CardInfoShape)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!card.registered) {
            Text(
                text = "신용 · 체크카드",
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
            Text(
                text = "등록한 카드가 없습니다",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = muted
            )
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = cardIssuerName(card.cardCompany) ?: "저장카드",
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
            // 결제 화면 카드 시트와 같은 신용·체크 태그
            card.cardType?.takeIf { it.isNotBlank() }?.let { type ->
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelSmall,
                    color = muted,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (hasAutoRenew) {
                val primary = MaterialTheme.colorScheme.primary
                Text(
                    text = "자동 갱신 중",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = primary,
                    modifier = Modifier
                        .background(primary.copy(alpha = 0.1f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        val tail = card.cardNumberMasked?.trim()?.takeLast(4).orEmpty()
        Text(
            text = "•••• $tail",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
