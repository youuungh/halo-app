package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.button.SheetActionButton
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.presentation.components.cardLabel
import com.ninezero.halo.feature.store.presentation.components.cardLogoRes

@Composable
fun CardPaymentBottomSheet(
    savedCard: BillingCard?,
    isLoading: Boolean,
    draftSelection: String?,
    error: String?,
    onSelectOption: (String) -> Unit,
    onAddCard: () -> Unit,
    onConfirm: () -> Unit,
    onErrorDismiss: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "신용 · 체크카드",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 저장카드 선택행 또는 카드 추가
                when {
                    isLoading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(size = LoadingIndicatorSize.Medium)
                    }

                    savedCard != null -> SheetOptionRow(
                        title = cardLabel(savedCard),
                        subtitle = null,
                        isSelected = draftSelection == "TOSS_BILLING",
                        onClick = { onSelectOption("TOSS_BILLING") },
                        leading = { CardLogo(savedCard) }
                    )

                    else -> SheetActionRow(
                        title = "카드 추가하기",
                        leadingIcon = R.drawable.ic_plus,
                        onClick = onAddCard
                    )
                }

                // 하단: 카드 일반결제
                SheetOptionRow(
                    title = "카드 일반결제",
                    subtitle = "결제 시마다 카드 정보 입력",
                    isSelected = draftSelection == "TOSS_CARD",
                    onClick = { onSelectOption("TOSS_CARD") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SheetActionButton(
                    text = "선택 완료",
                    onClick = onConfirm,
                    enabled = draftSelection != null
                )
            }

            // 시트 window에 가려지지 않게 시트 안에서 표시
            CenterToast(
                visible = error != null,
                message = error.orEmpty(),
                modifier = Modifier.matchParentSize(),  // 시트 높이를 바꾸지 않음
                onDismiss = onErrorDismiss
            )
        }
    }
}

@Composable
private fun SheetOptionRow(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    leading: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CardLogo(card: BillingCard?, iconSize: Dp = 28.dp) {
    val logoRes = cardLogoRes(card?.cardCompany)
    if (logoRes != null) {
        Image(
            painter = painterResource(logoRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.height(iconSize)
        )
    } else {
        Icon(
            painter = painterResource(R.drawable.ic_credit_card),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SheetActionRow(
    title: String,
    leadingIcon: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(leadingIcon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
