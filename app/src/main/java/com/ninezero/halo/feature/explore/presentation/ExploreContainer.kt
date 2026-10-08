package com.ninezero.halo.feature.explore.presentation

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.ninezero.halo.feature.explore.presentation.search.SearchScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun ExploreContainer(
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToEditPost: (Int) -> Unit,
    onNavigateToChatRoom: (Int) -> Unit,
    tabReselectFlow: Flow<Unit>? = null
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    // 검색모드 아닐 때만 ExploreScreen으로 릴레이
    val exploreReselectFlow = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }
    LaunchedEffect(tabReselectFlow) {
        tabReselectFlow?.collect {
            if (pagerState.currentPage == 0) exploreReselectFlow.tryEmit(Unit)
        }
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = true
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
                onNavigateToProfile = onNavigateToProfile,
                onBackPressed = { scope.launch { pagerState.animateScrollToPage(0) } }
            )
        }
    }
}
