package com.ninezero.halo.feature.profile.presentation.user_profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.comment.CommentItemSkeleton
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.feature.profile.domain.model.UserSummaryData
import com.ninezero.halo.feature.profile.presentation.components.CommentThreadItem
import com.ninezero.halo.feature.profile.presentation.components.ProfileFilterChipsRow
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.components.rememberActiveVideoKey
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileIntent
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileState

@Composable
fun UserRepliesTabContent(
    state: UserProfileState,
    listState: LazyListState,
    onShowMediaViewer: (List<ViewerMediaItem>, Int, String?) -> Unit,
    onProfileClick: (UserSummaryData, IntOffset) -> Unit,
    onIntent: (UserProfileIntent) -> Unit
) {
    val isBlocked = state.profile?.isBlocked == true
    val activeVideoKey = rememberActiveVideoKey(state.userCommentThreads, listState)

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
        item(key = "replies_filter_chips") {
            ProfileFilterChipsRow(
                selectedContextType = state.repliesContextType,
                selectedSortType = state.repliesSortType,
                onContextTypeChange = { onIntent(UserProfileIntent.ChangeRepliesContextType(it)) },
                onSortTypeChange = { onIntent(UserProfileIntent.ChangeRepliesSortType(it)) }
            )
        }

        when {
            state.isRepliesLoading && state.userCommentThreads.isEmpty() -> {
                items(3) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        CommentItemSkeleton(showLine = true)
                        CommentItemSkeleton(showLine = false)
                    }
                }
            }
            state.userCommentThreads.isEmpty() -> {
                item(key = "empty_replies") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 답글이 없어요",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                itemsIndexed(
                    items = state.userCommentThreads,
                    // 스레드 고유 식별자
                    key = { _, thread -> "thread_${thread.post.id}_${thread.comments.lastOrNull()?.id ?: 0}" }
                ) { index, thread ->
                    if (index == 0) {
                        HorizontalDivider(
                            thickness = 0.25.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    CommentThreadItem(
                        thread = thread,
                        onThreadClick = { onIntent(UserProfileIntent.NavigateToPostDetail(it)) },
                        onDeletedPostClick = { onIntent(UserProfileIntent.ShowDeletedPostSnackbar) },
                        onHiddenPostClick = { onIntent(UserProfileIntent.ShowRestrictedContentSnackbar("유효하지 않은 포스트입니다")) },
                        onPostLikeClick = { postId -> onIntent(UserProfileIntent.ToggleThreadPostLike(postId)) },
                        onPostCommentClick = { postId -> onIntent(UserProfileIntent.NavigateToPostDetail(postId)) },
                        onPostMoreClick = { onIntent(UserProfileIntent.ShowThreadPostOptions(thread.post)) },
                        onCommentLikeClick = { commentId ->
                            val comment = thread.comments.find { it.id == commentId }
                            if (comment?.isLiked == true) {
                                onIntent(UserProfileIntent.UnlikeComment(commentId))
                            } else {
                                onIntent(UserProfileIntent.LikeComment(commentId))
                            }
                        },
                        onCommentClick = { commentId -> onIntent(UserProfileIntent.NavigateToCommentDetail(commentId)) },
                        onCommentMoreClick = { commentId ->
                            val comment = thread.comments.find { it.id == commentId }
                            comment?.let { onIntent(UserProfileIntent.ShowCommentOptions(it, thread)) }
                        },
                        onProfileClick = { userId, offset ->
                            val author = if (thread.post.author.id == userId) {
                                thread.post.author
                            } else {
                                thread.comments.find { it.author.id == userId }?.author
                            }
                            if (author != null) {
                                onProfileClick(author, offset)
                            }
                        },
                        onMediaClick = { items, index, ownerKey -> onShowMediaViewer(items, index, ownerKey) },
                        activeVideoKey = activeVideoKey
                    )
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                if (state.isRepliesLoading && state.userCommentThreads.isNotEmpty()) {
                    items(2) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            CommentItemSkeleton(showLine = true)
                            CommentItemSkeleton(showLine = false)
                        }
                    }
                }
            }
        }
    }
}
