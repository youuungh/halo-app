package com.ninezero.halo.feature.notification.presentation

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.feature.notification.domain.model.Notification
import com.ninezero.halo.feature.notification.domain.model.NotificationType

internal fun previewNotificationState(now: Long = System.currentTimeMillis()): NotificationState {
    return NotificationState(
        notifications = listOf(
            notification(
                id = 1, senderId = 2, senderUsername = "creator1",
                type = NotificationType.LIKE_POST,
                message = "creator1님이 회원님의 포스트를 좋아합니다",
                targetType = "POST", targetId = 1, deepLink = "/posts/1",
                isRead = false, createdAt = now - 300_000
            ),
            notification(
                id = 2, senderId = 3, senderUsername = "user2",
                type = NotificationType.LIKE_COMMENT,
                message = "user2님이 회원님의 댓글을 좋아합니다",
                targetType = "POST", targetId = 2, deepLink = "/posts/2",
                isRead = false, createdAt = now - 600_000
            ),
            notification(
                id = 3, senderId = 4, senderUsername = "designer_kim",
                type = NotificationType.FOLLOW,
                message = "designer_kim님이 회원님을 팔로우하기 시작했습니다",
                targetType = "USER", targetId = 4, deepLink = "/users/4",
                isRead = false, createdAt = now - 1800_000
            ),
            notification(
                id = 4, senderId = 5, senderUsername = "photo_j",
                type = NotificationType.COMMENT,
                message = "photo_j님이 회원님의 포스트에 댓글을 남겼습니다",
                targetType = "POST", targetId = 5, deepLink = "/posts/5#comment-10",
                isRead = true, createdAt = now - 3600_000
            ),
            notification(
                id = 5, senderId = 6, senderUsername = "dev_park",
                type = NotificationType.REPLY,
                message = "dev_park님이 회원님의 댓글에 답글을 남겼습니다",
                targetType = "POST", targetId = 5, deepLink = "/posts/5#comment-11",
                isRead = true, createdAt = now - 5400_000
            ),
            notification(
                id = 6, senderId = 7, senderUsername = "artist_lee",
                type = NotificationType.MENTION,
                message = "artist_lee님이 댓글에서 회원님을 언급했습니다",
                targetType = "POST", targetId = 8, deepLink = "/posts/8#comment-15",
                isRead = true, createdAt = now - 7200_000
            ),
            notification(
                id = 7, senderId = 8, senderUsername = "travel_min",
                type = NotificationType.NEW_POST,
                message = "travel_min님이 새 포스트를 작성했습니다",
                targetType = "POST", targetId = 12, deepLink = "/posts/12",
                isRead = true, createdAt = now - 86400_000 - 1800_000
            ),
            notification(
                id = 9,
                type = NotificationType.ORDER_CREATED,
                message = "주문이 접수되었습니다 (주문번호: 2026031371523376)",
                targetType = "ORDER", targetId = 10, deepLink = "/orders/10",
                isRead = true, createdAt = now - 3 * 86400_000
            ),
            notification(
                id = 10,
                type = NotificationType.ORDER_RECEIVED,
                message = "판매자가 주문을 접수했습니다",
                targetType = "ORDER", targetId = 10, deepLink = "/creator/orders/10",
                isRead = true, createdAt = now - 3 * 86400_000 - 3600_000
            ),
            notification(
                id = 11,
                type = NotificationType.ORDER_CONFIRMED,
                message = "주문이 확정되었습니다",
                targetType = "ORDER", targetId = 9, deepLink = "/orders/9",
                isRead = true, createdAt = now - 4 * 86400_000
            ),
            notification(
                id = 12,
                type = NotificationType.ORDER_CANCELLED,
                message = "주문이 취소되었습니다 (주문번호: 2026031171716996)",
                targetType = "ORDER", targetId = 11, deepLink = "/orders/11",
                isRead = true, createdAt = now - 5 * 86400_000
            ),
            notification(
                id = 13,
                type = NotificationType.SHIPPING_STARTED,
                message = "상품이 발송되었습니다",
                targetType = "ORDER", targetId = 10, deepLink = "/orders/10",
                isRead = true, createdAt = now - 5 * 86400_000 - 3600_000
            ),
            notification(
                id = 14,
                type = NotificationType.SHIPPING_IN_TRANSIT,
                message = "상품이 배송 중입니다",
                targetType = "ORDER", targetId = 10, deepLink = "/orders/10",
                isRead = true, createdAt = now - 6 * 86400_000
            ),
            notification(
                id = 15,
                type = NotificationType.SHIPPING_OUT_FOR_DELIVERY,
                message = "상품이 배송지 근처에 도착했습니다",
                targetType = "ORDER", targetId = 8, deepLink = "/orders/8",
                isRead = true, createdAt = now - 10 * 86400_000
            ),
            notification(
                id = 16,
                type = NotificationType.SHIPPING_DELIVERED,
                message = "주문하신 상품이 배송 완료되었습니다. 리뷰를 남겨주세요!",
                targetType = "ORDER", targetId = 8, deepLink = "/orders/8",
                isRead = true, createdAt = now - 10 * 86400_000 - 3600_000
            ),
            notification(
                id = 17, senderId = 10, senderUsername = "food_lover",
                type = NotificationType.PRODUCT_REVIEWED,
                message = "food_lover님이 상품에 리뷰를 남겼습니다",
                targetType = "PRODUCT", targetId = 20, deepLink = "/products/20/reviews",
                isRead = true, createdAt = now - 12 * 86400_000
            ),
            notification(
                id = 18,
                type = NotificationType.NEW_PRODUCT,
                message = "팔로우 중인 크리에이터가 새 상품을 등록했습니다",
                targetType = "PRODUCT", targetId = 25, deepLink = "/products/25",
                isRead = true, createdAt = now - 14 * 86400_000
            ),
            notification(
                id = 19,
                type = NotificationType.POINT_EARNED,
                message = "500원의 포인트가 적립되었습니다",
                targetType = "ORDER", targetId = 7, deepLink = "/orders/7",
                isRead = true, createdAt = now - 15 * 86400_000
            ),
            notification(
                id = 20,
                type = NotificationType.POINT_EXPIRED,
                message = "200원의 포인트가 만료되었습니다",
                targetType = "POINT", deepLink = "/points/history",
                isRead = true, createdAt = now - 18 * 86400_000
            ),
            notification(
                id = 25,
                type = NotificationType.SYSTEM_ANNOUNCEMENT,
                message = "멤버십이 자동 갱신되었습니다. 구독 내역을 확인해보세요",
                targetType = "SUBSCRIPTION", targetId = 14, deepLink = "/subscriptions/14",
                isRead = true, createdAt = now - 20 * 86400_000
            ),
            notification(
                id = 21,
                type = NotificationType.CREATOR_APPROVED,
                message = "크리에이터로 승인되었습니다. 이제 상품을 등록하고 판매할 수 있습니다!",
                targetType = "APPLICATION", targetId = 1, deepLink = "/creator/dashboard",
                isRead = true, createdAt = now - 35 * 86400_000
            ),
            notification(
                id = 22,
                type = NotificationType.CREATOR_REJECTED,
                message = "크리에이터 신청이 반려되었습니다. 사유를 확인해주세요",
                targetType = "APPLICATION", targetId = 2, deepLink = "/creator/application",
                isRead = true, createdAt = now - 40 * 86400_000
            ),
            notification(
                id = 23,
                type = NotificationType.CREATOR_DEMOTED,
                message = "크리에이터 자격이 해제되었습니다. 자세한 내용을 확인해주세요",
                deepLink = "/creator/application",
                isRead = true, createdAt = now - 50 * 86400_000
            ),
            notification(
                id = 24,
                type = NotificationType.SYSTEM_ANNOUNCEMENT,
                message = "서비스 점검 안내: 3월 20일 02:00~04:00 서버 점검이 예정되어 있습니다",
                isRead = true, createdAt = now - 60 * 86400_000
            )
        )
    )
}

