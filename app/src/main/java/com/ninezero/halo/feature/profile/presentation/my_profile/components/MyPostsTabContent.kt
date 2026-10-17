package com.ninezero.halo.feature.profile.presentation.my_profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.media.PrefetchUpcomingPostImages
import com.ninezero.halo.core.ui.components.media.rememberSettledVideoPostId
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.post.PostCard
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.core.ui.components.video_player.VideoPreloader
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.nextVideoUrlsAfter
import com.ninezero.halo.feature.profile.presentation.components.ProfileFilterChipsRow
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileIntent
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileState

@Composable
fun MyPostsTabContent(
    state: MyProfileState,
    listState: LazyListState,
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit,
    onIntent: (MyProfileIntent) -> Unit
) {
    val allPosts = state.pendingPosts + state.myPosts

    // 자동재생 후보는 영상 포스트만
    val videoPostIds = remember(state.myPosts) {
        state.myPosts
            .filter { post -> post.mediaAttachments.any { it.type == MediaType.VIDEO } }
            .mapTo(HashSet()) { it.id }
    }
    // 중앙 최근접 영상 1개만, 스크롤 정착 후 확정
    // pending 키는 id 변환 실패라 재생 제외
    val visiblePostId by rememberSettledVideoPostId(listState, videoPostIds) {
        (it as? String)?.removePrefix("post_")?.toIntOrNull()
    }
    // 다음 영상 포스트 앞부분 미리 캐시 → 전환 지연 감소
    val context = LocalContext.current
    LaunchedEffect(visiblePostId, state.myPosts) {
        VideoPreloader.prefetch(context, state.myPosts.nextVideoUrlsAfter(visiblePostId, count = 2))
    }
    PrefetchUpcomingPostImages(listState = listState, posts = state.myPosts) {
        (it as? String)?.takeIf { k -> k.startsWith("post_") }?.removePrefix("post_")?.toIntOrNull()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = LocalBottomBarPadding.current)
    ) {
        // chip은 항상 상단 고정
        item(key = "posts_filter_chips") {
            ProfileFilterChipsRow(
                selectedContextType = state.postsContextType,
                selectedSortType = state.postsSortType,
                onContextTypeChange = { onIntent(MyProfileIntent.ChangePostsContextType(it)) },
                onSortTypeChange = { onIntent(MyProfileIntent.ChangePostsSortType(it)) }
            )
        }

        when {
            state.isPostsLoading && allPosts.isEmpty() -> {
                items(5) {
                    PostCardSkeleton()
                }
            }

            allPosts.isEmpty() -> {
                item(key = "empty_posts") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 포스트가 없어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                itemsIndexed(
                    items = allPosts,
                    key = { _, post -> post.pendingId?.let { "pending_$it" } ?: "post_${post.id}" }
                ) { index, post ->
                    Column {
                        if (index == 0) {
                            HorizontalDivider(
                                thickness = 0.25.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                        PostCard(
                            post = post,
                            isUploading = post.isUploading,
                        isVideoPlaying = visiblePostId == post.id,
                        onPostClick = {
                            if (!post.isUploading) {
                                onIntent(MyProfileIntent.NavigateToPostDetail(post.id))
                            }
                        },
                        onProfileClick = {},
                        onLikeClick = {
                            if (!post.isUploading) {
                                if (post.isLiked) {
                                    onIntent(MyProfileIntent.UnlikePost(post.id))
                                } else {
                                    onIntent(MyProfileIntent.LikePost(post.id))
                                }
                            }
                        },
                        onCommentClick = {
                            if (!post.isUploading) {
                                onIntent(MyProfileIntent.NavigateToPostDetail(post.id))
                            }
                        },
                        onShareClick = {
                            if (!post.isUploading) {
                                onIntent(MyProfileIntent.SharePost(post.id))
                            }
                        },
                        onMoreClick = {
                            if (!post.isUploading) {
                                onIntent(MyProfileIntent.ShowPostOptions(post))
                            }
                        },
                        onMediaClick = { index ->
                            if (!post.isUploading) {
                                onShowMediaViewer(post.mediaAttachments.toViewerItems(), index)
                            }
                        }
                    )
                        HorizontalDivider(
                            thickness = 0.25.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                if (state.isPostsLoading && state.myPosts.isNotEmpty()) {
                    items(3) {
                        PostCardSkeleton()
                    }
                }
            }
        }
    }
}
