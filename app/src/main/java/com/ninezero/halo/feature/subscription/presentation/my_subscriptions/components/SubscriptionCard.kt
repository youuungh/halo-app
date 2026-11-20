package com.ninezero.halo.feature.subscription.presentation.my_subscriptions.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.components.toggle.HaloSwitch
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.Success
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.subscription.domain.model.Subscription
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatus

@Composable
internal fun SubscriptionCard(
    subscription: Subscription,
    onAutoRenewChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    isUpdatingAutoRenew: Boolean,
    modifier: Modifier = Modifier
) {
    val isActive = subscription.status == SubscriptionStatus.ACTIVE
    val contentAlpha = if (isActive) 1f else 0.5f
    val numberFormat = rememberKoreanNumberFormat()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        AsyncImage(
            model = subscription.creatorAvatarUrl,
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_profile_placeholder),
            error = painterResource(R.drawable.ic_profile_placeholder),
            fallback = painterResource(R.drawable.ic_profile_placeholder),
            alpha = contentAlpha
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 정보
        Column(modifier = Modifier.weight(1f)) {
            // 크리에이터명 + 상태, 이용 중은 표시 없음
            Row {
                Text(
                    text = subscription.creatorUsername,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .alignByBaseline()
                )
                VerifiedBadge(
                    role = subscription.creatorRole,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha)
                )
                if (!isActive) {
                    StatusBadge(
                        status = subscription.status,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .alignByBaseline()
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 플랜명 · 가격
            val price = numberFormat.format(subscription.planPrice)
            Text(
                text = "${subscription.planName} · ${price}원/월",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // 자동 결제 켜짐은 다음 결제일, 아니면 끝나는 날
            val dateText = subscription.expiresAt.toFormattedDate("yyyy.MM.dd")
            val infoText = when {
                subscription.status == SubscriptionStatus.EXPIRED -> "$dateText 만료"
                isActive && subscription.autoRenew -> "다음 결제 $dateText"
                else -> "${dateText}까지 이용"
            }

            Text(
                text = infoText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Switch, 활성만
        if (isActive) {
            HaloSwitch(
                checked = subscription.autoRenew,
                onCheckedChange = onAutoRenewChange,
                enabled = !isUpdatingAutoRenew
            )
        }
    }
}

@Composable
private fun StatusBadge(
    status: SubscriptionStatus,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (status) {
        SubscriptionStatus.ACTIVE -> "활성" to Success
        SubscriptionStatus.CANCELLED -> "취소됨" to MaterialTheme.colorScheme.error
        SubscriptionStatus.EXPIRED -> "만료됨" to MaterialTheme.colorScheme.outline
    }

    Text(
        text = text,
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SubscriptionCardPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                SubscriptionCard(
                    subscription = Subscription(
                        id = 1, creatorId = 2,
                        creatorUsername = "크리에이터A",
                        creatorRole = "CREATOR",
                        planName = "Premium", planPrice = 9900L,
                        status = SubscriptionStatus.ACTIVE,
                        expiresAt = System.currentTimeMillis() + 10 * 86_400_000L,
                        autoRenew = false, daysRemaining = 10
                    ),
                    onAutoRenewChange = {},
                    onClick = {},
                    isUpdatingAutoRenew = false
                )
                SubscriptionCard(
                    subscription = Subscription(
                        id = 2, creatorId = 3,
                        creatorUsername = "크리에이터B",
                        creatorRole = "CREATOR",
                        planName = "Basic", planPrice = 4900L,
                        status = SubscriptionStatus.CANCELLED,
                        expiresAt = System.currentTimeMillis() + 2 * 86_400_000L,
                        autoRenew = false, daysRemaining = 2
                    ),
                    onAutoRenewChange = {},
                    onClick = {},
                    isUpdatingAutoRenew = false
                )
                SubscriptionCard(
                    subscription = Subscription(
                        id = 3, creatorId = 4,
                        creatorUsername = "크리에이터C",
                        creatorRole = "CREATOR",
                        planName = "VIP", planPrice = 19900L,
                        status = SubscriptionStatus.EXPIRED,
                        expiresAt = System.currentTimeMillis() - 30 * 86_400_000L,
                        autoRenew = false, daysRemaining = 0
                    ),
                    onAutoRenewChange = {},
                    onClick = {},
                    isUpdatingAutoRenew = false
                )
            }
        }
    }
}
