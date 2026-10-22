package com.ninezero.halo.feature.profile.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.badge.SecretPostBadge
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.comment.CommentItem
import com.ninezero.halo.core.ui.components.comment.CommentMedia
import com.ninezero.halo.core.ui.components.counter.AnimatedCounter
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.toViewerItems
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.LockedOverlay
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlay
import com.ninezero.halo.core.ui.components.overlay.ObscuredOverlayShape
import com.ninezero.halo.core.ui.components.text.buildHashtagAnnotatedString
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.PostSummaryData
import com.ninezero.halo.feature.profile.domain.model.ThreadCommentData
import com.ninezero.halo.feature.profile.domain.model.UserSummaryData
import com.ninezero.halo.ui.theme.HaloTheme
import com.ninezero.halo.ui.theme.Like
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/** 답글 탭 스레드 아이템 */
@Composable
fun CommentThreadItem(
    thread: CommentThreadData,
    onThreadClick: (Int) -> Unit,
    onDeletedPostClick: () -> Unit,
    onPostLikeClick: (Int) -> Unit,
    onPostCommentClick: (Int) -> Unit,
    onPostMoreClick: (Int) -> Unit,
    onCommentLikeClick: (Int) -> Unit,
    onCommentClick: (Int) -> Unit,
    onCommentMoreClick: (Int) -> Unit,
    onProfileClick: (Int, IntOffset) -> Unit,
    modifier: Modifier = Modifier,
    onHiddenPostClick: () -> Unit = {},
    onMediaClick: (List<ViewerMediaItem>, Int, String) -> Unit = { _, _, _ -> },
    activeVideoKey: String? = null
) {
    val horizontalPadding = 8.dp
    val avatarSize = 40.dp
    val avatarSpacing = 12.dp
    val contentStartPadding = horizontalPadding + avatarSize + avatarSpacing  // 8 + 40 + 12 = 60dp
    val lineWidth = 1.5.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val isSingleMyComment = thread.comments.size == 1 && thread.comments.first().isProfileUser
    val postMediaItems = thread.post.mediaAttachments
    val hasPostImages = postMediaItems.isNotEmpty()
    val isPostDeleted = thread.post.isDeleted
    val isPostHidden = thread.post.isHidden
    val isPostUnavailable = isPostDeleted || isPostHidden
    // 잠금 부모글
    val isPostRestricted = !thread.post.canAccess && !isPostUnavailable
    val postHazeState = remember { HazeState() }

    // 스레드 id + 슬롯으로 morph 키 분리
    val threadKey = thread.comments.lastOrNull()?.id ?: 0
    val postMediaOwnerKey = "thread$threadKey:post"

    var postAuthorProfilePosition by remember { mutableStateOf(IntOffset.Zero) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                when {
                    isPostDeleted -> onDeletedPostClick()
                    isPostHidden -> onHiddenPostClick()
                    else -> onThreadClick(thread.post.id)
                }
            }
            .padding(top = 12.dp)
    ) {
        // 단일 내 댓글인 경우: 포스트 정보를 먼저 표시
        if (isSingleMyComment) {
            // 포스트 작성자 + 내용
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .height(IntrinsicSize.Min)
            ) {
                // 왼쪽: 포스트 작성자 프로필 + 연결선 시작
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(avatarSize)
                ) {
                    // 삭제/숨김된 포스트는 프로필 placeholder로 표시
                    if (isPostUnavailable) {
                        Image(
                            painter = painterResource(R.drawable.ic_profile_placeholder),
                            contentDescription = null,
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        AsyncImage(
                            model = thread.post.author.avatarThumbUrl ?: thread.post.author.avatarUrl,
                            contentDescription = "포스트 작성자 프로필",
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .onGloballyPositioned { coordinates ->
                                    val position = coordinates.positionInWindow()
                                    postAuthorProfilePosition = IntOffset(
                                        position.x.toInt(),
                                        position.y.toInt() + coordinates.size.height
                                    )
                                }
                                .clickable(enabled = !thread.post.author.isDeleted) {  // 탈퇴자는 프로필로 이동 안 함
                                    onProfileClick(thread.post.author.id, postAuthorProfilePosition)
                                },
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_profile_placeholder),
                            error = painterResource(R.drawable.ic_profile_placeholder),
                            fallback = painterResource(R.drawable.ic_profile_placeholder)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .width(lineWidth)
                            .weight(1f)
                            .background(lineColor)
                    )
                }

                Spacer(modifier = Modifier.width(avatarSpacing))

                // 오른쪽: 포스트 정보
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp)
                ) {
                    // 원본 콘텐츠, 삭제돼도 표시
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isPostUnavailable || isPostRestricted) Modifier.heightIn(min = 80.dp) else Modifier)
                            .then(
                                if (isPostUnavailable || isPostRestricted) {
                                    Modifier.clip(ObscuredOverlayShape).hazeSource(state = postHazeState)
                                } else Modifier
                            )
                    ) {
                        // 포스트 작성자 이름 + 시간 + 더보기
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = thread.post.author.displayName ?: thread.post.author.username,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            VerifiedBadge(role = thread.post.author.role, size = 14.dp)
                            Text(
                                text = " · ${thread.post.createdAt.toRelativeTime()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (thread.post.isSecret && !isPostUnavailable && !isPostRestricted) SecretPostBadge()
                            Spacer(modifier = Modifier.weight(1f))
                            // 더보기 버튼, 삭제·숨김·잠금 시 숨김
                            if (!isPostUnavailable && !isPostRestricted) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                                    contentDescription = "더보기",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { onPostMoreClick(thread.post.id) }
                                        ),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 본문
                        if (thread.post.excerpt.isNotBlank()) {
                            val tagColor = MaterialTheme.colorScheme.onSurfaceVariant
                            val annotatedExcerpt = remember(thread.post.excerpt, tagColor) {
                                buildHashtagAnnotatedString(thread.post.excerpt, tagColor)
                            }
                            Text(
                                text = annotatedExcerpt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    // 삭제된 포스트 오버레이
                    if (isPostDeleted) {
                        ObscuredOverlay(
                            hazeState = postHazeState,
                            icon = R.drawable.ic_delete,
                            label = "삭제된 포스트입니다",
                            contentDescription = "삭제됨"
                        )
                    }

                    // 유효하지 않은 포스트 오버레이
                    if (isPostHidden) {
                        ObscuredOverlay(
                            hazeState = postHazeState,
                            icon = R.drawable.ic_eye_off,
                            label = "유효하지 않은 포스트입니다",
                            contentDescription = "유효하지 않음"
                        )
                    }

                    // 잠금 포스트 오버레이
                    if (isPostRestricted) {
                        LockedOverlay(
                            hazeState = postHazeState,
                            icon = R.drawable.ic_lock,
                            label = if (thread.post.isSecret) "비밀글" else "${thread.post.requiredTier} 이상 멤버십 전용",
                            contentDescription = "잠금"
                        )
                    }
                }
            }

            // 미디어 영역, 삭제·숨김 포스트는 숨김
            if (hasPostImages && !isPostUnavailable) {
                CommentMedia(
                    mediaAttachments = postMediaItems,
                    startPadding = contentStartPadding,
                    endPadding = horizontalPadding,
                    showLine = true,
                    linePadStart = horizontalPadding + (avatarSize - lineWidth) / 2,
                    lineWidth = lineWidth,
                    lineColor = lineColor,
                    onMediaClick = { index -> onMediaClick(postMediaItems.toViewerItems(), index, postMediaOwnerKey) },
                    isVideoPlaying = activeVideoKey == postVideoKey(thread.post.id),
                    mediaOwnerKey = postMediaOwnerKey
                )
            }

            // 삭제/숨김/잠금 포스트는 버튼 영역 숨김
            if (!isPostUnavailable && !isPostRestricted) Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // 선
                Box(
                    modifier = Modifier
                        .padding(start = horizontalPadding + (avatarSize - lineWidth) / 2)
                        .width(lineWidth)
                        .fillMaxHeight()
                        .background(lineColor)
                )

                // 좋아요/댓글 버튼
                Row(
                    modifier = Modifier
                        .padding(start = contentStartPadding, end = horizontalPadding)
                        .padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onPostLikeClick(thread.post.id) }
                        )
                    ) {
                        Icon(
                            painter = painterResource(
                                if (thread.post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                            ),
                            contentDescription = "좋아요",
                            modifier = Modifier
                                .size(20.dp)
                                .bounceClick(),
                            tint = if (thread.post.isLiked) {
                                Like
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (thread.post.likeCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            AnimatedCounter(
                                count = thread.post.likeCount,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (thread.post.isLiked) Like else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onPostCommentClick(thread.post.id) }
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_comment),
                            contentDescription = "댓글",
                            modifier = Modifier
                                .size(20.dp)
                                .bounceClick(),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        if (thread.post.commentCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            AnimatedCounter(
                                count = thread.post.commentCount,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 댓글 스레드
        thread.comments.forEachIndexed { index, comment ->
            val isLast = index == thread.comments.lastIndex
            val showLine = !isLast
            // 댓글별 슬롯 owner 키
            val commentMediaOwnerKey = "thread$threadKey:c${comment.id}"

            // 답글 대상 텍스트, 프로필 유저·단일 댓글 제외
            val replyToText = if (!comment.isProfileUser && !isSingleMyComment) {
                val replyTarget = comment.replyTo ?: thread.post.author
                "${replyTarget.displayName ?: replyTarget.username}님에게 남긴 답글"
            } else null

            CommentItem(
                authorName = comment.author.displayName ?: comment.author.username,
                authorRole = comment.author.role,
                authorAvatarUrl = comment.author.avatarThumbUrl ?: comment.author.avatarUrl,
                content = comment.content,
                createdAt = comment.createdAt,
                likeCount = comment.likeCount,
                isLiked = comment.isLiked,
                isBlocked = comment.isBlocked,
                isBlinded = comment.isBlinded,
                isDeleted = comment.isDeleted,
                showLine = showLine,
                mediaAttachments = comment.mediaAttachments,
                replyCount = comment.replyCount,
                replyToText = replyToText,
                authorFontWeight = if (comment.isProfileUser) FontWeight.Bold else FontWeight.SemiBold,
                horizontalPadding = horizontalPadding,
                avatarSize = avatarSize,
                avatarSpacing = avatarSpacing,
                lineWidth = lineWidth,
                lineColor = lineColor,
                onLikeClick = { onCommentLikeClick(comment.id) },
                // 본문·답글 아이콘 모두 그 댓글의 상세로
                onReplyClick = { onCommentClick(comment.id) },
                onCommentClick = { onCommentClick(comment.id) },
                onMoreClick = { onCommentMoreClick(comment.id) },
                onProfileClick = { offset -> if (!comment.author.isDeleted) onProfileClick(comment.author.id, offset) },
                onMediaClick = { index ->
                    onMediaClick(comment.mediaAttachments.toViewerItems(), index, commentMediaOwnerKey)
                },
                isVideoPlaying = activeVideoKey == commentVideoKey(comment.id),
                mediaOwnerKey = commentMediaOwnerKey
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentThreadItemPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentThreadItem(
                thread = CommentThreadData(
                    comments = listOf(
                        ThreadCommentData(
                            id = 1,
                            content = "원글입니다. 좋은 하루 되세요!",
                            author = UserSummaryData(
                                id = 2,
                                username = "user1",
                                displayName = "사용자1",
                                avatarUrl = null
                            ),
                            replyTo = null,
                            createdAt = System.currentTimeMillis() - 7200000,
                            likeCount = 5,
                            replyCount = 2,
                            isLiked = false,
                            isProfileUser = false
                        ),
                        ThreadCommentData(
                            id = 2,
                            content = "정말 좋은 포스트네요! 많은 도움이 되었습니다.",
                            author = UserSummaryData(
                                id = 1,
                                username = "me",
                                displayName = "나",
                                avatarUrl = null
                            ),
                            replyTo = UserSummaryData(
                                id = 2,
                                username = "user1",
                                displayName = "사용자1",
                                avatarUrl = null
                            ),
                            createdAt = System.currentTimeMillis() - 3600000,
                            likeCount = 3,
                            replyCount = 1,
                            isLiked = true,
                            isProfileUser = true
                        )
                    ),
                    post = PostSummaryData(
                        id = 1,
                        excerpt = "오늘 새로운 프로젝트를 시작했습니다. 많은 관심 부탁드려요!",
                        mediaAttachments = emptyList(),
                        author = UserSummaryData(
                            id = 3,
                            username = "creator",
                            displayName = "크리에이터",
                            avatarUrl = null
                        )
                    )
                ),
                onThreadClick = {},
                onDeletedPostClick = {},
                onPostLikeClick = {},
                onPostCommentClick = {},
                onPostMoreClick = {},
                onCommentLikeClick = {},
                onCommentClick = {},
                onCommentMoreClick = {},
                onProfileClick = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentThreadItemSingleCommentPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentThreadItem(
                thread = CommentThreadData(
                    comments = listOf(
                        ThreadCommentData(
                            id = 1,
                            content = "정말 멋진 사진이네요! 제주도 언제 가보고 싶었는데 이 사진 보니까 더 가고 싶어졌어요 😊",
                            author = UserSummaryData(
                                id = 1,
                                username = "me",
                                displayName = "나",
                                avatarUrl = null
                            ),
                            replyTo = null,
                            createdAt = System.currentTimeMillis() - 3600000,
                            likeCount = 2,
                            replyCount = 0,
                            isLiked = false,
                            isProfileUser = true
                        )
                    ),
                    post = PostSummaryData(
                        id = 1,
                        excerpt = "제주도 성산일출봉에서 찍은 일출 사진입니다. 새벽 5시에 일어나서 찍었어요!",
                        mediaAttachments = listOf(
                            MediaItem(id = 1, type = MediaType.IMAGE, url = "https://picsum.photos/400/300", thumbnailUrl = null),
                            MediaItem(id = 2, type = MediaType.IMAGE, url = "https://picsum.photos/401/300", thumbnailUrl = null)
                        ),
                        author = UserSummaryData(
                            id = 2,
                            username = "traveler",
                            displayName = "여행자",
                            avatarUrl = null
                        ),
                        createdAt = System.currentTimeMillis() - 86400000,
                        likeCount = 24,
                        commentCount = 5,
                        isLiked = true
                    )
                ),
                onThreadClick = {},
                onDeletedPostClick = {},
                onPostLikeClick = {},
                onPostCommentClick = {},
                onPostMoreClick = {},
                onCommentLikeClick = {},
                onCommentClick = {},
                onCommentMoreClick = {},
                onProfileClick = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentThreadItemSingleCommentNoImagePreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentThreadItem(
                thread = CommentThreadData(
                    comments = listOf(
                        ThreadCommentData(
                            id = 1,
                            content = "좋은 글 감사합니다! 많은 도움이 됐어요.",
                            author = UserSummaryData(
                                id = 1,
                                username = "me",
                                displayName = "나",
                                avatarUrl = null
                            ),
                            replyTo = null,
                            createdAt = System.currentTimeMillis() - 7200000,
                            likeCount = 5,
                            replyCount = 1,
                            isLiked = true,
                            isProfileUser = true
                        )
                    ),
                    post = PostSummaryData(
                        id = 1,
                        excerpt = "오늘 배운 Kotlin 코루틴 정리입니다. suspend 함수와 Flow에 대해 알아보았어요.",
                        mediaAttachments = emptyList(),
                        author = UserSummaryData(
                            id = 3,
                            username = "developer",
                            displayName = "개발자",
                            avatarUrl = null
                        ),
                        createdAt = System.currentTimeMillis() - 3600000,
                        likeCount = 15,
                        commentCount = 3,
                        isLiked = false
                    )
                ),
                onThreadClick = {},
                onDeletedPostClick = {},
                onPostLikeClick = {},
                onPostCommentClick = {},
                onPostMoreClick = {},
                onCommentLikeClick = {},
                onCommentClick = {},
                onCommentMoreClick = {},
                onProfileClick = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentThreadItemBlockedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentThreadItem(
                thread = CommentThreadData(
                    comments = listOf(
                        ThreadCommentData(
                            id = 1,
                            content = "",
                            author = UserSummaryData(
                                id = 2,
                                username = "blocked_user",
                                displayName = "차단된 사용자",
                                avatarUrl = null
                            ),
                            replyTo = null,
                            createdAt = System.currentTimeMillis() - 7200000,
                            likeCount = 0,
                            replyCount = 1,
                            isLiked = false,
                            isProfileUser = false,
                            isBlocked = true
                        ),
                        ThreadCommentData(
                            id = 2,
                            content = "내 답글입니다.",
                            author = UserSummaryData(
                                id = 1,
                                username = "me",
                                displayName = "나",
                                avatarUrl = null
                            ),
                            replyTo = UserSummaryData(
                                id = 2,
                                username = "blocked_user",
                                displayName = "차단된 사용자",
                                avatarUrl = null
                            ),
                            createdAt = System.currentTimeMillis() - 3600000,
                            likeCount = 1,
                            replyCount = 0,
                            isLiked = false,
                            isProfileUser = true
                        )
                    ),
                    post = PostSummaryData(
                        id = 1,
                        excerpt = "포스트 내용입니다.",
                        mediaAttachments = listOf(
                            MediaItem(id = 1, type = MediaType.IMAGE, url = "https://example.com/image.jpg", thumbnailUrl = null)
                        ),
                        author = UserSummaryData(
                            id = 3,
                            username = "author",
                            displayName = "작성자",
                            avatarUrl = null
                        )
                    )
                ),
                onThreadClick = {},
                onDeletedPostClick = {},
                onPostLikeClick = {},
                onPostCommentClick = {},
                onPostMoreClick = {},
                onCommentLikeClick = {},
                onCommentClick = {},
                onCommentMoreClick = {},
                onProfileClick = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentThreadItemDeletedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentThreadItem(
                thread = CommentThreadData(
                    comments = listOf(
                        ThreadCommentData(
                            id = 1,
                            content = "",
                            author = UserSummaryData(
                                id = 2,
                                username = "deleted_user",
                                displayName = "삭제된 댓글 작성자",
                                avatarUrl = null
                            ),
                            replyTo = null,
                            createdAt = System.currentTimeMillis() - 7200000,
                            likeCount = 0,
                            replyCount = 1,
                            isLiked = false,
                            isProfileUser = false,
                            isDeleted = true
                        ),
                        ThreadCommentData(
                            id = 2,
                            content = "삭제된 댓글에 대한 내 답글입니다.",
                            author = UserSummaryData(
                                id = 1,
                                username = "me",
                                displayName = "나",
                                avatarUrl = null
                            ),
                            replyTo = UserSummaryData(
                                id = 2,
                                username = "deleted_user",
                                displayName = "삭제된 댓글 작성자",
                                avatarUrl = null
                            ),
                            createdAt = System.currentTimeMillis() - 3600000,
                            likeCount = 1,
                            replyCount = 0,
                            isLiked = false,
                            isProfileUser = true
                        )
                    ),
                    post = PostSummaryData(
                        id = 1,
                        excerpt = "포스트 내용입니다.",
                        mediaAttachments = emptyList(),
                        author = UserSummaryData(
                            id = 3,
                            username = "author",
                            displayName = "작성자",
                            avatarUrl = null
                        )
                    )
                ),
                onThreadClick = {},
                onDeletedPostClick = {},
                onPostLikeClick = {},
                onPostCommentClick = {},
                onPostMoreClick = {},
                onCommentLikeClick = {},
                onCommentClick = {},
                onCommentMoreClick = {},
                onProfileClick = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentThreadItemDeletedPostPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentThreadItem(
                thread = CommentThreadData(
                    comments = listOf(
                        ThreadCommentData(
                            id = 1,
                            content = "삭제된 포스트에 남긴 내 댓글입니다.",
                            author = UserSummaryData(
                                id = 1,
                                username = "me",
                                displayName = "나",
                                avatarUrl = null
                            ),
                            replyTo = null,
                            createdAt = System.currentTimeMillis() - 3600000,
                            likeCount = 2,
                            replyCount = 0,
                            isLiked = false,
                            isProfileUser = true
                        )
                    ),
                    post = PostSummaryData(
                        id = 1,
                        excerpt = "",
                        mediaAttachments = emptyList(),
                        author = UserSummaryData(
                            id = 2,
                            username = "deleted_author",
                            displayName = "삭제된 포스트 작성자",
                            avatarUrl = null
                        ),
                        createdAt = System.currentTimeMillis() - 86400000,
                        likeCount = 0,
                        commentCount = 0,
                        isLiked = false,
                        isDeleted = true
                    )
                ),
                onThreadClick = {},
                onDeletedPostClick = {},
                onPostLikeClick = {},
                onPostCommentClick = {},
                onPostMoreClick = {},
                onCommentLikeClick = {},
                onCommentClick = {},
                onCommentMoreClick = {},
                onProfileClick = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentThreadItemCommentWithImagesPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentThreadItem(
                thread = CommentThreadData(
                    comments = listOf(
                        ThreadCommentData(
                            id = 1,
                            content = "이미지가 포함된 댓글입니다!",
                            mediaAttachments = listOf(
                                MediaItem(id = 1, type = MediaType.IMAGE, url = "https://picsum.photos/400/300", thumbnailUrl = null),
                                MediaItem(id = 2, type = MediaType.IMAGE, url = "https://picsum.photos/400/301", thumbnailUrl = null)
                            ),
                            author = UserSummaryData(
                                id = 1,
                                username = "me",
                                displayName = "나",
                                avatarUrl = null
                            ),
                            replyTo = null,
                            createdAt = System.currentTimeMillis() - 3600000,
                            likeCount = 5,
                            replyCount = 0,
                            isLiked = true,
                            isProfileUser = true
                        )
                    ),
                    post = PostSummaryData(
                        id = 1,
                        excerpt = "오늘 새로운 프로젝트를 시작했습니다!",
                        mediaAttachments = emptyList(),
                        author = UserSummaryData(
                            id = 2,
                            username = "developer",
                            displayName = "개발자",
                            avatarUrl = null
                        ),
                        createdAt = System.currentTimeMillis() - 86400000,
                        likeCount = 10,
                        commentCount = 3,
                        isLiked = false
                    )
                ),
                onThreadClick = {},
                onDeletedPostClick = {},
                onPostLikeClick = {},
                onPostCommentClick = {},
                onPostMoreClick = {},
                onCommentLikeClick = {},
                onCommentClick = {},
                onCommentMoreClick = {},
                onProfileClick = { _, _ -> }
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentThreadItemCommentWithVideoPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CommentThreadItem(
                thread = CommentThreadData(
                    comments = listOf(
                        ThreadCommentData(
                            id = 1,
                            content = "비디오가 포함된 댓글입니다!",
                            mediaAttachments = listOf(
                                MediaItem(id = 1, type = MediaType.VIDEO, url = "https://example.com/video.mp4", thumbnailUrl = null)
                            ),
                            author = UserSummaryData(
                                id = 1,
                                username = "me",
                                displayName = "나",
                                avatarUrl = null
                            ),
                            replyTo = null,
                            createdAt = System.currentTimeMillis() - 3600000,
                            likeCount = 5,
                            replyCount = 0,
                            isLiked = true,
                            isProfileUser = true
                        )
                    ),
                    post = PostSummaryData(
                        id = 1,
                        excerpt = "오늘 새로운 프로젝트를 시작했습니다!",
                        mediaAttachments = emptyList(),
                        author = UserSummaryData(
                            id = 2,
                            username = "developer",
                            displayName = "개발자",
                            avatarUrl = null
                        ),
                        createdAt = System.currentTimeMillis() - 86400000,
                        likeCount = 10,
                        commentCount = 3,
                        isLiked = false
                    )
                ),
                onThreadClick = {},
                onDeletedPostClick = {},
                onPostLikeClick = {},
                onPostCommentClick = {},
                onPostMoreClick = {},
                onCommentLikeClick = {},
                onCommentClick = {},
                onCommentMoreClick = {},
                onProfileClick = { _, _ -> }
            )
        }
    }
}
