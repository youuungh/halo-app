package com.ninezero.halo.feature.chat.data.repository

import com.ninezero.halo.core.database.dao.CachedChatRoomDao
import com.ninezero.halo.core.database.dao.CachedMessageDao
import com.ninezero.halo.core.fcm.ActiveChatRoomTracker
import com.ninezero.halo.core.model.UploadTicket
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallNoData
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.chat.data.mapper.ChatRoomCacheMapper.toDomain
import com.ninezero.halo.feature.chat.data.mapper.ChatRoomCacheMapper.toDomains
import com.ninezero.halo.feature.chat.data.mapper.ChatRoomCacheMapper.toEntities
import com.ninezero.halo.feature.chat.data.mapper.ChatRoomCacheMapper.toEntity
import com.ninezero.halo.feature.chat.data.mapper.MessageCacheMapper.toDomain
import com.ninezero.halo.feature.chat.data.mapper.MessageCacheMapper.toEntities
import com.ninezero.halo.feature.chat.data.mapper.MessageCacheMapper.toEntity
import com.ninezero.halo.feature.chat.data.remote.api.ChatApi
import com.ninezero.halo.feature.chat.data.remote.dto.ChatRequest
import com.ninezero.halo.feature.chat.data.remote.dto.ChatUploadPrepareRequest
import com.ninezero.halo.feature.chat.data.remote.dto.SendMessageRequest
import com.ninezero.halo.feature.chat.data.remote.dto.toDomain
import com.ninezero.halo.feature.chat.domain.model.Chat
import com.ninezero.halo.feature.chat.domain.model.ChatEvent
import com.ninezero.halo.feature.chat.domain.model.ChatImageUpload
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.MessageListResult
import com.ninezero.halo.feature.chat.domain.model.MessageSearchResult
import com.ninezero.halo.feature.chat.domain.model.MessageStatus
import com.ninezero.halo.feature.chat.domain.repository.ChatEventRepository
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.random.Random

