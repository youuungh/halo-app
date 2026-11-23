package com.ninezero.halo.feature.chat.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.ninezero.halo.core.database.dao.PendingChatMessageDao
import com.ninezero.halo.core.database.entity.PendingChatMessageEntity
import com.ninezero.halo.core.database.entity.UploadStatus
import com.ninezero.halo.core.media.MediaCopyStore
import com.ninezero.halo.feature.chat.data.worker.ChatDownloadEventBus
import com.ninezero.halo.feature.chat.data.worker.ChatFileDownloadWorker
import com.ninezero.halo.feature.chat.data.worker.ChatMediaUploadWorker
import com.ninezero.halo.feature.chat.data.worker.ChatUploadEventBus
import com.ninezero.halo.feature.chat.domain.model.ChatDownloadEvent
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.ChatUploadEvent
import com.ninezero.halo.feature.chat.domain.model.MediaUploadHandle
import com.ninezero.halo.feature.chat.domain.repository.ChatMediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMediaRepositoryImpl @Inject constructor(
    private val pendingChatMessageDao: PendingChatMessageDao,
    private val uploadEventBus: ChatUploadEventBus,
    private val downloadEventBus: ChatDownloadEventBus,
    private val mediaCopyStore: MediaCopyStore,
    @param:ApplicationContext private val context: Context
) : ChatMediaRepository {

    override val uploadEvents: SharedFlow<ChatUploadEvent> = uploadEventBus.uploadResults
    override val downloadEvents: SharedFlow<ChatDownloadEvent> = downloadEventBus.downloadResults

    override suspend fun enqueueUpload(
        roomId: Int,
        content: String?,
        messageType: ChatMessageType,
        mediaUris: List<String>
    ): MediaUploadHandle {
        val pendingMessage = PendingChatMessageEntity(
            roomId = roomId,
            content = content,
            messageType = messageType.name,
            mediaUris = Json.encodeToString(mediaUris)
        )
        val pendingId = pendingChatMessageDao.insert(pendingMessage)
        val clientTempId = clientTempIdOf(pendingId, pendingMessage.createdAt)

        // content 권한이 사라지기 전에 앱 저장소로 복사
        val copied = mediaCopyStore.copyForOwner(
            sourceUris = mediaUris.map { it.toUri() },
            ownerType = MediaCopyStore.OwnerType.PENDING_CHAT,
            ownerId = pendingId
        )
        val actualUris = if (copied.size == mediaUris.size) {
            copied.map { it.toString() }
        } else {
            // 일부 실패 시 원본 URI 폴백
            mediaCopyStore.cleanupOwner(MediaCopyStore.OwnerType.PENDING_CHAT, pendingId)
            mediaUris
        }
        if (actualUris !== mediaUris) {
            pendingChatMessageDao.update(
                pendingMessage.copy(id = pendingId, mediaUris = Json.encodeToString(actualUris))
            )
        }

        ChatMediaUploadWorker.enqueue(context, pendingId, clientTempId)
        return MediaUploadHandle(clientTempId = clientTempId, mediaUris = actualUris)
    }

    override suspend fun retryUpload(clientTempId: String): Boolean {
        val pendingMessageId = pendingIdOf(clientTempId) ?: return false
        if (pendingChatMessageDao.getById(pendingMessageId) == null) return false
        pendingChatMessageDao.updateStatus(pendingMessageId, UploadStatus.PENDING)
        ChatMediaUploadWorker.enqueue(context, pendingMessageId, clientTempId)
        return true
    }

    override suspend fun discardUpload(clientTempId: String) {
        val pendingMessageId = pendingIdOf(clientTempId) ?: return
        pendingChatMessageDao.deleteById(pendingMessageId)
        mediaCopyStore.cleanupOwner(MediaCopyStore.OwnerType.PENDING_CHAT, pendingMessageId)
    }

    override fun enqueueFileDownload(fileUrl: String, fileName: String) {
        ChatFileDownloadWorker.enqueue(
            context = context,
            fileUrl = fileUrl,
            fileName = fileName
        )
    }

    override fun cancelFileDownload(fileUrl: String) {
        ChatFileDownloadWorker.cancel(context, fileUrl)
    }

    private companion object {
        /** 만든 시각을 붙여 기기 간에 안 겹치는 임시 id */
        fun clientTempIdOf(pendingId: Long, createdAt: Long) = "pending-$createdAt-$pendingId"

        /** 임시 id에 담긴 대기 기록 번호 */
        fun pendingIdOf(clientTempId: String): Long? =
            clientTempId.takeIf { it.startsWith("pending-") }?.substringAfterLast('-')?.toLongOrNull()
    }
}
