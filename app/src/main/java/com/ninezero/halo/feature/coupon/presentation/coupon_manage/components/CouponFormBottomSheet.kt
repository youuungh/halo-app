package com.ninezero.halo.feature.coupon.presentation.coupon_manage.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.DatePickerBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.FormSection
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.presentation.coupon_manage.CouponFormState
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CouponFormBottomSheet(
    isEditing: Boolean,
    isIssued: Boolean,  // 받은 사람이 있으면 사용 조건 잠금
    formState: CouponFormState,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (CouponType) -> Unit,
    onDiscountValueChange: (String) -> Unit,
    onMinOrderAmountChange: (String) -> Unit,
    onMaxDiscountAmountChange: (String) -> Unit,
    onTotalQuantityChange: (String) -> Unit,
    onMaxUseCountChange: (String) -> Unit,
    onStartDateChange: (LocalDateTime) -> Unit,
    onEndDateChange: (LocalDateTime) -> Unit,
    onSave: () -> Unit,
    saveError: String? = null,
    onSaveErrorDismiss: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    if (isSaving) {
        LoadingDialog()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = null,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Box {
            CouponFormBottomSheetContent(
                isEditing = isEditing,
                isIssued = isIssued,
                formState = formState,
                onDismiss = onDismiss,
                onCodeChange = onCodeChange,
                onNameChange = onNameChange,
                onDescriptionChange = onDescriptionChange,
                onTypeChange = onTypeChange,
                onDiscountValueChange = onDiscountValueChange,
                onMinOrderAmountChange = onMinOrderAmountChange,
                onMaxDiscountAmountChange = onMaxDiscountAmountChange,
                onTotalQuantityChange = onTotalQuantityChange,
                onMaxUseCountChange = onMaxUseCountChange,
                onStartDateChange = onStartDateChange,
                onEndDateChange = onEndDateChange,
                onSave = onSave
            )

            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    SnackbarHandler(
        message = saveError,
        hostState = snackbarHostState,
        onDismiss = onSaveErrorDismiss
    )
}

@Composable
private fun CouponFormBottomSheetContent(
    isEditing: Boolean,
    isIssued: Boolean,
    formState: CouponFormState,
    onDismiss: () -> Unit,
    onCodeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (CouponType) -> Unit,
    onDiscountValueChange: (String) -> Unit,
    onMinOrderAmountChange: (String) -> Unit,
    onMaxDiscountAmountChange: (String) -> Unit,
    onTotalQuantityChange: (String) -> Unit,
    onMaxUseCountChange: (String) -> Unit,
    onStartDateChange: (LocalDateTime) -> Unit,
    onEndDateChange: (LocalDateTime) -> Unit,
    onSave: () -> Unit,
    initialShowErrors: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.surface
    var headerHeight by remember { mutableStateOf(0.dp) }
    var showErrors by remember { mutableStateOf(initialShowErrors) }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .blockSheetFling(),
            contentPadding = PaddingValues(
                top = headerHeight + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 32.dp + navBottom
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            overscrollEffect = null
        ) {
            if (!isEditing) {
                item {
                    FormSection(title = "쿠폰 코드") {
                        FormTextField(
                            value = formState.code,
                            onValueChange = { raw ->
                                val filtered = raw.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }
                                if (filtered.length <= 20) onCodeChange(filtered)
                            },
                            placeholder = "예: WELCOME10 (미입력 시 자동 생성)",
                            maxLength = 20,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            isError = showErrors && formState.code.isNotBlank()
                                    && (formState.code.length < 4 || !formState.code.matches("[A-Z0-9]+".toRegex())),
                            errorMessage = "영문 대문자, 숫자만 4~20자",
                            showCounter = true
                        )
                    }
                }
            }

            item {
                FormSection(title = "쿠폰 이름") {
                    FormTextField(
                        value = formState.name,
                        onValueChange = onNameChange,
                        placeholder = "예: 신규 가입 할인",
                        maxLength = 50,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        isError = showErrors && formState.name.isBlank(),
                        errorMessage = "쿠폰명을 입력해주세요",
                        showCounter = true
                    )
                }
            }

            item {
                FormSection(title = "할인 유형") {
                    CouponTypeSelector(
                        selectedType = formState.type,
                        onTypeSelect = onTypeChange,
                        enabled = !isEditing
                    )
                }
            }

            if (formState.type != CouponType.FREE_SHIPPING) {
                item {
                    val label = when (formState.type) {
                        CouponType.FIXED_AMOUNT -> "할인 금액"
                        CouponType.PERCENTAGE -> "할인 비율"
                    }
                    val prefix = when (formState.type) {
                        CouponType.FIXED_AMOUNT -> "₩"
                        CouponType.PERCENTAGE -> "%"
                    }
                    val placeholder = when (formState.type) {
                        CouponType.FIXED_AMOUNT -> "예: 5,000"
                        CouponType.PERCENTAGE -> "예: 10"
                    }
                    FormSection(title = label) {
                        FormTextField(
                            value = formState.discountValue,
                            onValueChange = { newValue ->
                                if (formState.type == CouponType.PERCENTAGE) {
                                    val filtered = newValue.filter { it.isDigit() }
                                    val number = filtered.toIntOrNull() ?: 0
                                    if (number <= 100) onDiscountValueChange(filtered)
                                } else {
                                    val filtered = newValue.filter { it.isDigit() }
                                    onDiscountValueChange(filtered)
                                }
                            },
                            placeholder = placeholder,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            prefix = prefix,
                            enabled = !isEditing,
                            isError = showErrors && formState.discountValue.isBlank(),
                            errorMessage = "할인 값을 입력해주세요",
                            numberFormat = formState.type == CouponType.FIXED_AMOUNT
                        )
                    }
                }
            }

            item {
                FormSection(title = "최소 주문 금액") {
                    FormTextField(
                        value = formState.minOrderAmount,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() }
                            onMinOrderAmountChange(filtered)
                        },
                        placeholder = "예: 10,000 (미입력 시 제한 없음)",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        prefix = "₩",
                        enabled = !isIssued,
                        numberFormat = true
                    )
                }
            }

            if (formState.type == CouponType.PERCENTAGE) {
                item {
                    FormSection(title = "최대 할인 금액") {
                        FormTextField(
                            value = formState.maxDiscountAmount,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                onMaxDiscountAmountChange(filtered)
                            },
                            placeholder = "예: 5,000 (미입력 시 제한 없음)",
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            prefix = "₩",
                            enabled = !isIssued,
                            numberFormat = true
                        )
                    }
                }
            }

            item {
                FormSection(title = "발급 수량") {
                    FormTextField(
                        value = formState.totalQuantity,
                        onValueChange = onTotalQuantityChange,
                        placeholder = "예: 100",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        isError = showErrors && (formState.totalQuantity.isBlank() || (formState.totalQuantity.toIntOrNull() ?: 0) <= 0),
                        errorMessage = "발급 수량을 입력해주세요"
                    )
                }
            }

            item {
                FormSection(title = "사용자당 사용 횟수") {
                    FormTextField(
                        value = formState.maxUseCount,
                        onValueChange = onMaxUseCountChange,
                        placeholder = "예: 1",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        enabled = !isIssued,
                        isError = showErrors && (formState.maxUseCount.isBlank() || (formState.maxUseCount.toIntOrNull() ?: 0) <= 0),
                        errorMessage = "사용 횟수를 입력해주세요"
                    )
                }
            }

            item {
                FormSection(title = "유효 기간") {
                    DateRangeField(
                        startDate = formState.startDate,
                        endDate = formState.endDate,
                        onStartDateChange = onStartDateChange,
                        onEndDateChange = onEndDateChange
                    )
                }
            }

            item {
                FormSection(title = "설명") {
                    FormTextField(
                        value = formState.description,
                        onValueChange = onDescriptionChange,
                        placeholder = "쿠폰에 대한 간단한 설명",
                        singleLine = false,
                        minLines = 2,
                        maxLength = 500,
                        showCounter = true
                    )
                }
            }

        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = HaloBlurTint(backgroundColor),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            BottomSheetDragHandle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "취소",
                    hazeState = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    size = 36.dp,
                    iconSize = 20.dp
                )

                Text(
                    text = if (isEditing) "쿠폰 수정" else "쿠폰 생성",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                GlassIconButton(
                    onClick = {
                        if (formState.isValid) onSave()
                        else showErrors = true
                    },
                    icon = painterResource(R.drawable.ic_check),
                    contentDescription = "저장",
                    hazeState = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    size = 36.dp,
                    iconSize = 20.dp
                )
            }
        }
    }
}