internal fun previewNotificationWithThumbnailsState(now: Long = System.currentTimeMillis()): NotificationState {
    return NotificationState(
        notifications = listOf(
            notification(
                id = 1, senderId = 2, senderUsername = "creator1",
                type = NotificationType.LIKE_POST,
                message = "creator1님이 회원님의 포스트를 좋아합니다",
                targetType = "POST", targetId = 1, imageUrl = "https://example.com/post1.jpg", deepLink = "/posts/1",
                isRead = false, createdAt = now - 300_000
            ),
            notification(
                id = 2, senderId = 3, senderUsername = "designer_kim",
                type = NotificationType.FOLLOW,
                message = "designer_kim님이 회원님을 팔로우하기 시작했습니다",
                targetType = "USER", targetId = 3, deepLink = "/users/3",
                isRead = false, createdAt = now - 600_000
            ),
            notification(
                id = 3, senderId = 4, senderUsername = "photo_j",
                type = NotificationType.COMMENT,
                message = "photo_j님이 회원님의 포스트에 댓글을 남겼습니다",
                targetType = "POST", targetId = 5, imageUrl = "https://example.com/post5.jpg", deepLink = "/posts/5#comment-10",
                isRead = false, createdAt = now - 1800_000
            ),
            notification(
                id = 4, senderId = 5, senderUsername = "dev_park",
                type = NotificationType.COMMENT,
                message = "dev_park님이 회원님의 포스트에 댓글을 남겼습니다",
                targetType = "POST", targetId = 6, deepLink = "/posts/6#comment-20",
                isRead = true, createdAt = now - 3600_000
            ),
            notification(
                id = 5, senderId = 6, senderUsername = "travel_min",
                type = NotificationType.NEW_POST,
                message = "travel_min님이 새 포스트를 올렸습니다",
                targetType = "POST", targetId = 12, imageUrl = "https://example.com/post12.jpg", deepLink = "/posts/12",
                isRead = true, createdAt = now - 86400_000 - 1800_000
            ),
            notification(
                id = 7,
                type = NotificationType.NEW_PRODUCT,
                message = "팔로우 중인 크리에이터가 새 상품을 등록했습니다",
                targetType = "PRODUCT", targetId = 25, imageUrl = "https://example.com/product25.jpg", deepLink = "/products/25",
                isRead = true, createdAt = now - 3 * 86400_000
            ),
            notification(
                id = 8,
                type = NotificationType.ORDER_CREATED,
                message = "주문이 접수되었습니다 (주문번호: 2026031371523376)",
                targetType = "ORDER", targetId = 10, deepLink = "/orders/10",
                isRead = true, createdAt = now - 3 * 86400_000 - 3600_000
            ),
            notification(
                id = 9,
                type = NotificationType.POINT_EARNED,
                message = "500원의 포인트가 적립되었습니다",
                targetType = "ORDER", targetId = 7, deepLink = "/orders/7",
                isRead = true, createdAt = now - 10 * 86400_000
            )
        )
    )
}

