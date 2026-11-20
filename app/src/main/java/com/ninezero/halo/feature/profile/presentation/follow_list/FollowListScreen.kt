package com.ninezero.halo.feature.profile.presentation.follow_list

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.profile.presentation.follow_list.components.FollowListPage
import com.ninezero.halo.feature.profile.presentation.follow_list.components.FollowTabRow
import com.ninezero.halo.feature.profile.presentation.follow_list.components.FollowTabSearchRow
import com.ninezero.halo.feature.profile.presentation.follow_list.components.FollowTabSpec
import dev.chrisbanes.haze.HazeState
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

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is FollowListSideEffect.NavigateBack -> onNavigateBack()
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
    val searchFocusRequester = remember { FocusRequester() }
    val closeSearch = {
        focusManager.clearFocus()
        onIntent(FollowListIntent.CloseSearch)
    }

    BackHandler(enabled = state.isSearchMode, onBack = closeSearch)

    // 화면을 떠나는 클릭은 키보드를 먼저 내리고 이동
    val navigate = { intent: FollowListIntent ->
        focusManager.clearFocus()
        onIntent(intent)
    }

    val pageCount = if (state.showSubscribers) 3 else 2
    val pagerState = rememberPagerState(
        initialPage = state.selectedTab.ordinal,
        pageCount = { pageCount }
    )

    val followersListState = rememberLazyListState()
    val followingListState = rememberLazyListState()
    val subscribersListState = rememberLazyListState()
    fun listStateOf(tab: FollowTab) = when (tab) {
        FollowTab.FOLLOWERS -> followersListState
        FollowTab.FOLLOWING -> followingListState
        FollowTab.SUBSCRIBERS -> subscribersListState
    }
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
        if (shouldLoadMoreFollowers && !state.isFollowersLoading && state.hasMoreFollowers) {
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
        if (shouldLoadMoreFollowing && !state.isFollowingLoading && state.hasMoreFollowing) {
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
        if (shouldLoadMoreSubscribers && !state.isSubscribersLoading && state.hasMoreSubscribers) {
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
                // 페이저 전에도 블러 원본이 있어야 탭이 그려짐
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator(size = LoadingIndicatorSize.Large)
                }
            }
            else -> {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !state.isSearchMode,  // 탭이 가려진 동안 다른 탭으로 넘어가지 않게
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                ) { page ->
                    val tab = FollowTab.entries[page]
                    FollowListPage(
                        hazeState = hazeState,
                        users = state.usersOf(tab),
                        isLoading = state.isLoadingOf(tab),
                        error = state.errorOf(tab),
                        errorTitle = "${tab.displayName} 목록을 불러오지 못했습니다",
                        emptyText = if (state.searchQueryOf(tab).isNotBlank()) "검색 결과가 없어요" else tab.emptyText,
                        isMyProfile = state.isMyProfile,
                        isFollowingTab = tab == FollowTab.FOLLOWING,
                        listState = listStateOf(tab),
                        headerHeight = headerHeight,
                        onRetry = { onIntent(FollowListIntent.Retry) },
                        onUserClick = { navigate(FollowListIntent.NavigateToUserProfile(it)) },
                        onFollowClick = { onIntent(FollowListIntent.ToggleFollow(it)) },
                        onMessageClick = { navigate(FollowListIntent.NavigateToMessage(it)) },
                        onUnfollowClick = { user ->
                            focusManager.clearFocus(force = true)
                            onIntent(FollowListIntent.ShowUnfollowConfirmDialog(user))
                        }
                    )
                }
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))  // 탭 캡슐이 자기 블러를 가짐
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeightPx = coordinates.size.height.toFloat()
                }
        ) {
            // 톱바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = { navigate(FollowListIntent.NavigateBack) },
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

            FollowTabSearchRow(
                isSearchMode = state.isSearchMode,
                query = state.searchQueryOf(state.selectedTab),
                placeholder = "${state.selectedTab.displayName} 검색",
                onQueryChange = { onIntent(FollowListIntent.UpdateSearchQuery(it)) },
                onSearch = {
                    focusManager.clearFocus()
                    onIntent(FollowListIntent.Search)
                },
                onToggleSearch = {
                    if (state.isSearchMode) closeSearch() else onIntent(FollowListIntent.OpenSearch)
                },
                focusRequester = searchFocusRequester,
                hazeState = hazeState,
                hazeBackgroundColor = backgroundColor
            ) {
                FollowTabRow(
                    tabs = buildList {
                        add(FollowTabSpec(FollowTab.FOLLOWERS, state.followerCount))
                        add(FollowTabSpec(FollowTab.FOLLOWING, state.followingCount))
                        if (state.showSubscribers) {
                            add(FollowTabSpec(FollowTab.SUBSCRIBERS, state.subscriberCount))
                        }
                    },
                    onTabSelected = { tab ->
                        val currentPage = pagerState.currentPage
                        val tappedSamePage = tab.ordinal == currentPage
                        if (tappedSamePage) {
                            // 같은 탭 재탭: 해당 리스트만 맨 위로
                            val listState = listStateOf(tab)
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        } else {
                            onIntent(FollowListIntent.SelectTab(tab))
                        }
                    },
                    hazeState = hazeState,
                    hazeBackgroundColor = backgroundColor,
                    scrollFraction = { pagerState.currentPage + pagerState.currentPageOffsetFraction }
                )
            }
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
