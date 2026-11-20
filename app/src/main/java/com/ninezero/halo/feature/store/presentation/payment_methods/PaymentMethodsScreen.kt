package com.ninezero.halo.feature.store.presentation.payment_methods

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.bottomsheet.WebViewAuthBottomSheet
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.list.PagedListScreenShell
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.store.presentation.payment_methods.components.PaymentCardInfo
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PaymentMethodsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentMethodsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // 카드 등록 WebView 허용 호스트: 서버 + 토스 도메인
    val billingHost = remember { Constants.BASE_URL.toUri().host ?: "10.0.2.2" }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is PaymentMethodsSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(PaymentMethodsIntent.SnackbarDismissed) }
    )

    PaymentMethodsScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        billingHost = billingHost,
        snackbarHostState = snackbarHostState
    )
}

@Composable
internal fun PaymentMethodsScreenContent(
    state: PaymentMethodsState,
    onIntent: (PaymentMethodsIntent) -> Unit,
    billingHost: String,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    PagedListScreenShell(
        title = "결제수단",
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading,
        hasMore = false,
        isEmpty = state.billingCard == null,  // 아직 못 받음
        error = state.error,
        errorTitle = "결제수단을 불러오지 못했습니다",
        emptyTitle = "등록된 결제수단이 없어요",
        emptyDescription = "결제수단을 추가해보세요",
        onNavigateBack = { onIntent(PaymentMethodsIntent.NavigateBack) },
        onRefresh = { onIntent(PaymentMethodsIntent.Refresh) },
        onRetry = { onIntent(PaymentMethodsIntent.Load) },
        onLoadMore = {},
        snackbarHostState = snackbarHostState,
        // 버튼 48 + 여백 16, 삭제 버튼 있으면 52 더함
        snackbarBottomPadding = when (state.billingCard?.registered) {
            true -> 116.dp
            false -> 64.dp
            null -> 0.dp
        }
    ) { _, headerHeight ->
        // 첫 로딩은 셸의 당김 표시가 보여 줌
        val card = state.billingCard ?: return@PagedListScreenShell
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = headerHeight + 12.dp)
            ) {
                item {
                    PaymentCardInfo(card = card, hasAutoRenew = state.hasAutoRenew)
                }
            }

            // 아래 버튼: 구매완료 화면과 같은 주 버튼 + 투명 버튼
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            ) {
                PrimaryButton(
                    text = if (card.registered) "카드 변경" else "카드 등록",
                    onClick = { onIntent(PaymentMethodsIntent.AddCard) },
                    isLoading = state.isRegistering
                )
                if (card.registered) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { onIntent(PaymentMethodsIntent.ShowDeleteDialog) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .bounceClick(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("카드 삭제")
                    }
                }
            }
        }
    }

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "카드를 삭제하시겠습니까?",
            message = if (state.hasAutoRenew) "멤버십 자동 갱신이 중단됩니다" else null,
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(PaymentMethodsIntent.ConfirmDelete) },
            onDismiss = { onIntent(PaymentMethodsIntent.DismissDeleteDialog) }
        )
    }

    val url = state.registerWebViewUrl
    if (state.showRegisterWebView && url != null) {
        WebViewAuthBottomSheet(
            url = url,
            title = "카드 등록",
            allowedHostSuffixes = listOf(billingHost, "tosspayments.com"),
            returnScheme = "haloapp",
            onReturn = { uri ->
                val authKey = uri.getQueryParameter("authKey")
                if (authKey != null) {
                    onIntent(PaymentMethodsIntent.RegisterReturned(authKey))
                } else {
                    onIntent(
                        PaymentMethodsIntent.RegisterFailed(uri.getQueryParameter("code"), uri.getQueryParameter("message"))
                    )
                }
            },
            onDismiss = { onIntent(PaymentMethodsIntent.RegisterDismissed) }
        )
    }
}
