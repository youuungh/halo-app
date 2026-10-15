package com.ninezero.halo.feature.store.presentation.payment_methods

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.WebViewAuthBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.PagedListScreenShell
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.store.presentation.payment_methods.components.AddPaymentCardPlaceholder
import com.ninezero.halo.feature.store.presentation.payment_methods.components.RegisteredPaymentCard
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
        isEmpty = state.error != null,
        error = state.error,
        emptyTitle = "등록된 결제수단이 없어요",
        emptyDescription = "결제수단을 추가해보세요",
        onNavigateBack = { onIntent(PaymentMethodsIntent.NavigateBack) },
        onRefresh = { onIntent(PaymentMethodsIntent.Refresh) },
        onRetry = { onIntent(PaymentMethodsIntent.Load) },
        onLoadMore = {},
        snackbarHostState = snackbarHostState,
        headerActions = { hazeState, backgroundColor ->
            if (state.billingCard?.registered == true) {
                GlassIconButton(
                    onClick = { onIntent(PaymentMethodsIntent.ShowDeleteDialog) },
                    icon = painterResource(R.drawable.ic_delete),
                    contentDescription = "결제수단 삭제",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    ) { _, _ ->
        val card = state.billingCard
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (card?.registered == true) {
                        RegisteredPaymentCard(
                            cardCompany = card.cardCompany,
                            cardNumberMasked = card.cardNumberMasked,
                            cardType = card.cardType
                        )
                    } else {
                        AddPaymentCardPlaceholder(
                            isLoading = state.isRegistering,
                            onClick = { onIntent(PaymentMethodsIntent.AddCard) }
                        )
                    }
                }
            }
        }
    }

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "결제수단을 삭제하시겠어요?",
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
