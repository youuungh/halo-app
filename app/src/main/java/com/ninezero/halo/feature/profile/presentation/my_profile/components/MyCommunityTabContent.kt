package com.ninezero.halo.feature.profile.presentation.my_profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.post.PostCard
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileIntent
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileState

@Composable
fun MyCommunityTabContent(
    state: MyProfileState,
    listState: LazyListState,
    onIntent: (MyProfileIntent) -> Unit,
    onPostClick: (Int) -> Unit,
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit,
    onProfileClick: (Int, IntOffset) -> Unit = { _, _ -> }
) {
    // 업로드 중인 내 글을 목록 상단에 Optimistic
    val communityPosts = state.pendingCommunityPosts + state.communityPosts
    val pinnedPosts = state.communityPinnedPosts
    val isLoading = state.isCommunityLoading
    val hasMore = state.communityHasMore

    LaunchedEffect(Unit) {
        if (state.communityPosts.isEmpty() && pinnedPosts.isEmpty() && !isLoading) {
            onIntent(MyProfileIntent.LoadCommunityPosts)
        }
    }

    if (isLoading && communityPosts.isEmpty() && pinnedPosts.isEmpty()) {
        // 초기 로딩 스켈레톤
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = LocalBottomBarPadding.current)
        ) {
            items(3) { PostCardSkeleton() }
        }
    } else if (communityPosts.isEmpty() && pinnedPosts.isEmpty()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = LocalBottomBarPadding.current)
        ) {
            item(key = "empty_community") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 커뮤니티 글이 없어요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        // 실제 콘텐츠
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = LocalBottomBarPadding.current)
        ) {
            // 고정 글
            if (pinnedPosts.isNotEmpty()) {
                itemsIndexed(
                    items = pinnedPosts,
                    key = { _, post -> "pinned_${post.id}" }
                ) { _, post ->
                    // 고정됨 표시
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 60.dp, end = 16.dp, top = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_pin),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "고정됨",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    PostCard(
                        post = post,
                        onPostClick = { onPostClick(post.id) },
                        onCommentClick = { onPostClick(post.id) },
                        onLikeClick = { onIntent(MyProfileIntent.LikeCommunityPost(post.id)) },
                        onShareClick = { onIntent(MyProfileIntent.SharePost(post.id)) },
                        onMoreClick = { onIntent(MyProfileIntent.ShowCommunityPostOptions(post)) },
                        onMediaClick = { index -> onShowMediaViewer(post.mediaAttachments.toViewerItems(), index) },
                        onProfileClick = { offset -> onProfileClick(post.author.id, offset) }
                    )
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            // 일반 글
            itemsIndexed(
                items = communityPosts,
                key = { _, post -> post.pendingId?.let { "pending_$it" } ?: "community_${post.id}" }
            ) { _, post ->
                // pending 카드는 클릭 차단
                PostCard(
                    post = post,
                    isUploading = post.isUploading,
                    onPostClick = { if (!post.isUploading) onPostClick(post.id) },
                    onCommentClick = { if (!post.isUploading) onPostClick(post.id) },
                    onLikeClick = { if (!post.isUploading) onIntent(MyProfileIntent.LikeCommunityPost(post.id)) },
                    onShareClick = { if (!post.isUploading) onIntent(MyProfileIntent.SharePost(post.id)) },
                    onMoreClick = { if (!post.isUploading) onIntent(MyProfileIntent.ShowCommunityPostOptions(post)) },
                    onMediaClick = { idx -> onShowMediaViewer(post.mediaAttachments.toViewerItems(), idx) },
                    onProfileClick = { offset -> if (!post.isUploading) onProfileClick(post.author.id, offset) }
                )
                HorizontalDivider(
                    thickness = 0.25.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // 추가 로딩
            if (hasMore) {
                item(key = "load_more_trigger") {
                    // 목록 수당 한 번, InfiniteScrollTrigger와 같은 규칙
                    var firedAtCount by remember { mutableStateOf<Int?>(null) }
                    LaunchedEffect(isLoading, state.communityPosts.size) {
                        if (!isLoading && firedAtCount != state.communityPosts.size) {
                            firedAtCount = state.communityPosts.size
                            onIntent(MyProfileIntent.LoadMoreCommunityPosts)
                        }
                    }
                }
            }

            // 로딩 인디케이터
            if (isLoading && communityPosts.isNotEmpty()) {
                item(key = "loading_more") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PostCardSkeleton()
                    }
                }
            }
        }
    }
}
