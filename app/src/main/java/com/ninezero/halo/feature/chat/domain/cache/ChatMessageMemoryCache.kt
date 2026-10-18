package com.ninezero.halo.feature.chat.domain.cache

import com.ninezero.halo.feature.chat.domain.model.Message
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/** 첫 프레임용 메시지 인메모리 캐시 */
@Singleton
class ChatMessageMemoryCache @Inject constructor() {
    private val cache = ConcurrentHashMap<Int, List<Message>>()
    private val historyComplete = ConcurrentHashMap<Int, Boolean>()  // 첫 프레임 상단 헤더 판단용

    fun get(roomId: Int): List<Message>? = if (roomId != 0) cache[roomId] else null

    fun put(roomId: Int, messages: List<Message>) {
        if (roomId != 0 && messages.isNotEmpty()) cache[roomId] = messages
    }

    fun isHistoryComplete(roomId: Int): Boolean = historyComplete[roomId] == true

    fun putHistoryComplete(roomId: Int, complete: Boolean) {
        if (roomId != 0) historyComplete[roomId] = complete
    }
}
