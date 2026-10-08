package com.ninezero.halo.feature.profile.presentation.follow_list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.profile.presentation.follow_list.components.FollowListPage
import com.ninezero.halo.feature.profile.presentation.follow_list.components.FollowTabRow
import com.ninezero.halo.feature.profile.presentation.follow_list.components.FollowTabSpec
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FollowListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Int) -> Unit,
    onNavigateToNewChat: (targetUserId: Int, username: String, displayName: String, avatarUrl: String, role: String) -> Unit,
    viewModel: FollowListViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FollowListSideEffect.NavigateBack -> {
                // 키보드 먼저 닫고 navigation
                keyboardController?.hide()
                onNavigateBack()
            }
            is FollowListSideEffect.NavigateToUserProfile -> onNavigateToUserProfile(sideEffect.userId)
            is FollowListSideEffect.NavigateToNewChat -> onNavigateToNewChat(
                sideEffect.targetUserId, sideEffect.username, sideEffect.displayName, sideEffect.avatarUrl, sideEffect.role
            )
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(FollowListIntent.SnackbarDismissed) }
    )

    FollowListScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun FollowListScreenContent(
    state: FollowListState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (FollowListIntent) -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    val density = LocalDensity.current
    // contentPadding용 px saveable 보존
    var headerHeightPx by rememberSaveable { mutableFloatStateOf(0f) }
    val headerHeight = with(density) { headerHeightPx.toDp() }
    val focusManager = LocalFocusManager.current

    val pageCount = if (state.showSubscribers) 3 else 2
    val pagerState = rememberPagerState(
        initialPage = state.selectedTab.ordinal,
        pageCount = { pageCount }
    )

    val followersListState = rememberLazyListState()
    val followingListState = rememberLazyListState()
    val subscribersListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 탭 선택 시 페이저 동기화
    val targetPage = state.selectedTab.ordinal
    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // 탭 전환 시 키보드·포커스 해제
    LaunchedEffect(pagerState.currentPage) {
        focusManager.clearFocus()
    }

    // 정착했을 때만 탭 동기화
    // currentPage 기준은 피드백 루프, 금지
    LaunchedEffect(pagerState.settledPage) {
        val newTab = when (pagerState.settledPage) {
            1 -> FollowTab.FOLLOWING
            2 -> FollowTab.SUBSCRIBERS
            else -> FollowTab.FOLLOWERS
        }
        if (state.selectedTab != newTab) {
            onIntent(FollowListIntent.SelectTab(newTab))
        }
    }

    // 팔로워 무한 스크롤
    val shouldLoadMoreFollowers by remember {
        derivedStateOf {
            val lastVisibleItem = followersListState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = followersListState.layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMoreFollowers) {
        if (shouldLoadMoreFollowers && !state.isFollowersLoading && state.followersHasMore) {
            onIntent(FollowListIntent.LoadMoreFollowers)
        }
    }

    // 팔로잉 무한 스크롤
    val shouldLoadMoreFollowing by remember {
        derivedStateOf {
            val lastVisibleItem = followingListState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = followingListState.layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMoreFollowing) {
        if (shouldLoadMoreFollowing && !state.isFollowingLoading && state.followingHasMore) {
            onIntent(FollowListIntent.LoadMoreFollowing)
        }
    }

    // 구독자 무한 스크롤
    val shouldLoadMoreSubscribers by remember {
        derivedStateOf {
            val lastVisibleItem = subscribersListState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = subscribersListState.layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMoreSubscribers) {
        if (shouldLoadMoreSubscribers && !state.isSubscribersLoading && state.subscribersHasMore) {
            onIntent(FollowListIntent.LoadMoreSubscribers)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 콘텐츠 - HorizontalPager
        when {
            state.isInitialLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(size = LoadingIndicatorSize.ExtraLarge)
                }
            }
            else -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                ) { page ->
                    when (page) {
                        0 -> FollowListPage(
                            users = state.followers,
                            isLoading = state.isFollowersLoading,
                            error = state.followersError,
                            emptyText = if (state.followersSearchQuery.isNotBlank()) "검색 결과가 없습니다" else "팔로워가 없습니다",
                            isMyProfile = state.isMyProfile,
                            isFollowingTab = false,
                            toggleLoadingUserIds = state.toggleLoadingUserIds,
                            listState = followersListState,
                            headerHeight = headerHeight,
                            searchQuery = state.followersSearchQuery,
                            onSearchQueryChange = { onIntent(FollowListIntent.UpdateSearchQuery(it)) },
                            onSearch = {
                                focusManager.clearFocus()
                                onIntent(FollowListIntent.Search)
                            },
                            onClearSearch = { onIntent(FollowListIntent.ClearSearch) },
                            onRetry = { onIntent(FollowListIntent.Retry) },
                            onUserClick = { onIntent(FollowListIntent.NavigateToUserProfile(it)) },
                            onFollowClick = { onIntent(FollowListIntent.ToggleFollow(it)) },
                            onMessageClick = { onIntent(FollowListIntent.NavigateToMessage(it)) },
                            onUnfollowClick = { user -> onIntent(FollowListIntent.ShowUnfollowConfirmDialog(user)) }
                        )
                        1 -> FollowListPage(
                            users = state.following,
                            isLoading = state.isFollowingLoading,
                            error = state.followingError,
                            emptyText = if (state.followingSearchQuery.isNotBlank()) "검색 결과가 없습니다" else "팔로잉이 없습니다",
                            isMyProfile = state.isMyProfile,
                            isFollowingTab = true,
                            toggleLoadingUserIds = state.toggleLoadingUserIds,
                            listState = followingListState,
                            headerHeight = headerHeight,
                            searchQuery = state.followingSearchQuery,
                            onSearchQueryChange = { onIntent(FollowListIntent.UpdateSearchQuery(it)) },
                            onSearch = {
                                focusManager.clearFocus()
                                onIntent(FollowListIntent.Search)
                            },
                            onClearSearch = { onIntent(FollowListIntent.ClearSearch) },
                            onRetry = { onIntent(FollowListIntent.Retry) },
                            onUserClick = { onIntent(FollowListIntent.NavigateToUserProfile(it)) },
                            onFollowClick = { onIntent(FollowListIntent.ToggleFollow(it)) },
                            onMessageClick = { onIntent(FollowListIntent.NavigateToMessage(it)) },
                            onUnfollowClick = { user -> onIntent(FollowListIntent.ShowUnfollowConfirmDialog(user)) }
                        )
                        2 -> FollowListPage(
                            users = state.subscribers,
                            isLoading = state.isSubscribersLoading,
                            error = state.subscribersError,
                            emptyText = if (state.subscribersSearchQuery.isNotBlank()) "검색 결과가 없습니다" else "구독자가 없습니다",
                            isMyProfile = state.isMyProfile,
                            isFollowingTab = false,
                            toggleLoadingUserIds = state.toggleLoadingUserIds,
                            listState = subscribersListState,
                            headerHeight = headerHeight,
                            searchQuery = state.subscribersSearchQuery,
                            onSearchQueryChange = { onIntent(FollowListIntent.UpdateSearchQuery(it)) },
                            onSearch = {
                                focusManager.clearFocus()
                                onIntent(FollowListIntent.Search)
                            },
                            onClearSearch = { onIntent(FollowListIntent.ClearSearch) },
                            onRetry = { onIntent(FollowListIntent.Retry) },
                            onUserClick = { onIntent(FollowListIntent.NavigateToUserProfile(it)) },
                            onFollowClick = { onIntent(FollowListIntent.ToggleFollow(it)) },
                            onMessageClick = { onIntent(FollowListIntent.NavigateToMessage(it)) },
                            onUnfollowClick = { user -> onIntent(FollowListIntent.ShowUnfollowConfirmDialog(user)) }
                        )
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
                        tint = HazeTint(backgroundColor.copy(alpha = 0.85f)),
                        blurRadius = 25.dp,
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeightPx = coordinates.size.height.toFloat()
                }
        ) {
            // TopAppBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = { onIntent(FollowListIntent.NavigateBack) },
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = state.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            FollowTabRow(
                tabs = buildList {
                    add(FollowTabSpec(FollowTab.FOLLOWERS, "팔로워", state.followerCount))
                    add(FollowTabSpec(FollowTab.FOLLOWING, "팔로잉", state.followingCount))
                    if (state.showSubscribers) {
                        add(FollowTabSpec(FollowTab.SUBSCRIBERS, "구독자", state.subscriberCount))
                    }
                },
                onTabSelected = { tab ->
                    val currentPage = pagerState.currentPage
                    val tappedSamePage = tab.ordinal == currentPage
                    if (tappedSamePage) {
                        // 같은 탭 재탭: 해당 리스트만 맨 위로
                        val listState = when (currentPage) {
                            1 -> followingListState
                            2 -> subscribersListState
                            else -> followersListState
                        }
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    } else {
                        onIntent(FollowListIntent.SelectTab(tab))
                    }
                },
                scrollFraction = pagerState.currentPage + pagerState.currentPageOffsetFraction
            )
        }

        CenterToast(
            visible = state.toastMessage != null,
            message = state.toastMessage ?: "",
            onDismiss = { onIntent(FollowListIntent.DismissToast) }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.showUnfollowConfirmDialog && state.unfollowTargetUser != null) {
        val displayName = state.unfollowTargetUser.displayName ?: state.unfollowTargetUser.username
        ConfirmDialog(
            profileImageUrl = state.unfollowTargetUser.avatarUrl,
            annotatedMessage = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(displayName)
                }
                append("님을 팔로우 취소하시겠어요?")
            },
            confirmText = "팔로우 취소",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(FollowListIntent.ConfirmUnfollow) },
            onDismiss = { onIntent(FollowListIntent.DismissUnfollowConfirmDialog) }
        )
    }
}

