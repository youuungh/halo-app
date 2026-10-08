package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.media.PrefetchUpcomingPostImages
import com.ninezero.halo.core.ui.components.media.rememberSettledVideoPostId
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.post.PostCard
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.core.ui.components.video_player.VideoPreloader
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.nextVideoUrlsAfter
import com.ninezero.halo.feature.profile.presentation.components.ProfileFilterChipsRow
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileIntent
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileState

@Composable
fun UserPostsTabContent(
    state: UserProfileState,
    listState: LazyListState,
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit,
    onProfileClick: (Author, IntOffset) -> Unit,
    onIntent: (UserProfileIntent) -> Unit
) {
    val isBlocked = state.profile?.isBlocked == true

    // 자동재생 후보는 영상 포스트만
    val videoPostIds = remember(state.userPosts) {
        state.userPosts
            .filter { post -> post.mediaAttachments.any { it.type == MediaType.VIDEO } }
            .mapTo(HashSet()) { it.id }
    }
    // 중앙 최근접 영상 1개만, 스크롤 정착 후 확정
    val visiblePostId by rememberSettledVideoPostId(listState, videoPostIds) {
        (it as? String)?.removePrefix("post_")?.toIntOrNull()
    }
    // 다음 영상 포스트 앞부분 미리 캐시 → 전환 지연 감소
    val context = LocalContext.current
    LaunchedEffect(visiblePostId, state.userPosts) {
        VideoPreloader.prefetch(context, state.userPosts.nextVideoUrlsAfter(visiblePostId, count = 2))
    }
    PrefetchUpcomingPostImages(listState = listState, posts = state.userPosts) {
        (it as? String)?.removePrefix("post_")?.toIntOrNull()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
    ) {
        if (isBlocked) {
            item(key = "blocked_message") { BlockedUserPlaceholder() }
            return@LazyColumn
        }

        // chip은 항상 상단 고정
        item(key = "posts_filter_chips") {
            ProfileFilterChipsRow(
                selectedContextType = state.postsContextType,
                selectedSortType = state.postsSortType,
                onContextTypeChange = { onIntent(UserProfileIntent.ChangePostsContextType(it)) },
                onSortTypeChange = { onIntent(UserProfileIntent.ChangePostsSortType(it)) }
            )
        }

        when {
            state.isPostsLoading && state.userPosts.isEmpty() -> {
                items(5) { PostCardSkeleton() }
            }
            state.userPosts.isEmpty() -> {
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
                items(
                    items = state.userPosts,
                    key = { post -> "post_${post.id}" }
                ) { post ->
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    PostCard(
                        post = post,
                        isVideoPlaying = visiblePostId == post.id,
                        onPostClick = { onIntent(UserProfileIntent.NavigateToPostDetail(post.id)) },
                        onProfileClick = { offset -> onProfileClick(post.author, offset) },
                        onLikeClick = {
                            if (post.isLiked) {
                                onIntent(UserProfileIntent.UnlikePost(post.id))
                            } else {
                                onIntent(UserProfileIntent.LikePost(post.id))
                            }
                        },
                        onCommentClick = { onIntent(UserProfileIntent.NavigateToPostDetail(post.id)) },
                        onShareClick = { onIntent(UserProfileIntent.SharePost(post.id)) },
                        onMoreClick = { onIntent(UserProfileIntent.ShowPostOptions(post)) },
                        onMediaClick = { index ->
                            onShowMediaViewer(post.mediaAttachments.toViewerItems(), index)
                        }
                    )
                }
                if (state.isPostsLoading && state.userPosts.isNotEmpty()) {
                    items(3) { PostCardSkeleton() }
                }
            }
        }
    }
}
