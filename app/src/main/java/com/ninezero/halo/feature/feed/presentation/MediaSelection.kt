package com.ninezero.halo.feature.feed.presentation

import android.net.Uri
import com.ninezero.halo.core.media.MediaFileReader
import com.ninezero.halo.core.media.MediaUtils

/** 고른 첨부의 검사 결과, 걸러진 이유는 순서대로 */
internal class MediaSelection(val validUris: List<Uri>, val errors: List<String>)

/** 크기 · 영상 형식이 맞는 첨부만 남김 */
internal fun checkMediaSelection(mediaFileReader: MediaFileReader, uris: List<Uri>, maxSizeBytes: Long): MediaSelection {
    val errors = mutableListOf<String>()
    val validUris = uris.filter { uri ->
        val fileInfo = mediaFileReader.getFileInfo(uri) ?: return@filter false

        if (fileInfo.fileSize > maxSizeBytes) {
            errors += "파일은 최대 10MB까지 업로드 가능합니다"
            return@filter false
        }
        // 영상은 서버가 받는 형식만
        val mimeType = mediaFileReader.resolveMimeType(uri)
        if (mimeType.startsWith("video/") && mimeType !in MediaUtils.ALLOWED_VIDEO_TYPES) {
            errors += "지원하지 않는 비디오 형식입니다"
            return@filter false
        }
        true
    }
    return MediaSelection(validUris, errors)
}
