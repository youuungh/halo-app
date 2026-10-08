package com.ninezero.halo.feature.explore.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.media.PrefetchUpcomingPostImages
import com.ninezero.halo.core.ui.components.media.rememberSettledVideoPostId
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.post.PostCard
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.core.ui.components.video_player.VideoPreloader
import com.ninezero.halo.feature.explore.presentation.ExploreIntent
import com.ninezero.halo.feature.explore.presentation.ExploreState
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.nextVideoUrlsAfter

@Composable
fun TrendingPostsContent(
    state: ExploreState,
    listState: LazyListState,
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit,
    onProfileClick: (Author, IntOffset) -> Unit,
    onIntent: (ExploreIntent) -> Unit
) {
    // 자동재생 후보는 영상 포스트만
    val videoPostIds = remember(state.trendingPosts) {
        state.trendingPosts
            .filter { post -> post.mediaAttachments.any { it.type == MediaType.VIDEO } }
            .mapTo(HashSet()) { it.id }
    }
    // 중앙 최근접 영상 1개만, 스크롤 정착 후 확정
    val visiblePostId by rememberSettledVideoPostId(listState, videoPostIds) {
        (it as? String)?.removePrefix("trending_")?.toIntOrNull()
    }
    // 다음 영상 포스트 앞부분 미리 캐시 → 전환 지연 감소
    val context = LocalContext.current
    LaunchedEffect(visiblePostId, state.trendingPosts) {
        VideoPreloader.prefetch(context, state.trendingPosts.nextVideoUrlsAfter(visiblePostId, count = 2))
    }
    PrefetchUpcomingPostImages(listState = listState, posts = state.trendingPosts) {
        (it as? String)?.removePrefix("trending_")?.toIntOrNull()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = ExploreTabRowHeight, bottom = LocalBottomBarPadding.current)
    ) {
        // 재시도 중 오류 화면 유지로 error 먼저
        val isInitialLoading = state.isTrendingLoading && state.trendingPosts.isEmpty() && state.trendingError == null

        when {
            // 에러 상태
            state.trendingError != null && state.trendingPosts.isEmpty() -> {
                item(key = "error") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.trendingError,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onIntent(ExploreIntent.LoadTrendingPosts) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }
            }

            // 초기 로딩 스켈레톤
            isInitialLoading -> {
                items(5) {
                    PostCardSkeleton()
                }
            }

            // 빈 상태
            state.trendingPosts.isEmpty() -> {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 포스트가 없어요",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 본문
            else -> {
                itemsIndexed(
                    items = state.trendingPosts,
                    key = { _, post -> "trending_${post.id}" }
                ) { index, post ->
                    if (index == 0) {
                        HorizontalDivider(
                            thickness = 0.25.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    PostCard(
                        post = post,
                        isVideoPlaying = visiblePostId == post.id,
                        onPostClick = { onIntent(ExploreIntent.NavigateToPostDetail(post.id)) },
                        onProfileClick = { offset -> onProfileClick(post.author, offset) },
                        onLikeClick = {
                            if (post.isLiked) {
                                onIntent(ExploreIntent.UnlikePost(post.id))
                            } else {
                                onIntent(ExploreIntent.LikePost(post.id))
                            }
                        },
                        onCommentClick = { onIntent(ExploreIntent.NavigateToPostDetail(post.id)) },
                        onShareClick = { onIntent(ExploreIntent.SharePost(post.id)) },
                        onMoreClick = { onIntent(ExploreIntent.ShowPostOptions(post)) },
                        onMediaClick = { index ->
                            onShowMediaViewer(post.mediaAttachments.toViewerItems(), index)
                        }
                    )
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                // 추가 로딩 스켈레톤
                if (state.isTrendingLoading && state.trendingPosts.isNotEmpty()) {
                    items(3) {
                        PostCardSkeleton()
                    }
                }
            }
        }
    }
}
