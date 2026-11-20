package com.ninezero.halo.feature.explore.presentation

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.ninezero.halo.feature.explore.presentation.search.SearchScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun ExploreContainer(
    isVisible: Boolean,  // 숨은 탭도 살아 있어 뒤로가기 가로채기 판단용
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToChatRoom: (Int) -> Unit,
    tabReselectFlow: Flow<Unit>? = null
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    var isSearchFocused by remember { mutableStateOf(false) }

    // 검색모드 아닐 때만 ExploreScreen으로 릴레이
    val exploreReselectFlow = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    LaunchedEffect(tabReselectFlow) {
        tabReselectFlow?.collect {
            if (pagerState.currentPage == 0) exploreReselectFlow.tryEmit(Unit)
        }
    }

    HorizontalPager(
        state = pagerState,
        // 검색 첫 화면에서만 밀어서 나감
        userScrollEnabled = pagerState.settledPage == 1 && !isSearchFocused
    ) { page ->
        when (page) {
            0 -> ExploreScreen(
                onNavigateToPostDetail = onNavigateToPostDetail,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToEditPost = onNavigateToEditPost,
                onNavigateToChatRoom = onNavigateToChatRoom,
                onNavigateToSearch = { scope.launch { pagerState.animateScrollToPage(1) } },
                tabReselectFlow = exploreReselectFlow
            )
            1 -> SearchScreen(
                isActive = isVisible && pagerState.currentPage == 1,
                onNavigateToProfile = onNavigateToProfile,
                onBackPressed = { scope.launch { pagerState.animateScrollToPage(0) } },
                onSearchFocusChange = { isSearchFocused = it }
            )
        }
    }
}
