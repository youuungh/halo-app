
package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.fab.AnimatedScrollAwareFAB
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHostState
import com.ninezero.halo.core.ui.components.post.LocalSubscribeRequester
import com.ninezero.halo.core.util.launchCustomTab
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRow
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileIntent
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScaffoldState
import me.onebone.toolbar.ScrollStrategy

private const val COVER_IMAGE_SCALE = 1.8f

@Composable
fun CollapsingToolbarLayout(
    state: UserProfileState,
    collapsingState: CollapsingToolbarScaffoldState,
    pagerState: PagerState,
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

    val progress = collapsingState.toolbarState.progress
    val backgroundColor = MaterialTheme.colorScheme.surface

    val tabHazeState = remember { HazeState() }

    val topAppBarAlpha = ((0.7f - progress) / 0.7f).coerceIn(0f, 1f)
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) {
            pullToRefreshState.animateToHidden()
        }
    }

    val isPullToRefreshEnabled by remember {
        derivedStateOf { collapsingState.toolbarState.progress >= 0.99f }
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
    ) {
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            state = collapsingState,
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
            toolbar = {
                val statusBarHeight = with(density) {
                    // 뷰어가 바를 숨겨도 높이 유지
                    WindowInsets.statusBarsIgnoringVisibility.getTop(this).toDp()
                }

                // 커버 이미지 + ProfileHeader 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollable(
                            state = collapsingState.toolbarState,
                            orientation = Orientation.Vertical
                        )
                        .parallax(1.0f)
                ) {
                    // 커버 이미지 또는 기본 배경
                    if (!profile.avatarUrl.isNullOrBlank()) {
                        val coverContext = LocalContext.current
                        val coverModel = remember(profile.avatarThumbUrl, profile.avatarUrl, coverContext) {
                            val url = profile.avatarThumbUrl ?: profile.avatarUrl
                            ImageRequest.Builder(coverContext)
                                .data(url)
                                .memoryCacheKey(url)
                                .size(Size.ORIGINAL)
                                .build()
                        }
                        AsyncImage(
                            model = coverModel,
                            contentDescription = "커버 이미지",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
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
                                .fillMaxWidth()
                                .height(420.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }

                    // 그라데이션 오버레이
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Transparent,
                                        backgroundColor.copy(alpha = 0.5f),
                                        backgroundColor
                                    )
                                )
                            )
                    )

                    // ProfileHeader
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .padding(top = statusBarHeight + 56.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        // 멤버십 시트는 전역 호스트
                        val subscribeRequester = LocalSubscribeRequester.current
                        UserProfileHeaderContent(
                            profile = profile,
                            onFollowClick = { onIntent(UserProfileIntent.ToggleFollow) },
                            onSubscribeClick = { subscribeRequester(profile.userId, profile.displayName) },
                            onMessageClick = { onIntent(UserProfileIntent.SendMessage) },
                            onWebsiteClick = { context.launchCustomTab(profile.website ?: "") },
                            onUnblockClick = { onIntent(UserProfileIntent.ShowUnblockConfirmDialog) },
                            onProfileImageClick = { onShowProfileImageViewer(profile.avatarUrl ?: profile.avatarThumbUrl) },
                            onFollowerClick = onFollowerClick,
                            onFollowingClick = onFollowingClick,
                            onSubscriberClick = onSubscriberClick,
                            onEditProfileClick = onEditProfileClick
                        )
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(statusBarHeight + 56.dp)
                )

                // TopAppBar
                val topBarInteractionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(statusBarHeight + 56.dp)
                        .pin()
                        .graphicsLayer { alpha = topAppBarAlpha }
                        .background(backgroundColor)
                        // 접혀서 바가 보일 때만 탭 소비
                        .then(
                            if (topAppBarAlpha > 0f) Modifier.clickable(
                                interactionSource = topBarInteractionSource,
                                indication = null
                            ) {} else Modifier
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPaddingIgnoringVisibility()
                            .padding(horizontal = 56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        VerifiedBadge(role = profile.role, size = 18.dp)
                    }
                }

                // 반투명 버튼들
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPaddingIgnoringVisibility()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .pin(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .bounceClick()
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { onIntent(UserProfileIntent.NavigateBack) },
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.3f * progress)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_left),
                                contentDescription = "뒤로가기",
                                tint = if (progress > 0.3f) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // 더보기는 남 프로필만
                    if (!profile.isOwnProfile) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // 알림 벨, 크리에이터만
                            if (profile.isCreator) {
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .bounceClick()
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true)
                                        ) { onIntent(UserProfileIntent.NotifyBellClick) },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.3f * progress)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(
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
                                            tint = if (progress > 0.3f) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .bounceClick()
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true)
                                    ) { onIntent(UserProfileIntent.ShowMoreOptions) },
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.3f * progress)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_ellipsis_horizontal_circle),
                                        contentDescription = "더보기",
                                        tint = if (progress > 0.3f) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) {
            // toolbar 측정 전 minHeight 0이면 스크롤 손상
            // minHeight>0 뒤에 body 렌더
            if (collapsingState.toolbarState.minHeight <= 0) {
                Spacer(modifier = Modifier.fillMaxSize())
                return@CollapsingToolbarScaffold
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
            ) {
                // 탭 Row
                Box(
                    modifier = Modifier
                        .zIndex(1f)
                        .scrollable(
                            state = collapsingState.toolbarState,
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
                                onIntent(UserProfileIntent.SelectTab(tab))
                            }
                        },
                        tabs = tabs,
                        hazeState = tabHazeState,
                        hazeBackgroundColor = backgroundColor,
                        scrollFraction = pagerState.currentPage + pagerState.currentPageOffsetFraction
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(tabHazeState)
                ) { page ->
                    val currentTab = tabs.getOrElse(page) { tabs.first() }
                    when (currentTab) {
                        // 일반 유저 탭
                        ProfileTab.POSTS -> UserPostsTabContent(
                            state = state,
                            listState = postsListState,
                            onShowMediaViewer = { items, idx -> onShowMediaViewer(items, idx, null) },
                            onProfileClick = { author, offset ->
                                if (profile.userId != author.id) {
                                    profilePopupState.show(author.id, offset)
                                }
                            },
                            onIntent = onIntent
                        )
                        ProfileTab.REPLIES -> UserRepliesTabContent(
                            state = state,
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
                            listState = homeListState,
                            onPostClick = { post ->
                                onIntent(UserProfileIntent.NavigateToPostDetail(post.id))
                            },
                            onProductClick = { product ->
                                if (product.canAccess == false) {
                                    onIntent(
                                        UserProfileIntent.ShowRestrictedContentSnackbar(
                                            "${product.requiredTier} 멤버십 전용 상품입니다"
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
                            listState = storeListState,
                            onIntent = onIntent,
                            onProductClick = { productId ->
                                onIntent(UserProfileIntent.NavigateToProductDetail(productId))
                            }
                        )
                    }
                }
            }
        }

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
            hazeState = tabHazeState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            contentDescription = "커뮤니티 글 작성",
            onClick = onCreateCommunityPost
        )
    }
}
