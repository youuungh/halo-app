package com.ninezero.halo.feature.profile.presentation.section_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.section_detail.components.PostsList
import com.ninezero.halo.feature.profile.presentation.section_detail.components.ProductsGrid
import dev.chrisbanes.haze.HazeState
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SectionDetailScreen(
    sectionType: SectionType,
    creatorId: Int,
    tagId: Int?,
    title: String,
    onNavigateBack: () -> Unit,
    onNavigateToPostDetail: (Int) -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    viewModel: SectionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(SectionDetailIntent.SnackbarDismissed) }
    )

    LaunchedEffect(sectionType, creatorId, tagId, title) {
        viewModel.onIntent(
            SectionDetailIntent.Initialize(
                sectionType = sectionType,
                creatorId = creatorId,
                tagId = tagId,
                title = title
            )
        )
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SectionDetailSideEffect.NavigateToPostDetail -> onNavigateToPostDetail(sideEffect.postId)
            is SectionDetailSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
        }
    }

    SectionDetailScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun SectionDetailScreenContent(
    state: SectionDetailState,
    onNavigateBack: () -> Unit,
    onIntent: (SectionDetailIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    var headerHeight by rememberSaveableDp()

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    // 포스트 무한 스크롤
    val shouldLoadMorePosts by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index >= totalItems - 3
        }
    }

    // 상품 무한 스크롤
    val shouldLoadMoreProducts by remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = gridState.layoutInfo.totalItemsCount
            lastVisibleItem != null && lastVisibleItem.index >= totalItems - 6
        }
    }

    LaunchedEffect(shouldLoadMorePosts, shouldLoadMoreProducts, state.sectionType) {
        val shouldLoad = if (state.sectionType == SectionType.PRODUCT_TAG) {
            shouldLoadMoreProducts
        } else {
            shouldLoadMorePosts
        }
        if (shouldLoad && !state.isLoading && state.hasMore) {
            onIntent(SectionDetailIntent.LoadMore)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isLoading && state.posts.isEmpty() && state.products.isEmpty(),
            onRefresh = { onIntent(SectionDetailIntent.Refresh) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
        when {
            // 에러 상태
            state.error != null && state.posts.isEmpty() && state.products.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { onIntent(SectionDetailIntent.Refresh) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.bounceClick()
                        ) {
                            Text("재시도")
                        }
                    }
                }
            }
            // 포스트 목록
            state.sectionType == SectionType.POST_TAG -> {
                PostsList(
                    posts = state.posts,
                    isLoading = state.isLoading,
                    listState = listState,
                    headerHeight = headerHeight,
                    hazeState = hazeState,
                    onPostClick = { post ->
                        onIntent(SectionDetailIntent.NavigateToPostDetail(post.id))
                    }
                )
            }
            // 상품 그리드
            state.sectionType == SectionType.PRODUCT_TAG -> {
                ProductsGrid(
                    products = state.products,
                    isLoading = state.isLoading,
                    gridState = gridState,
                    headerHeight = headerHeight,
                    hazeState = hazeState,
                    onProductClick = { product ->
                        if (product.canAccess == false) {
                            onIntent(
                                SectionDetailIntent.ShowRestrictedContentSnackbar(
                                    "${product.requiredTier} 멤버십 전용 상품입니다"
                                )
                            )
                        } else {
                            onIntent(SectionDetailIntent.NavigateToProductDetail(product.id))
                        }
                    }
                )
            }
            // PINNED_POST는 여기로 안 옴
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "지원하지 않는 섹션 타입입니다",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        }

        HazeScreenHeader(
            title = state.title,
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

