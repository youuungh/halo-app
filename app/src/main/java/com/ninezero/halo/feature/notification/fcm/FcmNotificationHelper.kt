package com.ninezero.halo.feature.notification.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.ninezero.halo.MainActivity
import com.ninezero.halo.R
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.notification.domain.model.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.absoluteValue

object FcmNotificationHelper {

    private const val CHANNEL_ID_SOCIAL = "halo_social"
    private const val CHANNEL_ID_COMMERCE = "halo_commerce"
    private const val CHANNEL_ID_CHAT = "halo_chat"
    private const val CHANNEL_ID_DEFAULT = "halo_default"

    // 전송 알림 채널 id 복제, 바꾸면 함께
    private const val CHANNEL_ID_MEDIA_TRANSFER = "halo_media_transfer"

    private const val GROUP_CHAT = "chat"
    private const val GROUP_COMMERCE = "commerce"
    private const val GROUP_SOCIAL = "social"
    private const val GROUP_SYSTEM = "system"

    /** 묶음 요약 탭 표식 */
    const val EXTRA_SUMMARY_GROUP = "summaryGroup"  // MainActivity가 읽음

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                CHANNEL_ID_SOCIAL,
                "소셜 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "좋아요, 댓글, 팔로우 등" },
            NotificationChannel(
                CHANNEL_ID_COMMERCE,
                "쇼핑 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "주문, 배송, 리뷰 등" },
            NotificationChannel(
                CHANNEL_ID_CHAT,
                "채팅 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "채팅 메시지" },
            NotificationChannel(
                CHANNEL_ID_DEFAULT,
                "기타 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "시스템 알림 등" }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    /** 트레이 알림 발행 */
    fun showNotification(  // 딥링크 키는 data 맵째로 extras에 전달
        context: Context,
        title: String,
        body: String,
        type: String?,
        senderAvatarUrl: String?,
        imageUrl: String?,
        data: Map<String, String>
    ) {
        val channelId = getChannelId(type)
        val targetId = data["targetId"]?.toIntOrNull()
        val notificationId = trayIdOf(type, targetId, data["notificationId"]?.toIntOrNull())
        val groupKey = groupKeyOf(type, targetId)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 두 장 동시·시간 제한 로드
        val (largeIconBitmap, bigPictureBitmap) = runBlocking(Dispatchers.IO) {
            val avatar = async { loadNotificationBitmap(context, senderAvatarUrl) }
            val picture = async { loadNotificationBitmap(context, imageUrl) }
            avatar.await() to picture.await()
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(getPriority(type))
            .setGroup(groupKey)  // 개별 알림은 소리

        if (largeIconBitmap != null) {
            builder.setLargeIcon(largeIconBitmap)
        }

        if (bigPictureBitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bigPictureBitmap)
                    .setSummaryText(body)
            )
        } else if (body.length > 40) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, builder.build())

        postOrPruneSummary(context, manager, groupKey, channelId, data["senderUsername"])
    }

    internal fun trayIdOf(type: String?, targetId: Int?, payloadId: Int?): Int {  // 개별 알림은 서버 id, 그 외 시각 기반
        if (targetId != null) {
            when (type) {
                "NEW_POST" -> return -1_000_001 - (targetId % 1_000_000)
                "NEW_PRODUCT" -> return -2_000_001 - (targetId % 1_000_000)
            }
            // 채팅은 방당 알림 하나, 고정 id로 누적
            // 일괄·요약·전송·서버 id와 안 겹치는 음수 대역
            if (type?.startsWith("CHAT") == true) return -4_000_001 - (targetId % 1_000_000)
        }
        if (payloadId != null) return payloadId
        return (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
    }

    /** 채팅방 고정 트레이 id */
    internal fun chatTrayId(roomId: Int): Int = trayIdOf(NotificationType.CHAT_MESSAGE, roomId, null)  // 발행과 취소가 공유

    internal fun groupKeyOf(type: String?, targetId: Int?): String = when (getChannelId(type)) {
        CHANNEL_ID_CHAT -> if (targetId != null) "${GROUP_CHAT}_$targetId" else GROUP_CHAT
        CHANNEL_ID_COMMERCE -> GROUP_COMMERCE
        CHANNEL_ID_SOCIAL -> GROUP_SOCIAL
        else -> GROUP_SYSTEM
    }

    /** 묶음 요약 id */
    private fun summaryIdOf(groupKey: String): Int =  // 음수 대역, 서버 id·전송 알림과 안 겹침
        -(groupKey.hashCode().toLong().absoluteValue % 1_000_000L).toInt() - 1

    /** 묶음 요약 발행 또는 정리 */
    private fun postOrPruneSummary(  // 개별 알림 2개 이상일 때만 발행
        context: Context,
        manager: NotificationManager,
        groupKey: String,
        channelId: String,
        senderUsername: String?
    ) {
        val summaryId = summaryIdOf(groupKey)
        val childCount = countChildren(manager, groupKey)

        if (childCount < 2) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SUMMARY_GROUP, groupKey)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            summaryId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summary = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(summaryTitleOf(groupKey, senderUsername))
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)  // 요약은 무음
            .setStyle(NotificationCompat.InboxStyle().setSummaryText("알림 ${childCount}개"))
            .build()

        manager.notify(summaryId, summary)
    }

