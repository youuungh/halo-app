package com.ninezero.halo.feature.store.presentation.sales_manage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

private val CARRIERS = listOf(
    "CJ대한통운",
    "우체국택배",
    "한진택배",
    "롯데택배",
    "로젠택배"
)

private const val MIN_TRACKING_LENGTH = 10
private const val MAX_TRACKING_LENGTH = 20

@Composable
fun ShippingInputBottomSheet(
    isEditing: Boolean,
    selectedCarrier: String,
    trackingNumber: String,
    isSaving: Boolean,
    shippingError: String?,
    onSelectCarrier: (String) -> Unit,
    onTrackingNumberChange: (String) -> Unit,
    onSave: () -> Unit,
    onShippingErrorDismiss: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        BottomSheetDragHandle()
        ShippingInputContent(
            isEditing = isEditing,
            selectedCarrier = selectedCarrier,
            trackingNumber = trackingNumber,
            isSaving = isSaving,
            shippingError = shippingError,
            onSelectCarrier = onSelectCarrier,
            onTrackingNumberChange = onTrackingNumberChange,
            onSave = onSave,
            onShippingErrorDismiss = onShippingErrorDismiss,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ShippingInputContent(
    isEditing: Boolean = false,
    selectedCarrier: String,
    trackingNumber: String,
    isSaving: Boolean,
    shippingError: String? = null,
    onSelectCarrier: (String) -> Unit,
    onTrackingNumberChange: (String) -> Unit,
    onSave: () -> Unit,
    onShippingErrorDismiss: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val hazeState = remember { HazeState() }
    val snackbarHostState = remember { SnackbarHostState() }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "송장번호 수정" else "송장번호 입력",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                GlassIconButton(
                    onClick = onDismiss,
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    size = 36.dp,
                    iconSize = 20.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val hasValidTrackingLength = trackingNumber.length in MIN_TRACKING_LENGTH..MAX_TRACKING_LENGTH

            Column(modifier = Modifier.hazeSource(hazeState)) {
                FormTextField(
                    value = trackingNumber,
                    onValueChange = { onTrackingNumberChange(it.filter { c -> c in '0'..'9' }) },  // 붙여넣은 하이픈·공백 제거
                    placeholder = "운송장번호를 입력하세요",
                    keyboardType = KeyboardType.Number,
                    maxLength = MAX_TRACKING_LENGTH,
                    isError = trackingNumber.isNotBlank() && !hasValidTrackingLength,
                    errorMessage = "운송장번호는 $MIN_TRACKING_LENGTH-$MAX_TRACKING_LENGTH 자리여야 합니다"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "택배사",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CARRIERS.forEach { carrier ->
                        val isSelected = carrier == selectedCarrier
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onSelectCarrier(carrier) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = carrier,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val canSave = selectedCarrier.isNotBlank() && hasValidTrackingLength && !isSaving

                Button(
                    onClick = { if (!isSaving) onSave() },
                    enabled = canSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .bounceClick(enabled = canSave)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isEditing) "수정" else "등록",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        if (isSaving) {
                            LoadingIndicator(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                size = LoadingIndicatorSize.Small,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        if (isSaving) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
            )
        }

        // 시트 window에 가려지지 않게 시트 안에서 표시
        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
            bottomBarPadding = 56.dp  // 등록 버튼 48 + 하단 여백 8
        )
    }

    SnackbarHandler(
        message = shippingError,
        hostState = snackbarHostState,
        onDismiss = onShippingErrorDismiss
    )
}

@HaloLightDarkPreview
@Composable
private fun ShippingInputEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ShippingInputContent(
                selectedCarrier = "",
                trackingNumber = "",
                isSaving = false,
                onSelectCarrier = {},
                onTrackingNumberChange = {},
                onSave = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ShippingInputFilledPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ShippingInputContent(
                selectedCarrier = "CJ대한통운",
                trackingNumber = "123456789012",
                isSaving = false,
                onSelectCarrier = {},
                onTrackingNumberChange = {},
                onSave = {}
            )
        }
    }
}
