package com.ninezero.halo.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.size.Scale
import coil3.toBitmap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

object MediaUtils {

    // 이미지 압축 설정
    private const val MAX_IMAGE_DIMENSION = 1920
    private const val IMAGE_QUALITY = 80
    // 이보다 긴 사진은 글자 보존으로 원본
    private const val MAX_COMPRESS_ASPECT_RATIO = 3

    // 파일 크기 제한
    const val MAX_CHAT_IMAGE_SIZE = 10 * 1024 * 1024L  // 10MB
    const val MAX_CHAT_VIDEO_SIZE = 50 * 1024 * 1024L  // 50MB
    const val MAX_CHAT_FILE_SIZE = 20 * 1024 * 1024L   // 20MB
    const val MAX_IMAGES_PER_MESSAGE = 10

    // 허용된 MIME 타입
    val ALLOWED_IMAGE_TYPES = setOf(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    )
    val ALLOWED_VIDEO_TYPES = setOf(
        "video/mp4", "video/quicktime", "video/x-msvideo"
    )

    fun resolveMimeType(context: Context, uri: Uri): String {  // file은 확장자로 보강
        context.contentResolver.getType(uri)?.let { return it }
        val ext = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
        if (!ext.isNullOrEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let { return it }
        }
        return "application/octet-stream"
    }

    fun fileExtension(fileName: String): String =  // 중복 접미사 무시
        fileName.substringAfterLast('.', "").takeWhile { it.isLetterOrDigit() }.uppercase()

    data class FileInfo(
        val uri: Uri,
        val fileName: String,
        val fileSize: Long,
        val mimeType: String
    )

    fun getFileInfo(context: Context, uri: Uri): FileInfo? {
        return try {
            // file:// file URI는 File에서 직접 추출
            // 한글 파일명 대비 File.extension
            if (uri.scheme == "file") {
                val file = File(uri.path ?: return null)
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(file.extension.lowercase())
                    ?: "application/octet-stream"
                return FileInfo(uri, file.name, file.length(), mimeType)
            }

            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromUri(uri)

            var fileName = ""
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: ""
                    if (sizeIndex >= 0) fileSize = cursor.getLong(sizeIndex)
                }
            }

            if (fileName.isBlank()) {
                fileName = uri.lastPathSegment ?: "unknown"
            }