    private fun summaryTitleOf(groupKey: String, senderUsername: String?): String = when {
        groupKey.startsWith(GROUP_CHAT) -> senderUsername?.takeIf { it.isNotBlank() }?.let { "${it}님" }
            ?: "새 메시지"
        groupKey == GROUP_COMMERCE -> "쇼핑 알림"
        groupKey == GROUP_SOCIAL -> "소셜 알림"
        else -> "알림"
    }

    private fun countChildren(manager: NotificationManager, groupKey: String): Int =
        manager.activeNotifications.count {
            NotificationCompat.getGroup(it.notification) == groupKey &&
                (it.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) == 0
        }

    /** 빈 묶음 요약 제거 */
    fun pruneOrphanSummaries(context: Context) {  // cancelAll 금지, 일반 알림까지 지워짐
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.activeNotifications
            .filter { (it.notification.flags and android.app.Notification.FLAG_GROUP_SUMMARY) != 0 }
            .forEach { summary ->
                val groupKey = NotificationCompat.getGroup(summary.notification) ?: return@forEach
                if (countChildren(manager, groupKey) == 0) manager.cancel(summary.id)
            }
    }

    /** 현재 계정 트레이 알림 정리 */
    fun clearUserNotifications(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.activeNotifications
            .filter { it.notification.channelId != CHANNEL_ID_MEDIA_TRANSFER }
            .forEach { manager.cancel(it.id) }
    }

    private fun getChannelId(type: String?): String {
        return when {
            type == null -> CHANNEL_ID_DEFAULT
            type.startsWith("CHAT") -> CHANNEL_ID_CHAT
            type.startsWith("ORDER") || type.startsWith("SHIPPING") ||
                type.startsWith("PRODUCT") || type == "POINT_EARNED" ||
                type == "POINT_EXPIRED" || type == "NEW_PRODUCT" -> CHANNEL_ID_COMMERCE
            type == "FOLLOW" || type.startsWith("LIKE") || type == "COMMENT" ||
                type == "REPLY" || type == "MENTION" || type == "NEW_POST" -> CHANNEL_ID_SOCIAL
            else -> CHANNEL_ID_DEFAULT
        }
    }

    private fun getPriority(type: String?): Int {
        return when {
            type == null -> NotificationCompat.PRIORITY_DEFAULT
            type.startsWith("CHAT") -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    suspend fun showChatMessagingStyle(  // 방당 고정 id, appendLatest면 마지막 줄 추가
        context: Context,
        roomId: Int,
        messages: List<Message>,
        appendLatest: Boolean,
        latestSenderName: String?,
        latestText: String,
        latestTimestamp: Long,
        senderAvatarUrl: String?,
        data: Map<String, String>
    ) {
        val notificationId = chatTrayId(roomId)
        val me = Person.Builder().setName("나").setKey("self").build()
        val senderBitmap = loadNotificationBitmap(context, senderAvatarUrl)
        val senderIcon = senderBitmap?.let { IconCompat.createWithBitmap(it) }
        // payload username으로 한 번 만들어 공유
        val other = Person.Builder()
            .setName(latestSenderName ?: messages.firstOrNull { !it.isFromMe }?.sender?.username ?: "")
            .apply { senderIcon?.let { setIcon(it) } }
            .build()

        val style = NotificationCompat.MessagingStyle(me)
        messages.forEach { msg ->
            style.addMessage(lineTextOf(msg), msg.createdAt, if (msg.isFromMe) me else other)
        }
        if (appendLatest) {
            style.addMessage(latestText, latestTimestamp, other)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_CHAT)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(style)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        senderBitmap?.let { builder.setLargeIcon(it) }

        context.getSystemService(NotificationManager::class.java).notify(notificationId, builder.build())
    }

    private fun lineTextOf(msg: Message): CharSequence =
        msg.content?.takeIf { it.isNotBlank() } ?: when (msg.chatMessageType) {
            ChatMessageType.IMAGE -> "사진"
            ChatMessageType.VIDEO -> "동영상"
            ChatMessageType.FILE -> "파일"
            ChatMessageType.PRODUCT_LINK -> "상품"
            ChatMessageType.POST_LINK -> "포스트"
            ChatMessageType.TEXT -> "메시지"
        }

    private suspend fun loadNotificationBitmap(context: Context, url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null

        return withTimeoutOrNull(IMAGE_TIMEOUT_MS) {
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()

            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) result.image.toBitmap() else null
        }
    }

    private const val IMAGE_TIMEOUT_MS = 3_000L
}