internal fun previewNotificationDeleteSnackbarState(now: Long = System.currentTimeMillis()): NotificationState {
    return NotificationState(
        snackbarState = SnackbarState(
            message = "알림이 삭제되었습니다",
            actionLabel = "취소"
        ),
        notifications = listOf(
            notification(
                id = 1, senderId = 2, senderUsername = "creator1",
                type = NotificationType.LIKE_POST,
                message = "creator1님이 회원님의 포스트를 좋아합니다",
                targetType = "POST", targetId = 1, deepLink = "/posts/1",
                isRead = false, createdAt = now - 300_000
            )
        )
    )
}

private fun notification(
    id: Int,
    senderId: Int? = null,
    senderUsername: String? = null,
    senderAvatarUrl: String? = "",
    type: String,
    message: String,
    targetType: String? = null,
    targetId: Int? = null,
    imageUrl: String? = null,
    deepLink: String? = null,
    isRead: Boolean,
    createdAt: Long
): Notification {
    return Notification(
        id = id,
        senderId = senderId,
        senderUsername = senderUsername,
        senderAvatarUrl = senderAvatarUrl,
        type = type,
        message = message,
        targetType = targetType,
        targetId = targetId,
        imageUrl = imageUrl,
        deepLink = deepLink,
        isRead = isRead,
        createdAt = createdAt
    )
}

@HaloLightDarkPreview
@Composable
private fun NotificationScreenPreview() {
    HaloScreenPreview {
        NotificationScreenContent(
            state = previewNotificationState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationScreenWithThumbnailsPreview() {
    HaloScreenPreview {
        NotificationScreenContent(
            state = previewNotificationWithThumbnailsState()
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun NotificationScreenDeleteSnackbarPreview() {
    HaloScreenPreview {
        NotificationScreenContent(
            state = previewNotificationDeleteSnackbarState()
        )
    }
}
