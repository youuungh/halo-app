package com.ninezero.halo.feature.chat.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.ninezero.halo.core.database.dao.PendingChatMessageDao
import com.ninezero.halo.core.database.entity.PendingChatMessageEntity
import com.ninezero.halo.core.database.entity.UploadStatus
import com.ninezero.halo.core.util.MediaPersistenceManager
import com.ninezero.halo.feature.chat.data.worker.ChatDownloadResultManager
import com.ninezero.halo.feature.chat.data.worker.ChatFileDownloadWorker
import com.ninezero.halo.feature.chat.data.worker.ChatMediaUploadWorker
import com.ninezero.halo.feature.chat.data.worker.ChatUploadResultManager
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
    private val uploadResultManager: ChatUploadResultManager,
    private val downloadResultManager: ChatDownloadResultManager,
    private val mediaPersistence: MediaPersistenceManager,
    @param:ApplicationContext private val context: Context
) : ChatMediaRepository {

    override val uploadEvents: SharedFlow<ChatUploadEvent> = uploadResultManager.uploadResults
    override val downloadEvents: SharedFlow<ChatDownloadEvent> = downloadResultManager.downloadResults

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

        // content 권한이 사라지기 전에 앱 저장소로 복사
        val staged = mediaPersistence.persistForOwner(
            sourceUris = mediaUris.map { it.toUri() },
            ownerType = MediaPersistenceManager.OwnerType.PENDING_CHAT,
            ownerId = pendingId
        )
        val effectiveUris = if (staged.size == mediaUris.size) {
            staged.map { it.toString() }
        } else {
            // 일부 실패 시 원본 URI 폴백
            mediaPersistence.cleanupOwner(MediaPersistenceManager.OwnerType.PENDING_CHAT, pendingId)
            mediaUris
        }
        if (effectiveUris !== mediaUris) {
            pendingChatMessageDao.update(
                pendingMessage.copy(id = pendingId, mediaUris = Json.encodeToString(effectiveUris))
            )
        }

        ChatMediaUploadWorker.enqueue(context, pendingId)
        return MediaUploadHandle(pendingMessageId = pendingId, mediaUris = effectiveUris)
    }

    override suspend fun retryUpload(pendingMessageId: Long): Boolean {
        if (pendingChatMessageDao.getById(pendingMessageId) == null) return false
        pendingChatMessageDao.updateStatus(pendingMessageId, UploadStatus.PENDING)
        ChatMediaUploadWorker.enqueue(context, pendingMessageId)
        return true
    }

    override suspend fun discardUpload(pendingMessageId: Long) {
        pendingChatMessageDao.deleteById(pendingMessageId)
        mediaPersistence.cleanupOwner(MediaPersistenceManager.OwnerType.PENDING_CHAT, pendingMessageId)
    }

    override fun enqueueFileDownload(fileUrl: String, fileName: String) {
        ChatFileDownloadWorker.enqueue(
            context = context,
            fileUrl = fileUrl,
            fileName = fileName
        )
    }
}
