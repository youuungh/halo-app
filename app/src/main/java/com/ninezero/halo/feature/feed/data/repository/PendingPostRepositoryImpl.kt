package com.ninezero.halo.feature.feed.data.repository

import android.content.Context
import android.net.Uri
import com.ninezero.halo.core.database.dao.PendingPostDao
import com.ninezero.halo.core.database.entity.PendingPostEntity
import com.ninezero.halo.core.database.entity.UploadStatus
import com.ninezero.halo.core.media.MediaCopyStore
import com.ninezero.halo.core.media.MediaUtils
import com.ninezero.halo.feature.feed.data.mapper.isVideoMediaUrl
import com.ninezero.halo.feature.feed.data.mapper.toPost
import com.ninezero.halo.feature.feed.data.worker.PostUploadWorker
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostType
import com.ninezero.halo.feature.feed.domain.repository.PendingPostRepository
import com.ninezero.halo.feature.subscription.domain.model.SubscriptionPlanTier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class PendingPostRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val pendingPostDao: PendingPostDao,
    private val mediaCopyStore: MediaCopyStore
) : PendingPostRepository {

    override suspend fun addPendingPost(
        content: String,
        postType: PostType,
        mediaUris: List<Uri>?,
        authorId: Int,
        authorUsername: String,
        authorDisplayName: String?,
        authorAvatarUrl: String?,
        tagIds: List<Int>?,
        sectionTagId: Int?,
        requiredTier: SubscriptionPlanTier,
        creatorId: Int?,
        isSecret: Boolean
    ): Long {
        val tagIdsJson = tagIds?.let { Json.encodeToString(it) }

        val initialEntity = PendingPostEntity(
            clientRequestId = UUID.randomUUID().toString(),
            content = content,
            postType = postType.name,
            mediaUris = null,  // auto-generated ID 먼저 확보
            authorId = authorId,
            authorUsername = authorUsername,
            authorDisplayName = authorDisplayName,
            authorAvatarUrl = authorAvatarUrl,
            tagIds = tagIdsJson,
            sectionTagId = sectionTagId,
            requiredTier = requiredTier.name,
            creatorId = creatorId,
            isSecret = isSecret,
            status = UploadStatus.PENDING
        )
        val id = pendingPostDao.insert(initialEntity)

        try {
            // 재시도에도 쓰는 사본
            val copied = if (mediaUris.isNullOrEmpty()) {
                emptyList()
            } else {
                mediaCopyStore.copyForOwner(
                    sourceUris = mediaUris,
                    ownerType = MediaCopyStore.OwnerType.PENDING_POST,
                    ownerId = id
                )
            }
            // 하나라도 못 옮기면 게시 안 함
            if (copied.size != mediaUris.orEmpty().size) throw IOException("게시 미디어 복사 실패")
            val copiedJson: String? = if (copied.isEmpty()) {
                null
            } else {
                Json.encodeToString(copied.map { it.toString() })
            }
            // 원본 크기 파싱
            val dimensionsJson: String? = if (copied.isEmpty()) {
                null
            } else {
                val dimensions = copied.map { uri ->
                    val (width, height) = if (isVideoMediaUrl(uri.toString())) {
                        MediaUtils.getVideoDimensions(context, uri)
                    } else {
                        MediaUtils.getImageDimensions(context, uri)
                    }
                    listOf(width, height)
                }
                Json.encodeToString(dimensions)
            }

            // 워커 시작 전에 미디어 먼저 저장
            pendingPostDao.update(initialEntity.copy(id = id, mediaUris = copiedJson, mediaDimensions = dimensionsJson))
        } catch (e: Exception) {
            // 워커 없이 남는 대기 행·사본 정리
            withContext(NonCancellable) {
                pendingPostDao.deleteById(id)
                mediaCopyStore.cleanupOwner(MediaCopyStore.OwnerType.PENDING_POST, id)
            }
            throw e
        }

        val workerId = PostUploadWorker.enqueue(context, id)
        pendingPostDao.updateStatusWithWorkerId(id, UploadStatus.UPLOADING, workerId)

        return id
    }

    override fun getPendingPostsByAuthor(authorId: Int): Flow<List<Post>> {
        return pendingPostDao.getByAuthorId(authorId).map { entities ->
            // 커뮤니티 글은 해당 탭에서만 노출
            entities.filter { it.creatorId == null }.map { it.toPost() }
        }
    }

    override fun getPendingCommunityPosts(authorId: Int, creatorId: Int): Flow<List<Post>> {
        return pendingPostDao.getByAuthorId(authorId).map { entities ->
            entities.filter { it.creatorId == creatorId }.map { it.toPost() }
        }
    }
}
