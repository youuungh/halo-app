package com.ninezero.halo.feature.store.presentation.checkout.components

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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.cardBorder
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.toggle.SelectionRadio
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.store.domain.model.BillingCard
import com.ninezero.halo.feature.store.presentation.components.cardIssuerName

private const val SAVED_CARD = "TOSS_BILLING"
private const val GENERAL_CARD = "TOSS_CARD"
private val OptionShape = RoundedCornerShape(14.dp)

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
    val snackbarHostState = remember { SnackbarHostState() }

    // 기본 선택: 저장 카드, 없으면 일반결제
    LaunchedEffect(isLoading, savedCard, draftSelection) {
        if (isLoading) return@LaunchedEffect
        when {
            savedCard == null && draftSelection != GENERAL_CARD -> onSelectOption(GENERAL_CARD)
            savedCard != null && draftSelection == null -> onSelectOption(SAVED_CARD)
        }
    }

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box {
            CardPaymentContent(
                savedCard = savedCard,
                isLoading = isLoading,
                draftSelection = draftSelection,
                onSelectOption = onSelectOption,
                onAddCard = onAddCard,
                onConfirm = onConfirm
            )

            // 시트 window에 가려지지 않게 시트 안에서 표시
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
                bottomBarPadding = 56.dp  // 결제 버튼 48 + 하단 여백 8
            )
        }
    }

    SnackbarHandler(
        message = error,
        hostState = snackbarHostState,
        onDismiss = onErrorDismiss
    )
}

@Composable
private fun CardPaymentContent(
    savedCard: BillingCard?,
    isLoading: Boolean,
    draftSelection: String?,
    onSelectOption: (String) -> Unit,
    onAddCard: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "신용 · 체크카드",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .cardBorder(OptionShape),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(size = LoadingIndicatorSize.Medium)
            }

            savedCard != null -> CardOptionRow(
                isSelected = draftSelection == SAVED_CARD,
                onClick = { onSelectOption(SAVED_CARD) },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cardIssuerName(savedCard.cardCompany) ?: "저장카드",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        savedCard.cardType?.takeIf { it.isNotBlank() }?.let { type ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = type,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraSmall)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                },
                subtitle = savedCard.cardNumberMasked?.trim()?.takeLast(4)?.let { "····$it" }
            )

            else -> CardOptionRow(
                isSelected = null,
                onClick = onAddCard,
                isDashed = true,
                iconRes = R.drawable.ic_plus,
                title = {
                    Text(
                        text = "카드 등록",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        CardOptionRow(
            isSelected = draftSelection == GENERAL_CARD,
            onClick = { onSelectOption(GENERAL_CARD) },
            title = {
                Text(
                    text = "카드 일반결제",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            subtitle = "결제할 때마다 카드 정보 입력"
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = if (draftSelection == SAVED_CARD) "이 카드로 결제" else "일반결제로 진행",
            onClick = onConfirm,
            enabled = draftSelection != null
        )
    }
}

/** 선택지 한 줄, isSelected가 null이면 이동만 */
@Composable
private fun CardOptionRow(
    isSelected: Boolean?,
    onClick: () -> Unit,
    title: @Composable () -> Unit,
    subtitle: String? = null,
    isDashed: Boolean = false,
    iconRes: Int = R.drawable.ic_credit_card
) {
    val primary = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(
                when {
                    isDashed -> Modifier.dashedBorder(lineColor.copy(alpha = 0.7f), 14.dp)
                    isSelected == true -> Modifier
                        .border(1.dp, primary, OptionShape)
                        .background(primary.copy(alpha = 0.04f), OptionShape)
                    else -> Modifier.border(0.5.dp, lineColor.copy(alpha = 0.5f), OptionShape)
                }
            )
            .clip(OptionShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected != null) {
            SelectionRadio(selected = isSelected)
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            // 라디오 자리를 비워 아래 선택지와 아이콘 줄을 맞춤
            Spacer(modifier = Modifier.width(32.dp))
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            title()
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 카드 등록 자리 점선 테두리 */
private fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = drawBehind {
    val strokeWidth = 1.dp.toPx()
    val inset = strokeWidth / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
        )
    )
}

@HaloLightDarkPreview
@Composable
private fun CardPaymentContentPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                CardPaymentContent(
                    savedCard = BillingCard(registered = true, cardCompany = "11", cardNumberMasked = "1234****1234", cardType = "신용"),
                    isLoading = false,
                    draftSelection = SAVED_CARD,
                    onSelectOption = {},
                    onAddCard = {},
                    onConfirm = {}
                )
                Spacer(modifier = Modifier.height(24.dp))
                CardPaymentContent(
                    savedCard = null,
                    isLoading = false,
                    draftSelection = GENERAL_CARD,
                    onSelectOption = {},
                    onAddCard = {},
                    onConfirm = {}
                )
            }
        }
    }
}
