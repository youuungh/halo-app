package com.ninezero.halo.feature.notification.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
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
import com.ninezero.halo.feature.notification.domain.model.Notification
import com.ninezero.halo.feature.notification.domain.model.NotificationType
import com.ninezero.halo.ui.theme.HaloTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    isSwipeOpen: Boolean = false,
    onSwipeOpen: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val actionWidthPx = with(density) { 80.dp.toPx() }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(isSwipeOpen) {
        if (!isSwipeOpen && offsetX.value != 0f) {
            offsetX.animateTo(0f, tween(200))
        }
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
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
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
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    val target = if (offsetX.value < -actionWidthPx / 2) -actionWidthPx else 0f
                                    offsetX.animateTo(target, tween(200))
                                    if (target != 0f) onSwipeOpen()
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    val newValue = (offsetX.value + dragAmount).coerceIn(-actionWidthPx, 0f)
                                    offsetX.snapTo(newValue)
                                }
                            }
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ) {
                        scope.launch { offsetX.animateTo(0f, tween(200)) }
                        onClick()
                    }
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
                } else {
                    NotificationIcon(type = notification.type)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = buildStyledMessage(
                            message = notification.message,
                            senderUsername = notification.senderUsername
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formatRelativeTime(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                val thumbnailUrl = notification.imageUrl?.takeUnless { it.isBlank() }
                if (thumbnailUrl != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
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
                    .clickable {
                        scope.launch {
                            offsetX.animateTo(0f, tween(200))
                        }
                        onDelete()
                    },
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
    // 알림 종류 구분용 고정 팔레트
    val indigo = Color(0xFF6366F1)
    val orange = Color(0xFFE67E22)
    val emerald = Color(0xFF10B981)
    val red = Color(0xFFEF4444)
    val amber = Color(0xFFF59E0B)

    return when (type) {
        // 소셜
        NotificationType.FOLLOW -> R.drawable.ic_profile to indigo
        NotificationType.LIKE_POST,
        NotificationType.LIKE_COMMENT -> R.drawable.ic_heart to red
        NotificationType.COMMENT,
        NotificationType.REPLY -> R.drawable.ic_comment to indigo
        NotificationType.MENTION -> R.drawable.ic_comment to primary
        NotificationType.NEW_POST -> R.drawable.ic_edit to indigo
        // 커머스
        NotificationType.ORDER_CREATED,
        NotificationType.ORDER_RECEIVED,
        NotificationType.ORDER_CONFIRMED,
        NotificationType.ORDER_CANCELLED -> R.drawable.ic_order to orange
        NotificationType.SHIPPING_STARTED,
        NotificationType.SHIPPING_IN_TRANSIT,
        NotificationType.SHIPPING_OUT_FOR_DELIVERY,
        NotificationType.SHIPPING_DELIVERED -> R.drawable.ic_shipping to orange
        NotificationType.PRODUCT_REVIEWED -> R.drawable.ic_review to orange
        NotificationType.NEW_PRODUCT -> R.drawable.ic_store to orange
        // 포인트
        NotificationType.POINT_EARNED,
        NotificationType.POINT_EXPIRED -> R.drawable.ic_point to amber
        // 크리에이터
        NotificationType.CREATOR_APPROVED -> R.drawable.ic_check to emerald
        NotificationType.CREATOR_REJECTED -> R.drawable.ic_close to red
        NotificationType.CREATOR_DEMOTED -> R.drawable.ic_warning to red
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
