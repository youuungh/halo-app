package com.ninezero.halo.feature.feed.data.repository

import android.net.Uri
import androidx.core.net.toUri
import com.ninezero.halo.core.database.dao.PostDraftDao
import com.ninezero.halo.core.database.entity.PostDraftEntity
import com.ninezero.halo.core.util.MediaPersistenceManager
import com.ninezero.halo.feature.feed.domain.model.PostDraft
import com.ninezero.halo.feature.feed.domain.repository.PostDraftRepository
import java.io.IOException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostDraftRepositoryImpl @Inject constructor(
    private val postDraftDao: PostDraftDao,
    private val mediaPersistence: MediaPersistenceManager
) : PostDraftRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getAllDrafts(): Flow<List<PostDraft>> {
        return postDraftDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveDraft(
        content: String,
        mediaUris: List<Uri>,
        authorId: Int,
        authorUsername: String,
        authorDisplayName: String?,
        authorAvatarUrl: String?
    ): Long {
        val initialEntity = PostDraftEntity(
            content = content,
            mediaUris = null,  // auto-generated ID 먼저 확보
            authorId = authorId,
            authorUsername = authorUsername,
            authorDisplayName = authorDisplayName,
            authorAvatarUrl = authorAvatarUrl
        )
        val id = postDraftDao.insert(initialEntity)

        if (mediaUris.isNotEmpty()) {
            try {
                val persistedUris = mediaPersistence.persistForOwner(
                    sourceUris = mediaUris,
                    ownerType = MediaPersistenceManager.OwnerType.DRAFT,
                    ownerId = id
                )
                // 하나라도 못 옮기면 저장 실패
                if (persistedUris.size != mediaUris.size) throw IOException("임시 저장 미디어 복사 실패")
                postDraftDao.update(initialEntity.copy(id = id, mediaUris = json.encodeToString(persistedUris.map { it.toString() })))
            } catch (e: Exception) {
                // 글만 남은 초안 줄·옮긴 사본 정리
                withContext(NonCancellable) {
                    postDraftDao.deleteById(id)
                    mediaPersistence.cleanupOwner(MediaPersistenceManager.OwnerType.DRAFT, id)
                }
                throw e
            }
        }

        return id
    }

    override suspend fun deleteDraft(id: Long) {
        postDraftDao.deleteById(id)
        mediaPersistence.cleanupOwner(MediaPersistenceManager.OwnerType.DRAFT, id)
    }

    override suspend fun deleteDraftRecordOnly(id: Long) {
        postDraftDao.deleteById(id)
    }

    private fun PostDraftEntity.toDomain(): PostDraft {
        val uriList = mediaUris?.let {
            try {
                json.decodeFromString<List<String>>(it).map { uriString -> uriString.toUri() }
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return PostDraft(
            id = id,
            content = content,
            mediaUris = uriList,
            authorUsername = authorUsername,
            authorDisplayName = authorDisplayName,
            updatedAt = updatedAt
        )
    }
}
