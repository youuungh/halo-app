package com.ninezero.halo.feature.profile.presentation.my_profile.components

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
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.feature.profile.presentation.components.CommentThreadItem
import com.ninezero.halo.feature.profile.presentation.components.ProfileFilterChipsRow
import com.ninezero.halo.feature.profile.presentation.components.ProfileTabRowHeight
import com.ninezero.halo.feature.profile.presentation.components.rememberActiveVideoKey
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileIntent
import com.ninezero.halo.feature.profile.presentation.my_profile.MyProfileState

@Composable
fun MyRepliesTabContent(
    state: MyProfileState,
    listState: LazyListState,
    onShowMediaViewer: (List<ViewerMediaItem>, Int, String?) -> Unit,
    onProfileClick: (Int, IntOffset) -> Unit,
    onIntent: (MyProfileIntent) -> Unit
) {
    val activeVideoKey = rememberActiveVideoKey(state.myCommentThreads, listState)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = ProfileTabRowHeight, bottom = LocalBottomBarPadding.current)
    ) {
        // chip은 항상 상단 고정
        item(key = "replies_filter_chips") {
            ProfileFilterChipsRow(
                selectedContextType = state.repliesContextType,
                selectedSortType = state.repliesSortType,
                onContextTypeChange = { onIntent(MyProfileIntent.ChangeRepliesContextType(it)) },
                onSortTypeChange = { onIntent(MyProfileIntent.ChangeRepliesSortType(it)) }
            )
        }

        // 답글 목록
        when {
            state.isRepliesLoading && state.myCommentThreads.isEmpty() -> {
                items(3) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        CommentItemSkeleton(showLine = true)
                        CommentItemSkeleton(showLine = false)
                    }
                }
            }

            state.myCommentThreads.isEmpty() -> {
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
                    items = state.myCommentThreads,
                    // 스레드 고유 식별자 - 본인 댓글 id는 전역 유일
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
                        onThreadClick = { onIntent(MyProfileIntent.NavigateToPostDetail(it)) },
                        onDeletedPostClick = { onIntent(MyProfileIntent.ShowDeletedPostSnackbar) },
                        onHiddenPostClick = { onIntent(MyProfileIntent.ShowHiddenPostSnackbar) },
                        onPostLikeClick = { postId ->
                            onIntent(MyProfileIntent.ToggleThreadPostLike(postId))
                        },
                        onPostCommentClick = { postId ->
                            onIntent(MyProfileIntent.NavigateToPostDetail(postId))
                        },
                        onPostMoreClick = { postId ->
                            onIntent(MyProfileIntent.ShowThreadPostOptions(thread.post))
                        },
                        onCommentLikeClick = { commentId ->
                            val comment = thread.comments.find { it.id == commentId }
                            if (comment?.isLiked == true) {
                                onIntent(MyProfileIntent.UnlikeComment(commentId))
                            } else {
                                onIntent(MyProfileIntent.LikeComment(commentId))
                            }
                        },
                        onCommentClick = { commentId ->
                            onIntent(MyProfileIntent.NavigateToCommentDetail(commentId))
                        },
                        onCommentMoreClick = { commentId ->
                            val comment = thread.comments.find { it.id == commentId }
                            comment?.let {
                                onIntent(MyProfileIntent.ShowCommentOptions(it, thread))
                            }
                        },
                        onProfileClick = { userId, offset ->
                            onProfileClick(userId, offset)
                        },
                        onMediaClick = { items, index, ownerKey -> onShowMediaViewer(items, index, ownerKey) },
                        activeVideoKey = activeVideoKey
                    )
                    HorizontalDivider(
                        thickness = 0.25.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                if (state.isRepliesLoading && state.myCommentThreads.isNotEmpty()) {
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
