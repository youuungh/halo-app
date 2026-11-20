package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Size
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.layout.ResetListsWhenHeaderShows
import com.ninezero.halo.core.ui.components.layout.clipAboveCollapsingTab
import com.ninezero.halo.core.ui.components.layout.collapsingGlassTopBarHeight
import com.ninezero.halo.core.ui.components.layout.markGestureStarts
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalSubscribeRequester
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.theme.FallbackCoverGray
import com.ninezero.halo.core.util.launchCustomTab
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.ProfileHeaderScroll
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRow
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.components.rememberCollapsibleHeaderState
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileIntent
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val COVER_IMAGE_SCALE = 1.8f

// 톱바 안 이름 줄의 위쪽 여백
private val CollapsedTitleTop = 12.dp

// 이름이 안착하기 직전부터 톱바 블러가 깔리는 거리
private val TopBarRevealDistance = 56.dp

// 버튼 색이 톱바 뒤로 비치지 않게 다 접히기 전에 헤더를 지움
private val HeaderFadeDistance = 96.dp

// 블러 끝을 탭 뒤로 숨겨 지나가는 카드 끝이 안 보이게 함
private val TopBarBlurExtra = 24.dp

// 커버가 녹기 시작하는 지점을 마지막 카드 안쪽까지 올린 거리
private val CoverFadeCardOverlap = 50.dp  // 위치 줄 위부터 녹음

// 카드 위에선 옅게 깔리다가 카드 밑에서 진해지는 곡선
private val CoverFadeEasing = CubicBezierEasing(0.45f, 0f, 0.75f, 0.85f)


