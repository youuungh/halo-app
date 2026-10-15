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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.blockSheetFling
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.button.SheetActionButton
import com.ninezero.halo.core.ui.components.input.FormTextField
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
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
    val hasContent = value.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "배송 메모 (선택)",
            style = if (hasContent) {
                MaterialTheme.typography.labelSmall
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (hasContent) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier
                .align(if (hasContent) Alignment.TopStart else Alignment.CenterStart)
                .padding(top = if (hasContent) 8.dp else 0.dp)
        )

        if (hasContent) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(end = 28.dp, bottom = 8.dp)
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(18.dp),
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

    val customFieldRequester = remember { BringIntoViewRequester() }
    var customFieldSize by remember { mutableStateOf(IntSize.Zero) }
    var isCustomFocused by remember { mutableStateOf(false) }
    var viewportHeight by remember { mutableIntStateOf(0) }

    // 키보드만큼 시트가 줄면 스크롤은 필드를 바텀바 밑까지만 올림, 줄 때마다 바텀바 위로 재요청
    LaunchedEffect(isCustomFocused, customFieldSize, bottomBarHeight, viewportHeight) {
        if (!isCustomFocused || customFieldSize == IntSize.Zero) return@LaunchedEffect
        val reservedPx = with(density) { (bottomBarHeight + 8.dp).toPx() }
        customFieldRequester.bringIntoView(
            Rect(
                left = 0f,
                top = 0f,
                right = customFieldSize.width.toFloat(),
                bottom = customFieldSize.height.toFloat() + reservedPx
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surface,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) },
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        BottomSheetDragHandle()

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .hazeSource(hazeState)
                    .blockSheetFling()
                    .onSizeChanged { viewportHeight = it.height }
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(customFieldRequester)
                                .onSizeChanged { customFieldSize = it }
                                .onFocusChanged { isCustomFocused = it.hasFocus }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 헤더
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            backgroundColor = surface,
                            tint = HazeTint(surface),
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f
                        )
                    }
                    .onGloballyPositioned { headerHeight = with(density) { it.size.height.toDp() } }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "배송 메모",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    GlassIconButton(
                        onClick = onDismiss,
                        icon = painterResource(R.drawable.ic_close),
                        contentDescription = "닫기",
                        hazeState = hazeState,
                        backgroundColor = surface,
                        size = 36.dp,
                        iconSize = 20.dp
                    )
                }
            }

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
                        .hazeEffect(state = hazeState) {
                            style = HazeStyle(
                                backgroundColor = surface,
                                tint = HazeTint(surface),
                                noiseFactor = 0f
                            )
                            progressive = HazeProgressive.verticalGradient(
                                startIntensity = 0f,
                                endIntensity = 1f
                            )
                        }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.exclude(WindowInsets.ime))
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp, bottom = 8.dp)
                ) {
                    SheetActionButton(
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
