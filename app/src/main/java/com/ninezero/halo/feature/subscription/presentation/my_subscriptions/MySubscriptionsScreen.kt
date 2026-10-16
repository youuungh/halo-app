package com.ninezero.halo.feature.subscription.presentation.my_subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.chip.HaloFilterChip
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionStatus
import com.ninezero.halo.feature.subscription.presentation.my_subscriptions.components.SubscriptionCard
import com.ninezero.halo.feature.subscription.presentation.my_subscriptions.components.SubscriptionCardSkeleton
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MySubscriptionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreatorProfile: (Int) -> Unit = {},
    viewModel: MySubscriptionsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MySubscriptionsSideEffect.NavigateBack -> onNavigateBack()
            is MySubscriptionsSideEffect.NavigateToCreatorProfile -> onNavigateToCreatorProfile(
                sideEffect.creatorId
            )
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(MySubscriptionsIntent.SnackbarDismissed) }
    )

    MySubscriptionsScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

@Composable
internal fun MySubscriptionsScreenContent(
    state: MySubscriptionsState,
    onIntent: (MySubscriptionsIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    val listState = rememberLazyListState()

    var headerHeight by rememberSaveableDp()

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(MySubscriptionsIntent.LoadMoreSubscriptions) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(MySubscriptionsIntent.RefreshSubscriptions) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            // 본문
            when {
                state.isLoading && state.subscriptions.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        items(5) { index ->
                            SubscriptionCardSkeleton()
                            if (index < 4) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                // 에러 상태는 정적 Box, 재시도 버튼으로 복구
                !state.isLoading && state.error != null && state.subscriptions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                onClick = { onIntent(MySubscriptionsIntent.RefreshSubscriptions) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                !state.isLoading && state.subscriptions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "구독 중인 크리에이터가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(
                            top = headerHeight,
                            bottom = 32.dp
                        )
                    ) {
                        items(
                            items = state.subscriptions,
                            key = { it.id }
                        ) { subscription ->
                            Column(modifier = Modifier.animateItem()) {
                                SubscriptionCard(
                                    subscription = subscription,
                                    onAutoRenewChange = { autoRenew ->
                                        if (autoRenew) {
                                            onIntent(
                                                MySubscriptionsIntent.ToggleAutoRenew(
                                                    subscription.id,
                                                    true
                                                )
                                            )
                                        } else {
                                            onIntent(
                                                MySubscriptionsIntent.RequestAutoRenewOff(
                                                    subscription
                                                )
                                            )
                                        }
                                    },
                                    onClick = {
                                        onIntent(
                                            MySubscriptionsIntent.NavigateToCreatorProfile(
                                                subscription.creatorId
                                            )
                                        )
                                    },
                                    isUpdatingAutoRenew = state.isUpdatingAutoRenew
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // 추가 로딩 스켈레톤
                        if (state.isLoading && state.subscriptions.isNotEmpty()) {
                            items(3) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                SubscriptionCardSkeleton()
                            }
                        }
                    }
                }
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(state = hazeState) {
                    style = HaloBlurStyle(
                        backgroundColor = backgroundColor,
                        tint = HaloBlurTint(backgroundColor),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1.0f,
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
                    onClick = { onIntent(MySubscriptionsIntent.NavigateBack) },
                    icon = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "내 멤버십",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 필터 칩
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HaloFilterChip(
                    selected = state.selectedStatus == null,
                    label = "전체",
                    onClick = { onIntent(MySubscriptionsIntent.FilterByStatus(null)) },
                    hazeState = hazeState
                )

                HaloFilterChip(
                    selected = state.selectedStatus == SubscriptionStatus.ACTIVE,
                    label = "활성",
                    onClick = { onIntent(MySubscriptionsIntent.FilterByStatus(SubscriptionStatus.ACTIVE)) },
                    hazeState = hazeState
                )

                HaloFilterChip(
                    selected = state.selectedStatus == SubscriptionStatus.EXPIRED,
                    label = "만료됨",
                    onClick = { onIntent(MySubscriptionsIntent.FilterByStatus(SubscriptionStatus.EXPIRED)) },
                    hazeState = hazeState
                )
            }
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // 자동 갱신 해제 확인 다이얼로그
    state.autoRenewOffTarget?.let { target ->
        ConfirmDialog(
            profileImageUrl = target.creatorAvatarUrl,
            annotatedMessage = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(target.creatorUsername)
                }
                append("님 멤버십 자동 갱신을 해제할까요?\n만료일까지 혜택은 그대로 유지됩니다.")
            },
            confirmText = "해제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(MySubscriptionsIntent.ConfirmAutoRenewOff) },
            onDismiss = { onIntent(MySubscriptionsIntent.DismissAutoRenewOffDialog) }
        )
    }

    CenterToast(
        visible = state.toastMessage != null,
        message = state.toastMessage ?: "",
        onDismiss = { onIntent(MySubscriptionsIntent.DismissToast) }
    )
}
