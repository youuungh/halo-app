package com.ninezero.halo.feature.chat.domain.repository

import com.ninezero.halo.feature.chat.domain.model.ChatDownloadEvent
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.ChatUploadEvent
import com.ninezero.halo.feature.chat.domain.model.MediaUploadHandle
import kotlinx.coroutines.flow.SharedFlow

interface ChatMediaRepository {
    val uploadEvents: SharedFlow<ChatUploadEvent>
    val downloadEvents: SharedFlow<ChatDownloadEvent>
    /** 미디어 업로드 예약 */
    suspend fun enqueueUpload(roomId: Int, content: String?, messageType: ChatMessageType, mediaUris: List<String>): MediaUploadHandle

    /** 실패한 업로드 재시도 */
    suspend fun retryUpload(pendingMessageId: Long): Boolean

    /** 실패한 업로드 파기 */
    suspend fun discardUpload(pendingMessageId: Long)

    /** 첨부파일 다운로드 예약 */
    fun enqueueFileDownload(fileUrl: String, fileName: String)
}
