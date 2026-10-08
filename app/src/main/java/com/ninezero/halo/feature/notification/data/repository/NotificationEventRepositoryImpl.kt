package com.ninezero.halo.feature.notification.data.repository

import com.ninezero.halo.core.network.websocket.RealtimeFrameType
import com.ninezero.halo.core.network.websocket.RealtimeSocket
import com.ninezero.halo.feature.notification.data.remote.dto.NotificationDto
import com.ninezero.halo.feature.notification.data.remote.dto.toDomain
import com.ninezero.halo.feature.notification.domain.model.NotificationEvent
import com.ninezero.halo.feature.notification.domain.repository.NotificationEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** 알림 프레임을 NotificationEvent로 변환 */
@Singleton
class NotificationEventRepositoryImpl @Inject constructor(
    private val socket: RealtimeSocket,
    private val json: Json
) : NotificationEventRepository {

    override val events: Flow<NotificationEvent> = socket.frames
        .buffer(64)
        .mapNotNull { frame ->
        try {
            when (frame.type) {
                // data 없는 NOTIFICATION은 재조회 신호
                RealtimeFrameType.NOTIFICATION -> frame.data?.let {
                    NotificationEvent.NewNotification(json.decodeFromJsonElement<NotificationDto>(it).toDomain())
                } ?: NotificationEvent.RefreshRequired

                RealtimeFrameType.CONNECTED -> NotificationEvent.Connected
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Notification: 프레임 파싱 실패 type=${frame.type}")
            null
        }
    }
}
