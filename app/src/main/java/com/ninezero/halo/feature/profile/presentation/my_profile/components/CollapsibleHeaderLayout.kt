package com.ninezero.halo.feature.profile.presentation.my_profile.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.layout.ResetListsWhenHeaderShows
import com.ninezero.halo.core.ui.components.layout.clipAboveCollapsingTab
import com.ninezero.halo.core.ui.components.layout.markGestureStarts
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaItem
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.CollapsibleHeaderState
import com.ninezero.halo.feature.profile.presentation.components.ProfileHeaderScroll
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRow
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.components.rememberCollapsibleHeaderState
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileIntent
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CollapsibleHeaderLayout(
    state: MyProfileState,
    pagerState: PagerState,
    homeListState: LazyListState,
    postsListState: LazyListState,
    repliesListState: LazyListState,
    communityListState: LazyListState,
    storeListState: LazyListState,
    tabs: List<ProfileTab>,
    hazeState: HazeState,
    topBarHeight: Dp,
    topBar: @Composable (collapseReveal: () -> Float, blurAlpha: () -> Float) -> Unit,
    onShowMediaViewer: (List<ViewerMediaItem>, Int, String?) -> Unit,
    onShowProfileImageViewer: (String?) -> Unit,
    onProfileClick: (Int, IntOffset) -> Unit,
    onFollowerClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    onSubscriberClick: () -> Unit = {},
    onMembershipClick: () -> Unit = {},
    onAddressClick: () -> Unit = {},
    onMembershipManageClick: () -> Unit = {},
    onReviewClick: () -> Unit = {},
    onPointClick: () -> Unit = {},
    onCouponClick: () -> Unit = {},
    onCouponManageClick: () -> Unit = {},
    onStoreClick: () -> Unit = {},
    onSalesClick: () -> Unit = {},
    onOrderClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    headerState: CollapsibleHeaderState = rememberCollapsibleHeaderState(),
    onIntent: (MyProfileIntent) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    // 탭이 톱바 아래에 붙으면 접힌 상태
    val isCollapsed by remember(headerState) {
        derivedStateOf { headerState.headerHeight > 0f && headerState.scrollOffset <= -headerState.headerHeight }
    }

    fun currentListState(): LazyListState {
        val currentTab = tabs.getOrNull(pagerState.currentPage) ?: tabs.firstOrNull()
        return when (currentTab) {
            ProfileTab.HOME -> homeListState
            ProfileTab.POSTS -> postsListState
            ProfileTab.REPLIES -> repliesListState
            ProfileTab.COMMUNITY -> communityListState
            ProfileTab.STORE -> storeListState
            null -> postsListState
        }
    }

    // 접히기 직전 구간에서 헤더가 사라져 톱바 뒤로 비치지 않음
    val headerFadePx = with(LocalDensity.current) { HeaderFadeDistance.toPx() }
    val headerAlpha = remember(headerState, headerFadePx) {
        { ((headerState.headerHeight + headerState.scrollOffset) / headerFadePx).coerceIn(0f, 1f) }
    }

    // 다 접히기 직전부터 톱바 블러가 탭 뒤까지 내려옴
    val topBarRevealPx = with(LocalDensity.current) { TopBarRevealDistance.toPx() }
    val topBarReveal = remember(headerState, topBarRevealPx) {
        {
            val height = headerState.headerHeight
            if (height > 0f) (1f - (height + headerState.scrollOffset) / topBarRevealPx).coerceIn(0f, 1f) else 0f
        }
    }

    // 맨 위에선 블러 없음, 헤더가 톱바 밑으로 들면 나타남
    val topBarBlurInPx = with(LocalDensity.current) { TopBarBlurInDistance.toPx() }
    val topBarBlurAlpha = remember(headerState, topBarBlurInPx) {
        { (-headerState.scrollOffset / topBarBlurInPx).coerceIn(0f, 1f) }
    }

    ResetListsWhenHeaderShows(
        lists = listOf(homeListState, postsListState, repliesListState, communityListState, storeListState),
        activeList = { currentListState() },
        isHeaderVisible = { headerState.headerHeight > 0f && !isCollapsed }
    )

    val headerScroll = remember(headerState, tabs) {
        ProfileHeaderScroll(
            headerState = headerState,
            maxCollapse = { headerState.headerHeight },
            isListAtTop = {
                val listState = currentListState()
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            }
        )
    }
    val headerScrollableState = rememberScrollableState { delta -> headerScroll.consume(delta) }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                modifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        // 헤더 높이를 재기 전엔 그리지 않음
                        if (headerState.headerHeight > 0f) {
                            placeable.place(0, (headerState.headerHeight + headerState.scrollOffset).roundToInt())
                        }
                    }
                }.clipAboveCollapsingTab(topBarHeight + ProfileTabRowHeight) { if (isCollapsed) 1f else 0f }
            ) { page ->
                when (tabs.getOrNull(page)) {
                    ProfileTab.HOME -> MyHomeTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = homeListState,
                        onPostClick = { post -> onIntent(MyProfileIntent.NavigateToPostDetail(post.id)) },
                        onProductClick = { product ->
                            onIntent(
                                MyProfileIntent.NavigateToProductDetail(
                                    product.id
                                )
                            )
                        },
                        onViewAllPostSectionClick = { section ->
                            onIntent(
                                MyProfileIntent.NavigateToSectionDetail(
                                    SectionType.POST_TAG,
                                    section.tag.id,
                                    section.tag.name
                                )
                            )
                        },
                        onViewAllProductSectionClick = { section ->
                            onIntent(
                                MyProfileIntent.NavigateToSectionDetail(
                                    SectionType.PRODUCT_TAG,
                                    section.tag.id,
                                    section.tag.name
                                )
                            )
                        }
                    )

                    ProfileTab.POSTS -> MyPostsTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = postsListState,
                        // 포스트는 한 번씩만 렌더
                        onShowMediaViewer = { items, mediaIndex -> onShowMediaViewer(items, mediaIndex, null) },
                        onIntent = onIntent
                    )

                    ProfileTab.REPLIES -> MyRepliesTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = repliesListState,
                        onShowMediaViewer = onShowMediaViewer,
                        onProfileClick = onProfileClick,
                        onIntent = onIntent
                    )

                    ProfileTab.COMMUNITY -> MyCommunityTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = communityListState,
                        onIntent = onIntent,
                        onPostClick = { postId ->
                            onIntent(
                                MyProfileIntent.NavigateToPostDetail(
                                    postId
                                )
                            )
                        },
                        onShowMediaViewer = { items, mediaIndex -> onShowMediaViewer(items, mediaIndex, null) },
                        onProfileClick = onProfileClick
                    )

                    ProfileTab.STORE -> MyStoreTabContent(
                        state = state,
                        topPadding = topBarHeight,
                        listState = storeListState,
                        onIntent = onIntent,
                        onProductClick = { productId ->
                            onIntent(
                                MyProfileIntent.NavigateToProductDetail(
                                    productId
                                )
                            )
                        }
                    )

                    null -> {}
                }
            }

            // 프로필 사진 빛, 헤더와 함께 움직임
            val glowUrl = state.profile?.let { it.avatarThumbUrl ?: it.avatarUrl }?.takeIf { isAvatarGlowSupported }
            if (glowUrl != null) {
                ProfileAvatarGlow(
                    imageUrl = glowUrl,
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val height = (topBarHeight.toPx() + minOf(GlowHeight.toPx(), headerState.headerHeight)).roundToInt()
                            val placeable = measurable.measure(Constraints.fixed(constraints.maxWidth, height))
                            // 헤더 높이를 재기 전엔 그리지 않음
                            layout(constraints.maxWidth, height) {
                                if (headerState.headerHeight > 0f) placeable.place(0, 0)
                            }
                        }
                        .graphicsLayer {
                            translationY = headerState.scrollOffset
                            alpha = headerAlpha()
                        }
                )
            }

            // 프로필 헤더
            Box(
                modifier = Modifier
                    .offset(y = topBarHeight)
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        headerState.headerHeight = coordinates.size.height.toFloat()
                    }
                    .graphicsLayer {
                        translationY = headerState.scrollOffset
                        alpha = headerAlpha()
                    }
                    .scrollable(
                        state = headerScrollableState,
                        orientation = Orientation.Vertical
                    )
            ) {
                state.profile?.let { profile ->
                    MyProfileHeaderContent(
                        profile = profile,
                        onEditClick = { onIntent(MyProfileIntent.NavigateToEditProfile) },
                        onProfileImageClick = {
                            onShowProfileImageViewer(
                                profile.avatarThumbUrl ?: profile.avatarUrl
                            )
                        },
                        onOrderClick = onOrderClick,
                        onCartClick = onCartClick,
                        onWishlistClick = onWishlistClick,
                        onFollowerClick = onFollowerClick,
                        onFollowingClick = onFollowingClick,
                        onSubscriberClick = onSubscriberClick,
                        onReviewClick = onReviewClick,
                        onMembershipClick = onMembershipClick,
                        onAddressClick = onAddressClick,
                        onMembershipManageClick = onMembershipManageClick,
                        onPointClick = onPointClick,
                        onCouponClick = onCouponClick,
                        onCouponManageClick = onCouponManageClick,
                        onStoreClick = onStoreClick,
                        onSalesClick = onSalesClick
                    )
                }
            }
        }

        topBar(topBarReveal, topBarBlurAlpha)

        // 탭 - 헤더 끝을 따라 올라가 톱바 아래에 붙음
        ProfileTabRow(
            selectedTab = state.selectedTab,
            onTabSelected = { tab ->
                // 같은 탭 재탭: 현재 listState만 맨 위로
                val currentTab = tabs.getOrNull(pagerState.currentPage)
                if (tab == currentTab) {
                    val listState = when (tab) {
                        ProfileTab.HOME -> homeListState
                        ProfileTab.POSTS -> postsListState
                        ProfileTab.REPLIES -> repliesListState
                        ProfileTab.COMMUNITY -> communityListState
                        ProfileTab.STORE -> storeListState
                    }
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                } else {
                    onIntent(MyProfileIntent.SelectTab(tab))
                }
            },
            tabs = tabs,
            hazeState = hazeState,
            scrollFraction = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        // 헤더 높이를 재기 전 첫 프레임엔 그리지 않음
                        if (headerState.headerHeight > 0f) {
                            placeable.place(
                                x = 0,
                                y = (topBarHeight.toPx() + headerState.headerHeight + headerState.scrollOffset).roundToInt()
                            )
                        }
                    }
                }
                .scrollable(
                    state = headerScrollableState,
                    orientation = Orientation.Vertical
                )
        )
    }
}

private val GlowHeight = 280.dp
private val HeaderFadeDistance = 96.dp
private val TopBarRevealDistance = 56.dp
private val TopBarBlurInDistance = 24.dp
