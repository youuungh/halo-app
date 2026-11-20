package com.ninezero.halo.feature.store.presentation.checkout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.bringIntoViewOnFocus
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.point.domain.model.PointBalance
import java.text.NumberFormat

@Composable
fun PointSection(
    pointBalance: PointBalance?,
    usedPoints: Int,
    maxUsablePoints: Int,
    isRateLimited: Boolean,  // 상한이 상품 금액 50%에서 나왔는지, 아니면 결제 금액
    validationMessage: String?,
    numberFormat: NumberFormat,
    onUpdateUsedPoints: (String) -> Unit,
    onUseAllPoints: () -> Unit,
    bottomBarHeight: Dp = 0.dp  // 내용 위에 떠 있는 결제 바
) {
    val focusManager = LocalFocusManager.current
    var localText by remember { mutableStateOf(if (usedPoints == 0) "" else numberFormat.format(usedPoints)) }
    var isFocused by remember { mutableStateOf(false) }
    val hasBalance = (pointBalance?.totalPoints ?: 0) > 0
    val fieldShape = MaterialTheme.shapes.medium

    // 입력 중엔 그대로, 포커스가 빠지면 적용값으로
    LaunchedEffect(usedPoints, isFocused) {
        if (!isFocused) {
            localText = if (usedPoints == 0) "" else numberFormat.format(usedPoints)
        }
    }

    // 입력칸 · 안내 문구까지 결제 바 위로
    CheckoutCard(modifier = Modifier.bringIntoViewOnFocus(isFocused, bottomBarHeight)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, fieldShape)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "사용할 포인트",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = localText,
                        // 키보드를 닫지 않고 결제해도 반영되도록 입력마다 적용
                        onValueChange = {
                            localText = it.filter { c -> c.isDigit() }
                            onUpdateUsedPoints(localText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState -> isFocused = focusState.isFocused },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterEnd) {
                                if (localText.isEmpty() && !isFocused) {
                                    Text(
                                        text = "0",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "P",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(fieldShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(
                            enabled = hasBalance,
                            onClick = {
                                onUseAllPoints()
                                // 사용제한에 맞춘 값, 보유 전액 아님
                                val clampedPoints = minOf(pointBalance?.totalPoints ?: 0, maxUsablePoints)
                                localText = if (clampedPoints > 0) numberFormat.format(clampedPoints) else ""
                            }
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "전액 사용",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasBalance) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 보유량과 상한 중 실제로 막히는 쪽 기준
            val balance = pointBalance?.totalPoints
            val hint = when {
                balance == 0 -> "사용할 수 있는 포인트가 없습니다"
                balance != null && balance < maxUsablePoints ->
                    "최대 ${numberFormat.format(balance)}P까지 사용할 수 있습니다"
                isRateLimited -> "상품 금액의 50%인 최대 ${numberFormat.format(maxUsablePoints)}P까지 사용할 수 있습니다"
                else -> "최대 ${numberFormat.format(maxUsablePoints)}P까지 사용할 수 있습니다"
            }
            Text(
                text = validationMessage ?: hint,
                style = MaterialTheme.typography.labelSmall,
                color = if (validationMessage != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun PointSectionPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                PointSection(
                    pointBalance = PointBalance(totalPoints = 12_500),
                    usedPoints = 5_000,
                    maxUsablePoints = 12_500,
                    isRateLimited = true,
                    validationMessage = null,
                    numberFormat = rememberKoreanNumberFormat(),
                    onUpdateUsedPoints = {},
                    onUseAllPoints = {}
                )
            }
        }
    }
}