            FileInfo(uri, fileName, fileSize, mimeType ?: "application/octet-stream")
        } catch (e: Exception) {
            Timber.e(e, "파일 정보 조회 실패: $uri")
            null
        }
    }

    private fun getMimeTypeFromUri(uri: Uri): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    /** 서브샘플링 디코드 */
    private suspend fun decodeScaledBitmap(  // 실패 시 null
        context: Context,
        uri: Uri,
        maxSize: Int,
        scale: Scale
    ): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(maxSize)
            .scale(scale)
            .precision(Precision.INEXACT)  // 축소만, 업스케일 없음
            .allowHardware(false)  // 소프트웨어 비트맵 필요
            // 업로드용 대형 비트맵이 UI 이미지 캐시를 밀어내지 않도록
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()

        return (context.imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
    }

    suspend fun compressImage(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {  // 최대 1920px JPEG 80%
        try {
            val bitmap = decodeScaledBitmap(context, uri, MAX_IMAGE_DIMENSION, Scale.FIT)
                ?: return@withContext null

            ByteArrayOutputStream().use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
                outputStream.toByteArray()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "이미지 압축 실패: $uri")
            null
        }
    }

    /** 업로드용 multipart 파트 */
    suspend fun createUploadPart(context: Context, uri: Uri, fieldName: String, baseName: String): MultipartBody.Part? {
        val payload = readForUpload(context, uri) ?: return null
        return MultipartBody.Part.createFormData(
            fieldName,
            "$baseName.${payload.extension}",
            payload.bytes.toRequestBody(payload.mimeType.toMediaTypeOrNull())
        )
    }

    private class UploadPayload(val bytes: ByteArray, val mimeType: String, val extension: String)

    private suspend fun readForUpload(context: Context, uri: Uri): UploadPayload? = withContext(Dispatchers.IO) {  // 카메라 사진만 압축 JPEG, 나머지는 원본
        val mimeType = resolveMimeType(context, uri)
        if (shouldCompressForUpload(context, uri, mimeType)) {
            compressImage(context, uri)?.let { return@withContext UploadPayload(it, "image/jpeg", "jpg") }
        }
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                UploadPayload(input.readBytes(), mimeType, mimeType.substringAfter("/"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "업로드 파일 읽기 실패: $uri")
            null
        }
    }

    private suspend fun shouldCompressForUpload(context: Context, uri: Uri, mimeType: String): Boolean =  // PNG·WebP·GIF와 아주 긴 사진은 원본
        when (mimeType) {
            "image/heic", "image/heif" -> true  // 서버가 받지 않는 형식이라 항상 JPEG로
            "image/jpeg" -> {
                val (width, height) = getImageDimensions(context, uri)
                width == null || height == null ||
                    maxOf(width, height) <= minOf(width, height) * MAX_COMPRESS_ASPECT_RATIO
            }
            else -> false
        }

    /** 비디오 첫 프레임 썸네일 */
    suspend fun extractVideoThumbnail(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }
        } catch (e: Exception) {
            Timber.e(e, "비디오 썸네일 추출 실패: $uri")
            null
        }
    }

    /** 비디오 재생 시간 ms */
    suspend fun getVideoDuration(context: Context, uri: Uri): Long = withContext(Dispatchers.IO) {
        try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            }
        } catch (e: Exception) {
            Timber.e(e, "비디오 재생 시간 조회 실패: $uri")
            0L
        }
    }

    /** 이미지 원본 크기 */
    suspend fun getImageDimensions(context: Context, uri: Uri): Pair<Int?, Int?> = withContext(Dispatchers.IO) {  // 실패 시 null
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            options.outWidth.takeIf { it > 0 } to options.outHeight.takeIf { it > 0 }
        } catch (e: Exception) {
            Timber.e(e, "이미지 크기 조회 실패: $uri")
            null to null
        }
    }

    /** 비디오 원본 크기 */
    suspend fun getVideoDimensions(context: Context, uri: Uri): Pair<Int?, Int?> = withContext(Dispatchers.IO) {  // 실패 시 null
        try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
                val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
                if (rotation == 90 || rotation == 270) h to w else w to h
            }
        } catch (e: Exception) {
            Timber.e(e, "비디오 크기 조회 실패: $uri")
            null to null
        }
    }

    /** 이미지 미리보기 썸네일 */
    suspend fun createImageThumbnail(context: Context, uri: Uri, maxSize: Int = 300): Bitmap? = withContext(Dispatchers.IO) {
        try {
            decodeScaledBitmap(context, uri, maxSize, Scale.FILL)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "이미지 썸네일 생성 실패: $uri")
            null
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateImage(fileInfo: FileInfo): ValidationResult {
        if (!ALLOWED_IMAGE_TYPES.contains(fileInfo.mimeType)) {
            return ValidationResult(false, "지원하지 않는 이미지 형식입니다")
        }
        if (fileInfo.fileSize > MAX_CHAT_IMAGE_SIZE) {
            return ValidationResult(false, "이미지는 최대 10MB까지 업로드 가능합니다")
        }
        return ValidationResult(true)
    }

    fun validateVideo(fileInfo: FileInfo): ValidationResult {
        if (!ALLOWED_VIDEO_TYPES.contains(fileInfo.mimeType)) {
            return ValidationResult(false, "지원하지 않는 비디오 형식입니다")
        }
        if (fileInfo.fileSize > MAX_CHAT_VIDEO_SIZE) {
            return ValidationResult(false, "비디오는 최대 50MB까지 업로드 가능합니다")
        }
        return ValidationResult(true)
    }

    fun validateFile(fileInfo: FileInfo): ValidationResult {
        if (fileInfo.fileSize > MAX_CHAT_FILE_SIZE) {
            return ValidationResult(false, "파일은 최대 20MB까지 업로드 가능합니다")
        }
        return ValidationResult(true)
    }
}
