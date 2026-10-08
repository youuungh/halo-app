package com.ninezero.halo.feature.feed.data.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ninezero.halo.core.database.dao.PendingPostDao
import com.ninezero.halo.core.database.entity.UploadStatus
import com.ninezero.halo.core.util.MediaPersistenceManager
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.feed.domain.model.MediaDraftTargetType
import com.ninezero.halo.feature.feed.domain.model.PostType
import com.ninezero.halo.feature.feed.domain.model.PostUploadEvent
import com.ninezero.halo.feature.feed.domain.repository.MediaDraftRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import timber.log.Timber

@HiltWorker
class PostUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingPostDao: PendingPostDao,
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val mediaDraftRepository: MediaDraftRepository,
    private val uploadResultManager: PostUploadResultManager,
    private val mediaPersistence: MediaPersistenceManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingPostId = inputData.getLong(KEY_PENDING_POST_ID, -1)
        if (pendingPostId == -1L) {
            Timber.e("PostUploadWorker: 유효하지 않은 pending post ID")
            return Result.failure()
        }

        val pendingPost = pendingPostDao.getById(pendingPostId)
        if (pendingPost == null) {
            Timber.e("PostUploadWorker: pending post를 찾을 수 없음: $pendingPostId")
            return Result.failure()
        }

        pendingPostDao.updateStatus(pendingPostId, UploadStatus.UPLOADING)

        return try {
            val mediaUris: List<String> = pendingPost.mediaUris?.let {
                try {
                    Json.decodeFromString<List<String>>(it)
                } catch (e: Exception) {
                    Timber.e(e, "mediaUris JSON 파싱 실패")
                    emptyList()
                }
            } ?: emptyList()

            val postType = when {
                mediaUris.isEmpty() -> PostType.TEXT
                mediaUris.any { uriString ->
                    val uri = uriString.toUri()
                    MediaUtils.resolveMimeType(context, uri).startsWith("video/")
                } -> PostType.VIDEO
                else -> PostType.IMAGE
            }

            val tagIds: List<Int>? = pendingPost.tagIds?.let {
                try {
                    Json.decodeFromString<List<Int>>(it)
                } catch (e: Exception) {
                    Timber.e(e, "tagIds JSON 파싱 실패")
                    null
                }
            }

            // 미디어 초안 먼저, 글은 준비된 뒤 생성
            val mediaDraftIds = mediaDraftRepository.createDrafts(MediaDraftTargetType.POST, mediaUris).getOrElse { exception ->
                Timber.e(exception, "PostUploadWorker: 미디어 초안 생성 실패")
                handleFailure(pendingPostId, pendingPost.creatorId)
                return Result.success()
            }

            val createResult = if (pendingPost.creatorId != null) {
                profileRepository.createCommunityPost(
                    creatorId = pendingPost.creatorId,
                    content = pendingPost.content,
                    isSecret = pendingPost.isSecret,
                    mediaDraftIds = mediaDraftIds,
                    clientRequestId = pendingPost.clientRequestId
                )
            } else {
                postRepository.createPost(
                    content = pendingPost.content,
                    postType = postType,
                    requiredTier = pendingPost.requiredTier,
                    tagIds = tagIds,
                    sectionTagId = pendingPost.sectionTagId,
                    mediaDraftIds = mediaDraftIds,
                    clientRequestId = pendingPost.clientRequestId
                )
            }

            createResult.fold(
                onSuccess = { post ->
                    Timber.d("PostUploadWorker: 포스트 생성됨: ${post.id}")

                    pendingPostDao.deleteById(pendingPostId)
                    mediaPersistence.cleanupOwner(
                        MediaPersistenceManager.OwnerType.PENDING_POST,
                        pendingPostId
                    )
                    uploadResultManager.emitResult(
                        PostUploadEvent.Success(
                            creatorId = pendingPost.creatorId
                        )
                    )
                    Result.success()
                },
                onFailure = { exception ->
                    Timber.e(exception, "PostUploadWorker: 포스트 생성 실패")
                    handleFailure(pendingPostId, pendingPost.creatorId)
                    Result.success()
                }
            )
        } catch (e: CancellationException) {
            // WorkManager가 재실행하므로 실패 처리하지 않음
            throw e
        } catch (e: Exception) {
            Timber.e(e, "PostUploadWorker: 업로드 중 예외 발생")
            handleFailure(pendingPostId, pendingPost.creatorId)
            Result.success()
        }
    }

    private suspend fun handleFailure(pendingPostId: Long, creatorId: Int?) {
        pendingPostDao.deleteById(pendingPostId)
        mediaPersistence.cleanupOwner(
            MediaPersistenceManager.OwnerType.PENDING_POST,
            pendingPostId
        )
        uploadResultManager.emitResult(
            PostUploadEvent.Failure(
                creatorId = creatorId
            )
        )
    }

    companion object {
        const val KEY_PENDING_POST_ID = "pending_post_id"

        fun createWorkRequest(pendingPostId: Long) =
            OneTimeWorkRequestBuilder<PostUploadWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putLong(KEY_PENDING_POST_ID, pendingPostId)
                        .build()
                )
                .addTag("post_upload_$pendingPostId")
                .build()

        fun enqueue(context: Context, pendingPostId: Long): String {
            val workRequest = createWorkRequest(pendingPostId)
            WorkManager.getInstance(context).enqueue(workRequest)
            return workRequest.id.toString()
        }
    }
}
