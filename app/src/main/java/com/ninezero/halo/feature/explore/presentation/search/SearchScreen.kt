
package com.ninezero.halo.feature.explore.presentation.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.explore.presentation.search.components.RecommendationsSection
import com.ninezero.halo.feature.explore.presentation.search.components.SearchBar
import com.ninezero.halo.feature.explore.presentation.search.components.SearchHistorySection
import com.ninezero.halo.feature.explore.presentation.search.components.SearchResultsSection
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SearchScreen(
    isActive: Boolean,
    onNavigateToProfile: (Int) -> Unit,
    onBackPressed: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isActive && state.isSearchFocused) {
        focusManager.clearFocus()
        viewModel.onIntent(SearchIntent.ClearSearch)
        onBackPressed()
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SearchSideEffect.NavigateToProfile -> onNavigateToProfile(sideEffect.userId)
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(SearchIntent.SnackbarDismissed) }
    )

    SearchScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackPressed = onBackPressed,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun SearchScreenContent(
    state: SearchState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackPressed: () -> Unit = {},
    onIntent: (SearchIntent) -> Unit
) {
    val hazeState = remember { HazeState() }
    val topBarBlurColor = MaterialTheme.colorScheme.background
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isSearching,
        hasMore = state.searchHasMore && state.searchQuery.isNotBlank(),
        onLoadMore = { onIntent(SearchIntent.LoadMoreSearchResults) }
    )

    Scaffold(
        snackbarHost = {
            // 키보드 없으면 바텀바 위, 있으면 키보드 위
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                bottomBarPadding = BottomBarDefaults.PillFootprint
            )
        },
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBarsIgnoringVisibility,
                title = {
                    SearchBar(
                        query = state.searchQuery,
                        isFocused = state.isSearchFocused,
                        onQueryChange = { onIntent(SearchIntent.UpdateSearchQuery(it)) },
                        onSearch = { onIntent(SearchIntent.Search(state.searchQuery)) },
                        onFocusChange = { focused ->
                            if (focused) {
                                onIntent(SearchIntent.FocusSearchBar)
                            }
                        },
                        focusRequester = focusRequester,
                        hazeState = hazeState,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    AnimatedVisibility(
                        visible = state.isSearchFocused,
                        enter = fadeIn() + slideInHorizontally { -it } + expandHorizontally(),
                        exit = fadeOut() + slideOutHorizontally { -it } + shrinkHorizontally()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .bounceClick()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    focusManager.clearFocus()
                                    onIntent(SearchIntent.ClearSearch)
                                    onBackPressed()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_left),
                                contentDescription = "뒤로가기"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
                    .hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            backgroundColor = topBarBlurColor,
                            tint = HazeTint(topBarBlurColor),
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0.5f,
                            endIntensity = 0f
                        )
                    }
            )
        }
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()

        AnimatedContent(
            targetState = state.isSearchFocused,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "contentTransition",
            modifier = Modifier.fillMaxSize()
        ) { isFocused ->
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { focusManager.clearFocus() })
                        }
                ) {
                    if (state.isSearchQueryValid) {
                        SearchResultsSection(
                            results = state.searchResults,
                            isLoading = state.isSearching,
                            hazeState = hazeState,
                            listState = listState,
                            topPadding = topPadding,
                            // 먼저 clearFocus해야 키보드가 전환과 같이 내려감
                            onCreatorClick = {
                                focusManager.clearFocus(force = true)
                                onIntent(SearchIntent.NavigateToProfile(it))
                            }
                        )
                    } else {
                        SearchHistorySection(
                            history = state.searchHistory,
                            isLoading = state.isHistoryLoading,
                            isEditing = state.isEditingHistory,
                            hazeState = hazeState,
                            topPadding = topPadding,
                            onHistoryClick = { onIntent(SearchIntent.SelectHistoryItem(it)) },
                            onDeleteClick = { onIntent(SearchIntent.DeleteSearchHistoryItem(it)) },
                            onEditClick = { onIntent(SearchIntent.ToggleEditHistory) },
                            onClearAll = { onIntent(SearchIntent.ClearSearchHistory) }
                        )
                    }
                }
            } else {
                HaloPullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onIntent(SearchIntent.RefreshRecommendations) },
                    modifier = Modifier.fillMaxSize(),
                    indicatorPadding = PaddingValues(top = topPadding)
                ) {
                    RecommendationsSection(
                        recommendations = state.recommendations,
                        isLoading = state.isRecommendationsLoading,
                        hazeState = hazeState,
                        topPadding = topPadding,
                        onUserClick = { onIntent(SearchIntent.NavigateToProfile(it)) },
                        onFollowClick = { onIntent(SearchIntent.ToggleFollow(it)) }
                    )
                }
            }
        }
    }
}
