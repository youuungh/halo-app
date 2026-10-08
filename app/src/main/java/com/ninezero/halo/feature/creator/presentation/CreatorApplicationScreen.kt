package com.ninezero.halo.feature.creator.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.creator.presentation.components.ApplicationFormContent
import com.ninezero.halo.feature.creator.presentation.components.ApplicationStatusContent
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CreatorApplicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreatorApplicationViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is CreatorApplicationSideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(CreatorApplicationIntent.SnackbarDismissed) }
    )

    CreatorApplicationScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun CreatorApplicationScreenContent(
    state: CreatorApplicationState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (CreatorApplicationIntent) -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val focusManager = LocalFocusManager.current
    var headerHeight by rememberSaveableDp()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(rememberScrollState())
                .padding(top = headerHeight)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CenteredLoadingIndicator(size = LoadingIndicatorSize.Large)
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onIntent(CreatorApplicationIntent.LoadApplication) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                state.application != null -> {
                    ApplicationStatusContent(
                        application = state.application,
                        isSubmitting = state.isSubmitting,
                        onCancel = { onIntent(CreatorApplicationIntent.ShowCancelDialog) },
                        onReapply = {
                            onIntent(CreatorApplicationIntent.StartReapply)
                        }
                    )
                }

                else -> {
                    ApplicationFormContent(
                        reason = state.reason,
                        portfolioUrl = state.portfolioUrl,
                        isSubmitting = state.isSubmitting,
                        onReasonChange = { onIntent(CreatorApplicationIntent.UpdateReason(it)) },
                        onPortfolioUrlChange = { onIntent(CreatorApplicationIntent.UpdatePortfolioUrl(it)) },
                        onSubmit = { onIntent(CreatorApplicationIntent.Submit) }
                    )
                }
            }
        }

        HazeScreenHeader(
            title = "크리에이터 신청",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(CreatorApplicationIntent.NavigateBack) },
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (state.isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
            )
        }
    }

    if (state.showCancelDialog) {
        ConfirmDialog(
            title = "신청을 취소하시겠어요?",
            confirmText = "취소하기",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(CreatorApplicationIntent.ConfirmCancel) },
            onDismiss = { onIntent(CreatorApplicationIntent.DismissCancelDialog) }
        )
    }
}
