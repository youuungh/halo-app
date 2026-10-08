package com.ninezero.halo.core.ui.components.selector

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import kotlinx.coroutines.flow.collectLatest

/** 수량 선택기 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuantitySelector(
    quantity: Int,
    maxStock: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    bottomReservedSpace: Dp = 0.dp
) {
    var isFocused by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = "$quantity", selection = TextRange("$quantity".length)))
    }
    // 미포커스일 때만 외부 quantity 반영
    LaunchedEffect(quantity) {
        if (!isFocused) {
            textFieldValue = TextFieldValue("$quantity", TextRange("$quantity".length))
        }
    }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var fieldSize by remember { mutableStateOf(IntSize.Zero) }

    // 포커스 시 필드가 바텀바 위로 보이게 재요청
    // bottomReservedSpace 0이면 동작 안 함
    LaunchedEffect(isFocused, fieldSize, bottomReservedSpace) {
        if (!isFocused || fieldSize == IntSize.Zero || bottomReservedSpace <= 0.dp) return@LaunchedEffect
        val reservedPx = with(density) { (bottomReservedSpace + 8.dp).toPx() }
        snapshotFlow { imeInsets.getBottom(density) }
            .collectLatest { imeBottomPx ->
                if (imeBottomPx <= 0) return@collectLatest
                bringIntoViewRequester.bringIntoView(
                    Rect(
                        left = 0f,
                        top = 0f,
                        right = fieldSize.width.toFloat(),
                        bottom = fieldSize.height.toFloat() + reservedPx
                    )
                )
            }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (showLabel) Arrangement.SpaceBetween else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLabel) {
            Text(
                text = "수량",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 감소 버튼
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(
                        enabled = quantity > 1,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) {
                        onQuantityChange(quantity - 1)
                        focusManager.clearFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_minus),
                    contentDescription = "감소",
                    modifier = Modifier.size(18.dp),
                    tint = if (quantity > 1) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                )
            }

            // 수량 입력
            Box(
                modifier = Modifier
                    .then(
                        if (isFocused) Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
                    .width(48.dp)
                    .height(36.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onSizeChanged { fieldSize = it }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusRequester.requestFocus() },
                contentAlignment = Alignment.Center
            ) {
                // if로 가리면 requestFocus 무시돼 항상 컴포즈
                // 평소 애니메이션, 포커스 시 입력 필드
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        // 숫자만, 선행 0 제거·6자리
                        val filtered = newValue.text.filter { it.isDigit() }.trimStart('0').take(6)
                        textFieldValue = if (filtered.isEmpty()) {
                            TextFieldValue(text = "", selection = TextRange.Zero)
                        } else {
                            TextFieldValue(text = filtered, selection = TextRange(filtered.length))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                textFieldValue = TextFieldValue(
                                    text = "$quantity",
                                    selection = TextRange("$quantity".length)
                                )
                            } else {
                                // 포커스를 잃을 때 확정
                                val num = textFieldValue.text.toIntOrNull()
                                when {
                                    // 공란/무효 → 1로 강제하지 않고 직전 수량 그대로 복귀
                                    num == null || num < 1 ->
                                        textFieldValue = TextFieldValue("$quantity", TextRange("$quantity".length))
                                    // 재고 초과는 최대치 클램프, 토스트 1회
                                    num > maxStock -> {
                                        textFieldValue = TextFieldValue("$maxStock", TextRange("$maxStock".length))
                                        if (maxStock != quantity) onQuantityChange(maxStock)
                                        Toast.makeText(context, "최대 ${maxStock}개까지 구매 가능합니다", Toast.LENGTH_SHORT).show()
                                    }
                                    // 정상 → 값이 바뀌었을 때만 커밋
                                    else -> {
                                        textFieldValue = TextFieldValue("$num", TextRange("$num".length))
                                        if (num != quantity) onQuantityChange(num)
                                    }
                                }
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.alpha(if (isFocused) 1f else 0f)) {
                                innerTextField()
                            }
                            if (!isFocused) {
                                val shownText = textFieldValue.text.ifEmpty { "$quantity" }
                                AnimatedContent(
                                    targetState = shownText,
                                    transitionSpec = {
                                        val to = targetState.toIntOrNull() ?: 0
                                        val from = initialState.toIntOrNull() ?: 0
                                        if (to > from) {
                                            slideInVertically { -it } togetherWith slideOutVertically { it }
                                        } else {
                                            slideInVertically { it } togetherWith slideOutVertically { -it }
                                        }
                                    },
                                    label = "quantity",
                                    contentAlignment = Alignment.Center
                                ) { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // 증가 버튼
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(
                        enabled = quantity < maxStock,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) {
                        onQuantityChange(quantity + 1)
                        focusManager.clearFocus()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = "증가",
                    modifier = Modifier.size(18.dp),
                    tint = if (quantity < maxStock) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                )
            }
        }
    }
}
