package com.ninezero.halo.feature.store.presentation.review_list

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.core.ui.components.bottomsheet.EditDeleteOptionBottomSheet
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.feature.store.domain.model.Review
import com.ninezero.halo.feature.store.presentation.review_list.components.ReviewItem
import com.ninezero.halo.feature.store.presentation.review_list.components.ReviewItemSkeleton
import com.ninezero.halo.feature.store.presentation.review_list.components.ReviewSortPopupMenu
import com.ninezero.halo.feature.store.presentation.review_list.components.ReviewSummaryCard
import com.ninezero.halo.feature.store.presentation.review_list.components.ReviewSummarySkeleton
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ReviewListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditReview: (productId: Int, orderId: Int, productName: String, productImageUrl: String?, reviewId: Int, rating: Int, content: String) -> Unit = { _, _, _, _, _, _, _ -> },
    viewModel: ReviewListViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    // 리뷰 작성/수정 화면에서 돌아왔을 때 목록을 다시 읽음
    // 최초 진입 스킵, 가드는 saveable
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(ReviewListIntent.ScreenResumed)
        } else {
            hasInitialResumeHandled = true
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(ReviewListIntent.SnackbarDismissed) }
    )

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ReviewListSideEffect.NavigateBack -> onNavigateBack()
            is ReviewListSideEffect.NavigateToEditReview -> onNavigateToEditReview(
                sideEffect.productId, sideEffect.orderId, sideEffect.productName,
                sideEffect.productImageUrl, sideEffect.reviewId, sideEffect.rating, sideEffect.content
            )
        }
    }

    ReviewListScreenContent(
        state = state,
        onSortChange = { viewModel.onIntent(ReviewListIntent.ChangeSortBy(it)) },
        onLoadMore = { viewModel.onIntent(ReviewListIntent.LoadMore) },
        onRefresh = { viewModel.onIntent(ReviewListIntent.Refresh) },
        onRetry = { viewModel.onIntent(ReviewListIntent.LoadReviews) },
        onNavigateBack = { viewModel.onIntent(ReviewListIntent.NavigateBack) },
        onMoreClick = { viewModel.onIntent(ReviewListIntent.ShowOptions(it)) },
        onDismissOptions = { viewModel.onIntent(ReviewListIntent.DismissOptions) },
        onEditClick = {
            state.selectedReview?.let { review ->
                viewModel.onIntent(ReviewListIntent.EditReview(review))
            }
        },
        onDeleteClick = { viewModel.onIntent(ReviewListIntent.ShowDeleteConfirm) },
        onConfirmDelete = { viewModel.onIntent(ReviewListIntent.ConfirmDelete) },
        onDismissDeleteConfirm = { viewModel.onIntent(ReviewListIntent.DismissDeleteConfirm) },
        snackbarHostState = snackbarHostState
    )
}

@Composable
internal fun ReviewListScreenContent(
    state: ReviewListState,
    onSortChange: (ReviewSortType) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onMoreClick: (Review) -> Unit = {},
    onDismissOptions: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onDismissDeleteConfirm: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    var headerHeight by rememberSaveableDp()
    val viewerHostState = rememberViewerOverlayHostState()

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoadingMore,
        hasMore = state.hasMore,
        onLoadMore = onLoadMore
    )

    var reviewHighlightPlayed by rememberSaveable { mutableStateOf(false) }
    val reviewHighlight = remember { Animatable(0f) }
    val highlightTargetId = state.highlightReviewId
    var reviewHighlightItemIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.reviews, headerHeight) {
        if (highlightTargetId != null && !reviewHighlightPlayed && headerHeight > 0.dp) {
            val index = state.reviews.indexOfFirst { it.id == highlightTargetId }
            if (index >= 0) {
                reviewHighlightPlayed = true
                reviewHighlightItemIndex = 4 + index  // 고정 아이템 4개
            }
        }
    }
    LaunchedEffect(reviewHighlightItemIndex) {
        val itemIndex = reviewHighlightItemIndex ?: return@LaunchedEffect
        listState.scrollToItem(itemIndex)
        reviewHighlight.snapTo(1f)
        delay(400)
        reviewHighlight.animateTo(0f, animationSpec = tween(durationMillis = 1200))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val isInitialLoading = state.isLoading && state.reviews.isEmpty()

        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            when {
                // 초기 로딩 스켈레톤
                isInitialLoading -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 32.dp)
                    ) {
                        item { ReviewSummarySkeleton() }
                        items(5) { ReviewItemSkeleton() }
                    }
                }

                // 에러 상태
                state.error != null && state.reviews.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                                onClick = onRetry,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
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
                        if (state.reviewSummary != null) {
                            item(key = "summary") {
                                ReviewSummaryCard(summary = state.reviewSummary)
                            }
                        }

                        item(key = "sort_divider_top") {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }

                        item(key = "sort") {
                            ReviewSortPopupMenu(
                                currentSort = state.sortBy,
                                onSortChange = onSortChange
                            )
                        }

                        item(key = "sort_divider_bottom") {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }

                        items(
                            items = state.reviews,
                            key = { "review_${it.id}" }
                        ) { review ->
                            // 알림 진입 하이라이트 배경
                            val highlightColor = MaterialTheme.colorScheme.primary
                            Column(
                                modifier = if (review.id == highlightTargetId) {
                                    Modifier.drawBehind {
                                        val alpha = reviewHighlight.value * 0.04f
                                        if (alpha > 0f) drawRect(highlightColor.copy(alpha = alpha))
                                    }
                                } else {
                                    Modifier
                                }
                            ) {
                                ReviewItem(
                                    review = review,
                                    isMyReview = state.currentUserId != null && review.user.id == state.currentUserId,
                                    onMoreClick = { onMoreClick(review) },
                                    onImageClick = { images, index ->
                                        viewerHostState.showImageViewer(images, index)
                                    }
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // 추가 로딩 스켈레톤
                        if (state.isLoadingMore) {
                            items(2, key = { "loading_more_$it" }) {
                                ReviewItemSkeleton()
                            }
                        }

                        // 빈 상태
                        if (state.reviews.isEmpty() && !state.isLoading) {
                            item(key = "empty") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "아직 리뷰가 없어요",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HazeScreenHeader(
            title = "리뷰",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.showOptionSheet) {
        EditDeleteOptionBottomSheet(
            onDismiss = onDismissOptions,
            onEdit = onEditClick,
            onDelete = onDeleteClick
        )
    }

    if (state.showDeleteConfirm) {
        ConfirmDialog(
            title = "리뷰를 삭제하시겠어요?",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDeleteConfirm
        )
    }

    ViewerOverlayHost(state = viewerHostState)
}

