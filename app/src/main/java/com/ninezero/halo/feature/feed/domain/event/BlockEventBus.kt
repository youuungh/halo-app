package com.ninezero.halo.feature.feed.domain.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<BlockEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events: SharedFlow<BlockEvent> = _events.asSharedFlow()

    suspend fun emitBlocked(userId: Int) {
        _events.emit(BlockEvent.Blocked(userId))
    }

    suspend fun emitUnblocked(userId: Int) {
        _events.emit(BlockEvent.Unblocked(userId))
    }
}
