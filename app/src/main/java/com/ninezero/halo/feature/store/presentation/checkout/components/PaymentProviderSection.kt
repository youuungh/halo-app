package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ninezero.halo.BuildConfig
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.toggle.SelectionRadio
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.LocalIsDarkTheme
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.presentation.components.cardLabel

@Composable
fun PaymentProviderSection(
    selectedProvider: String,
    savedCard: BillingCard?,
    onShowCardSheet: () -> Unit,
    onSelectProvider: (String) -> Unit
) {
    val isCardSelected = selectedProvider == "TOSS_CARD" || selectedProvider == "TOSS_BILLING"
    val cardSubtitle = when (selectedProvider) {
        "TOSS_BILLING" -> cardLabel(savedCard)
        "TOSS_CARD" -> "카드 일반결제"
        else -> null
    }

    CheckoutCard {
        ProviderRow(
            isSelected = selectedProvider == "TOSS_TOSSPAY",
            onClick = { onSelectProvider("TOSS_TOSSPAY") }
        ) {
            Image(
                painter = painterResource(
                    if (LocalIsDarkTheme.current) R.drawable.tosspay_logo_primary_white
                    else R.drawable.tosspay_logo_primary
                ),
                contentDescription = "토스페이",
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(18.dp)
            )
        }
        ProviderDivider()
        // 카드는 시트에서 저장 카드·일반결제를 고름
        ProviderRow(
            isSelected = isCardSelected,
            subtitle = cardSubtitle,
            onClick = onShowCardSheet
        ) {
            ProviderTitle("신용 · 체크카드")
        }
        ProviderDivider()
        ProviderRow(
            isSelected = selectedProvider == "TOSS_MOBILE",
            onClick = { onSelectProvider("TOSS_MOBILE") }
        ) {
            ProviderTitle("휴대폰 결제")
        }
        ProviderDivider()
        ProviderRow(
            isSelected = selectedProvider == "TOSS_TRANSFER",
            onClick = { onSelectProvider("TOSS_TRANSFER") }
        ) {
            ProviderTitle("계좌이체")
        }
        if (BuildConfig.DEBUG) {
            ProviderDivider()
            ProviderRow(
                isSelected = selectedProvider == "MOCK",
                onClick = { onSelectProvider("MOCK") }
            ) {
                ProviderTitle("테스트 결제")
            }
        }
    }
}

@Composable
private fun ProviderRow(
    isSelected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null,
    title: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectionRadio(selected = isSelected)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            title()
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProviderTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ProviderDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@HaloLightDarkPreview
@Composable
private fun PaymentProviderSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                PaymentProviderSection(
                    selectedProvider = "TOSS_BILLING",
                    savedCard = BillingCard(
                        registered = true,
                        cardCompany = "11",
                        cardNumberMasked = "1234-****-****-5678",
                        cardType = "신용"
                    ),
                    onShowCardSheet = {},
                    onSelectProvider = {}
                )
            }
        }
    }
}
