package com.ninezero.halo.feature.store.presentation.my_products.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.DatePickerBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.SheetHeader
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.FormSection
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.input.inputBorder
import com.ninezero.halo.core.ui.components.selector.SegmentedTabRow
import com.ninezero.halo.core.ui.components.text.rememberKoreanNumberFormat
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.presentation.components.discountRatePercent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

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
    val sellingPrice = product.basePrice ?: product.price

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
            if (dealNotExpired) product.dealPrice?.toString() ?: "" else ""
        )
    }
    // 새 딜은 지금부터 1일, 분 단위
    var startDateTime by remember {
        mutableStateOf(
            if (dealNotExpired && existingStart != null) existingStart
            else LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        )
    }
    var endDateTime by remember {
        mutableStateOf(if (dealNotExpired) existingEnd else startDateTime.plusDays(1))
    }
    var showErrors by remember { mutableStateOf(false) }

    val dealPriceValue = dealPrice.toBigDecimalOrNull()
    val priceValid = dealPriceValue != null && dealPriceValue > BigDecimal.ZERO &&
            dealPriceValue < BigDecimal.valueOf(sellingPrice)
    val periodValid = endDateTime.isAfter(startDateTime) && endDateTime.isAfter(LocalDateTime.now())
    val isValid = dealPrice.isNotBlank() && priceValid && periodValid

    if (isSaving) {
        LoadingDialog()
    }

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        showDragHandle = false  // 헤더 안에 있음
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
                    .padding(bottom = 24.dp)  // 태그 · 상세설정 시트와 같은 아래 여백
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 어떤 상품인지: 사진 · 이름 · 판매가
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = product.imageUrls.firstOrNull(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_image_placeholder),
                        error = painterResource(R.drawable.ic_image_placeholder),
                        fallback = painterResource(R.drawable.ic_image_placeholder)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "판매가 ${numberFormat.format(sellingPrice)}원",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                FormSection(title = "딜 가격") {
                    FormTextField(
                        value = dealPrice,
                        onValueChange = { raw -> dealPrice = raw.filter { it.isDigit() } },
                        placeholder = "0",
                        keyboardType = KeyboardType.Number,
                        prefix = "₩",
                        numberFormat = true,
                        isError = showErrors && !priceValid,
                        errorMessage = "판매가보다 낮게 입력해주세요"
                    )
                }

                // 상품 등록과 같은 할인율 줄
                sellingPrice.let { base ->
                    discountRatePercent(base.toString(), dealPrice)?.let { rate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 2.dp, end = 2.dp, top = 10.dp),
                            // 이름과 숫자를 붙이고 숫자는 상품 상세 할인율과 같은 빨강
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "할인율",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.alignByBaseline()
                            )
                            Text(
                                text = "$rate%",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                FormSection(title = "기간") {
                    DealPeriodField(
                        startDate = startDateTime,
                        endDate = endDateTime,
                        onStartDateChange = { startDateTime = it },
                        onEndDateChange = { endDateTime = it }
                    )
                }

                if (showErrors && !periodValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "종료는 시작과 지금보다 뒤여야 합니다",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            SheetHeader(
                title = "타임딜",
                onClose = onDismiss,
                hazeState = hazeState,
                onHeightChange = { headerHeight = it },
                closeDescription = "취소",
                onConfirm = onSave,
                confirmDescription = "저장"
            )
        }
    }
}

private val DealPresetDays = listOf(1L, 3L, 7L)

/** 기간 탭 + 시작 · 종료 칸, 탭은 시작 + N일 */
@Composable
private fun DealPeriodField(
    startDate: LocalDateTime,
    endDate: LocalDateTime,
    onStartDateChange: (LocalDateTime) -> Unit,
    onEndDateChange: (LocalDateTime) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MM.dd (E) HH:mm", Locale.KOREAN) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    // 차이가 딱 N일이면 그 탭, 아니면 직접
    val selectedIndex = DealPresetDays.indexOfFirst { endDate == startDate.plusDays(it) }
        .takeIf { it >= 0 } ?: DealPresetDays.size

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SegmentedTabRow(
            tabs = listOf("1일", "3일", "7일", "직접"),
            selectedIndex = selectedIndex,
            onTabSelected = { index ->
                DealPresetDays.getOrNull(index)?.let { onEndDateChange(startDate.plusDays(it)) }
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DealDateTimeBox(
                label = "시작",
                value = startDate.format(formatter),
                onClick = { showStartPicker = true },
                modifier = Modifier.weight(1f)
            )
            DealDateTimeBox(
                label = "종료",
                value = endDate.format(formatter),
                onClick = { showEndPicker = true },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showStartPicker) {
        DatePickerBottomSheet(
            initialDate = startDate,
            minDate = LocalDateTime.now(),
            title = "시작 일시",
            includeTime = true,
            onConfirm = { dateTime ->
                // 기간 탭을 고른 상태면 길이를 유지한 채 함께 옮김
                DealPresetDays.getOrNull(selectedIndex)?.let { onEndDateChange(dateTime.plusDays(it)) }
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

/** 칸 안 작은 이름 + 일시 + 화살표 */
@Composable
private fun DealDateTimeBox(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .inputBorder(shape)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
