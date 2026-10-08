package com.ninezero.halo.feature.chat.data.repository

import com.ninezero.halo.core.network.websocket.RealtimeFrameType
import com.ninezero.halo.core.network.websocket.RealtimeSocket
import com.ninezero.halo.feature.chat.data.remote.dto.MessageDeletedEventDto
import com.ninezero.halo.feature.chat.data.remote.dto.MessageDto
import com.ninezero.halo.feature.chat.data.remote.dto.MessageReadEventDto
import com.ninezero.halo.feature.chat.data.remote.dto.toDomain
import com.ninezero.halo.feature.chat.domain.model.ChatEvent
import com.ninezero.halo.feature.chat.domain.repository.ChatEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** 채팅 프레임을 ChatEvent로 변환 */
@Singleton
class ChatEventRepositoryImpl @Inject constructor(
    private val socket: RealtimeSocket,
    private val json: Json
) : ChatEventRepository {

    override val events: Flow<ChatEvent> = socket.frames
        .buffer(64)
        .mapNotNull { frame ->
        try {
            when (frame.type) {
                RealtimeFrameType.MESSAGE -> frame.data?.let {
                    ChatEvent.MessageReceived(json.decodeFromJsonElement<MessageDto>(it).toDomain())
                }

                RealtimeFrameType.MESSAGE_READ -> frame.data?.let {
                    val dto = json.decodeFromJsonElement<MessageReadEventDto>(it)
                    ChatEvent.MessagesRead(dto.roomId, dto.messageIds, dto.readerId)
                }

                RealtimeFrameType.MESSAGE_DELETED -> frame.data?.let {
                    val dto = json.decodeFromJsonElement<MessageDeletedEventDto>(it)
                    ChatEvent.MessageDeleted(dto.roomId, dto.messageId)
                }

                RealtimeFrameType.CONNECTED -> ChatEvent.Connected
                RealtimeFrameType.ERROR -> ChatEvent.Error(frame.message ?: "연결 실패")
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Chat: 프레임 파싱 실패 type=${frame.type}")
            null
        }
    }
}
