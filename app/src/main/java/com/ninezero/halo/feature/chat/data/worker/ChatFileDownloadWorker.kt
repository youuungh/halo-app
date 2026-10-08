package com.ninezero.halo.feature.chat.data.worker

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ninezero.halo.feature.chat.domain.model.ChatDownloadEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

@HiltWorker
class ChatFileDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadResultManager: ChatDownloadResultManager
) : CoroutineWorker(context, workerParams) {

    private var foregroundActive = false
    private var lastNotifyAt = 0L
    private var notificationId = 0

    override suspend fun doWork(): Result {
        val fileUrl = inputData.getString(KEY_FILE_URL)
        val fileName = inputData.getString(KEY_FILE_NAME)

        if (fileUrl.isNullOrBlank() || fileName.isNullOrBlank()) {
            Timber.e("ChatFileDownloadWorker: 유효하지 않은 파라미터")
            return Result.failure()
        }

        // FGS 승격
        notificationId = ChatTransferNotifications.downloadNotificationId(fileUrl)
        try {
            setForeground(
                ChatTransferNotifications.foregroundInfo(context, notificationId, "파일 다운로드 중", fileName)
            )
            foregroundActive = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "ChatFileDownloadWorker: setForeground 거부됨 — 백그라운드 작업으로 계속 진행")
        }

        return try {
            val saved = saveFileToDownloads(fileUrl, fileName)
            // 화면 밖에서도 결과를 알 수 있도록 시스템 알림
            if (saved != null) {
                ChatTransferNotifications.notifyDownloadComplete(
                    context, fileUrl, fileName, saved.first, saved.second
                )
            } else {
                ChatTransferNotifications.notifyDownloadFailed(context, fileUrl, fileName)
            }
            val success = saved != null

            if (success) {
                downloadResultManager.emitResult(
                    ChatDownloadEvent.Success(
                        fileUrl = fileUrl,
                        fileName = fileName
                    )
                )
                Result.success()
            } else {
                downloadResultManager.emitResult(
                    ChatDownloadEvent.Failure(
                        fileUrl = fileUrl
                    )
                )
                Result.success()
            }
        } catch (e: CancellationException) {
            // WorkManager가 재실행하므로 실패 처리하지 않음
            throw e
        } catch (e: Exception) {
            Timber.e(e, "ChatFileDownloadWorker: 다운로드 중 예외 발생")
            ChatTransferNotifications.notifyDownloadFailed(context, fileUrl, fileName)
            downloadResultManager.emitResult(
                ChatDownloadEvent.Failure(
                    fileUrl = fileUrl
                )
            )
            Result.success()
        }
    }

    private suspend fun saveFileToDownloads(fileUrl: String, fileName: String): Pair<Uri, String>? {
        return withContext(Dispatchers.IO) {
            var insertedUri: Uri? = null  // 실패하면 보이지 않는 미완성 항목이 남아 지움
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection()
                // 무한 대기 방지
                connection.connectTimeout = 30_000
                connection.readTimeout = 30_000
                // gzip+chunked면 Content-Length 없음
                // identity로 압축 꺼 Content-Length 확보
                connection.setRequestProperty("Accept-Encoding", "identity")
                connection.connect()

                val totalBytes = connection.contentLengthLong
                val mimeType = connection.contentType ?: "application/octet-stream"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Halo")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                insertedUri = uri

                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        BufferedInputStream(connection.getInputStream()).use { inputStream ->
                            copyWithProgress(inputStream, outputStream, fileUrl, fileName, totalBytes)
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    uri to mimeType
                }
            } catch (e: CancellationException) {
                insertedUri?.let { context.contentResolver.delete(it, null, null) }
                throw e
            } catch (e: Exception) {
                Timber.e(e, "다운로드 폴더에 파일 저장 실패")
                insertedUri?.let { context.contentResolver.delete(it, null, null) }
                null
            }
        }
    }

    private suspend fun copyWithProgress(
        inputStream: InputStream,
        outputStream: OutputStream,
        fileUrl: String,
        fileName: String,
        totalBytes: Long
    ) {
        val buffer = ByteArray(8192)
        var downloadedBytes = 0L
        var lastEmitTime = 0L

        while (true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) break

            outputStream.write(buffer, 0, bytesRead)
            downloadedBytes += bytesRead

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastEmitTime >= 100 && totalBytes > 0) {
                val progress = downloadedBytes.toFloat() / totalBytes
                downloadResultManager.emitResult(
                    ChatDownloadEvent.Progress(
                        fileUrl = fileUrl,
                        progress = progress
                    )
                )
                lastEmitTime = currentTime

                // FGS 알림 갱신
                if (foregroundActive && currentTime - lastNotifyAt >= 500) {
                    lastNotifyAt = currentTime
                    ChatTransferNotifications.notifyProgress(
                        context, notificationId, "파일 다운로드 중", fileName, (progress * 100).toInt()
                    )
                }
            }
        }
    }

    companion object {
        const val KEY_FILE_URL = "file_url"
        const val KEY_FILE_NAME = "file_name"

        fun createWorkRequest(fileUrl: String, fileName: String) =
            OneTimeWorkRequestBuilder<ChatFileDownloadWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putString(KEY_FILE_URL, fileUrl)
                        .putString(KEY_FILE_NAME, fileName)
                        .build()
                )
                .addTag("chat_download_${fileUrl.hashCode()}")
                .build()

        fun enqueue(context: Context, fileUrl: String, fileName: String) {
            // 진행률이 뜨기 전 연타해도 같은 파일은 한 번만 받음
            WorkManager.getInstance(context).enqueueUniqueWork(
                "chat_download_${fileUrl.hashCode()}",
                ExistingWorkPolicy.KEEP,
                createWorkRequest(fileUrl, fileName)
            )
        }
    }
}
