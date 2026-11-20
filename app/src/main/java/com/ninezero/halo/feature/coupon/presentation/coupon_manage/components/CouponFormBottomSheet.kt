package com.ninezero.halo.feature.coupon.presentation.coupon_manage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.bottomsheet.DatePickerBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.FullScreenBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.SheetHeader
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.FormSection
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.input.inputBorder
import com.ninezero.halo.core.ui.components.selector.SegmentedTabRow
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.feature.coupon.domain.model.CouponType
import com.ninezero.halo.feature.coupon.presentation.coupon_manage.CouponFormState
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

    FullScreenBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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
            // 칸마다 위에 이름, 묶음 이름 없음
            item {
                FormSection(title = "쿠폰 이름") {
                    FormTextField(
                        value = formState.name,
                        onValueChange = onNameChange,
                        placeholder = "쿠폰 이름",
                        maxLength = 50,
                        imeAction = ImeAction.Next,
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        isError = showErrors && formState.name.isBlank(),
                        errorMessage = "쿠폰 이름을 입력해주세요"
                    )
                }
            }

            if (!isEditing) {
                item {
                    FormSection(title = "쿠폰 코드") {
                        FormTextField(
                            value = formState.code,
                            onValueChange = { raw ->
                                val filtered = raw.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }
                                if (filtered.length <= 20) onCodeChange(filtered)
                            },
                            placeholder = "비우면 자동 생성",
                            maxLength = 20,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            isError = showErrors && formState.code.isNotBlank()
                                    && (formState.code.length < 4 || !formState.code.matches("[A-Z0-9]+".toRegex())),
                            errorMessage = "영문 대문자 · 숫자 4~20자"
                        )
                    }
                }
            }

            item {
                FormSection(title = "할인 유형") {
                    val types = listOf(CouponType.FIXED_AMOUNT, CouponType.PERCENTAGE, CouponType.FREE_SHIPPING)
                    SegmentedTabRow(
                        tabs = listOf("정액", "비율", "무료 배송"),
                        selectedIndex = types.indexOf(formState.type),
                        onTabSelected = { if (!isEditing) onTypeChange(types[it]) },
                        modifier = Modifier.alpha(if (isEditing) 0.5f else 1f)  // 수정할 땐 유형 잠김
                    )
                }
            }

            // 유형마다 칸이 다름
            if (formState.type != CouponType.FREE_SHIPPING) {
                item {
                    val isPercent = formState.type == CouponType.PERCENTAGE
                    FormSection(title = if (isPercent) "할인율" else "할인 금액") {
                        FormTextField(
                            value = formState.discountValue,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                if (!isPercent || (filtered.toIntOrNull() ?: 0) <= 100) onDiscountValueChange(filtered)
                            },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            prefix = if (isPercent) "%" else "₩",
                            enabled = !isEditing,
                            isError = showErrors && formState.discountValue.isBlank(),
                            errorMessage = "할인 값을 입력해주세요",
                            numberFormat = !isPercent
                        )
                    }
                }
            }

            item {
                val minOrder: @Composable () -> Unit = {
                    FormSection(title = "최소 주문 금액") {
                        FormTextField(
                            value = formState.minOrderAmount,
                            onValueChange = { onMinOrderAmountChange(it.filter { c -> c.isDigit() }) },
                            placeholder = "없음",
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) { minOrder() }
                        Box(modifier = Modifier.weight(1f)) {
                            FormSection(title = "최대 할인 금액") {
                                FormTextField(
                                    value = formState.maxDiscountAmount,
                                    onValueChange = { onMaxDiscountAmountChange(it.filter { c -> c.isDigit() }) },
                                    placeholder = "없음",
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
                } else {
                    minOrder()
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormSection(title = "발급 수량") {
                            FormTextField(
                                value = formState.totalQuantity,
                                onValueChange = onTotalQuantityChange,
                                placeholder = "",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Right) },
                                isError = showErrors && (formState.totalQuantity.isBlank() || (formState.totalQuantity.toIntOrNull() ?: 0) <= 0),
                                errorMessage = "발급 수량을 입력해주세요"
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FormSection(title = "1인당 횟수") {
                            FormTextField(
                                value = formState.maxUseCount,
                                onValueChange = onMaxUseCountChange,
                                placeholder = "",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                enabled = !isIssued,
                                isError = showErrors && (formState.maxUseCount.isBlank() || (formState.maxUseCount.toIntOrNull() ?: 0) <= 0),
                                errorMessage = "사용 횟수를 입력해주세요"
                            )
                        }
                    }
                }
            }

            item {
                FormSection(title = "기간") {
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
                        placeholder = "쿠폰 설명 (선택)",
                        singleLine = false,
                        minLines = 2,
                        maxLength = 500
                    )
                }
            }
        }

        SheetHeader(
            title = if (isEditing) "쿠폰 수정" else "새 쿠폰",
            onClose = onDismiss,
            hazeState = hazeState,
            onHeightChange = { headerHeight = it },
            closeDescription = "취소",
            onConfirm = {
                if (formState.isValid) onSave()
                else showErrors = true
            },
            confirmDescription = "저장"
        )
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
                .inputBorder(RoundedCornerShape(10.dp))
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
                .inputBorder(RoundedCornerShape(10.dp))
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
