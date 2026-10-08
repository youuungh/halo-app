package com.ninezero.halo.feature.feed.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ninezero.halo.core.database.dao.PendingCommentDao
import com.ninezero.halo.core.database.entity.UploadStatus
import com.ninezero.halo.core.util.MediaPersistenceManager
import com.ninezero.halo.feature.feed.domain.model.CommentUploadEvent
import com.ninezero.halo.feature.feed.domain.model.MediaDraftTargetType
import com.ninezero.halo.feature.feed.domain.repository.CommentRepository
import com.ninezero.halo.feature.feed.domain.repository.MediaDraftRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import timber.log.Timber

@HiltWorker
class CommentUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingCommentDao: PendingCommentDao,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val mediaDraftRepository: MediaDraftRepository,
    private val uploadResultManager: CommentUploadResultManager,
    private val mediaPersistence: MediaPersistenceManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingCommentId = inputData.getLong(KEY_PENDING_COMMENT_ID, -1)
        if (pendingCommentId == -1L) {
            Timber.e("CommentUploadWorker: 유효하지 않은 pending comment ID")
            return Result.failure()
        }

        val pendingComment = pendingCommentDao.getById(pendingCommentId)
        if (pendingComment == null) {
            Timber.e("CommentUploadWorker: pending comment를 찾을 수 없음: $pendingCommentId")
            return Result.failure()
        }

        pendingCommentDao.updateStatus(pendingCommentId, UploadStatus.UPLOADING)

        return try {
            val mediaUris: List<String> = pendingComment.mediaUris?.let {
                try {
                    Json.decodeFromString<List<String>>(it)
                } catch (e: Exception) {
                    Timber.e(e, "mediaUris JSON 파싱 실패")
                    emptyList()
                }
            } ?: emptyList()

            // 미디어 초안 먼저, 댓글은 준비된 뒤 생성
            val mediaDraftIds = mediaDraftRepository.createDrafts(MediaDraftTargetType.COMMENT, mediaUris).getOrElse { exception ->
                Timber.e(exception, "CommentUploadWorker: 미디어 초안 생성 실패")
                handleFailure(
                    pendingCommentId,
                    pendingComment.postId,
                    pendingComment.parentCommentId,
                    exception.message ?: "Media draft failed"
                )
                return Result.success()
            }

            val result = commentRepository.createComment(
                postId = pendingComment.postId,
                content = pendingComment.content,
                parentCommentId = pendingComment.parentCommentId,
                mediaDraftIds = mediaDraftIds,
                clientRequestId = pendingComment.clientRequestId
            )

            result.fold(
                onSuccess = { comment ->
                    Timber.d("CommentUploadWorker: 댓글 생성됨: ${comment.id}")

                    pendingCommentDao.deleteById(pendingCommentId)
                    mediaPersistence.cleanupOwner(
                        MediaPersistenceManager.OwnerType.PENDING_COMMENT,
                        pendingCommentId
                    )
                    // 화면 목록이 캐시 기준이라 새 댓글도 캐시에 넣음
                    commentRepository.cacheComment(comment)
                    pendingComment.parentCommentId?.let { parentId ->
                        commentRepository.applyReplyCreatedToCache(parentId, comment)
                    }
                    postRepository.getCachedPost(pendingComment.postId)?.let { post ->
                        postRepository.updateCachedPost(post.copy(commentCount = post.commentCount + 1))
                    }
                    uploadResultManager.emitResult(
                        CommentUploadEvent.Success(
                            postId = pendingComment.postId,
                            parentCommentId = pendingComment.parentCommentId,
                            comment = comment
                        )
                    )
                    Result.success()
                },
                onFailure = { exception ->
                    Timber.e(exception, "CommentUploadWorker: 댓글 생성 실패")
                    handleFailure(
                        pendingCommentId,
                        pendingComment.postId,
                        pendingComment.parentCommentId,
                        exception.message ?: "Unknown error"
                    )
                    Result.success()
                }
            )
        } catch (e: CancellationException) {
            // WorkManager가 재실행하므로 실패 처리하지 않음
            throw e
        } catch (e: Exception) {
            Timber.e(e, "CommentUploadWorker: 업로드 중 예외 발생")
            handleFailure(
                pendingCommentId,
                pendingComment.postId,
                pendingComment.parentCommentId,
                e.message ?: "Unknown error"
            )
            Result.success()
        }
    }

    private suspend fun handleFailure(
        pendingCommentId: Long,
        postId: Int,
        parentCommentId: Int?,
        errorMessage: String
    ) {
        pendingCommentDao.deleteById(pendingCommentId)
        mediaPersistence.cleanupOwner(
            MediaPersistenceManager.OwnerType.PENDING_COMMENT,
            pendingCommentId
        )
        uploadResultManager.emitResult(
            CommentUploadEvent.Failure(
                postId = postId,
                parentCommentId = parentCommentId,
                errorMessage = errorMessage
            )
        )
    }

    companion object {
        const val KEY_PENDING_COMMENT_ID = "pending_comment_id"

        fun createWorkRequest(pendingCommentId: Long) =
            OneTimeWorkRequestBuilder<CommentUploadWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putLong(KEY_PENDING_COMMENT_ID, pendingCommentId)
                        .build()
                )
                .addTag("comment_upload_$pendingCommentId")
                .build()

        fun enqueue(context: Context, pendingCommentId: Long): String {
            val workRequest = createWorkRequest(pendingCommentId)
            WorkManager.getInstance(context).enqueue(workRequest)
            return workRequest.id.toString()
        }
    }
}
