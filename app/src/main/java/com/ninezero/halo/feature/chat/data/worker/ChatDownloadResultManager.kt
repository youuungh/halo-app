package com.ninezero.halo.feature.chat.data.worker

import com.ninezero.halo.feature.chat.domain.model.ChatDownloadEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 채팅 다운로드 결과 전달 */
@Singleton
class ChatDownloadResultManager @Inject constructor() {

    private val _downloadResults = MutableSharedFlow<ChatDownloadEvent>(extraBufferCapacity = 10)
    val downloadResults: SharedFlow<ChatDownloadEvent> = _downloadResults.asSharedFlow()

    suspend fun emitResult(result: ChatDownloadEvent) {
        _downloadResults.emit(result)
    }
}