class ChatRepositoryImpl @Inject constructor(
    private val api: ChatApi,
    private val cachedMessageDao: CachedMessageDao,
    private val cachedChatRoomDao: CachedChatRoomDao,
    private val chatEventRepository: ChatEventRepository
) : ChatRepository {

    // 방을 나가도 텍스트 전송은 끝까지 진행
    private val sendScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val inFlightTempIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override fun observeChatRooms(): Flow<List<Chat>> {
        return cachedChatRoomDao.observeActiveChatRooms()
            .map { it.toDomains() }
            .flowOn(Dispatchers.IO)
    }

    override fun observeChatRoom(roomId: Int): Flow<Chat?> {
        return cachedChatRoomDao.observeChatRoom(roomId)
            .map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    override fun observeMessages(roomId: Int): Flow<List<Message>> {
        return cachedMessageDao.observeMessages(roomId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getCachedMessages(roomId: Int, limit: Int): List<Message> =
        cachedMessageDao.getMessages(roomId, limit)
            .map { it.toDomain() }
            // 삭제·미전송 메시지는 알림 대화에 넣지 않음
            .filter { !it.isDeleted && it.status == MessageStatus.SENT }

    override suspend fun syncChatRooms(): Result<Unit> {
        return safeApiCall {
            api.getChatRooms(page = 1, limit = 50, includeArchived = false)
        }.map { response ->
            val rooms = response.items.map { it.toDomain() }
            cachedChatRoomDao.upsertAll(rooms.toEntities())
        }
    }

    private suspend fun syncChatRoom(roomId: Int): Result<Unit> {
        return safeApiCall { api.getChatRoom(roomId) }
            .map { dto ->
                cachedChatRoomDao.upsert(dto.toDomain().toEntity())
            }
    }

    override suspend fun syncRecentMessages(roomId: Int, limit: Int): Result<Unit> {
        return safeApiCall {
            api.getMessages(roomId = roomId, limit = limit, beforeMessageId = null)
        }.map { response ->
            val messages = response.messages.map { it.toDomain() }
            if (messages.isNotEmpty()) {
                cachedMessageDao.insertMessages(messages.toEntities())
            }
        }
    }

    // 탭·방 화면이 없어도 포그라운드 동안 수신
    override suspend fun applyRealtimeEvents() {
        chatEventRepository.events.collect { event ->
            when (event) {
                is ChatEvent.MessageReceived -> handleIncomingMessage(event.message)
                is ChatEvent.MessagesRead -> event.messageIds.maxOrNull()?.let { lastReadId ->
                    cachedMessageDao.markMyMessagesAsRead(
                        roomId = event.roomId,
                        lastReadMessageId = lastReadId,
                        readAt = System.currentTimeMillis()
                    )
                }
                is ChatEvent.MessageDeleted -> {
                    cachedMessageDao.markAsDeleted(event.messageId)
                    syncChatRooms()  // 마지막 메시지가 지워지면 목록 미리보기 갱신
                }
                is ChatEvent.Connected -> syncChatRooms()  // 끊긴 동안 놓친 방·미리보기·안 읽음 수
                is ChatEvent.Error -> Timber.e("채팅 실시간 오류: ${event.error}")
            }
        }
    }

    private suspend fun handleIncomingMessage(message: Message) {
        cachedMessageDao.insertMessage(message.toEntity())
        updateRoomForMessage(message)
    }

    private suspend fun updateRoomForMessage(message: Message) {
        val existing = cachedChatRoomDao.getChatRoom(message.roomId)
        if (existing == null) {
            syncChatRoom(message.roomId)
        } else {
            val preview = generateMessagePreview(message)
            cachedChatRoomDao.updateLastMessage(
                roomId = message.roomId,
                preview = preview,
                timestamp = message.createdAt
            )
            // 보고 있는 방은 안 읽음 수를 올리지 않음
            if (!message.isFromMe && ActiveChatRoomTracker.activeRoomId != message.roomId) {
                cachedChatRoomDao.updateUnreadCount(
                    roomId = message.roomId,
                    count = existing.unreadCount + 1
                )
            }
        }
    }

    private fun generateMessagePreview(message: Message): String {
        return when (message.chatMessageType) {
            ChatMessageType.TEXT -> message.content.orEmpty()
            ChatMessageType.IMAGE -> "사진"
            ChatMessageType.VIDEO -> "동영상"
            ChatMessageType.FILE -> "파일"
            ChatMessageType.PRODUCT_LINK -> message.productInfo?.name?.let { "상품: $it" } ?: "상품"
            ChatMessageType.POST_LINK -> "포스트 공유"
        }
    }

    // 채팅방
    override suspend fun getChatRooms(
        page: Int,
        limit: Int,
        includeArchived: Boolean
    ): Result<List<Chat>> {
        return safeApiCall {
            api.getChatRooms(page, limit, includeArchived)
        }.map { response ->
            response.items.map { it.toDomain() }
        }
    }

    override suspend fun getChatRoom(roomId: Int): Result<Chat> {
        return safeApiCall {
            api.getChatRoom(roomId)
        }.map { it.toDomain() }
    }

    override suspend fun createOrGetChatRoom(otherUserId: Int): Result<Chat> {
        return safeApiCall {
            api.createChatRoom(ChatRequest(otherUserId))
        }.map { it.toDomain() }
            .onSuccess { chat -> cachedChatRoomDao.upsert(chat.toEntity()) }
    }

    override suspend fun findChatRoomWithUser(otherUserId: Int): Result<Chat?> {
        return try {
            val response = api.findChatRoomWithUser(otherUserId)
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(body.data?.toDomain())
                    } else {
                        Result.success(null)
                    }
                }
                response.code() == 404 -> Result.success(null)
                else -> Result.failure(
                    ApiException(response.code(), "채팅방 조회 실패: ${response.code()}")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleArchive(roomId: Int, archived: Boolean): Result<Unit> {
        cachedChatRoomDao.setArchived(roomId, archived)
        // data 없는 성공이라 safeApiCallNoData
        return safeApiCallNoData {
            api.toggleArchive(roomId, archived)
        }.onSuccess {
            // 재로그인 뒤엔 보관 방이 캐시에 없어 해제한 방을 받아 넣음
            if (!archived) syncChatRoom(roomId)
        }.onFailure { cachedChatRoomDao.setArchived(roomId, !archived) }
    }

    override suspend fun clearChatRoom(roomId: Int): Result<Unit> {
        return safeApiCallNoData {
            api.clearChatRoom(roomId)
        }.onSuccess {
            cachedMessageDao.deleteByRoomId(roomId)
            cachedChatRoomDao.delete(roomId)
        }
    }

    // 메시지
    override suspend fun getMessages(
        roomId: Int,
        limit: Int,
        beforeMessageId: Int?
    ): Result<MessageListResult> {
        return safeApiCall {
            api.getMessages(roomId, limit, beforeMessageId)
        }.map { response ->
            MessageListResult(
                messages = response.messages.map { it.toDomain() },
                lastReadMessageId = response.lastReadMessageId,
                hasNext = response.hasNext
            )
        }
    }

    override suspend fun sendMessage(
        roomId: Int,
        content: String?,
        messageType: ChatMessageType,
        mediaAttachments: List<String>?,
        mediaThumbnails: List<String>?,
        productId: Int?,
        thumbnailUrl: String?,
        duration: Long?,
        mediaWidth: Int?,
        mediaHeight: Int?,
        fileSize: Long?,
        fileName: String?,
        clientTempId: String?,
        uploadId: Int?
    ): Result<Message> = sendScope.async {
        // 재전송돼도 서버가 같은 메시지로 인식하도록 식별자 고정
        val tempId = clientTempId ?: UUID.randomUUID().toString()
        inFlightTempIds += tempId
        try {
            val optimistic = if (messageType == ChatMessageType.TEXT && !content.isNullOrBlank()) {
                insertOptimisticMessage(roomId, content, messageType, tempId)
            } else null

            val result = safeApiCall {
                api.sendMessage(
                    roomId = roomId,
                    request = SendMessageRequest(
                        content = content,
                        chatMessageType = messageType.name,
                        mediaAttachments = mediaAttachments,
                        mediaThumbnails = mediaThumbnails,
                        productId = productId,
                        thumbnailUrl = thumbnailUrl,
                        duration = duration,
                        mediaWidth = mediaWidth,
                        mediaHeight = mediaHeight,
                        fileSize = fileSize,
                        fileName = fileName,
                        clientTempId = tempId,
                        uploadId = uploadId
                    )
                )
            }.map { it.toDomain() }

            result.onSuccess { serverMessage ->
                if (optimistic != null) {
                    cachedMessageDao.replaceOptimisticMessage(tempId, serverMessage.toEntity())
                    updateRoomForMessage(serverMessage)
                } else {
                    handleIncomingMessage(serverMessage)
                }
            }.onFailure {
                optimistic?.let {
                    cachedMessageDao.updateStatusByClientTempId(
                        clientTempId = tempId,
                        status = MessageStatus.FAILED.name
                    )
                }
            }
            result
        } finally {
            inFlightTempIds -= tempId
        }
    }.await()

    override suspend fun retryFailedMessage(clientTempId: String): Result<Message> = sendScope.async {
        val entity = cachedMessageDao.getByClientTempId(clientTempId)
            ?: return@async Result.failure(IllegalStateException("재시도할 메시지를 찾을 수 없습니다"))

        inFlightTempIds += clientTempId
        try {
            cachedMessageDao.updateStatusByClientTempId(clientTempId, MessageStatus.SENDING.name)

            val result = safeApiCall {
                api.sendMessage(
                    roomId = entity.roomId,
                    request = SendMessageRequest(
                        content = entity.content,
                        chatMessageType = entity.chatMessageType,
                        mediaAttachments = null,
                        productId = entity.productId,
                        thumbnailUrl = null,
                        duration = null,
                        fileSize = null,
                        fileName = null,
                        clientTempId = clientTempId
                    )
                )
            }.map { it.toDomain() }

            result.onSuccess { serverMessage ->
                cachedMessageDao.replaceOptimisticMessage(clientTempId, serverMessage.toEntity())
                updateRoomForMessage(serverMessage)
            }.onFailure {
                cachedMessageDao.updateStatusByClientTempId(clientTempId, MessageStatus.FAILED.name)
            }
            result
        } finally {
            inFlightTempIds -= clientTempId
        }
    }.await()

    override suspend fun cancelFailedMessage(clientTempId: String) {
        cachedMessageDao.deleteFailedByClientTempId(clientTempId)
    }

    override suspend fun failStuckTextMessages(roomId: Int) {
        cachedMessageDao.failStuckSendingText(roomId, inFlightTempIds.toList())
    }

    override suspend fun markMessagesAsRead(roomId: Int): Result<Int?> {
        return safeApiCall {
            api.markMessagesAsRead(roomId)
        }.map { it.lastReadMessageId }
            .onSuccess {
                cachedChatRoomDao.updateUnreadCount(roomId, 0)
                cachedMessageDao.markIncomingMessagesAsRead(roomId, System.currentTimeMillis())
            }
    }

    override suspend fun deleteMessage(messageId: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deleteMessage(messageId)
        }.onSuccess { cachedMessageDao.markAsDeleted(messageId) }
    }

    override suspend fun searchMessages(
        roomId: Int,
        query: String,
        page: Int,
        limit: Int
    ): Result<MessageSearchResult> {
        return safeApiCall {
            api.searchMessages(roomId, query, page, limit)
        }.map { response ->
            MessageSearchResult(
                items = response.items.map { it.toDomain() },
                hasNext = response.hasNext,
                totalCount = response.totalCount
            )
        }
    }

    override suspend fun cacheMessages(messages: List<Message>) {
        if (messages.isNotEmpty()) {
            cachedMessageDao.insertMessages(messages.toEntities())
        }
    }

    // 옛 구간은 위로 올릴 때 서버에서 다시 받음
    override suspend fun cacheLatestMessages(roomId: Int, messages: List<Message>, hasNext: Boolean) {
        cacheMessages(messages)
        cachedMessageDao.deleteSentBefore(roomId, messages.minOfOrNull { it.id } ?: Int.MAX_VALUE)
        // 옛 구간을 지웠으니 처음까지 닿았는지 다시 기록
        cachedChatRoomDao.setHistoryComplete(roomId, !hasNext)
    }

    override suspend fun isChatHistoryComplete(roomId: Int): Boolean =
        cachedChatRoomDao.isHistoryComplete(roomId) == true

    override suspend fun markChatHistoryComplete(roomId: Int) {
        cachedChatRoomDao.setHistoryComplete(roomId, true)
    }

    // 낙관적 갱신

    override suspend fun insertImageMessage(
        roomId: Int,
        localUris: List<String>,
        clientTempId: String,
        content: String?,
        mediaWidth: Int?,
        mediaHeight: Int?
    ) {
        val tempId = -Random.nextInt(1, Int.MAX_VALUE)
        val placeholder = Message(
            id = tempId,
            roomId = roomId,
            sender = UserSummary(id = 0, username = "", displayName = null, avatarUrl = null),
            chatMessageType = ChatMessageType.IMAGE,
            content = content,
            mediaAttachments = localUris,  // 로컬 URI 그대로 저장해 표시
            mediaWidth = mediaWidth,
            mediaHeight = mediaHeight,
            productInfo = null,
            isRead = true,
            readAt = null,
            createdAt = System.currentTimeMillis(),
            isFromMe = true,
            isDeleted = false,
            status = MessageStatus.SENDING,
            clientTempId = clientTempId,
            uploadProgress = 0f
        )
        cachedMessageDao.insertMessage(placeholder.toEntity())
    }

    override suspend fun insertMediaMessage(
        roomId: Int,
        messageType: ChatMessageType,
        localUri: String,
        clientTempId: String,
        content: String?,
        fileName: String?,
        fileSize: Long?,
        duration: Long?,
        mediaWidth: Int?,
        mediaHeight: Int?
    ) {
        val tempId = -Random.nextInt(1, Int.MAX_VALUE)
        val placeholder = Message(
            id = tempId,
            roomId = roomId,
            sender = UserSummary(id = 0, username = "", displayName = null, avatarUrl = null),
            chatMessageType = messageType,
            content = content,
            mediaAttachments = listOf(localUri),
            thumbnailUrl = null,
            duration = duration,
            mediaWidth = mediaWidth,
            mediaHeight = mediaHeight,
            fileSize = fileSize,
            fileName = fileName,
            productInfo = null,
            isRead = true,
            readAt = null,
            createdAt = System.currentTimeMillis(),
            isFromMe = true,
            isDeleted = false,
            status = MessageStatus.SENDING,
            clientTempId = clientTempId,
            uploadProgress = 0f
        )
        cachedMessageDao.insertMessage(placeholder.toEntity())
    }

    override suspend fun updateUploadProgress(clientTempId: String, progress: Float) {
        cachedMessageDao.updateUploadProgressByClientTempId(clientTempId, progress)
    }

    override suspend fun markMediaMessageSent(clientTempId: String) {
        cachedMessageDao.deleteByClientTempId(clientTempId)
    }

    override suspend fun markMediaMessageFailed(clientTempId: String) {
        cachedMessageDao.updateStatusByClientTempId(clientTempId, MessageStatus.FAILED.name)
    }

    override suspend fun markMediaMessageSending(clientTempId: String) {
        cachedMessageDao.updateStatusByClientTempId(clientTempId, MessageStatus.SENDING.name)
        cachedMessageDao.updateUploadProgressByClientTempId(clientTempId, 0f)
    }

    private suspend fun insertOptimisticMessage(
        roomId: Int,
        content: String,
        messageType: ChatMessageType,
        clientTempId: String
    ): Message {
        val tempId = -Random.nextInt(1, Int.MAX_VALUE)
        val placeholder = Message(
            id = tempId,
            roomId = roomId,
            sender = UserSummary(id = 0, username = "", displayName = null, avatarUrl = null),
            chatMessageType = messageType,
            content = content,
            mediaAttachments = null,
            productInfo = null,
            isRead = true,
            readAt = null,
            createdAt = System.currentTimeMillis(),
            isFromMe = true,
            isDeleted = false,
            status = MessageStatus.SENDING,
            clientTempId = clientTempId
        )
        cachedMessageDao.insertMessage(placeholder.toEntity())
        return placeholder
    }

    // 미디어 업로드
    override suspend fun uploadImages(
        roomId: Int,
        images: List<MultipartBody.Part>
    ): Result<ChatImageUpload> {
        return safeApiCall {
            api.uploadImages(roomId, images)
        }.map { response ->
            val urls = response["imageUrls"] ?: emptyList()
            val thumbnails = response["thumbnailUrls"] ?: urls
            ChatImageUpload(urls = urls, thumbnails = thumbnails)
        }
    }

    override suspend fun prepareUpload(
        roomId: Int,
        messageType: ChatMessageType,
        mimeType: String,
        size: Long,
        fileName: String?
    ): Result<UploadTicket> {
        return safeApiCall {
            api.prepareUpload(
                roomId = roomId,
                request = ChatUploadPrepareRequest(
                    messageType = messageType.name,
                    mimeType = mimeType,
                    size = size,
                    fileName = fileName
                )
            )
        }.map { it.toDomain() }
    }
}
