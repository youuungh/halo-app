package com.ninezero.halo.feature.creator.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.button.ActionButton
import com.ninezero.halo.feature.creator.presentation.CREATOR_APPLICATION_REASON_MAX_LENGTH
import com.ninezero.halo.feature.creator.presentation.isValidApplicationReason
import com.ninezero.halo.feature.creator.presentation.isValidPortfolioUrl

@Composable
internal fun ApplicationFormContent(
    reason: String,
    portfolioUrl: String,
    isSubmitting: Boolean,
    onReasonChange: (String) -> Unit,
    onPortfolioUrlChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val canSubmit = reason.isValidApplicationReason() &&
            portfolioUrl.isValidPortfolioUrl()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "크리에이터가 되어\n나만의 스토어를 운영해보세요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "심사 후 승인되면 크리에이터 활동을 시작할 수 있습니다",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        CreatorFormTextField(
            value = reason,
            onValueChange = onReasonChange,
            label = "신청 사유",
            placeholder = "크리에이터로 활동하고 싶은 이유를 알려주세요",
            singleLine = false,
            maxLines = 5,
            minHeight = 140.dp,
            maxLength = CREATOR_APPLICATION_REASON_MAX_LENGTH
        )

        Spacer(modifier = Modifier.height(16.dp))

        CreatorFormTextField(
            value = portfolioUrl,
            onValueChange = onPortfolioUrlChange,
            label = "포트폴리오 URL (선택)",
            placeholder = "https://",
            singleLine = true,
            keyboardType = KeyboardType.Uri
        )

        Spacer(modifier = Modifier.height(32.dp))

        ActionButton(
            text = "신청하기",
            onClick = onSubmit,
            isLoading = isSubmitting,
            enabled = canSubmit
        )
    }
}
