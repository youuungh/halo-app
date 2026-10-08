package com.ninezero.halo.feature.chat.data.worker

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ninezero.halo.core.database.dao.CachedMessageDao
import com.ninezero.halo.core.database.dao.PendingChatMessageDao
import com.ninezero.halo.core.database.entity.UploadStatus
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.network.upload.DirectUploader
import com.ninezero.halo.core.network.upload.ProgressRequestBody
import com.ninezero.halo.core.network.upload.UriRequestBody
import com.ninezero.halo.core.util.MediaPersistenceManager
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.ChatUploadEvent
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException

data class MediaUploadData(
    val mediaAttachments: List<String>? = null,
    // 직접 업로드한 영상·파일
    val uploadId: Int? = null,
    val mediaThumbnails: List<String>? = null,
    val thumbnailUrl: String? = null,
    val duration: Long? = null,
    val mediaWidth: Int? = null,
    val mediaHeight: Int? = null,
    val fileSize: Long? = null,
    val fileName: String? = null
)

@HiltWorker
class ChatMediaUploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pendingChatMessageDao: PendingChatMessageDao,
    private val cachedMessageDao: CachedMessageDao,
    private val chatRepository: ChatRepository,
    private val uploadResultManager: ChatUploadResultManager,
    private val mediaPersistence: MediaPersistenceManager,
    private val directUploader: DirectUploader
) : CoroutineWorker(context, workerParams) {

    private var foregroundActive = false
    private var lastNotifyAt = 0L
    private var notificationId = 0
    private var notificationTitle = "미디어 전송 중"

    override suspend fun doWork(): Result {
        val pendingMessageId = inputData.getLong(KEY_PENDING_MESSAGE_ID, -1)
        if (pendingMessageId == -1L) {
            Timber.e("ChatMediaUploadWorker: 유효하지 않은 pending message ID")
            return Result.failure()
        }

        val pendingMessage = pendingChatMessageDao.getById(pendingMessageId)
        if (pendingMessage == null) {
            Timber.e("ChatMediaUploadWorker: pending message를 찾을 수 없음: $pendingMessageId")
            chatRepository.markMediaMessageSent(pendingMessageId.toString())
            return Result.failure()
        }

        val messageType = ChatMessageType.from(pendingMessage.messageType)
        notificationId = ChatTransferNotifications.uploadNotificationId(pendingMessageId)
        notificationTitle = when (messageType) {
            ChatMessageType.IMAGE -> "사진 전송 중"
            ChatMessageType.VIDEO -> "동영상 전송 중"
            ChatMessageType.FILE -> "파일 전송 중"
            else -> "미디어 전송 중"
        }

        // FGS 승격
        try {
            setForeground(ChatTransferNotifications.foregroundInfo(context, notificationId, notificationTitle))
            foregroundActive = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "ChatMediaUploadWorker: setForeground 거부됨 — 백그라운드 작업으로 계속 진행")
        }

        pendingChatMessageDao.updateStatus(pendingMessageId, UploadStatus.UPLOADING)

        return try {
            val mediaUris: List<String> = pendingMessage.mediaUris?.let {
                try {
                    Json.decodeFromString<List<String>>(it)
                } catch (e: Exception) {
                    Timber.e(e, "mediaUris JSON 파싱 실패")
                    emptyList()
                }
            } ?: emptyList()

            if (mediaUris.isEmpty()) {
                handleFailure(pendingMessageId, pendingMessage.roomId, LOCAL_FILE_MESSAGE)
                return Result.success()
            }

            val roomId = pendingMessage.roomId
            val content = pendingMessage.content

            val uploadResult: kotlin.Result<MediaUploadData> = when (messageType) {
                ChatMessageType.IMAGE -> uploadImages(roomId, mediaUris, pendingMessageId)
                ChatMessageType.VIDEO -> uploadVideo(roomId, mediaUris.first(), pendingMessageId)
                ChatMessageType.FILE -> uploadFile(roomId, mediaUris.first(), pendingMessageId)
                else -> kotlin.Result.failure(Exception("Unknown message type: $messageType"))
            }

            uploadResult.fold(
                onSuccess = { uploadData ->
                    chatRepository.sendMessage(
                        roomId = roomId,
                        content = content,
                        messageType = messageType,
                        mediaAttachments = uploadData.mediaAttachments,
                        mediaThumbnails = uploadData.mediaThumbnails,
                        thumbnailUrl = uploadData.thumbnailUrl,
                        duration = uploadData.duration,
                        mediaWidth = uploadData.mediaWidth,
                        mediaHeight = uploadData.mediaHeight,
                        fileSize = uploadData.fileSize,
                        fileName = uploadData.fileName,
                        // 재시도엔 같은 값, 다른 기기와는 시각으로 구분
                        clientTempId = "pending-${pendingMessage.createdAt}-$pendingMessageId",
                        uploadId = uploadData.uploadId
                    ).fold(
                        onSuccess = { message ->
                            Timber.d("ChatMediaUploadWorker: 메시지 전송 성공: ${message.id}")
                            pendingChatMessageDao.deleteById(pendingMessageId)
                            chatRepository.markMediaMessageSent(pendingMessageId.toString())
                            mediaPersistence.cleanupOwner(
                                MediaPersistenceManager.OwnerType.PENDING_CHAT,
                                pendingMessageId
                            )
                            Result.success()
                        },
                        onFailure = { error ->
                            Timber.e(error, "ChatMediaUploadWorker: 메시지 전송 실패")
                            handleFailure(pendingMessageId, roomId, error.toFailureMessage())
                            Result.success()
                        }
                    )
                },
                onFailure = { error ->
                    Timber.e(error, "ChatMediaUploadWorker: 미디어 업로드 실패")
                    handleFailure(pendingMessageId, roomId, error.toFailureMessage())
                    Result.success()
                }
            )
        } catch (e: CancellationException) {
            // WorkManager가 재실행하므로 실패 처리하지 않음
            throw e
        } catch (e: Exception) {
            Timber.e(e, "ChatMediaUploadWorker: 업로드 중 예외 발생")
            handleFailure(pendingMessageId, pendingMessage.roomId, e.toFailureMessage())
            Result.success()
        }
    }

    /** 진행률을 placeholder에 기록 */
    private suspend fun publishProgress(pendingMessageId: Long, fraction: Float) {
        val clamped = fraction.coerceIn(0f, 1f)
        chatRepository.updateUploadProgress(pendingMessageId.toString(), clamped)
        if (foregroundActive) {
            val now = System.currentTimeMillis()
            if (now - lastNotifyAt >= 500) {
                lastNotifyAt = now
                ChatTransferNotifications.notifyProgress(
                    context, notificationId, notificationTitle, null, (clamped * 100).toInt()
                )
            }
        }
    }

    private suspend fun <T> uploadWithProgress(  // fromFraction~1.0 구간에 매핑
        pendingMessageId: Long,
        sentBytes: MutableStateFlow<Long>,
        totalBytes: Long,
        fromFraction: Float,
        upload: suspend () -> T
    ): T = coroutineScope {
        val progressJob = launch {
            if (totalBytes <= 0) return@launch
            sentBytes.collect { sent ->
                publishProgress(
                    pendingMessageId,
                    fromFraction + (1f - fromFraction) * (sent.toFloat() / totalBytes)
                )
                // 긴 대화방 버벅임 방지 간격
                delay(300)
            }
        }
        try {
            upload()
        } finally {
            progressJob.cancel()
        }
    }

    private suspend fun uploadImages(
        roomId: Int,
        mediaUris: List<String>,
        pendingMessageId: Long
    ): kotlin.Result<MediaUploadData> {
        return try {
            val totalCount = mediaUris.size
            val parts = mutableListOf<MultipartBody.Part>()
            val sentBytes = MutableStateFlow(0L)
            var totalUploadBytes = 0L

            mediaUris.forEachIndexed { index, uriString ->
                val uri = uriString.toUri()
                val compressedBytes = MediaUtils.compressImage(context, uri)
                val part = if (compressedBytes != null) {
                    totalUploadBytes += compressedBytes.size
                    val requestBody = ProgressRequestBody(
                        compressedBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    ) { delta -> sentBytes.value += delta }
                    MultipartBody.Part.createFormData(
                        "images",
                        "image_${System.currentTimeMillis()}_$index.jpg",
                        requestBody
                    )
                } else {
                    totalUploadBytes += MediaUtils.getFileInfo(context, uri)?.fileSize ?: 0L
                    uriToMultipartPart(uri, "images") { delta -> sentBytes.value += delta }
                }

                if (part != null) {
                    parts.add(part)
                    // 준비
                    publishProgress(pendingMessageId, (index + 1).toFloat() / totalCount * 0.3f)
                }
            }

            if (parts.isEmpty()) {
                return kotlin.Result.failure(IOException("No valid image files"))
            }

            suspend fun upload() = uploadWithProgress(
                pendingMessageId = pendingMessageId,
                sentBytes = sentBytes,
                totalBytes = totalUploadBytes,
                fromFraction = 0.3f
            ) { chatRepository.uploadImages(roomId, parts) }

            var result = upload()
            // 본문 1회 요청이라 401 뒤 직접 한 번 더
            if ((result.exceptionOrNull() as? ApiException)?.code == 401) {
                sentBytes.value = 0L
                result = upload()
            }

            if (result.isSuccess) {
                publishProgress(pendingMessageId, 1f)
            }

            // 단일 이미지면 원본 크기 추출
            val (imgWidth, imgHeight) = if (mediaUris.size == 1) {
                MediaUtils.getImageDimensions(context, mediaUris.first().toUri())
            } else null to null

            result.map { uploaded ->
                MediaUploadData(
                    mediaAttachments = uploaded.urls,
                    mediaThumbnails = uploaded.thumbnails,
                    mediaWidth = imgWidth,
                    mediaHeight = imgHeight
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "이미지 업로드 실패")
            kotlin.Result.failure(e)
        }
    }

    private suspend fun uploadVideo(
        roomId: Int,
        uriString: String,
        pendingMessageId: Long
    ): kotlin.Result<MediaUploadData> {
        return try {
            val uri = uriString.toUri()
            val fileInfo = MediaUtils.getFileInfo(context, uri)
            val fileSize = fileInfo?.fileSize ?: 0L
            // 원본 이름은 placeholder에서
            val fileName = cachedMessageDao.getByClientTempId(pendingMessageId.toString())?.fileName
                ?: fileInfo?.fileName

            val duration = MediaUtils.getVideoDuration(context, uri)
            val (vidWidth, vidHeight) = MediaUtils.getVideoDimensions(context, uri)

            publishProgress(pendingMessageId, 0.05f)

            var thumbnailUrl: String? = null
            val thumbnail = MediaUtils.extractVideoThumbnail(context, uri)
            if (thumbnail != null) {
                val outputStream = ByteArrayOutputStream()
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val thumbnailBytes = outputStream.toByteArray()
                thumbnail.recycle()

                val thumbnailRequestBody = thumbnailBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val thumbnailPart = MultipartBody.Part.createFormData(
                    "images",
                    "thumbnail_${System.currentTimeMillis()}.jpg",
                    thumbnailRequestBody
                )

                // 썸네일 업로드
                publishProgress(pendingMessageId, 0.1f)

                chatRepository.uploadImages(roomId, listOf(thumbnailPart)).onSuccess { uploaded ->
                    thumbnailUrl = uploaded.urls.firstOrNull()
                }
            }

            // 비디오는 서버 허가를 받아 스토리지에 직접
            val mimeType = MediaUtils.resolveMimeType(context, uri)
            val ticket = chatRepository.prepareUpload(roomId, ChatMessageType.VIDEO, mimeType, fileSize, fileName)
                .getOrElse { return kotlin.Result.failure(it) }

            val sentBytes = MutableStateFlow(0L)
            val result = uploadWithProgress(
                pendingMessageId = pendingMessageId,
                sentBytes = sentBytes,
                totalBytes = fileSize,
                fromFraction = 0.1f
            ) {
                directUploader.upload(ticket.uploadUrl, uri, ticket.contentType) { delta -> sentBytes.value += delta }
            }

            if (result.isSuccess) {
                publishProgress(pendingMessageId, 1f)
            }

            result.map {
                MediaUploadData(
                    uploadId = ticket.uploadId,
                    thumbnailUrl = thumbnailUrl,
                    duration = duration,
                    mediaWidth = vidWidth,
                    mediaHeight = vidHeight,
                    fileSize = fileSize,
                    fileName = fileName
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "비디오 업로드 실패")
            kotlin.Result.failure(e)
        }
    }

    private suspend fun uploadFile(
        roomId: Int,
        uriString: String,
        pendingMessageId: Long
    ): kotlin.Result<MediaUploadData> {
        return try {
            val uri = uriString.toUri()
            val fileInfo = MediaUtils.getFileInfo(context, uri)
            val fileSize = fileInfo?.fileSize ?: 0L
            // 원본 이름은 placeholder에서
            val fileName = cachedMessageDao.getByClientTempId(pendingMessageId.toString())?.fileName
                ?: fileInfo?.fileName

            // 샌드박스 사본은 확장자가 bin이라 원래 파일명으로 형식 판정
            val mimeType = fileName?.substringAfterLast('.', "")?.lowercase()
                ?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
                ?: "application/octet-stream"
            val ticket = chatRepository.prepareUpload(roomId, ChatMessageType.FILE, mimeType, fileSize, fileName)
                .getOrElse { return kotlin.Result.failure(it) }

            val sentBytes = MutableStateFlow(0L)
            val result = uploadWithProgress(
                pendingMessageId = pendingMessageId,
                sentBytes = sentBytes,
                totalBytes = fileSize,
                fromFraction = 0f
            ) {
                directUploader.upload(ticket.uploadUrl, uri, ticket.contentType) { delta -> sentBytes.value += delta }
            }

            if (result.isSuccess) {
                publishProgress(pendingMessageId, 1f)
            }

            result.map {
                MediaUploadData(
                    uploadId = ticket.uploadId,
                    fileSize = fileSize,
                    fileName = fileName
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "파일 업로드 실패")
            kotlin.Result.failure(e)
        }
    }

    private fun uriToMultipartPart(
        uri: Uri,
        fieldName: String,
        onBytesWritten: ((delta: Long) -> Unit)? = null
    ): MultipartBody.Part? {
        return try {
            val mimeType = MediaUtils.resolveMimeType(context, uri)
            val requestBody = UriRequestBody(context.contentResolver, uri, mimeType.toMediaTypeOrNull())
            val body = onBytesWritten?.let { ProgressRequestBody(requestBody, it) } ?: requestBody

            val extension = mimeType.substringAfter("/")
            MultipartBody.Part.createFormData(
                fieldName,
                "${fieldName}_${System.currentTimeMillis()}.$extension",
                body
            )
        } catch (e: Exception) {
            Timber.e(e, "URI를 MultipartBody.Part로 변환 실패: $uri")
            null
        }
    }

    // 토스트 문구, 통신·서버 오류는 ApiException으로 옴
    private fun Throwable.toFailureMessage(): String = when (this) {
        is ApiException -> message
        is IOException -> LOCAL_FILE_MESSAGE
        else -> "알 수 없는 오류"
    }

    private suspend fun handleFailure(pendingMessageId: Long, roomId: Int, errorMessage: String) {
        pendingChatMessageDao.updateStatus(pendingMessageId, UploadStatus.FAILED)
        chatRepository.markMediaMessageFailed(pendingMessageId.toString())
        uploadResultManager.emitResult(
            ChatUploadEvent.Failure(
                pendingMessageId = pendingMessageId,
                roomId = roomId,
                errorMessage = errorMessage
            )
        )
    }

    companion object {
        const val KEY_PENDING_MESSAGE_ID = "pending_message_id"
        private const val LOCAL_FILE_MESSAGE = "첨부 파일을 읽지 못했습니다"

        fun createWorkRequest(pendingMessageId: Long) =
            OneTimeWorkRequestBuilder<ChatMediaUploadWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putLong(KEY_PENDING_MESSAGE_ID, pendingMessageId)
                        .build()
                )
                .addTag("chat_upload_$pendingMessageId")
                .build()

        fun enqueue(context: Context, pendingMessageId: Long): String {
            val workRequest = createWorkRequest(pendingMessageId)
            // 같은 pending에 워커 둘 방지
            WorkManager.getInstance(context).enqueueUniqueWork(
                "chat_upload_$pendingMessageId",
                ExistingWorkPolicy.KEEP,
                workRequest
            )
            return workRequest.id.toString()
        }
    }
}
