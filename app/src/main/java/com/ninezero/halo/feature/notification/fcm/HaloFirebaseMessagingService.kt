package com.ninezero.halo.feature.notification.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ninezero.halo.core.fcm.ActiveChatRoomTracker
import com.ninezero.halo.core.fcm.FcmRegistrationManager
import com.ninezero.halo.core.fcm.FcmRegistrationRepository
import com.ninezero.halo.core.network.auth.TokenProvider
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import com.ninezero.halo.feature.notification.domain.model.NotificationType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HaloFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenProvider: TokenProvider

    @Inject
    lateinit var fcmRegistrationRepository: FcmRegistrationRepository

    @Inject
    lateinit var chatRepository: ChatRepository

    @Inject
    lateinit var fcmRegistrationManager: FcmRegistrationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onRegistered(token: String) {
        super.onRegistered(token)
        Timber.d("FCM 등록됨: ${token.take(10)}...")

        serviceScope.launch {
            fcmRegistrationManager.cache(token)
            val userId = tokenProvider.getUserId()
            if (userId != null) {
                fcmRegistrationRepository.registerFid(token)
                    .onSuccess { Timber.d("FCM 토큰 등록 성공") }
                    .onFailure { Timber.e(it, "FCM 토큰 등록 실패") }
            }
        }
    }

    override fun onUnregistered(token: String) {
        super.onUnregistered(token)
        Timber.d("FCM 등록 해제됨")

        serviceScope.launch { fcmRegistrationManager.clear() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM 메시지 수신: ${message.data}")

        // 로그아웃 뒤 늦게 온 푸시 차단
        if (runBlocking { tokenProvider.getUserId() } == null) return

        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: return
        val body = message.notification?.body ?: data["message"] ?: ""
        val notificationType = data["type"]
        val senderAvatarUrl = data["senderAvatarUrl"]
        val imageUrl = data["imageUrl"]

        // 채팅은 방당 알림 하나로 쌓는 별도 경로
        if (notificationType == NotificationType.CHAT_MESSAGE) {
            val roomId = data["targetId"]?.toIntOrNull()
            if (roomId != null) {
                showChatNotification(roomId, data)
                return
            }
        }

        FcmNotificationHelper.showNotification(
            context = this,
            title = title,
            body = body,
            type = notificationType,
            senderAvatarUrl = senderAvatarUrl,
            imageUrl = imageUrl,
            data = data
        )
    }

    private fun showChatNotification(roomId: Int, data: Map<String, String>) {  // 메시지 미리 받은 뒤 발행, DB 예외는 삼킴
        runBlocking {
            runCatching {
                val prefetched = withTimeoutOrNull(PREFETCH_TIMEOUT_MS) {
                    chatRepository.syncRecentMessages(roomId, PREFETCH_LIMIT).isSuccess
                } ?: false
                if (!prefetched) Timber.w("FCM: 방 $roomId 프리페치 실패/초과 — 캐시+폴백으로 발행")

                // 지금 이 방을 보고 있으면 트레이 알림 생략
                if (ActiveChatRoomTracker.activeRoomId == roomId) {
                    Timber.d("FCM: 방 $roomId 을 보고 있어 트레이 알림 생략(프리페치는 수행)")
                    return@runCatching
                }

                val messages = chatRepository.getCachedMessages(roomId, LINE_LIMIT + 6)
                    .reversed()
                    .takeLast(LINE_LIMIT)
                FcmNotificationHelper.showChatMessagingStyle(
                    context = this@HaloFirebaseMessagingService,
                    roomId = roomId,
                    messages = messages,
                    appendLatest = !prefetched || messages.isEmpty(),  // 미리 받기 실패·빈 캐시면 이번 메시지를 덧붙임
                    latestSenderName = data["senderUsername"],
                    latestText = data["messagePreview"]?.takeIf { it.isNotBlank() } ?: data["message"] ?: "새 메시지",
                    latestTimestamp = System.currentTimeMillis(),
                    senderAvatarUrl = data["senderAvatarUrl"],
                    data = data
                )
            }.onFailure { Timber.w(it, "FCM: 방 $roomId 채팅 알림 발행 중 예외") }
        }
    }

    private companion object {
        const val PREFETCH_TIMEOUT_MS = 3_000L
        const val PREFETCH_LIMIT = 10   // 프리페치로 받아올 메시지 수
        const val LINE_LIMIT = 6        // MessagingStyle에 쌓을 대화 라인 수
    }
}