@Composable
fun CollapsingToolbarLayout(
    state: UserProfileState,
    pagerState: PagerState,
    hazeState: HazeState,
    homeListState: LazyListState,
    postsListState: LazyListState,
    repliesListState: LazyListState,
    communityListState: LazyListState,
    storeListState: LazyListState,
    tabs: List<ProfileTab>,
    profilePopupState: ProfilePopupHostState,
    onShowMediaViewer: (List<ViewerMediaItem>, Int, String?) -> Unit,
    onShowProfileImageViewer: (String?) -> Unit,
    onCreateCommunityPost: () -> Unit,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onSubscriberClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onIntent: (UserProfileIntent) -> Unit
) {
    val profile = state.profile ?: return

    val backgroundColor = MaterialTheme.colorScheme.background
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val headerState = rememberCollapsibleHeaderState()

    val topBarHeight = collapsingGlassTopBarHeight()
    val statusBarHeight =
        WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
    val titleExpandedTop = topBarHeight + ProfileHeaderTitleOffset
    val titleCollapsedTop = statusBarHeight + CollapsedTitleTop
    val topBarHeightPx = with(density) { topBarHeight.toPx() }
    val titleTravelPx = with(density) { (titleExpandedTop - titleCollapsedTop).toPx() }
    val topBarRevealPx = with(density) { TopBarRevealDistance.toPx() }
    val headerFadePx = with(density) { HeaderFadeDistance.toPx() }

    val maxCollapse = remember(headerState, topBarHeightPx) {
        { (headerState.headerHeight - topBarHeightPx).coerceAtLeast(0f) }
    }
    val headerAlpha = remember(headerState, maxCollapse, headerFadePx) {
        { ((maxCollapse() + headerState.scrollOffset) / headerFadePx).coerceIn(0f, 1f) }
    }
    // 헤더 높이를 재기 전엔 가림
    val belowHeaderAlpha = remember(headerState) {
        { if (headerState.headerHeight > 0f) 1f else 0f }
    }
    val titleProgress = remember(headerState, titleTravelPx) {
        { (-headerState.scrollOffset / titleTravelPx).coerceIn(0f, 1f) }
    }
    val topBarReveal = remember(headerState, titleTravelPx, topBarRevealPx) {
        {
            ((-headerState.scrollOffset - titleTravelPx + topBarRevealPx) / topBarRevealPx).coerceIn(
                0f,
                1f
            )
        }
    }
    val isCollapsed by remember(headerState, maxCollapse) {
        derivedStateOf { headerState.headerHeight > 0f && headerState.scrollOffset <= -maxCollapse() }
    }
    val showTopBarBlur by remember(topBarReveal) { derivedStateOf { topBarReveal() > 0f } }
    val isPullToRefreshEnabled by remember(headerState) { derivedStateOf { headerState.isFullyExpanded } }

    fun currentListState(): LazyListState = when (tabs.getOrNull(pagerState.currentPage)) {
        ProfileTab.HOME -> homeListState
        ProfileTab.REPLIES -> repliesListState
        ProfileTab.COMMUNITY -> communityListState
        ProfileTab.STORE -> storeListState
        ProfileTab.POSTS, null -> postsListState
    }

    val headerScroll = remember(headerState, tabs, maxCollapse) {
        ProfileHeaderScroll(
            headerState = headerState,
            maxCollapse = maxCollapse,
            isListAtTop = {
                val listState = currentListState()
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            }
        )
    }
    val headerScrollableState = rememberScrollableState { delta -> headerScroll.consume(delta) }

    ResetListsWhenHeaderShows(
        lists = listOf(
            homeListState,
            postsListState,
            repliesListState,
            communityListState,
            storeListState
        ),
        activeList = { currentListState() },
        isHeaderVisible = { headerState.headerHeight > 0f && !isCollapsed }
    )

    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) {
            pullToRefreshState.animateToHidden()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(UserProfileIntent.RefreshProfile) },
                enabled = isPullToRefreshEnabled
            )
            .markGestureStarts(headerScroll.gestureStart)
            .nestedScroll(headerScroll)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        val y = headerState.headerHeight + headerState.scrollOffset - topBarHeightPx
                        IntOffset(0, y.roundToInt().coerceAtLeast(0))
                    }
                    .graphicsLayer { alpha = belowHeaderAlpha() }
                    .clipAboveCollapsingTab(topBarHeight + ProfileTabRowHeight) { if (isCollapsed) 1f else 0f }
            ) { page ->
                when (tabs.getOrElse(page) { tabs.first() }) {
                    ProfileTab.POSTS -> UserPostsTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = postsListState,
                        onShowMediaViewer = { items, mediaIndex ->
                            onShowMediaViewer(
                                items,
                                mediaIndex,
                                null
                            )
                        },
                        onProfileClick = { author, offset ->
                            if (profile.userId != author.id) {
                                profilePopupState.show(author.id, offset)
                            }
                        },
                        onIntent = onIntent
                    )

                    ProfileTab.REPLIES -> UserRepliesTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = repliesListState,
                        onShowMediaViewer = onShowMediaViewer,
                        onProfileClick = { author, offset ->
                            if (profile.userId != author.id) {
                                profilePopupState.show(author.id, offset)
                            }
                        },
                        onIntent = onIntent
                    )
                    // Creator 전용 탭
                    ProfileTab.HOME -> CreatorHomeTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = homeListState,
                        onPostClick = { post ->
                            onIntent(UserProfileIntent.NavigateToPostDetail(post.id))
                        },
                        onProductClick = { product ->
                            if (product.canAccess == false) {
                                onIntent(
                                    UserProfileIntent.ShowRestrictedContentSnackbar(
                                        "${product.requiredTier.label} 멤버십 전용 상품입니다"
                                    )
                                )
                            } else {
                                onIntent(UserProfileIntent.NavigateToProductDetail(product.id))
                            }
                        },
                        onViewAllPostSectionClick = { section ->
                            onIntent(
                                UserProfileIntent.NavigateToSectionDetail(
                                    sectionType = SectionType.POST_TAG,
                                    tagId = section.tag.id,
                                    title = section.tag.name
                                )
                            )
                        },
                        onViewAllProductSectionClick = { section ->
                            onIntent(
                                UserProfileIntent.NavigateToSectionDetail(
                                    sectionType = SectionType.PRODUCT_TAG,
                                    tagId = section.tag.id,
                                    title = section.tag.name
                                )
                            )
                        }
                    )

                    ProfileTab.COMMUNITY -> UserCommunityTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = communityListState,
                        onIntent = onIntent,
                        onPostClick = { postId ->
                            onIntent(UserProfileIntent.NavigateToPostDetail(postId))
                        },
                        onProfileClick = { authorId, offset ->
                            if (profile.userId != authorId) {
                                profilePopupState.show(authorId, offset)
                            }
                        },
                        onShowMediaViewer = { items, index ->
                            onShowMediaViewer(items, index, null)
                        }
                    )

                    ProfileTab.STORE -> CreatorStoreTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = storeListState,
                        onIntent = onIntent,
                        onProductClick = { productId ->
                            onIntent(UserProfileIntent.NavigateToProductDetail(productId))
                        }
                    )
                }
            }

            // 프로필 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // 소수 위치면 덮개 끝이 선으로 비침
                        translationY = headerState.scrollOffset.roundToInt().toFloat()
                        alpha = headerAlpha()
                    }
                    .onGloballyPositioned { coordinates ->
                        headerState.headerHeight = coordinates.size.height.toFloat()
                    }
                    .scrollable(
                        state = headerScrollableState,
                        orientation = Orientation.Vertical
                    )
            ) {
                ProfileCover(
                    avatarUrl = profile.avatarUrl,
                    avatarThumbUrl = profile.avatarThumbUrl,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.matchParentSize()
                )

                // 멤버십 시트는 전역 호스트
                val subscribeRequester = LocalSubscribeRequester.current
                UserProfileHeaderContent(
                    profile = profile,
                    topPadding = topBarHeight,
                    avatarCollapse = titleProgress,
                    onFollowClick = { onIntent(UserProfileIntent.ToggleFollow) },
                    onSubscribeClick = { subscribeRequester(profile.userId, profile.displayName) },
                    onMessageClick = { onIntent(UserProfileIntent.SendMessage) },
                    onWebsiteClick = { context.launchCustomTab(profile.website ?: "") },
                    onUnblockClick = { onIntent(UserProfileIntent.ShowUnblockConfirmDialog) },
                    onProfileImageClick = {
                        onShowProfileImageViewer(
                            profile.avatarUrl ?: profile.avatarThumbUrl
                        )
                    },
                    onFollowerClick = onFollowerClick,
                    onFollowingClick = onFollowingClick,
                    onSubscriberClick = onSubscriberClick,
                    onEditProfileClick = onEditProfileClick
                )
            }
        }

        if (showTopBarBlur) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight + TopBarBlurExtra)
                    .graphicsLayer { alpha = topBarReveal() }
                    .then(if (isCollapsed) Modifier.pointerInput(Unit) { detectTapGestures { /* 터치 이벤트 소비 */ } } else Modifier)
                    .haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))
            )
        }

        // 헤더에서 톱바로 옮겨 앉는 이름
        CollapsingProfileTitle(
            profile = profile,
            expandedTop = titleExpandedTop,
            collapsedTop = titleCollapsedTop,
            scrollOffset = { headerState.scrollOffset },
            topBarReveal = topBarReveal
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPaddingIgnoringVisibility()
                .height(topBarHeight - statusBarHeight)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassIconButton(
                onClick = { onIntent(UserProfileIntent.NavigateBack) },
                icon = painterResource(R.drawable.ic_chevron_left),
                contentDescription = "뒤로가기",
                hazeState = hazeState,
                backgroundColor = backgroundColor
            )
            Spacer(modifier = Modifier.weight(1f))
            // 더보기는 남 프로필만
            if (!profile.isOwnProfile) {
                // 알림 벨, 크리에이터만
                if (profile.isCreator) {
                    GlassIconButton(
                        onClick = { onIntent(UserProfileIntent.NotifyBellClick) },
                        icon = painterResource(
                            when {
                                !profile.isFollowing -> R.drawable.ic_bell
                                profile.isNotifyEnabled -> R.drawable.ic_bell_alert
                                else -> R.drawable.ic_bell_slash
                            }
                        ),
                        contentDescription = when {
                            !profile.isFollowing -> "알림 설정"
                            profile.isNotifyEnabled -> "알림 켜짐"
                            else -> "알림 꺼짐"
                        },
                        hazeState = hazeState,
                        backgroundColor = backgroundColor
                    )
                }
                GlassIconButton(
                    onClick = { onIntent(UserProfileIntent.ShowMoreOptions) },
                    icon = painterResource(R.drawable.ic_ellipsis_horizontal_circle),
                    contentDescription = "더보기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor
                )
            }
        }

        ProfileTabRow(
            selectedTab = state.selectedTab,
            onTabSelected = { tab ->
                // 같은 탭 재탭: 현재 listState만 맨 위로
                if (tab == tabs.getOrNull(pagerState.currentPage)) {
                    coroutineScope.launch { currentListState().animateScrollToItem(0) }
                } else {
                    onIntent(UserProfileIntent.SelectTab(tab))
                }
            },
            tabs = tabs,
            hazeState = hazeState,
            hazeBackgroundColor = backgroundColor,
            scrollFraction = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
            modifier = Modifier
                .offset {
                    IntOffset(
                        0,
                        (headerState.headerHeight + headerState.scrollOffset).roundToInt()
                    )
                }
                .graphicsLayer { alpha = belowHeaderAlpha() }
                .scrollable(
                    state = headerScrollableState,
                    orientation = Orientation.Vertical
                )
        )

        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = state.isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = MaterialTheme.colorScheme.surface,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 커뮤니티 글 작성 FAB
        val canWriteCommunityPost = state.selectedTab == ProfileTab.COMMUNITY && profile.isFollowing
        AnimatedScrollAwareFAB(
            visible = canWriteCommunityPost,
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            contentDescription = "커뮤니티 글 작성",
            onClick = onCreateCommunityPost
        )
    }
}

