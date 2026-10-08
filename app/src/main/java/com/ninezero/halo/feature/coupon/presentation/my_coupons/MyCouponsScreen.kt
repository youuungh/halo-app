package com.ninezero.halo.feature.coupon.presentation.my_coupons

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.coupon.presentation.my_coupons.components.CouponCard
import com.ninezero.halo.feature.coupon.presentation.my_coupons.components.CouponCardSkeleton
import com.ninezero.halo.feature.coupon.presentation.my_coupons.components.CouponCodeInputField
import com.ninezero.halo.feature.coupon.presentation.my_coupons.components.CouponFilterChips
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyCouponsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MyCouponsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MyCouponsSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(MyCouponsIntent.SnackbarDismissed) }
    )

    MyCouponsScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

private const val CHIP_SLOT_KEY = "coupon_filter_chips"

@Composable
internal fun MyCouponsScreenContent(
    state: MyCouponsState,
    onIntent: (MyCouponsIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()

    var chipsHeight by rememberSaveableDp()

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(MyCouponsIntent.LoadMoreCoupons) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(MyCouponsIntent.RefreshCoupons) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
        // 본문
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
            contentPadding = PaddingValues(
                top = headerHeight,
                bottom = 16.dp
            )
        ) {
            item {
                CouponCodeInputField(
                    value = state.couponCode,
                    onValueChange = { onIntent(MyCouponsIntent.UpdateCouponCode(it)) },
                    onClaimClick = { onIntent(MyCouponsIntent.ClaimCoupon) },
                    isClaiming = state.isClaiming,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 필터 칩 자리
            item(key = CHIP_SLOT_KEY) {
                Spacer(modifier = Modifier.height(chipsHeight))
            }

            // 에러 상태
            if (state.error != null && state.coupons.isEmpty() && !state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 174.dp),
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
                                onClick = { onIntent(MyCouponsIntent.LoadCoupons) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }
            }

            // 초기 로딩 스켈레톤
            if (state.isLoading && state.coupons.isEmpty()) {
                items(5) {
                    CouponCardSkeleton(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // 빈 상태
            if (state.coupons.isEmpty() && !state.isLoading && state.error == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 174.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "쿠폰이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = state.coupons,
                    key = { it.id }
                ) { coupon ->
                    CouponCard(
                        coupon = coupon,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // 추가 로딩 스켈레톤
                if (state.isLoading && state.coupons.isNotEmpty()) {
                    items(3) {
                        CouponCardSkeleton(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
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
                        tint = HazeTint(backgroundColor.copy(alpha = 0.4f)),
                        blurRadius = 20.dp,
                        noiseFactor = 0f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        headerHeight = with(density) { coordinates.size.height.toDp() }
                    }
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GlassIconButton(
                    onClick = { onIntent(MyCouponsIntent.NavigateBack) },
                    icon = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "내 쿠폰",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

        }

        // 필터 칩, 플로팅
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    val headerPx = headerHeight.roundToPx()
                    val info = listState.layoutInfo
                    val slot = info.visibleItemsInfo.firstOrNull { it.key == CHIP_SLOT_KEY }
                    // 슬롯이 안 보임 = 스크롤로 지나감 → 톱바 아래 고정
                    val y = slot?.let { it.offset - info.viewportStartOffset } ?: headerPx
                    IntOffset(0, y.coerceAtLeast(headerPx))
                }
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = backgroundColor,
                        tint = HazeTint(backgroundColor.copy(alpha = 0.4f)),
                        blurRadius = 20.dp,
                        noiseFactor = 0f
                    )
                }
                .onGloballyPositioned { coordinates ->
                    chipsHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            CouponFilterChips(
                selectedStatus = state.selectedStatus,
                onFilterByStatus = { onIntent(MyCouponsIntent.FilterByStatus(it)) },
                hazeState = hazeState
            )
        }

        CenterToast(
            visible = state.toastMessage != null,
            message = state.toastMessage ?: "",
            onDismiss = { onIntent(MyCouponsIntent.DismissToast) }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
