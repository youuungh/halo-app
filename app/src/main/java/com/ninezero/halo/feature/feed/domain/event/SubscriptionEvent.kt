package com.ninezero.halo.feature.feed.domain.event

sealed class SubscriptionEvent {
    abstract val creatorId: Int

    data class Subscribed(override val creatorId: Int) : SubscriptionEvent()
}
