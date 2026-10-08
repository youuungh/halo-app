package com.ninezero.halo.feature.feed.domain.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<FollowEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events: SharedFlow<FollowEvent> = _events.asSharedFlow()

    suspend fun emitFollowed(userId: Int) {
        _events.emit(FollowEvent.Followed(userId))
    }

    suspend fun emitUnfollowed(userId: Int) {
        _events.emit(FollowEvent.Unfollowed(userId))
    }
}
