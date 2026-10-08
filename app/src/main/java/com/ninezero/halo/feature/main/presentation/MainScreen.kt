package com.ninezero.halo.feature.main.presentation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.ninezero.halo.core.ui.components.media.LocalAutoplayEnabled
import com.ninezero.halo.core.ui.components.modifiers.LocalActiveViewerMediaKey
import com.ninezero.halo.core.ui.components.modifiers.LocalMediaSharedTransitionScope
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.profile_image_viewer.LocalProfileImageOverlayVisible
import com.ninezero.halo.core.ui.components.profile_image_viewer.LocalProfileImageTransitionScope
import com.ninezero.halo.core.ui.components.profile_image_viewer.ProfileImageViewerOverlay
import com.ninezero.halo.core.ui.components.profile_image_viewer.ProfileImageViewerState
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.LocalSnackbarRelay
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.snackbar.SnackbarRelay
import com.ninezero.halo.core.ui.components.viewer.LocalViewerOverlayHostState
import com.ninezero.halo.core.ui.components.viewer.ViewerOverlayHost
import com.ninezero.halo.core.ui.components.viewer.rememberViewerOverlayHostState
import com.ninezero.halo.feature.chat.presentation.chat_list.ChatListScreen
import com.ninezero.halo.feature.explore.presentation.ExploreContainer
import com.ninezero.halo.feature.feed.presentation.FeedScreen
import com.ninezero.halo.feature.main.presentation.components.BottomNavBar
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MainScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToCommentDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToChat: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToSectionDetail: (SectionType, Int, Int?, String) -> Unit,
    onNavigateToCreateCommunityPost: (Int) -> Unit,
    onNavigateToFollowList: (userId: Int, displayName: String, initialTab: Int, followerCount: Int, followingCount: Int, subscriberCount: Int, showSubscribers: Boolean) -> Unit = { _, _, _, _, _, _, _ -> },
    onNavigateToMembershipManage: () -> Unit = {},
    onNavigateToMySubscriptions: () -> Unit = {},
    onNavigateToPoint: () -> Unit = {},
    onNavigateToCoupon: () -> Unit = {},
    onNavigateToCouponManage: () -> Unit = {},
    onNavigateToMyReviews: () -> Unit = {},
    onNavigateToAddress: () -> Unit = {},
    onNavigateToWishlist: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToMyOrders: () -> Unit = {},
    onNavigateToMyProducts: () -> Unit = {},
    onNavigateToSalesManage: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val myUserId = state.currentUserId
    val onTabProfileClick: (Int) -> Unit = { userId ->
        if (myUserId != null && userId == myUserId) {
            viewModel.onIntent(MainIntent.SelectTab(MainTab.PROFILE))
        } else {
            onNavigateToProfile(userId)
        }
    }

    // 탭 재선택 이벤트 버스, 버퍼 1로 최근 신호 보장
    val tabReselectFlow = remember { MutableSharedFlow<MainTab>(extraBufferCapacity = 1) }

    var profileImageViewerState by remember { mutableStateOf(ProfileImageViewerState()) }
    val profileViewerHazeState = remember { HazeState() }

    val bottomBarBackdrop = rememberLayerBackdrop()
    val bottomBarReservePadding = BottomBarDefaults.reservedContentPadding()  // BottomNavBar 만큼 각 탭 스크롤이 예약할 패딩

    val viewerHostState = rememberViewerOverlayHostState()  // 이미지/비디오 뷰어 호스트

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MainSideEffect.NavigateToCreatePost -> onNavigateToCreatePost()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(MainIntent.RefreshUnreadCounts)
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(MainIntent.SnackbarDismissed) }
    )

    // SharedTransitionLayout으로 아바타 morph
    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalProfileImageTransitionScope provides this@SharedTransitionLayout,
            LocalProfileImageOverlayVisible provides profileImageViewerState.isVisible,
            LocalViewerOverlayHostState provides viewerHostState,
            LocalMediaSharedTransitionScope provides this@SharedTransitionLayout,
            LocalActiveViewerMediaKey provides viewerHostState.activeMediaKey,
            LocalBottomBarPadding provides bottomBarReservePadding
        ) {
            // 오버레이는 Scaffold 밖이라 바 위 풀스크린
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(profileViewerHazeState)) {
                    Scaffold(
                        snackbarHost = {
                            DefaultSnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.padding(bottom = BottomBarDefaults.PillFootprint)
                            )
                        },
                        contentWindowInsets = WindowInsets(0)
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                // 하단 바는 이 Box 밖이라 source에서 빠짐
                                .layerBackdrop(bottomBarBackdrop)
                        ) {
                            TabSlot(visible = state.selectedTab == MainTab.FEED) {
                                FeedTabContent(
                                    onNavigateToPostDetail = onNavigateToPostDetail,
                                    onNavigateToProfile = onTabProfileClick,
                                    onNavigateToEditPost = onNavigateToEditPost,
                                    onNavigateToNotifications = onNavigateToNotifications,
                                    onNavigateToChat = onNavigateToChat,
                                    hasUnreadNotification = state.hasUnreadNotification,
                                    tabReselectFlow = remember(tabReselectFlow) {
                                        tabReselectFlow.filter { it == MainTab.FEED }.map { }
                                    }
                                )
                            }
                            TabSlot(visible = state.selectedTab == MainTab.EXPLORE) {
                                ExploreTabContent(
                                    onNavigateToPostDetail = onNavigateToPostDetail,
                                    onNavigateToProfile = onTabProfileClick,
                                    onNavigateToEditPost = onNavigateToEditPost,
                                    onNavigateToChat = onNavigateToChat,
                                    tabReselectFlow = remember(tabReselectFlow) {
                                        tabReselectFlow.filter { it == MainTab.EXPLORE }.map { }
                                    }
                                )
                            }
                            TabSlot(visible = state.selectedTab == MainTab.CHAT) {
                                ChatTabContent(
                                    onNavigateToChat = onNavigateToChat,
                                    tabReselectFlow = remember(tabReselectFlow) {
                                        tabReselectFlow.filter { it == MainTab.CHAT }.map { }
                                    }
                                )
                            }
                            TabSlot(visible = state.selectedTab == MainTab.PROFILE) {
                                ProfileTabContent(
                                    onNavigateToSettings = onNavigateToSettings,
                                    onNavigateToEditProfile = onNavigateToEditProfile,
                                    onNavigateToEditPost = onNavigateToEditPost,
                                    onNavigateToPostDetail = onNavigateToPostDetail,
                                    onNavigateToCommentDetail = onNavigateToCommentDetail,
                                    onNavigateToUserProfile = onNavigateToProfile,
                                    onNavigateToProductDetail = onNavigateToProductDetail,
                                    onNavigateToSectionDetail = onNavigateToSectionDetail,
                                    onNavigateToCreateCommunityPost = onNavigateToCreateCommunityPost,
                                    onNavigateToFollowList = onNavigateToFollowList,
                                    onNavigateToMembershipManage = onNavigateToMembershipManage,
                                    onNavigateToMySubscriptions = onNavigateToMySubscriptions,
                                    onNavigateToPoint = onNavigateToPoint,
                                    onNavigateToCoupon = onNavigateToCoupon,
                                    onNavigateToCouponManage = onNavigateToCouponManage,
                                    onNavigateToMyReviews = onNavigateToMyReviews,
                                    onNavigateToAddress = onNavigateToAddress,
                                    onNavigateToWishlist = onNavigateToWishlist,
                                    onNavigateToCart = onNavigateToCart,
                                    onNavigateToMyOrders = onNavigateToMyOrders,
                                    onNavigateToMyProducts = onNavigateToMyProducts,
                                    onNavigateToSalesManage = onNavigateToSalesManage,
                                    onNavigateToChat = onNavigateToChat,
                                    tabReselectFlow = remember(tabReselectFlow) {
                                        tabReselectFlow.filter { it == MainTab.PROFILE }.map { }
                                    },
                                    onShowProfileImageViewer = { url ->
                                        profileImageViewerState = ProfileImageViewerState(
                                            isVisible = true,
                                            imageUrl = url
                                        )
                                    }
                                )
                            }
                        }
                    }

                    BottomNavBar(
                        selectedTab = state.selectedTab,
                        hasUnreadMessage = state.hasUnreadMessage,
                        backdrop = bottomBarBackdrop,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onTabSelected = { tab ->
                            if (tab == state.selectedTab && tab != MainTab.CREATE) {
                                tabReselectFlow.tryEmit(tab)
                            } else {
                                viewModel.onIntent(MainIntent.SelectTab(tab))
                            }
                        }
                    )
                }  // hazeSource Box

                ProfileImageViewerOverlay(
                    state = profileImageViewerState,
                    // isVisible만 끔, imageUrl은 유지
                    onDismiss = {
                        profileImageViewerState = profileImageViewerState.copy(isVisible = false)
                    },
                    hazeState = profileViewerHazeState,
                    sharedTransitionScope = this@SharedTransitionLayout
                )

                // 하위 탭은 이 호스트에 위임
                ViewerOverlayHost(
                    state = viewerHostState,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }  // outer Box
        }  // CompositionLocalProvider
    }  // SharedTransitionLayout
}

