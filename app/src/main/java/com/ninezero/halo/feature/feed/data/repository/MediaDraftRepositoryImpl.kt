package com.ninezero.halo.feature.feed.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.upload.DirectUploader
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.feed.data.remote.api.MediaDraftApi
import com.ninezero.halo.feature.feed.data.remote.dto.VideoDraftRequest
import com.ninezero.halo.feature.feed.data.remote.dto.VideoUploadPrepareRequest
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.feed.domain.model.MediaDraft
import com.ninezero.halo.feature.feed.domain.model.MediaDraftStatus
import com.ninezero.halo.feature.feed.domain.model.MediaDraftTargetType
import com.ninezero.halo.feature.feed.domain.repository.MediaDraftRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import okhttp3.MultipartBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val VIDEO_DRAFT_POLL_INTERVAL_MS = 2_000L
private const val VIDEO_DRAFT_TIMEOUT_MS = 120_000L

@Singleton
class MediaDraftRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: MediaDraftApi,
    private val directUploader: DirectUploader
) : MediaDraftRepository {

    override suspend fun createDrafts(targetType: MediaDraftTargetType, uris: List<String>): Result<List<Int>> {
        val draftIds = mutableListOf<Int>()
        val imageParts = mutableListOf<MultipartBody.Part>()

        // 사진은 연속 구간 한 요청, 영상은 하나씩
        suspend fun flushImages(): Result<Unit> {
            if (imageParts.isEmpty()) return Result.success(Unit)
            draftIds += createImageDrafts(targetType, imageParts.toList()).getOrElse { return Result.failure(it) }
            imageParts.clear()
            return Result.success(Unit)
        }

        for (uriString in uris) {
            val uri = uriString.toUri()
            if (MediaUtils.resolveMimeType(context, uri).startsWith("video/")) {
                flushImages().getOrElse { return Result.failure(it) }
                draftIds += createVideoDraft(targetType, uri).getOrElse { return Result.failure(it) }
            } else {
                // 하나라도 못 읽으면 실패, 빠진 채 게시하지 않음
                imageParts += MediaUtils.createUploadPart(context, uri, "mediaFiles", "image_${imageParts.size}")
                    ?: return Result.failure(IOException("미디어를 읽을 수 없음: $uriString"))
            }
        }
        flushImages().getOrElse { return Result.failure(it) }

        return Result.success(draftIds)
    }

    private suspend fun createImageDrafts(targetType: MediaDraftTargetType, files: List<MultipartBody.Part>): Result<List<Int>> {
        return safeApiCall {
            api.createImageDrafts(targetType.name, files)
        }.map { drafts ->
            drafts.map { it.toDomain().id }
        }
    }

    private suspend fun createVideoDraft(targetType: MediaDraftTargetType, uri: Uri): Result<Int> {
        val uploadId = directUploader.uploadVideo(uri) { mimeType, size ->
            safeApiCall {
                api.prepareVideoUpload(targetType.name, VideoUploadPrepareRequest(mimeType = mimeType, size = size))
            }.map { it.toDomain() }
        }.getOrElse { return Result.failure(it) }

        val draft = safeApiCall {
            api.createVideoDraft(targetType.name, VideoDraftRequest(uploadId = uploadId))
        }.map { it.toDomain() }.getOrElse { return Result.failure(it) }
        return awaitVideoDraft(draft)
    }

    /** 서버 가공 완료까지 상태 조회 */
    private suspend fun awaitVideoDraft(draft: MediaDraft): Result<Int> {  // 서버가 실패로 답하거나 시한을 넘기면 실패
        var current = draft
        val deadline = System.currentTimeMillis() + VIDEO_DRAFT_TIMEOUT_MS
        while (current.status == MediaDraftStatus.PROCESSING) {
            if (System.currentTimeMillis() >= deadline) {
                return Result.failure(IOException("영상 처리가 끝나지 않았습니다"))
            }
            delay(VIDEO_DRAFT_POLL_INTERVAL_MS)
            current = safeApiCall { api.getDraft(draft.id) }.map { it.toDomain() }.getOrElse { error ->
                // 4xx면 중단, 그 외 오류는 시한 안에서 재조회
                val code = (error as? ApiException)?.code
                if (code != null && code in 400..499) return Result.failure(error)
                current
            }
        }
        return if (current.status == MediaDraftStatus.READY) {
            Result.success(current.id)
        } else {
            Result.failure(IOException("영상 처리에 실패했습니다"))
        }
    }
}
