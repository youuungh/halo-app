package com.ninezero.halo.feature.notification.presentation.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import com.ninezero.halo.core.ui.theme.Like
import com.ninezero.halo.feature.notification.domain.model.Notification
import com.ninezero.halo.feature.notification.domain.model.NotificationType
import kotlinx.coroutines.flow.drop
import kotlin.math.roundToInt

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    isSwipeOpen: Boolean = false,
    onSwipeStart: () -> Unit = {},
    onSwipeOpen: () -> Unit = {},
    onSwipeClose: () -> Unit = {}
) {
    val density = LocalDensity.current
    val actionWidthPx = with(density) { 80.dp.toPx() }
    // 닫힘 0 · 열림 -80, 놓으면 가까운 자리로
    val swipeState = remember(actionWidthPx) {
        AnchoredDraggableState(
            initialValue = SwipeValue.Closed,
            anchors = DraggableAnchors {
                SwipeValue.Closed at 0f
                SwipeValue.Open at -actionWidthPx
            }
        )
    }
    val dragInteraction = remember { MutableInteractionSource() }
    val currentOnSwipeStart by rememberUpdatedState(onSwipeStart)
    val currentOnSwipeOpen by rememberUpdatedState(onSwipeOpen)
    val currentOnSwipeClose by rememberUpdatedState(onSwipeClose)

    // 열림 여부는 화면이 정함
    LaunchedEffect(isSwipeOpen) {
        if (!isSwipeOpen && swipeState.targetValue != SwipeValue.Closed) {
            swipeState.animateTo(SwipeValue.Closed, tween(200))
        }
    }
    // 밀기 시작하면 다른 열린 칸부터 닫음
    LaunchedEffect(dragInteraction) {
        dragInteraction.interactions.collect { if (it is DragInteraction.Start) currentOnSwipeStart() }
    }
    // 손으로 열고 닫은 결과를 화면에 알림
    LaunchedEffect(swipeState) {
        snapshotFlow { swipeState.settledValue }
            .drop(1)
            .collect { if (it == SwipeValue.Open) currentOnSwipeOpen() else currentOnSwipeClose() }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        val contentWidth = maxWidth

        // 삭제 영역은 화면 밖, 스와이프 시 밀려 들어옴
        Row(
            modifier = Modifier
                .wrapContentWidth(unbounded = true, align = Alignment.Start)
                .height(IntrinsicSize.Max)
                .offset { IntOffset(swipeState.requireOffset().roundToInt(), 0) }
        ) {
            // 콘텐츠 영역
            Row(
                modifier = Modifier
                    .width(contentWidth)
                    .background(MaterialTheme.colorScheme.background)
                    .background(
                        if (!notification.isRead)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                        else
                            Color.Transparent
                    )
                    .anchoredDraggable(
                        state = swipeState,
                        reverseDirection = false,
                        orientation = Orientation.Horizontal,
                        interactionSource = dragInteraction,
                        flingBehavior = AnchoredDraggableDefaults.flingBehavior(swipeState)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = onClick
                    )
                    .padding(start = 6.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 좌측: 읽지 않음 dot
                Box(
                    modifier = Modifier.width(12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // 프로필 이미지 또는 카테고리 아이콘
                if (notification.senderId != null) {
                    Box {
                        AsyncImage(
                            model = notification.senderAvatarUrl,
                            contentDescription = "프로필 이미지",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onProfileClick() },
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_profile_placeholder),
                            error = painterResource(R.drawable.ic_profile_placeholder),
                            fallback = painterResource(R.drawable.ic_profile_placeholder)
                        )
                        NotificationTypeBadge(
                            type = notification.type,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 3.dp, y = 3.dp)
                        )
                    }
                } else {
                    NotificationIcon(type = notification.type)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 글은 두 줄에서 줄이고 시간은 아래 고정
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = buildStyledMessage(
                            message = notification.message,
                            senderUsername = notification.senderUsername
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formatRelativeTime(notification.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                val thumbnailUrl = notification.imageUrl?.takeUnless { it.isBlank() }
                if (thumbnailUrl != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = "알림 썸네일",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }

            // 삭제 영역
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.error)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private enum class SwipeValue { Closed, Open }

/** 프로필 사진 오른쪽 아래 종류 배지 */
@Composable
private fun NotificationTypeBadge(type: String, modifier: Modifier = Modifier) {
    val (iconRes, tintColor) = getNotificationIconAndColor(type)
    val isLike = type == NotificationType.LIKE_POST || type == NotificationType.LIKE_COMMENT
    Box(
        modifier = modifier
            .size(18.dp)
            .background(MaterialTheme.colorScheme.background, CircleShape)
            .padding(1.5.dp)
            .background(tintColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(if (isLike) R.drawable.ic_heart_filled else iconRes),
            contentDescription = null,
            modifier = Modifier.size(10.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun NotificationIcon(type: String) {
    val (iconRes, tintColor) = getNotificationIconAndColor(type)

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tintColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = tintColor
        )
    }
}

@Composable
private fun getNotificationIconAndColor(type: String): Pair<Int, Color> {
    val primary = MaterialTheme.colorScheme.primary
    val systemColor = MaterialTheme.colorScheme.onSurfaceVariant
    val like = Like

    return when (type) {
        // 소셜
        NotificationType.FOLLOW -> R.drawable.ic_profile to primary
        NotificationType.LIKE_POST,
        NotificationType.LIKE_COMMENT -> R.drawable.ic_heart to like
        NotificationType.COMMENT,
        NotificationType.REPLY -> R.drawable.ic_comment to primary
        NotificationType.MENTION -> R.drawable.ic_comment to primary
        NotificationType.NEW_POST -> R.drawable.ic_edit to primary
        // 커머스
        NotificationType.ORDER_CREATED,
        NotificationType.ORDER_RECEIVED,
        NotificationType.ORDER_CONFIRMED,
        NotificationType.ORDER_CANCELLED -> R.drawable.ic_order to primary
        NotificationType.SHIPPING_STARTED,
        NotificationType.SHIPPING_IN_TRANSIT,
        NotificationType.SHIPPING_OUT_FOR_DELIVERY,
        NotificationType.SHIPPING_DELIVERED -> R.drawable.ic_shipping to primary
        NotificationType.PRODUCT_REVIEWED -> R.drawable.ic_review to primary
        NotificationType.NEW_PRODUCT -> R.drawable.ic_store to primary
        // 포인트
        NotificationType.POINT_EARNED,
        NotificationType.POINT_EXPIRED -> R.drawable.ic_point to primary
        // 크리에이터
        NotificationType.CREATOR_APPROVED -> R.drawable.ic_check to primary
        NotificationType.CREATOR_REJECTED -> R.drawable.ic_close to primary
        NotificationType.CREATOR_DEMOTED -> R.drawable.ic_warning to primary
        // 시스템 기본
        else -> R.drawable.ic_notification to systemColor
    }
}

private fun buildStyledMessage(message: String, senderUsername: String?): AnnotatedString {
    if (senderUsername.isNullOrEmpty()) return AnnotatedString(message)

    val index = message.indexOf(senderUsername)
    if (index == -1) return AnnotatedString(message)

    return buildAnnotatedString {
        append(message.substring(0, index))
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(senderUsername)
        }
        append(message.substring(index + senderUsername.length))
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "방금 전"
        minutes < 60 -> "${minutes}분 전"
        hours < 24 -> "${hours}시간 전"
        days < 7 -> "${days}일 전"
        days < 30 -> "${days / 7}주 전"
        days < 365 -> "${days / 30}개월 전"
        else -> "${days / 365}년 전"
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationItemUnreadPreview() {
    HaloTheme {
        Surface {
            NotificationItem(
                notification = Notification(
                    id = 1,
                    senderId = 2,
                    senderUsername = "username",
                    senderAvatarUrl = "",
                    type = NotificationType.LIKE_POST,
                    message = "username님이 회원님의 포스트를 좋아합니다",
                    targetType = "POST",
                    targetId = 1,
                    imageUrl = null,
                    deepLink = "/posts/1",
                    isRead = false,
                    createdAt = System.currentTimeMillis() - 300_000
                ),
                onClick = {},
                onDelete = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationItemReadPreview() {
    HaloTheme {
        Surface {
            NotificationItem(
                notification = Notification(
                    id = 2,
                    senderId = null,
                    senderUsername = null,
                    senderAvatarUrl = null,
                    type = NotificationType.ORDER_CREATED,
                    message = "주문이 접수되었습니다 (주문번호: 2026031371523376)",
                    targetType = "ORDER",
                    targetId = 10,
                    imageUrl = null,
                    deepLink = "/orders/10",
                    isRead = true,
                    createdAt = System.currentTimeMillis() - 86400_000
                ),
                onClick = {},
                onDelete = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationItemWithThumbnailPreview() {
    HaloTheme {
        Surface {
            NotificationItem(
                notification = Notification(
                    id = 3,
                    senderId = 2,
                    senderUsername = "creator1",
                    senderAvatarUrl = "",
                    type = NotificationType.LIKE_POST,
                    message = "creator1님이 회원님의 포스트를 좋아합니다",
                    targetType = "POST",
                    targetId = 1,
                    imageUrl = "https://example.com/post-image.jpg",
                    deepLink = "/posts/1",
                    isRead = false,
                    createdAt = System.currentTimeMillis() - 300_000
                ),
                onClick = {},
                onDelete = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationItemWithoutThumbnailPreview() {
    HaloTheme {
        Surface {
            NotificationItem(
                notification = Notification(
                    id = 4,
                    senderId = 3,
                    senderUsername = "designer_kim",
                    senderAvatarUrl = "",
                    type = NotificationType.FOLLOW,
                    message = "designer_kim님이 회원님을 팔로우하기 시작했습니다",
                    targetType = "USER",
                    targetId = 3,
                    imageUrl = null,
                    deepLink = "/users/3",
                    isRead = true,
                    createdAt = System.currentTimeMillis() - 3600_000
                ),
                onClick = {},
                onDelete = {}
            )
        }
    }
}
