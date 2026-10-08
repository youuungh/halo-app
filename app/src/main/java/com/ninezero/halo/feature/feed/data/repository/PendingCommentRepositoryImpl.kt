package com.ninezero.halo.feature.feed.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.ninezero.halo.core.database.dao.PendingCommentDao
import com.ninezero.halo.core.database.entity.PendingCommentEntity
import com.ninezero.halo.core.database.entity.UploadStatus
import com.ninezero.halo.core.util.MediaPersistenceManager
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.feed.data.mapper.toComment
import com.ninezero.halo.feature.feed.data.worker.CommentUploadWorker
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.repository.PendingCommentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class PendingCommentRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pendingCommentDao: PendingCommentDao,
    private val mediaPersistence: MediaPersistenceManager
) : PendingCommentRepository {

    override suspend fun addPendingComment(
        postId: Int,
        content: String,
        parentCommentId: Int?,
        authorId: Int,
        authorUsername: String,
        authorDisplayName: String?,
        authorAvatarUrl: String?,
        mediaUris: List<String>?
    ): Long {
        val initialEntity = PendingCommentEntity(
            clientRequestId = UUID.randomUUID().toString(),
            postId = postId,
            content = content,
            parentCommentId = parentCommentId,
            authorId = authorId,
            authorUsername = authorUsername,
            authorDisplayName = authorDisplayName,
            authorAvatarUrl = authorAvatarUrl,
            mediaUris = null,  // auto-generated ID 먼저 확보
            status = UploadStatus.PENDING
        )
        val id = pendingCommentDao.insert(initialEntity)

        try {
            // 선택기 권한이 끝나도 쓰는 사본
            val persisted = if (mediaUris.isNullOrEmpty()) {
                emptyList()
            } else {
                mediaPersistence.persistForOwner(
                    sourceUris = mediaUris.map { it.toUri() },
                    ownerType = MediaPersistenceManager.OwnerType.PENDING_COMMENT,
                    ownerId = id
                )
            }
            // 하나라도 못 옮기면 게시 안 함
            if (persisted.size != mediaUris.orEmpty().size) throw IOException("게시 미디어 복사 실패")
            // 원본 크기 파싱
            val mediaDimensions = persisted.map { uri ->
                val isVideo = MediaUtils.resolveMimeType(context, uri).startsWith("video/")
                val (w, h) = if (isVideo) {
                    MediaUtils.getVideoDimensions(context, uri)
                } else {
                    MediaUtils.getImageDimensions(context, uri)
                }
                if (w != null && h != null) "${w}x${h}" else null
            }

            // 워커 시작 전에 미디어 먼저 저장
            if (persisted.isNotEmpty()) {
                pendingCommentDao.update(
                    initialEntity.copy(
                        id = id,
                        mediaUris = Json.encodeToString(persisted.map { it.toString() }),
                        mediaDimensions = Json.encodeToString(mediaDimensions)
                    )
                )
            }
        } catch (e: Exception) {
            // 워커 없이 남는 대기 행·사본 정리
            withContext(NonCancellable) {
                pendingCommentDao.deleteById(id)
                mediaPersistence.cleanupOwner(MediaPersistenceManager.OwnerType.PENDING_COMMENT, id)
            }
            throw e
        }

        val workerId = CommentUploadWorker.enqueue(context, id)
        pendingCommentDao.updateStatusWithWorkerId(id, UploadStatus.UPLOADING, workerId)

        return id
    }

    override fun getPendingCommentsByPostId(postId: Int): Flow<List<Comment>> {
        return pendingCommentDao.getByPostId(postId).map { entities ->
            entities.map { it.toComment() }
        }
    }

    override fun getPendingCommentsByParentId(parentCommentId: Int): Flow<List<Comment>> {
        return pendingCommentDao.getByParentCommentId(parentCommentId).map { entities ->
            entities.map { it.toComment() }
        }
    }
}
