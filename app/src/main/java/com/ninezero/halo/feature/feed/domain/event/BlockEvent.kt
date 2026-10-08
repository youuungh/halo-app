package com.ninezero.halo.feature.feed.domain.event

sealed class BlockEvent {
    abstract val userId: Int

    data class Blocked(override val userId: Int) : BlockEvent()
    data class Unblocked(override val userId: Int) : BlockEvent()
}
