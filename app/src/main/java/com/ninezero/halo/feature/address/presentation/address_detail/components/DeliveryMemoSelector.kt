package com.ninezero.halo.feature.address.presentation.address_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.bottomsheet.SheetHeader
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.input.inputBorder
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

internal val DELIVERY_MEMO_PRESETS = listOf(
    "문 앞에 놓아주세요",
    "경비실에 맡겨주세요",
    "택배함에 넣어주세요",
    "배송 전 연락주세요",
    "부재 시 연락주세요"
)

private const val MEMO_MAX_LENGTH = 200

@Composable
fun MemoSelectorField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .inputBorder(shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value.ifEmpty { "배송 메모 선택" },
            style = MaterialTheme.typography.bodyMedium,
            color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DeliveryMemoBottomSheet(
    currentMemo: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val surface = MaterialTheme.colorScheme.surface
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    var bottomBarHeight by remember { mutableStateOf(0.dp) }

    val initialIsCustom = currentMemo.isNotBlank() && currentMemo !in DELIVERY_MEMO_PRESETS
    var isCustom by remember { mutableStateOf(initialIsCustom) }
    // 직접입력 텍스트 분리, exit 애니메이션 잔상 방지
    var selectedPreset by remember {
        mutableStateOf(if (!initialIsCustom && currentMemo.isNotBlank()) currentMemo else null)
    }
    var customText by remember { mutableStateOf(if (initialIsCustom) currentMemo else "") }

    FloatingBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        showDragHandle = false  // 헤더 안에 있음
    ) {

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .hazeSource(hazeState)
                    .blockSheetFling()
                    .verticalScroll(rememberScrollState(), overscrollEffect = null)
                    .padding(top = headerHeight, bottom = bottomBarHeight)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DELIVERY_MEMO_PRESETS.forEach { preset ->
                        MemoOptionRow(
                            title = preset,
                            isSelected = !isCustom && selectedPreset == preset,
                            onClick = {
                                selectedPreset = preset
                                isCustom = false
                            }
                        )
                    }

                    MemoOptionRow(
                        title = "직접 입력",
                        isSelected = isCustom,
                        onClick = {
                            selectedPreset = null
                            isCustom = true
                        }
                    )
                }

                AnimatedVisibility(
                    visible = isCustom,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        FormTextField(
                            value = customText,
                            onValueChange = { customText = it },
                            placeholder = "배송 시 요청사항을 입력해주세요",
                            maxLength = MEMO_MAX_LENGTH,
                            showCounter = true,
                            modifier = Modifier.fillMaxWidth(),
                            bringIntoViewReserve = bottomBarHeight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            SheetHeader(
                title = "배송 메모",
                onClose = onDismiss,
                hazeState = hazeState,
                onHeightChange = { headerHeight = it },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // 바텀바
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onGloballyPositioned { bottomBarHeight = with(density) { it.size.height.toDp() } }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .haloBlur(hazeState, HaloBlurs.fadeUp(surface, end = 1f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.exclude(WindowInsets.ime))
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 8.dp)
                ) {
                    PrimaryButton(
                        text = "적용",
                        onClick = {
                            onConfirm((if (isCustom) customText else selectedPreset.orEmpty()).trim())
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoOptionRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
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
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