/** 탭 콘텐츠 상주 슬롯 */
@Composable
private fun TabSlot(  // 한 번 보인 탭은 alive 유지, 비선택은 alpha 0
    visible: Boolean,
    content: @Composable () -> Unit
) {
    var hasBeenVisible by remember { mutableStateOf(visible) }
    if (visible) hasBeenVisible = true

    // 보이는 탭에만 scope, 활성 탭만 morph
    val mediaSharedScope = LocalMediaSharedTransitionScope.current

    // 숨은 탭 스낵바 등록 흡수용 더미 relay
    val snackbarRelay = LocalSnackbarRelay.current
    val detachedSnackbarRelay = remember { SnackbarRelay() }

    if (hasBeenVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (visible) 1f else 0f)
                .alpha(if (visible) 1f else 0f)
        ) {
            CompositionLocalProvider(
                LocalMediaSharedTransitionScope provides if (visible) mediaSharedScope else null,
                LocalSnackbarRelay provides if (visible) snackbarRelay else detachedSnackbarRelay,
                // 숨긴 탭은 자동재생 끔
                LocalAutoplayEnabled provides visible
            ) {
                content()
            }
        }
    }
}

@Composable
private fun FeedTabContent(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToChat: (Int) -> Unit,
    tabReselectFlow: Flow<Unit>,
    hasUnreadNotification: Boolean = false
) {
    FeedScreen(
        onNavigateToPostDetail = onNavigateToPostDetail,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToEditPost = onNavigateToEditPost,
        onNavigateToNotifications = onNavigateToNotifications,
        // 공유 완료 스낵바 "채팅 열기"가 채팅방으로 이동하려면 필요
        onNavigateToChatRoom = onNavigateToChat,
        hasUnreadNotification = hasUnreadNotification,
        tabReselectFlow = tabReselectFlow
    )
}