@Composable
private fun CouponTypeSelector(
    selectedType: CouponType,
    onTypeSelect: (CouponType) -> Unit,
    enabled: Boolean = true
) {
    val types = listOf(
        CouponType.FIXED_AMOUNT to "정액 할인",
        CouponType.PERCENTAGE to "비율 할인",
        CouponType.FREE_SHIPPING to "무료 배송"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { (type, label) ->
            val isSelected = selectedType == type
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled) { onTypeSelect(type) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateRangeField(
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    onStartDateChange: (LocalDateTime) -> Unit,
    onEndDateChange: (LocalDateTime) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 시작일
        Box(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(10.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
                .clickable { showStartDatePicker = true }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = startDate.format(dateFormatter),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "~",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 종료일
        Box(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(10.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
                .clickable { showEndDatePicker = true }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = endDate.format(dateFormatter),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (showStartDatePicker) {
        DatePickerBottomSheet(
            initialDate = startDate,
            minDate = LocalDateTime.now(),
            title = "시작일 선택",
            onConfirm = { date ->
                onStartDateChange(date)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerBottomSheet(
            initialDate = endDate,
            minDate = startDate,
            title = "종료일 선택",
            onConfirm = { date ->
                onEndDateChange(LocalDateTime.of(date.toLocalDate(), LocalTime.of(23, 59)))
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponFormBottomSheetCreatePreview() {
    HaloTheme {
        Surface {
            CouponFormBottomSheetContent(
                isEditing = false,
                isIssued = false,
                formState = CouponFormState(),
                onDismiss = {},
                onCodeChange = {},
                onNameChange = {},
                onDescriptionChange = {},
                onTypeChange = {},
                onDiscountValueChange = {},
                onMinOrderAmountChange = {},
                onMaxDiscountAmountChange = {},
                onTotalQuantityChange = {},
                onMaxUseCountChange = {},
                onStartDateChange = {},
                onEndDateChange = {},
                onSave = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponFormBottomSheetEditPreview() {
    HaloTheme {
        Surface {
            CouponFormBottomSheetContent(
                isEditing = true,
                isIssued = true,
                formState = CouponFormState(
                    code = "WELCOME10",
                    name = "신규 가입 할인 쿠폰",
                    type = CouponType.PERCENTAGE,
                    discountValue = "10",
                    minOrderAmount = "10000",
                    maxDiscountAmount = "5000",
                    totalQuantity = "100",
                    maxUseCount = "1",
                    startDate = LocalDateTime.now(),
                    endDate = LocalDateTime.now().plusDays(30),
                    description = "신규 가입 시 10% 할인 쿠폰입니다."
                ),
                onDismiss = {},
                onCodeChange = {},
                onNameChange = {},
                onDescriptionChange = {},
                onTypeChange = {},
                onDiscountValueChange = {},
                onMinOrderAmountChange = {},
                onMaxDiscountAmountChange = {},
                onTotalQuantityChange = {},
                onMaxUseCountChange = {},
                onStartDateChange = {},
                onEndDateChange = {},
                onSave = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponFormBottomSheetFreeShippingPreview() {
    HaloTheme {
        Surface {
            CouponFormBottomSheetContent(
                isEditing = false,
                isIssued = false,
                formState = CouponFormState(
                    code = "FREESHIP",
                    name = "무료배송 쿠폰",
                    type = CouponType.FREE_SHIPPING,
                    discountValue = "0",
                    minOrderAmount = "10000",
                    totalQuantity = "50",
                    maxUseCount = "1",
                    startDate = LocalDateTime.now(),
                    endDate = LocalDateTime.now().plusDays(14),
                    description = "10,000원 이상 주문 시 무료배송"
                ),
                onDismiss = {},
                onCodeChange = {},
                onNameChange = {},
                onDescriptionChange = {},
                onTypeChange = {},
                onDiscountValueChange = {},
                onMinOrderAmountChange = {},
                onMaxDiscountAmountChange = {},
                onTotalQuantityChange = {},
                onMaxUseCountChange = {},
                onStartDateChange = {},
                onEndDateChange = {},
                onSave = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CouponFormBottomSheetValidationPreview() {
    HaloTheme {
        Surface {
            CouponFormBottomSheetContent(
                isEditing = false,
                isIssued = false,
                formState = CouponFormState(
                    name = "",
                    discountValue = "",
                    totalQuantity = ""
                ),
                onDismiss = {},
                onCodeChange = {},
                onNameChange = {},
                onDescriptionChange = {},
                onTypeChange = {},
                onDiscountValueChange = {},
                onMinOrderAmountChange = {},
                onMaxDiscountAmountChange = {},
                onTotalQuantityChange = {},
                onMaxUseCountChange = {},
                onStartDateChange = {},
                onEndDateChange = {},
                onSave = {},
                initialShowErrors = true
            )
        }
    }
}
