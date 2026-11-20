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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.input.CouponCodeInputField
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.list.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.list.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.coupon.presentation.my_coupons.components.CouponCard
import com.ninezero.halo.feature.coupon.presentation.my_coupons.components.CouponCardSkeleton
import com.ninezero.halo.feature.coupon.presentation.my_coupons.components.CouponFilterChips
import dev.chrisbanes.haze.HazeState
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
    // 칩이 톱바에 붙었을 때만 블러, 가장자리 얼룩 방지
    val isChipsPinned by remember(listState, density) {
        derivedStateOf {
            val info = listState.layoutInfo
            val slot = info.visibleItemsInfo.firstOrNull { it.key == CHIP_SLOT_KEY }
            slot == null || slot.offset - info.viewportStartOffset <= with(density) { headerHeight.roundToPx() }
        }
    }

    var chipsHeight by rememberSaveableDp()
    var inputHeightPx by remember { mutableIntStateOf(0) }

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
                        modifier = Modifier
                            .onSizeChanged { inputHeightPx = it.height }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                .excludeHeightAbove(inputHeightPx + with(density) { chipsHeight.roundToPx() })
                                .fillParentMaxSize()
                                .padding(bottom = 174.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ListErrorState(
                                title = "쿠폰 목록을 불러오지 못했습니다",
                                reason = state.error,
                                onRetry = { onIntent(MyCouponsIntent.LoadCoupons) }
                            )
                        }
                    }
                }

                // 초기 로딩 스켈레톤
                if (state.isLoading && state.coupons.isEmpty()) {
                    items(5) {
                        CouponCardSkeleton()
                    }
                }

                // 빈 상태
                if (state.coupons.isEmpty() && !state.isLoading && state.error == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .excludeHeightAbove(inputHeightPx + with(density) { chipsHeight.roundToPx() })
                                .fillParentMaxSize()
                                .padding(bottom = 174.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "쿠폰이 없어요",
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
                        CouponCard(coupon = coupon)
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    // 추가 로딩 스켈레톤
                    if (state.isLoading && state.coupons.isNotEmpty()) {
                        items(3) {
                            CouponCardSkeleton()
                        }
                    }
                }
            }

            // 필터 칩, 플로팅. 새로고침 표시가 위에 오도록 박스 안
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset {
                        val headerPx = headerHeight.roundToPx()
                        val info = listState.layoutInfo
                        val slot = info.visibleItemsInfo.firstOrNull { it.key == CHIP_SLOT_KEY }
                        val y = slot?.let { it.offset - info.viewportStartOffset } ?: headerPx
                        IntOffset(0, y.coerceAtLeast(headerPx))
                    }
                    .then(
                        if (isChipsPinned) {
                            Modifier.haloBlur(hazeState, HaloBlurs.floatingHeader(backgroundColor))
                        } else {
                            Modifier.background(backgroundColor)
                        }
                    )
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
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(hazeState, HaloBlurs.floatingHeader(backgroundColor))
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

// 위 입력칸과 칩만큼 줄여 내용이 없을 때 스크롤되지 않게
private fun Modifier.excludeHeightAbove(heightPx: Int): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, (placeable.height - heightPx).coerceAtLeast(0)) {
        placeable.place(0, 0)
    }
}