@Composable
private fun ExploreTabContent(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToChat: (Int) -> Unit,
    tabReselectFlow: Flow<Unit>
) {
    ExploreContainer(
        onNavigateToPostDetail = onNavigateToPostDetail,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToEditPost = onNavigateToEditPost,
        onNavigateToChatRoom = onNavigateToChat,
        tabReselectFlow = tabReselectFlow
    )
}

@Composable
private fun ChatTabContent(
    onNavigateToChat: (Int) -> Unit,
    tabReselectFlow: Flow<Unit>
) {
    ChatListScreen(
        onNavigateToChatRoom = onNavigateToChat,
        tabReselectFlow = tabReselectFlow
    )
}

@Composable
private fun ProfileTabContent(
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToCommentDetail: (Int) -> Unit,
    onNavigateToUserProfile: (Int) -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    onNavigateToSectionDetail: (SectionType, Int, Int?, String) -> Unit,
    onNavigateToCreateCommunityPost: (Int) -> Unit,
    onNavigateToFollowList: (userId: Int, displayName: String, initialTab: Int, followerCount: Int, followingCount: Int, subscriberCount: Int, showSubscribers: Boolean) -> Unit,
    tabReselectFlow: Flow<Unit>,
    onNavigateToMembershipManage: () -> Unit = {},
    onNavigateToMySubscriptions: () -> Unit = {},
    onNavigateToPoint: () -> Unit = {},
    onNavigateToCoupon: () -> Unit = {},
    onNavigateToCouponManage: () -> Unit = {},
    onNavigateToMyReviews: () -> Unit = {},
    onNavigateToAddress: () -> Unit = {},
    onNavigateToWishlist: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToMyOrders: () -> Unit = {},
    onNavigateToMyProducts: () -> Unit = {},
    onNavigateToSalesManage: () -> Unit = {},
    onNavigateToChat: (Int) -> Unit = {},
    onShowProfileImageViewer: ((String?) -> Unit)? = null
) {
    MyProfileScreen(
        onNavigateToEditProfile = onNavigateToEditProfile,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToPostDetail = onNavigateToPostDetail,
        onNavigateToCommentDetail = onNavigateToCommentDetail,
        onNavigateToUserProfile = onNavigateToUserProfile,
        onNavigateToEditPost = onNavigateToEditPost,
        onNavigateToProductDetail = onNavigateToProductDetail,
        onNavigateToSectionDetail = onNavigateToSectionDetail,
        onNavigateToCreateCommunityPost = onNavigateToCreateCommunityPost,
        onNavigateToFollowList = onNavigateToFollowList,
        onNavigateToMembershipManage = onNavigateToMembershipManage,
        onNavigateToMySubscriptions = onNavigateToMySubscriptions,
        onNavigateToPoint = onNavigateToPoint,
        onNavigateToCoupon = onNavigateToCoupon,
        onNavigateToCouponManage = onNavigateToCouponManage,
        onNavigateToMyReviews = onNavigateToMyReviews,
        onNavigateToAddress = onNavigateToAddress,
        onNavigateToWishlist = onNavigateToWishlist,
        onNavigateToCart = onNavigateToCart,
        onNavigateToMyOrders = onNavigateToMyOrders,
        onNavigateToMyProducts = onNavigateToMyProducts,
        onNavigateToSalesManage = onNavigateToSalesManage,
        onNavigateToChatRoom = onNavigateToChat,
        tabReselectFlow = tabReselectFlow,
        onShowProfileImageViewer = onShowProfileImageViewer
    )
}
