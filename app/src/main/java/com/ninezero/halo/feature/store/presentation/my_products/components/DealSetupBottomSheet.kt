package com.ninezero.halo.feature.store.presentation.my_products.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.DatePickerBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.FormSection
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.util.rememberKoreanNumberFormat
import com.ninezero.halo.feature.store.domain.model.Product
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DealSetupBottomSheet(
    product: Product,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (dealPrice: String, startAtMillis: Long, endAtMillis: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val numberFormat = rememberKoreanNumberFormat()
    val zone = remember { ZoneId.systemDefault() }

    // 딜 진행 중엔 price가 딜가라 basePrice 기준
    val sellingPrice = remember(product.basePrice, product.price) {
        product.basePrice.ifBlank { product.price }.toBigDecimalOrNull()
    }

    // 만료된 딜 값은 안 채움
    val existingStart = remember(product.dealStartAt) {
        product.dealStartAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime() }
    }
    val existingEnd = remember(product.dealEndAt) {
        product.dealEndAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime() }
    }
    val dealNotExpired = existingEnd != null && existingEnd.isAfter(LocalDateTime.now())

    var dealPrice by remember {
        mutableStateOf(
            if (dealNotExpired) product.dealPrice?.toBigDecimalOrNull()?.toLong()?.toString() ?: "" else ""
        )
    }
    var startDateTime by remember {
        mutableStateOf(if (dealNotExpired && existingStart != null) existingStart else LocalDateTime.now())
    }
    var endDateTime by remember {
        mutableStateOf(if (dealNotExpired) existingEnd else LocalDateTime.now().plusDays(1))
    }
    var showErrors by remember { mutableStateOf(false) }

    val dealPriceValue = dealPrice.toBigDecimalOrNull()
    val priceValid = dealPriceValue != null && dealPriceValue > BigDecimal.ZERO &&
            (sellingPrice == null || dealPriceValue < sellingPrice)
    val periodValid = endDateTime.isAfter(startDateTime) && endDateTime.isAfter(LocalDateTime.now())
    val isValid = dealPrice.isNotBlank() && priceValid && periodValid

    if (isSaving) {
        LoadingDialog()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        val density = LocalDensity.current
        val hazeState = remember { HazeState() }
        val backgroundColor = MaterialTheme.colorScheme.surface
        var headerHeight by remember { mutableStateOf(0.dp) }

        val onSave: () -> Unit = {
            if (isValid) {
                val startMillis = startDateTime.atZone(zone).toInstant().toEpochMilli()
                val endMillis = endDateTime.atZone(zone).toInstant().toEpochMilli()
                onConfirm(dealPrice, startMillis, endMillis)
            } else {
                showErrors = true
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            // 본문
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .hazeSource(hazeState)
                    .padding(top = headerHeight)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "현재 판매가",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${sellingPrice?.let { numberFormat.format(it) } ?: product.price}원",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FormSection(title = "딜 가격") {
                    FormTextField(
                        value = dealPrice,
                        onValueChange = { raw -> dealPrice = raw.filter { it.isDigit() } },
                        placeholder = "예: 15,000",
                        keyboardType = KeyboardType.Number,
                        prefix = "₩",
                        numberFormat = true,
                        isError = showErrors && !priceValid,
                        errorMessage = "딜 가격은 판매가보다 낮아야 합니다"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FormSection(title = "딜 기간") {
                    DealDateRangeField(
                        startDate = startDateTime,
                        endDate = endDateTime,
                        onStartDateChange = { startDateTime = it },
                        onEndDateChange = { endDateTime = it }
                    )
                }

                if (showErrors && !periodValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "종료는 시작과 현재 시각보다 뒤여야 합니다",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            backgroundColor = backgroundColor,
                            tint = HazeTint(backgroundColor),
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
                        text = "타임딜 설정",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    GlassIconButton(
                        onClick = onSave,
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
}

@Composable
private fun DealDateRangeField(
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    onStartDateChange: (LocalDateTime) -> Unit,
    onEndDateChange: (LocalDateTime) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd  HH:mm") }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(10.dp)
            )
            .clip(RoundedCornerShape(10.dp))
    ) {
        DealDateTimeRow(
            label = "시작",
            value = startDate.format(formatter),
            onClick = { showStartPicker = true }
        )
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
        DealDateTimeRow(
            label = "종료",
            value = endDate.format(formatter),
            onClick = { showEndPicker = true }
        )
    }

    if (showStartPicker) {
        DatePickerBottomSheet(
            initialDate = startDate,
            minDate = LocalDateTime.now(),
            title = "시작 일시",
            includeTime = true,
            onConfirm = { dateTime ->
                onStartDateChange(dateTime)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        DatePickerBottomSheet(
            initialDate = endDate,
            minDate = startDate,
            title = "종료 일시",
            includeTime = true,
            onConfirm = { dateTime ->
                onEndDateChange(dateTime)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
private fun DealDateTimeRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
