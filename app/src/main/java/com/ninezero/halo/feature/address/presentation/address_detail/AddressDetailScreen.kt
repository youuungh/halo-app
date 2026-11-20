package com.ninezero.halo.feature.address.presentation.address_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.input.FormSection
import com.ninezero.halo.core.ui.components.input.FormTextField
import com.ninezero.halo.core.ui.components.input.inputBorder
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.toggle.HaloCheckbox
import com.ninezero.halo.feature.address.presentation.address_detail.components.DeliveryMemoBottomSheet
import com.ninezero.halo.feature.address.presentation.address_detail.components.MemoSelectorField
import com.ninezero.halo.feature.address.presentation.components.DaumPostcodeBottomSheet
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun AddressDetailScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddressDetailViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is AddressDetailSideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is AddressDetailSideEffect.SaveSuccess -> {
                keyboardController?.hide()
                onSaveSuccess()
            }
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(AddressDetailIntent.SnackbarDismissed) }
    )

    AddressDetailScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )

    if (state.showPostcodeSheet) {
        DaumPostcodeBottomSheet(
            onDismiss = { viewModel.onIntent(AddressDetailIntent.DismissPostcodeSheet) },
            onAddressSelected = { zipCode, address ->
                viewModel.onIntent(AddressDetailIntent.UpdateAddress(zipCode, address))
            }
        )
    }

    if (state.showMemoSheet) {
        DeliveryMemoBottomSheet(
            currentMemo = state.memo,
            onConfirm = { memo ->
                viewModel.onIntent(AddressDetailIntent.UpdateMemo(memo))
                viewModel.onIntent(AddressDetailIntent.DismissMemoSheet)
            },
            onDismiss = { viewModel.onIntent(AddressDetailIntent.DismissMemoSheet) }
        )
    }
}

@Composable
internal fun AddressDetailScreenContent(
    state: AddressDetailState,
    onIntent: (AddressDetailIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp)
        ) {
            item(key = "address") {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 칸마다 위에 작은 이름 + 칸 안 힌트
                    FormSection(title = "주소") {
                        AddressRow(
                            address = state.address,
                            zipCode = state.zipCode,
                            onChangeClick = if (state.isEditMode) {
                                {
                                    focusManager.clearFocus(force = true)
                                    onIntent(AddressDetailIntent.ShowPostcodeSheet)
                                }
                            } else {
                                null
                            }
                        )
                    }
                    FormSection(title = "상세주소") {
                        FormTextField(
                            value = state.addressDetail,
                            onValueChange = { onIntent(AddressDetailIntent.UpdateAddressDetail(it)) },
                            placeholder = "동/호수",
                            imeAction = ImeAction.Next,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            FormSection(title = "받는 분") {
                                FormTextField(
                                    value = state.recipientName,
                                    onValueChange = { onIntent(AddressDetailIntent.UpdateRecipientName(it)) },
                                    placeholder = "이름",
                                    isError = state.recipientNameError != null,
                                    errorMessage = state.recipientNameError,
                                    imeAction = ImeAction.Next,
                                    onNext = { focusManager.moveFocus(FocusDirection.Right) }
                                )
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FormSection(title = "연락처") {
                                FormTextField(
                                    value = state.recipientPhone,
                                    onValueChange = { onIntent(AddressDetailIntent.UpdateRecipientPhone(it)) },
                                    placeholder = "010-0000-0000",
                                    keyboardType = KeyboardType.Number,
                                    isError = state.recipientPhoneError != null,
                                    errorMessage = state.recipientPhoneError,
                                    maxLength = 11,
                                    visualTransformation = PhoneVisualTransformation
                                )
                            }
                        }
                    }
                    FormSection(title = "배송 메모") {
                        MemoSelectorField(
                            value = state.memo,
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onIntent(AddressDetailIntent.ShowMemoSheet)
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onIntent(AddressDetailIntent.UpdateDefault(!state.isDefault)) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HaloCheckbox(checked = state.isDefault, contentDescription = "기본 배송지로 설정")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "기본 배송지로 설정",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GlassIconButton(
                    onClick = { onIntent(AddressDetailIntent.NavigateBack) },
                    icon = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "주소 상세",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )

                GlassActionButton(
                    onClick = { if (!state.isSaving) onIntent(AddressDetailIntent.Save) },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    hazeState = hazeState,
                    enabled = true
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "저장",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (state.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
            )
        }

        // 편집 모드 초기 로드 중 폼 가림
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(top = headerHeight),
                contentAlignment = Alignment.Center
            ) {
                CenteredLoadingIndicator(size = LoadingIndicatorSize.Large)
            }
        }
    }
}

/** 위치 아이콘 + 주소 · 우편번호, 오른쪽 '변경' */
@Composable
private fun AddressRow(
    address: String,
    zipCode: String,
    onChangeClick: (() -> Unit)?
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .inputBorder(shape)
            .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_map_pin),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = zipCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onChangeClick != null) {
            Text(
                text = "변경",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onChangeClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}
