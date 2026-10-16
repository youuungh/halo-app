package com.ninezero.halo.feature.store.presentation.sales_stats

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.MonthPickerBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.layout.ListEmptyState
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.picker.MonthNavigationBar
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.toFormattedPrice
import com.ninezero.halo.feature.store.domain.model.CreatorMonthlySales
import com.ninezero.halo.feature.store.presentation.components.SalesDeltaPill
import com.ninezero.halo.feature.store.presentation.components.SalesInfoTooltip
import com.ninezero.halo.feature.store.presentation.sales_stats.components.BestSellerRow
import com.ninezero.halo.feature.store.presentation.sales_stats.components.SalesKpiData
import com.ninezero.halo.feature.store.presentation.sales_stats.components.SalesKpiGrid
import com.ninezero.halo.feature.store.presentation.sales_stats.components.SalesStatusBar
import com.ninezero.halo.feature.store.presentation.sales_stats.components.SalesTrendBars
import com.ninezero.halo.feature.store.presentation.sales_stats.components.StatSection
import com.ninezero.halo.feature.store.presentation.sales_stats.components.salesStatsSkeleton
import com.ninezero.halo.ui.theme.Error
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.util.Locale

@Composable
fun SalesStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SalesStatsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SalesStatsSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(SalesStatsIntent.SnackbarDismissed) }
    )

    SalesStatsScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

@Composable
internal fun SalesStatsScreenContent(
    state: SalesStatsState,
    onIntent: (SalesStatsIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()

    // 월 변경 시 이전 스크롤 위치가 남지 않도록 맨 위로 리셋
    LaunchedEffect(state.selectedMonth) {
        listState.scrollToItem(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(SalesStatsIntent.RefreshData) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            when {
                state.isLoading && state.monthly == null -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        salesStatsSkeleton()
                    }
                }

                // 빈 상태보다 먼저 판정, 순서 변경 금지
                state.error != null && state.monthly == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState)
                            .padding(top = headerHeight),
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
                                onClick = { onIntent(SalesStatsIntent.LoadData) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                state.monthly == null || state.monthly.grossRevenue == 0L -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState)
                            .padding(top = headerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        ListEmptyState(
                            title = "이 달의 매출이 없어요",
                            description = "상품이 판매되면 통계가 표시돼요"
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        salesStatsContent(monthly = state.monthly)
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
                    onClick = { onIntent(SalesStatsIntent.NavigateBack) },
                    icon = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "매출 통계",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 월 선택
            MonthNavigationBar(
                monthDisplay = state.selectedMonthDisplay,
                dateRange = state.selectedMonthDateRange,
                canGoPrevious = state.canGoPreviousMonth,
                canGoNext = state.canGoNextMonth,
                onPreviousMonth = { onIntent(SalesStatsIntent.GoToPreviousMonth) },
                onNextMonth = { onIntent(SalesStatsIntent.GoToNextMonth) },
                onMonthClick = { onIntent(SalesStatsIntent.ShowMonthPicker) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.showMonthPicker) {
        MonthPickerBottomSheet(
            availableMonths = state.availableMonths,
            selectedMonth = state.selectedMonth,
            onMonthSelected = { onIntent(SalesStatsIntent.SelectMonth(it)) },
            onDismiss = { onIntent(SalesStatsIntent.HideMonthPicker) }
        )
    }
}

private fun LazyListScope.salesStatsContent(
    monthly: CreatorMonthlySales
) {
    // ① Hero
    item {
        val prevRevenue =
            monthly.monthlyTrend.getOrNull(monthly.monthlyTrend.size - 2)?.revenue ?: 0L
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "총 매출",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    SalesInfoTooltip(text = "결제 완료 기준이에요. 취소·환불 금액은 빠져 있어요.")
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = monthly.grossRevenue.toFormattedPrice(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                SalesDeltaPill(
                    thisMonth = monthly.grossRevenue,
                    lastMonth = prevRevenue,
                    prefix = "지난달보다",
                    verb = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SalesTrendBars(
                trend = monthly.monthlyTrend,
                selectedMonth = monthly.month,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    // 핵심 지표
    item {
        StatSection(
            title = "핵심 지표",
            modifier = Modifier.padding(top = 28.dp)
        ) {
            SalesKpiGrid(tiles = buildKpiTiles(monthly))
        }
    }

    // 주문 상태
    item {
        val b = monthly.statusBreakdown
        val total = b.pending + b.confirmed + b.shipping + b.delivered + b.cancelled
        StatSection(
            title = "주문 현황",
            trailing = "총 ${total}건",
            modifier = Modifier.padding(top = 28.dp)
        ) {
            SalesStatusBar(breakdown = b)
        }
    }

    // 베스트셀러
    if (monthly.bestSellers.isNotEmpty()) {
        item {
            val topRevenue = monthly.bestSellers.first().revenue
            StatSection(
                title = "베스트셀러",
                modifier = Modifier.padding(top = 28.dp)
            ) {
                monthly.bestSellers.forEachIndexed { index, item ->
                    BestSellerRow(rank = index + 1, item = item, topRevenue = topRevenue)
                    if (index < monthly.bestSellers.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun buildKpiTiles(monthly: CreatorMonthlySales): List<SalesKpiData> {
    val gross = monthly.grossRevenue
    val netPct = if (gross > 0L) (monthly.netRevenue * 100 / gross).toInt() else 0
    val refundRate = if (gross > 0L) monthly.refundAmount * 100.0 / gross else 0.0
    val cancelled = monthly.statusBreakdown.cancelled

    return listOf(
        SalesKpiData(
            label = "순매출",
            value = monthly.netRevenue.toFormattedPrice(),
            sub = "총매출의 ${netPct}%"
        ),
        SalesKpiData(
            label = "주문 건수",
            value = "${monthly.orderCount}건",
            sub = if (cancelled > 0) "취소 ${cancelled}건" else null
        ),
        SalesKpiData(
            label = "평균 주문가",
            value = monthly.avgOrderValue.toFormattedPrice()
        ),
        SalesKpiData(
            label = "환불",
            value = monthly.refundAmount.toFormattedPrice(),
            sub = if (monthly.refundAmount > 0L) {
                String.format(Locale.KOREA, "환불율 %.1f%%", refundRate)
            } else {
                null
            },
            tint = if (monthly.refundAmount > 0L) Error else null
        )
    )
}

