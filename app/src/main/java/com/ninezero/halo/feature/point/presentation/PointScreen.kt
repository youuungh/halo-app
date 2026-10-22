package com.ninezero.halo.feature.point.presentation

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.MonthPickerBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.picker.MonthNavigationBar
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.point.presentation.components.PointBalanceCard
import com.ninezero.halo.feature.point.presentation.components.PointBalanceCardSkeleton
import com.ninezero.halo.feature.point.presentation.components.PointFilterChips
import com.ninezero.halo.feature.point.presentation.components.PointHistoryItem
import com.ninezero.halo.feature.point.presentation.components.PointHistoryItemSkeleton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PointScreen(
    onNavigateBack: () -> Unit,
    viewModel: PointViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PointSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(PointIntent.SnackbarDismissed) }
    )

    PointScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )
}

private const val CHIP_SLOT_KEY = "point_filter_chips"

@Composable
internal fun PointScreenContent(
    state: PointState,
    onIntent: (PointIntent) -> Unit,
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
        onLoadMore = { onIntent(PointIntent.LoadMoreHistory) }
    )

    var chipsHeight by rememberSaveableDp()
    val isInitialLoading = state.isLoading && state.balance == null && state.histories.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(PointIntent.RefreshData) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            // 본문
            when {
                isInitialLoading -> {
                    // 초기 로딩 스켈레톤
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(
                            top = headerHeight,
                            bottom = 16.dp
                        )
                    ) {
                        item { PointBalanceCardSkeleton() }
                        items(8) {
                            PointHistoryItemSkeleton()
                        }
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
                            bottom = 16.dp
                        )
                    ) {
                        item {
                            state.balance?.let { balance ->
                                PointBalanceCard(balance = balance)
                            }
                        }

                        item {
                            MonthNavigationBar(
                                monthDisplay = state.selectedMonthDisplay,
                                dateRange = state.selectedMonthDateRange,
                                canGoPrevious = state.canGoPreviousMonth,
                                canGoNext = state.canGoNextMonth,
                                onPreviousMonth = { onIntent(PointIntent.GoToPreviousMonth) },
                                onNextMonth = { onIntent(PointIntent.GoToNextMonth) },
                                onMonthClick = { onIntent(PointIntent.ShowMonthPicker) }
                            )
                        }

                        // 필터 칩 자리
                        item(key = CHIP_SLOT_KEY) {
                            Spacer(modifier = Modifier.height(chipsHeight))
                        }

                        // 필터 변경 시 로딩
                        if (state.isLoading && state.histories.isEmpty()) {
                            items(5) {
                                PointHistoryItemSkeleton()
                            }
                        }

                        // 포인트 내역 리스트
                        if (state.error != null && state.histories.isEmpty() && !state.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
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
                                            onClick = { onIntent(PointIntent.RefreshData) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.bounceClick()
                                        ) {
                                            Text("재시도")
                                        }
                                    }
                                }
                            }
                        } else if (state.histories.isEmpty() && !state.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${state.selectedMonthDisplay} 포인트 내역이 없습니다",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(
                                items = state.histories,
                                key = { _, item -> item.id }
                            ) { index, history ->
                                PointHistoryItem(history = history)
                                if (index < state.histories.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            // 추가 로딩 스켈레톤
                            if (state.isLoading && state.histories.isNotEmpty()) {
                                items(3) {
                                    PointHistoryItemSkeleton()
                                }
                            }
                        }
                    }
                }
            }

            // 필터 칩, 플로팅. 새로고침 표시가 위에 오도록 박스 안
            if (!isInitialLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            val headerPx = headerHeight.roundToPx()
                            val info = listState.layoutInfo
                            val slot = info.visibleItemsInfo.firstOrNull { it.key == CHIP_SLOT_KEY }
                            // 슬롯이 안 보임 = 스크롤로 지나감 -> 톱바 아래 고정
                            val y = slot?.let { it.offset - info.viewportStartOffset } ?: headerPx
                            IntOffset(0, y.coerceAtLeast(headerPx))
                        }
                        .haloBlur(state = hazeState) {
                            style = HaloBlurStyle(
                                backgroundColor = backgroundColor,
                                tint = HaloBlurTint(backgroundColor.copy(alpha = 0.4f)),
                                blurRadius = 20.dp,
                                noiseFactor = 0f
                            )
                        }
                        .onGloballyPositioned { coordinates ->
                            chipsHeight = with(density) { coordinates.size.height.toDp() }
                        }
                ) {
                    PointFilterChips(
                        selectedType = state.selectedType,
                        onFilterByType = { onIntent(PointIntent.FilterByType(it)) },
                        hazeState = hazeState
                    )
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
                        tint = HaloBlurTint(backgroundColor.copy(alpha = 0.4f)),
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
                    onClick = { onIntent(PointIntent.NavigateBack) },
                    icon = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = "뒤로가기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Text(
                    text = "포인트",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

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
            onMonthSelected = { onIntent(PointIntent.SelectMonth(it)) },
            onDismiss = { onIntent(PointIntent.HideMonthPicker) }
        )
    }
}

