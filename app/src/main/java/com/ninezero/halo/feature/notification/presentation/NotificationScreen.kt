package com.ninezero.halo.feature.notification.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.notification.domain.model.Notification
import com.ninezero.halo.feature.notification.presentation.components.NotificationItem
import com.ninezero.halo.feature.notification.presentation.components.NotificationItemSkeleton
import com.ninezero.halo.feature.notification.presentation.components.NotificationSectionHeader
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPostDetail: (postId: Int, highlightCommentId: Int?) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToOrderDetail: (Int) -> Unit,
    onNavigateToChatRoom: (Int) -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToCommentDetail: (commentId: Int, highlightReplyId: Int?) -> Unit = { _, _ -> },
    onNavigateToPoint: () -> Unit = {},
    onNavigateToCreatorApplication: () -> Unit = {},
    onNavigateToMySubscriptions: () -> Unit = {},
    onNavigateToSalesManage: () -> Unit = {},
    onNavigateToReviewList: (productId: Int, reviewId: Int?) -> Unit = { _, _ -> },
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    // 화면 복귀 시 서버 상태로 조용히 재조회
    var hasInitialResumeHandled by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (hasInitialResumeHandled) {
            viewModel.onIntent(NotificationIntent.RefreshOnResume)
        } else {
            hasInitialResumeHandled = true
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is NotificationSideEffect.NavigateBack -> onNavigateBack()
            is NotificationSideEffect.NavigateToPost -> onNavigateToPostDetail(sideEffect.postId, sideEffect.highlightCommentId)
            is NotificationSideEffect.NavigateToComment -> onNavigateToCommentDetail(sideEffect.commentId, sideEffect.highlightReplyId)
            is NotificationSideEffect.NavigateToUser -> onNavigateToProfile(sideEffect.userId)
            is NotificationSideEffect.NavigateToOrder -> onNavigateToOrderDetail(sideEffect.orderId)
            is NotificationSideEffect.NavigateToChatRoom -> onNavigateToChatRoom(sideEffect.roomId)
            is NotificationSideEffect.NavigateToProduct -> onNavigateToProductDetail(sideEffect.productId)
            is NotificationSideEffect.NavigateToPoint -> onNavigateToPoint()
            is NotificationSideEffect.NavigateToCreatorApplication -> onNavigateToCreatorApplication()
            is NotificationSideEffect.NavigateToMySubscriptions -> onNavigateToMySubscriptions()
            is NotificationSideEffect.NavigateToSalesManage -> onNavigateToSalesManage()
            is NotificationSideEffect.NavigateToReviewList -> onNavigateToReviewList(sideEffect.productId, sideEffect.reviewId)
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onAction = { viewModel.onIntent(NotificationIntent.UndoDelete) },
        onDismiss = { viewModel.onIntent(NotificationIntent.SnackbarDismissed) }
    )

    NotificationScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun NotificationScreenContent(
    state: NotificationState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (NotificationIntent) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.background
    var swipedOpenId by remember { mutableIntStateOf(-1) }
    var headerHeight by rememberSaveableDp()

    // 스크롤 시 열린 스와이프 닫기
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && swipedOpenId != -1) {
            swipedOpenId = -1
        }
    }

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(NotificationIntent.LoadMore) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(NotificationIntent.RefreshNotifications) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            when {
                // 초기 로딩 스켈레톤
                state.isLoading && state.notifications.isEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = headerHeight)
                    ) {
                        items(8) {
                            NotificationItemSkeleton()
                        }
                    }
                }

                // 에러 상태
                state.error != null && state.notifications.isEmpty() -> {
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
                                onClick = { onIntent(NotificationIntent.LoadNotifications) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                // 정상 목록
                else -> {
                    val grouped = remember(state.notifications) {
                        groupNotificationsBySection(state.notifications)
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState),
                        contentPadding = PaddingValues(top = headerHeight, bottom = 16.dp)
                    ) {
                        if (state.notifications.isEmpty() && !state.isLoading) {
                            item(key = "empty") {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "아직 알림이 없어요",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            grouped.forEach { (section, notifications) ->
                                item(key = "section_$section", contentType = "section_header") {
                                    NotificationSectionHeader(
                                        title = section,
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                items(
                                    items = notifications,
                                    key = { it.id },
                                    contentType = { "notification" }
                                ) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onClick = {
                                            swipedOpenId = -1
                                            onIntent(NotificationIntent.NavigateToTarget(notification))
                                        },
                                        onDelete = {
                                            swipedOpenId = -1
                                            onIntent(NotificationIntent.DeleteNotification(notification.id))
                                        },
                                        onProfileClick = {
                                            swipedOpenId = -1
                                            notification.senderId?.let { senderId ->
                                                // 아바타 탭은 프로필로, deepLink 비움
                                                onIntent(NotificationIntent.NavigateToTarget(
                                                    notification.copy(targetType = "USER", targetId = senderId, deepLink = null)
                                                ))
                                            }
                                        },
                                        isSwipeOpen = swipedOpenId == notification.id,
                                        onSwipeOpen = { swipedOpenId = notification.id },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }

                            // 페이지네이션 로딩
                            if (state.isLoading && state.notifications.isNotEmpty()) {
                                items(3) {
                                    NotificationItemSkeleton()
                                }
                            }
                        }
                    }
                }
            }
        }

        HazeScreenHeader(
            title = "알림",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(NotificationIntent.NavigateBack) },
            onHeightMeasured = { headerHeight = it }
        )


        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        CenterToast(
            visible = state.toastMessage != null,
            message = state.toastMessage ?: "",
            onDismiss = { onIntent(NotificationIntent.DismissToast) }
        )
    }
}

private fun groupNotificationsBySection(
    notifications: List<Notification>
): List<Pair<String, List<Notification>>> {
    // 반드시 Long, Int 곱은 오버플로
    val dayMillis = 86_400_000L
    val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val yesterdayStart = todayStart - dayMillis
    val weekStart = todayStart - 6 * dayMillis
    val monthStart = todayStart - 29 * dayMillis

    val sections = linkedMapOf<String, MutableList<Notification>>(
        "오늘" to mutableListOf(),
        "어제" to mutableListOf(),
        "최근 7일" to mutableListOf(),
        "최근 30일" to mutableListOf(),
        "이전 활동" to mutableListOf()
    )

    notifications.forEach { notification ->
        val key = when {
            notification.createdAt >= todayStart -> "오늘"
            notification.createdAt >= yesterdayStart -> "어제"
            notification.createdAt >= weekStart -> "최근 7일"
            notification.createdAt >= monthStart -> "최근 30일"
            else -> "이전 활동"
        }
        sections[key]?.add(notification)
    }

    return sections.filter { it.value.isNotEmpty() }.map { it.key to it.value }
}
