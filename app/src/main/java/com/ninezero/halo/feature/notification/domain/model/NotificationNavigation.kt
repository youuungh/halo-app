package com.ninezero.halo.feature.notification.domain.model

import java.util.Locale

sealed interface NotificationDestination {
    data class Post(val postId: Int, val highlightCommentId: Int? = null) : NotificationDestination
    data class Comment(val commentId: Int, val highlightReplyId: Int? = null) :
        NotificationDestination

    data class User(val userId: Int) : NotificationDestination
    data class Order(val orderId: Int) : NotificationDestination
    data class ChatRoom(val roomId: Int) : NotificationDestination
    data class Product(val productId: Int) : NotificationDestination
    data class ProductReviews(val productId: Int, val reviewId: Int? = null) :
        NotificationDestination

    data object SalesManage : NotificationDestination
    data object Point : NotificationDestination
    data object CreatorApplication : NotificationDestination
    data object MySubscriptions : NotificationDestination
}

fun normalizeTargetType(targetType: String?): String? {
    return targetType
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.uppercase(Locale.US)
}

fun resolveNotificationDestination(
    targetType: String?,
    targetId: Int?,
    deepLink: String?,
    notificationType: String? = null
): NotificationDestination? {
    // 댓글 단위 이동이라 targetType보다 먼저
    // REPLY: 부모 CommentDetail에서 답글 이동
    // COMMENT: PostDetail 댓글 영역
    // MENTION·LIKE_COMMENT: 댓글 상세 앵커
    parseCommentAnchor(deepLink)?.let { commentId ->
        parseReplyAnchor(deepLink)?.let { replyId ->
            return NotificationDestination.Comment(commentId, highlightReplyId = replyId)
        }
        if (normalizeTargetType(notificationType) == "COMMENT") {
            parsePostId(deepLink)?.let {
                return NotificationDestination.Post(
                    it,
                    highlightCommentId = commentId
                )
            }
        }
        return NotificationDestination.Comment(commentId)
    }

    when (normalizeTargetType(targetType)) {
        "POST" -> targetId?.let { return NotificationDestination.Post(it) }
        "USER" -> targetId?.let { return NotificationDestination.User(it) }
        "ORDER" -> targetId?.let { return NotificationDestination.Order(it) }
        "CHAT" -> targetId?.let { return NotificationDestination.ChatRoom(it) }
        "PRODUCT" -> targetId?.let { return NotificationDestination.Product(it) }
        "COMMENT" -> {
            targetId?.let { return NotificationDestination.Comment(it) }
            parsePostId(deepLink)?.let { return NotificationDestination.Post(it) }
        }

        "POINT" -> return NotificationDestination.Point
        "APPLICATION" -> return NotificationDestination.CreatorApplication
        "SUBSCRIPTION" -> return NotificationDestination.MySubscriptions
    }

    return resolveNotificationDeepLink(deepLink)
}

private fun resolveNotificationDeepLink(deepLink: String?): NotificationDestination? {
    val link = deepLink ?: return null

    parsePostId(link)?.let { return NotificationDestination.Post(it) }
    parseId(link, "/users/(\\d+)")?.let { return NotificationDestination.User(it) }
    // 판매자 주문 링크는 판매관리로, /orders/보다 먼저
    parseId(link, "/creator/orders/(\\d+)")?.let { return NotificationDestination.SalesManage }
    parseId(link, "/orders/(\\d+)")?.let { return NotificationDestination.Order(it) }
    parseId(link, "/chats/(\\d+)")?.let { return NotificationDestination.ChatRoom(it) }
    // 리뷰 목록은 상품보다 먼저 검사
    parseId(link, "/products/(\\d+)/reviews")?.let {
        return NotificationDestination.ProductReviews(it, parseId(link, "#review-(\\d+)"))
    }
    parseId(link, "/products/(\\d+)")?.let { return NotificationDestination.Product(it) }

    return when {
        link.startsWith("/points") -> NotificationDestination.Point
        link.startsWith("/creator/application") -> NotificationDestination.CreatorApplication
        link.startsWith("/creator/dashboard") -> NotificationDestination.CreatorApplication
        link.startsWith("/subscriptions") -> NotificationDestination.MySubscriptions
        else -> null
    }
}

private fun parsePostId(deepLink: String?): Int? = parseId(deepLink, "/posts/(\\d+)")

private fun parseCommentAnchor(deepLink: String?): Int? = parseId(deepLink, "#comment-(\\d+)")

private fun parseReplyAnchor(deepLink: String?): Int? = parseId(deepLink, "#reply-(\\d+)")

private fun parseId(value: String?, pattern: String): Int? {
    return value
        ?.let { Regex(pattern).find(it)?.groupValues?.getOrNull(1) }
        ?.toIntOrNull()
}
