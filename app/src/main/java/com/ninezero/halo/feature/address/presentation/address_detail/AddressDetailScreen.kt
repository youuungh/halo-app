package com.ninezero.halo.feature.address.presentation.address_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.address.presentation.address_detail.components.DeliveryMemoBottomSheet
import com.ninezero.halo.feature.address.presentation.address_detail.components.DetailTextField
import com.ninezero.halo.feature.address.presentation.address_detail.components.MemoSelectorField
import com.ninezero.halo.feature.address.presentation.components.DaumPostcodeBottomSheet
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
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
            item(key = "address_info") {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${state.address} (${state.zipCode})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    )

                    if (state.isEditMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onIntent(AddressDetailIntent.ShowPostcodeSheet)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick()
                        ) {
                            Text("주소 변경")
                        }
                    }
                }
            }

            item(key = "address_detail") {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailTextField(
                        value = state.addressDetail,
                        onValueChange = { onIntent(AddressDetailIntent.UpdateAddressDetail(it)) },
                        label = "상세주소",
                        placeholder = "동/호수를 입력해주세요"
                    )
                }
            }

            item(key = "recipient_name") {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailTextField(
                        value = state.recipientName,
                        onValueChange = { onIntent(AddressDetailIntent.UpdateRecipientName(it)) },
                        label = "받는 이",
                        error = state.recipientNameError
                    )
                }
            }

            item(key = "recipient_phone") {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailTextField(
                        value = state.recipientPhone,
                        onValueChange = { onIntent(AddressDetailIntent.UpdateRecipientPhone(it)) },
                        label = "연락처",
                        error = state.recipientPhoneError,
                        placeholder = "01012345678",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            item(key = "memo") {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MemoSelectorField(
                        value = state.memo,
                        onClick = {
                            focusManager.clearFocus(force = true)
                            onIntent(AddressDetailIntent.ShowMemoSheet)
                        }
                    )
                }
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
                        startIntensity = 0.5f,
                        endIntensity = 0f
                    )
                }
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
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
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
