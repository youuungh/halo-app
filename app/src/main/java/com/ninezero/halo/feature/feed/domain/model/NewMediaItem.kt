package com.ninezero.halo.feature.feed.domain.model

import android.net.Uri

/** 새 미디어 순서 항목 */
sealed interface NewMediaItem {  // 사진은 기기 주소, 영상은 직접 업로드 ID
    data class Image(val uri: Uri) : NewMediaItem
    data class DirectUpload(val uploadId: Int) : NewMediaItem
}
