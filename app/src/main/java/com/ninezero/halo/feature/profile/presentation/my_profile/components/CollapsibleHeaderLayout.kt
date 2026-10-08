package com.ninezero.halo.feature.profile.presentation.my_profile.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRow
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileIntent
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

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

    val tabHazeState = remember { HazeState() }

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

    // 헤더 스크롤 처리
    val headerScrollableState = rememberScrollableState { delta ->
        if (headerState.headerHeight == 0f) return@rememberScrollableState 0f

        // 위로 스크롤: 헤더 접기
        if (delta < 0) {
            val newOffset =
                (headerState.scrollOffset + delta).coerceIn(-headerState.headerHeight, 0f)
            val consumed = newOffset - headerState.scrollOffset
            headerState.scrollOffset = newOffset
            return@rememberScrollableState consumed
        }

        // 아래로 스크롤: 리스트가 맨 위일 때만 헤더 펼치기
        val listState = currentListState()
        if (delta > 0 && listState.firstVisibleItemIndex == 0 &&
            listState.firstVisibleItemScrollOffset == 0
        ) {
            val newOffset =
                (headerState.scrollOffset + delta).coerceIn(-headerState.headerHeight, 0f)
            val consumed = newOffset - headerState.scrollOffset
            headerState.scrollOffset = newOffset
            return@rememberScrollableState consumed
        }

        0f
    }

    val nestedScrollConnection = remember(headerState.headerHeight, tabs) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (headerState.headerHeight == 0f) return Offset.Zero

                val delta = available.y

                // 위로 스크롤: 헤더 접기
                if (delta < 0) {
                    val newOffset =
                        (headerState.scrollOffset + delta).coerceIn(-headerState.headerHeight, 0f)
                    val consumed = newOffset - headerState.scrollOffset
                    headerState.scrollOffset = newOffset
                    return Offset(0f, consumed)
                }

                // 아래로 스크롤: 리스트가 맨 위일 때만 헤더 펼치기
                val listState = currentListState()
                if (delta > 0 && listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
                ) {
                    val newOffset =
                        (headerState.scrollOffset + delta).coerceIn(-headerState.headerHeight, 0f)
                    val consumed = newOffset - headerState.scrollOffset
                    headerState.scrollOffset = newOffset
                    return Offset(0f, consumed)
                }

                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
            .nestedScroll(nestedScrollConnection)
    ) {
        // 프로필 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    headerState.headerHeight = coordinates.size.height.toFloat()
                }
                .graphicsLayer { translationY = headerState.scrollOffset }
                .scrollable(
                    state = headerScrollableState,
                    orientation = Orientation.Vertical
                )
        ) {
            MyProfileHeaderContent(
                profile = state.profile!!,
                onEditClick = { onIntent(MyProfileIntent.NavigateToEditProfile) },
                onProfileImageClick = {
                    onShowProfileImageViewer(
                        state.profile.avatarThumbUrl ?: state.profile.avatarUrl
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

        // 탭 + 페이저, 탭이 zIndex로 blur 위에
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY =
                        (headerState.headerHeight + headerState.scrollOffset).coerceAtLeast(0f)
                }
        ) {
            // 탭
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .scrollable(
                        state = headerScrollableState,
                        orientation = Orientation.Vertical
                    )
            ) {
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
                    hazeState = tabHazeState,
                    scrollFraction = pagerState.currentPage + pagerState.currentPageOffsetFraction
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(tabHazeState)
            ) { page ->
                when (tabs.getOrNull(page)) {
                    ProfileTab.HOME -> MyHomeTabContent(
                        state = state,
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
                        listState = postsListState,
                        // 포스트는 한 번씩만 렌더
                        onShowMediaViewer = { items, idx -> onShowMediaViewer(items, idx, null) },
                        onIntent = onIntent
                    )

                    ProfileTab.REPLIES -> MyRepliesTabContent(
                        state = state,
                        listState = repliesListState,
                        onShowMediaViewer = onShowMediaViewer,
                        onProfileClick = onProfileClick,
                        onIntent = onIntent
                    )

                    ProfileTab.COMMUNITY -> MyCommunityTabContent(
                        state = state,
                        listState = communityListState,
                        onIntent = onIntent,
                        onPostClick = { postId ->
                            onIntent(
                                MyProfileIntent.NavigateToPostDetail(
                                    postId
                                )
                            )
                        },
                        onShowMediaViewer = { items, idx -> onShowMediaViewer(items, idx, null) },
                        onProfileClick = onProfileClick
                    )

                    ProfileTab.STORE -> MyStoreTabContent(
                        state = state,
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
        }
    }
}
