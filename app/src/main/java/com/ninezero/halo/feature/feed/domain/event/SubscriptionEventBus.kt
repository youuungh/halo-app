package com.ninezero.halo.feature.feed.domain.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<SubscriptionEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events: SharedFlow<SubscriptionEvent> = _events.asSharedFlow()

    suspend fun emitSubscribed(creatorId: Int) {
        _events.emit(SubscriptionEvent.Subscribed(creatorId))
    }
}
