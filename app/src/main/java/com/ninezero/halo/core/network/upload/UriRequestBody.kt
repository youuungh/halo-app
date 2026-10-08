package com.ninezero.halo.core.network.upload

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException

/** URI 내용을 메모리에 모으지 않고 흘려보내는 RequestBody */
class UriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val mediaType: MediaType?
) : RequestBody() {
    private val length = contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L

    override fun contentType() = mediaType
    override fun contentLength() = length
    // IOException으로만 던짐, 그 외는 앱이 죽음
    override fun writeTo(sink: BufferedSink) {
        val input = try {
            contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            throw LocalFileReadException("Cannot open input stream for URI: $uri", e)
        } ?: throw LocalFileReadException("Cannot open input stream for URI: $uri")

        input.use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                sink.write(buffer, 0, bytesRead)
            }
        }
    }
}

/** 기기 파일 읽기 실패, 통신 오류와 구분 */
class LocalFileReadException(message: String, cause: Throwable? = null) : IOException(message, cause)
