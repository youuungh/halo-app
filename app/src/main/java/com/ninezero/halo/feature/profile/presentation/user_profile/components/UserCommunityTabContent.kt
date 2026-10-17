package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.post.PostCard
import com.ninezero.halo.core.ui.components.post.PostCardSkeleton
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileIntent
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileState
import com.ninezero.halo.ui.theme.HaloTheme

@Composable
fun UserCommunityTabContent(
    state: UserProfileState,
    listState: LazyListState,
    onIntent: (UserProfileIntent) -> Unit,
    onPostClick: (Int) -> Unit,
    onProfileClick: (Int, IntOffset) -> Unit,
    onShowMediaViewer: (List<ViewerMediaItem>, Int) -> Unit
) {
    // 업로드 중인 내 글을 목록 상단에 Optimistic
    val communityPosts = state.pendingCommunityPosts + state.communityPosts
    val pinnedPosts = state.communityPinnedPosts
    val isLoading = state.isCommunityLoading
    val hasMore = state.communityHasMore

    // 차단한 사용자
    if (state.profile?.isBlocked == true) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
        ) {
            item(key = "blocked_message") { BlockedUserPlaceholder() }
        }
        return
    }

    LaunchedEffect(Unit) {
        if (state.communityPosts.isEmpty() && pinnedPosts.isEmpty() && !isLoading) {
            onIntent(UserProfileIntent.LoadCommunityPosts)
        }
    }

    if (isLoading && communityPosts.isEmpty() && pinnedPosts.isEmpty()) {
        // 초기 로딩 스켈레톤
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
        ) {
            items(5) { PostCardSkeleton() }
        }
    } else if (communityPosts.isEmpty() && pinnedPosts.isEmpty()) {
        // 빈 상태
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
        ) {
            item(key = "empty_community") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "아직 커뮤니티 글이 없어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.profile?.isFollowing == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "첫 번째 글을 작성해보세요!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = 16.dp)
        ) {
            // 공지 섹션
            if (pinnedPosts.isNotEmpty()) {
                item(key = "pinned_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_message_square_warning),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "공지",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                item(key = "pinned_card") {
                    PinnedAnnouncementCard(
                        posts = pinnedPosts,
                        onPostClick = onPostClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item(key = "pinned_divider") {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            // 일반 글 목록
            itemsIndexed(
                items = communityPosts,
                key = { _, post -> post.pendingId?.let { "pending_$it" } ?: "post_${post.id}" }
            ) { _, post ->
                // pending 카드는 클릭 차단
                PostCard(
                    post = post,
                    isUploading = post.isUploading,
                    onPostClick = { if (!post.isUploading) onPostClick(post.id) },
                    onProfileClick = { offset ->
                        if (!post.isUploading) onProfileClick(post.author.id, offset)
                    },
                    onLikeClick = {
                        if (!post.isUploading) onIntent(UserProfileIntent.LikeCommunityPost(post.id))
                    },
                    onCommentClick = { if (!post.isUploading) onPostClick(post.id) },
                    onShareClick = {
                        if (!post.isUploading) onIntent(UserProfileIntent.SharePost(post.id))
                    },
                    onMoreClick = {
                        if (!post.isUploading) onIntent(UserProfileIntent.ShowCommunityPostOptions(post))
                    },
                    onMediaClick = { index ->
                        onShowMediaViewer(post.mediaAttachments.toViewerItems(), index)
                    }
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
                    LaunchedEffect(state.isCommunityLoading, state.communityPosts.size) {
                        if (!state.isCommunityLoading && firedAtCount != state.communityPosts.size) {
                            firedAtCount = state.communityPosts.size
                            onIntent(UserProfileIntent.LoadMoreCommunityPosts)
                        }
                    }
                }
                items(3) { PostCardSkeleton() }
            }
        }
    }
}

private val previewAuthor = Author(
    id = 1,
    username = "creator",
    displayName = "크리에이터",
    avatarUrl = "https://picsum.photos/200"
)

private val previewMember = Author(
    id = 2,
    username = "member",
    displayName = "팬클럽회원",
    avatarUrl = "https://picsum.photos/201"
)

private val previewPinnedPosts = listOf(
    Post(
        id = 1,
        content = "[공지] 이번 주 라이브 방송 일정 안내드립니다!\n\n금요일 저녁 8시에 팬미팅 라이브가 있을 예정이에요. 많은 참여 부탁드려요~",
        author = previewAuthor,
        likeCount = 234,
        commentCount = 56,
        isPinned = true,
        createdAt = System.currentTimeMillis() - 3600000L,
    ),
    Post(
        id = 2,
        content = "[공지] 새 앨범 발매 기념 이벤트!\n\n멤버십 회원분들께 특별 포토카드를 증정합니다. 자세한 내용은 링크를 확인해주세요.",
        author = previewAuthor,
        likeCount = 189,
        commentCount = 42,
        isPinned = true,
        createdAt = System.currentTimeMillis() - 86400000L,
    )
)

private val previewCommunityPosts = listOf(
    // 텍스트만
    Post(
        id = 3,
        content = "오늘 콘서트 정말 최고였어요! 다음에 또 봐요~",
        author = previewMember,
        likeCount = 45,
        commentCount = 12,
        isLiked = true,
        createdAt = System.currentTimeMillis() - 1800000L,
    ),
    // TIER1 전용, 접근 불가
    Post(
        id = 4,
        content = "비하인드 사진 공개!\n\n이번 촬영 현장에서 찍은 미공개 사진들이에요~",
        author = previewAuthor,
        mediaAttachments = listOf(
            MediaItem(
                id = 2,
                type = MediaType.IMAGE,
                url = "https://picsum.photos/600/600",
                thumbnailUrl = null
            )
        ),
        likeCount = 312,
        commentCount = 89,
        requiredTier = "TIER1",
        canAccess = false,
        createdAt = System.currentTimeMillis() - 3600000L,
    ),
    // 이미지 1장
    Post(
        id = 5,
        content = "오늘 득템한 굿즈들! 포토카드 대박이에요",
        author = previewMember,
        mediaAttachments = listOf(
            MediaItem(
                id = 3,
                type = MediaType.IMAGE,
                url = "https://picsum.photos/600/601",
                thumbnailUrl = null
            )
        ),
        likeCount = 78,
        commentCount = 23,
        createdAt = System.currentTimeMillis() - 7200000L,
    ),
    // TIER2 전용, 접근 불가
    Post(
        id = 6,
        content = "다음 앨범 컨셉 힌트를 드릴게요! 이번에는 정말 특별한 준비를 하고 있어요...",
        author = previewAuthor,
        likeCount = 567,
        commentCount = 234,
        requiredTier = "TIER2",
        canAccess = false,
        createdAt = System.currentTimeMillis() - 9000000L,
    ),
    // 비밀글
    Post(
        id = 7,
        content = "크리에이터님께 드리는 비밀 메시지입니다...",
        author = previewMember,
        likeCount = 0,
        commentCount = 1,
        isSecret = true,
        canAccess = false,
        createdAt = System.currentTimeMillis() - 10800000L,
    ),
    // 이미지 여러 장
    Post(
        id = 8,
        content = "콘서트 현장 사진 공유합니다!",
        author = previewMember,
        mediaAttachments = listOf(
            MediaItem(id = 4, type = MediaType.IMAGE, url = "https://picsum.photos/800/600", thumbnailUrl = null),
            MediaItem(id = 5, type = MediaType.IMAGE, url = "https://picsum.photos/800/601", thumbnailUrl = null),
            MediaItem(id = 6, type = MediaType.IMAGE, url = "https://picsum.photos/800/602", thumbnailUrl = null)
        ),
        likeCount = 156,
        commentCount = 34,
        isLiked = true,
        createdAt = System.currentTimeMillis() - 12000000L,
    ),
    // 긴 텍스트
    Post(
        id = 9,
        content = "어제 팬미팅 후기입니다!\n\n처음으로 직접 만났는데 정말 친절하시더라고요. 사인회 때 눈도 마주치고 손도 잡았어요.\n\n다음에 또 기회가 있으면 꼭 가고 싶어요. 정말 잊지 못할 추억이 될 것 같아요.",
        author = previewMember,
        likeCount = 92,
        commentCount = 18,
        createdAt = System.currentTimeMillis() - 14400000L,
    )
)

private val previewRestrictedPosts = listOf(
    // TIER1 멤버십 전용
    Post(
        id = 101,
        content = "TIER1 멤버십 전용 콘텐츠입니다.\n\n비하인드 사진 공개!",
        author = previewAuthor,
        mediaAttachments = listOf(
            MediaItem(
                id = 101,
                type = MediaType.IMAGE,
                url = "https://picsum.photos/600/600",
                thumbnailUrl = null
            )
        ),
        likeCount = 312,
        commentCount = 89,
        requiredTier = "TIER1",
        canAccess = false,
        createdAt = System.currentTimeMillis() - 3600000L,
    ),
    // TIER2 멤버십 전용
    Post(
        id = 102,
        content = "TIER2 멤버십 전용 콘텐츠입니다.\n\n다음 앨범 컨셉 힌트를 드릴게요!",
        author = previewAuthor,
        likeCount = 567,
        commentCount = 234,
        requiredTier = "TIER2",
        canAccess = false,
        createdAt = System.currentTimeMillis() - 7200000L,
    ),
    // 비밀글
    Post(
        id = 103,
        content = "크리에이터님께 드리는 비밀 메시지입니다.\n\n이 글은 크리에이터만 볼 수 있어요.",
        author = previewMember,
        likeCount = 0,
        commentCount = 1,
        isSecret = true,
        canAccess = false,
        createdAt = System.currentTimeMillis() - 10800000L,
    )
)

@HaloLightDarkPreview
@Composable
private fun CommunityTabContentPreview() {
    HaloTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UserCommunityTabContent(
                state = UserProfileState(
                    selectedTab = ProfileTab.COMMUNITY,
                    communityPosts = previewCommunityPosts,
                    communityPinnedPosts = previewPinnedPosts,
                    isCommunityLoading = false,
                    communityHasMore = true
                ),
                listState = rememberLazyListState(),
                onIntent = {},
                onPostClick = {},
                onProfileClick = { _, _ -> },
                onShowMediaViewer = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommunityTabContentRestrictedPreview() {
    HaloTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UserCommunityTabContent(
                state = UserProfileState(
                    selectedTab = ProfileTab.COMMUNITY,
                    communityPosts = previewRestrictedPosts,
                    communityPinnedPosts = emptyList(),
                    isCommunityLoading = false,
                    communityHasMore = false
                ),
                listState = rememberLazyListState(),
                onIntent = {},
                onPostClick = {},
                onProfileClick = { _, _ -> },
                onShowMediaViewer = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommunityTabContentLoadingPreview() {
    HaloTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UserCommunityTabContent(
                state = UserProfileState(
                    selectedTab = ProfileTab.COMMUNITY,
                    communityPosts = emptyList(),
                    communityPinnedPosts = emptyList(),
                    isCommunityLoading = true,
                    communityHasMore = false
                ),
                listState = rememberLazyListState(),
                onIntent = {},
                onPostClick = {},
                onProfileClick = { _, _ -> },
                onShowMediaViewer = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommunityTabContentEmptyPreview() {
    HaloTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UserCommunityTabContent(
                state = UserProfileState(
                    selectedTab = ProfileTab.COMMUNITY,
                    communityPosts = emptyList(),
                    communityPinnedPosts = emptyList(),
                    isCommunityLoading = false,
                    communityHasMore = false
                ),
                listState = rememberLazyListState(),
                onIntent = {},
                onPostClick = {},
                onProfileClick = { _, _ -> },
                onShowMediaViewer = { _, _ -> }
            )
        }
    }
}