@Composable
private fun ProfileCover(
    avatarUrl: String?,
    avatarThumbUrl: String?,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val coverHazeState = remember { HazeState() }

    Box(modifier = modifier.clipToBounds()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(coverHazeState)
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                val context = LocalContext.current
                // 4×4로 줄여 색만 남김
                val model = remember(avatarThumbUrl, avatarUrl, context) {
                    val url = avatarThumbUrl ?: avatarUrl
                    ImageRequest.Builder(context)
                        .data(url)
                        .memoryCacheKey("$url#4x4")
                        .size(Size(4, 4))
                        .precision(Precision.EXACT)
                        .build()
                }
                AsyncImage(
                    model = model,
                    contentDescription = "커버 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = COVER_IMAGE_SCALE
                            scaleY = COVER_IMAGE_SCALE
                        }
                        .blur(40.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(FallbackCoverGray)
                )
            }

            // 흰 글자가 읽히는 어두운 층
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.3f),
                            0.5f to Color.Black.copy(alpha = 0.14f)
                        )
                    )
            )
        }

        // 마지막 카드 가운데부터 탭 위까지 표면색으로 녹음
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(ProfileHeaderBottomGap + CoverFadeCardOverlap)
                .haloBlur(
                    coverHazeState,
                    HaloBlurs.fadeUp(backgroundColor, end = 1f, easing = CoverFadeEasing)
                )
        )
    }
}
