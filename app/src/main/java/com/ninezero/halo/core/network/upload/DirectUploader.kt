package com.ninezero.halo.core.network.upload

import android.content.Context
import android.net.Uri
import com.ninezero.halo.core.di.DirectUpload
import com.ninezero.halo.core.model.UploadTicket
import com.ninezero.halo.core.network.ApiException
import com.ninezero.halo.core.network.exceptionFailure
import com.ninezero.halo.core.util.MediaUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** 서버가 발급한 서명 URL로 스토리지에 업로드 */
@Singleton
class DirectUploader @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:DirectUpload private val okHttpClient: OkHttpClient
) {

    suspend fun upload(
        uploadUrl: String,
        uri: Uri,
        contentType: String,
        onBytesWritten: ((delta: Long) -> Unit)? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = UriRequestBody(context.contentResolver, uri, contentType.toMediaTypeOrNull())
            val request = Request.Builder()
                .url(uploadUrl)
                .put(onBytesWritten?.let { ProgressRequestBody(body, it) } ?: body)
                .build()

            val call = okHttpClient.newCall(request)
            val code = suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation { call.cancel() }  // 작업이 취소되면 전송도 끊음
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) = continuation.resumeWithException(e)
                    override fun onResponse(call: Call, response: Response) = response.use { continuation.resume(it.code) }
                })
            }
            if (code in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(ApiException(code, "파일 업로드에 실패했습니다"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "직접 업로드 실패: $uri")
            Result.failure(exceptionFailure(e))  // API 호출과 같은 실패 문구
        }
    }

    /** 형식·크기로 허가를 받아 올리고 업로드 ID 반환 */
    suspend fun uploadVideo(
        uri: Uri,
        prepare: suspend (mimeType: String, size: Long) -> Result<UploadTicket>
    ): Result<Int> {
        val (mimeType, size) = withContext(Dispatchers.IO) {
            MediaUtils.resolveMimeType(context, uri) to (MediaUtils.getFileInfo(context, uri)?.fileSize ?: 0L)
        }
        val ticket = prepare(mimeType, size).getOrElse { return Result.failure(it) }
        return upload(ticket.uploadUrl, uri, ticket.contentType).map { ticket.uploadId }
    }
}
