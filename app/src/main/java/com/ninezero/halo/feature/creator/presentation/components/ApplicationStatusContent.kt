package com.ninezero.halo.feature.creator.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.ActionButton
import com.ninezero.halo.core.ui.components.button.ActionButtonStyle
import com.ninezero.halo.core.util.toFormattedDate
import com.ninezero.halo.feature.creator.domain.model.CreatorApplication
import com.ninezero.halo.feature.creator.domain.model.CreatorApplicationStatus
import com.ninezero.halo.feature.creator.presentation.reapplyDaysLeft

@Composable
internal fun ApplicationStatusContent(
    application: CreatorApplication,
    isSubmitting: Boolean,
    onCancel: () -> Unit,
    onReapply: () -> Unit
) {
    val reapplyRemainingDays = application.reapplyDaysLeft()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (iconRes, iconTint, bgColor) = when (application.status) {
            CreatorApplicationStatus.PENDING -> Triple(
                R.drawable.ic_timer,
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            CreatorApplicationStatus.APPROVED -> Triple(
                R.drawable.ic_check,
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            CreatorApplicationStatus.REJECTED,
            CreatorApplicationStatus.REVOKED -> Triple(
                R.drawable.ic_close,
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = iconTint
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = when (application.status) {
                CreatorApplicationStatus.PENDING -> "심사 중입니다"
                CreatorApplicationStatus.APPROVED -> "크리에이터로 승인되었습니다"
                CreatorApplicationStatus.REJECTED -> "신청이 반려되었습니다"
                CreatorApplicationStatus.REVOKED -> "크리에이터 자격이 해제되었습니다"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (application.status) {
                CreatorApplicationStatus.PENDING -> "신청서를 검토하고 있어요.\n빠른 시일 내에 결과를 알려드릴게요."
                CreatorApplicationStatus.APPROVED -> "이제 크리에이터 기능을 사용할 수 있습니다."
                CreatorApplicationStatus.REJECTED -> {
                    if (reapplyRemainingDays != null) {
                        "${reapplyRemainingDays}일 후 다시 신청할 수 있어요.\n아래 사유를 먼저 확인해주세요."
                    } else {
                        "아래 사유를 확인하고 다시 신청해주세요."
                    }
                }
                CreatorApplicationStatus.REVOKED -> {
                    if (reapplyRemainingDays != null) {
                        "${reapplyRemainingDays}일 후 다시 신청할 수 있어요.\n아래 사유를 먼저 확인해주세요."
                    } else {
                        "아래 사유를 확인하고 다시 신청해주세요."
                    }
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // 반려/해제 사유
        if (application.status in listOf(CreatorApplicationStatus.REJECTED, CreatorApplicationStatus.REVOKED) && !application.rejectionReason.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = if (application.status == CreatorApplicationStatus.REVOKED) "해제 사유" else "반려 사유",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = application.rejectionReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 신청 정보
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "신청 정보",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            InfoRow("신청 사유", application.reason)
            if (!application.portfolioUrl.isNullOrBlank()) {
                InfoRow("포트폴리오", application.portfolioUrl)
            }
            InfoRow("신청일", application.createdAt.toFormattedDate("yyyy-MM-dd"))
            if (application.reviewedAt != null) {
                InfoRow("심사일", application.reviewedAt.toFormattedDate("yyyy-MM-dd"))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (application.status) {
            CreatorApplicationStatus.PENDING -> {
                ActionButton(
                    text = "신청 취소",
                    onClick = onCancel,
                    style = ActionButtonStyle.Text,
                    contentColor = MaterialTheme.colorScheme.error,
                    isLoading = isSubmitting
                )
            }
            CreatorApplicationStatus.REJECTED,
            CreatorApplicationStatus.REVOKED -> {
                ActionButton(
                    text = if (reapplyRemainingDays != null) "${reapplyRemainingDays}일 후 다시 신청 가능" else "다시 신청하기",
                    onClick = onReapply,
                    enabled = reapplyRemainingDays == null,
                    isLoading = isSubmitting
                )
            }
            CreatorApplicationStatus.APPROVED -> { /* 추가 액션 없음 */ }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            textAlign = TextAlign.End
        )
    }
}
