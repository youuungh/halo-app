package com.ninezero.halo.feature.feed.domain.model

/** 새 미디어 순서 항목 */
sealed interface NewMediaItem {  // 사진은 파일 파트 번호, 영상은 직접 업로드 ID
    data class FilePart(val index: Int) : NewMediaItem
    data class DirectUpload(val uploadId: Int) : NewMediaItem
}
