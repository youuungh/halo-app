package com.ninezero.halo.feature.chat.data.worker

import com.ninezero.halo.feature.chat.domain.model.ChatUploadEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 채팅 업로드 결과 전달 */
@Singleton
class ChatUploadResultManager @Inject constructor() {

    private val _uploadResults = MutableSharedFlow<ChatUploadEvent>(extraBufferCapacity = 10)
    val uploadResults: SharedFlow<ChatUploadEvent> = _uploadResults.asSharedFlow()

    suspend fun emitResult(result: ChatUploadEvent) {
        _uploadResults.emit(result)
    }
}
