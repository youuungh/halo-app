package com.ninezero.halo.feature.feed.domain.event

sealed class FollowEvent {
    abstract val userId: Int

    data class Followed(override val userId: Int) : FollowEvent()
    data class Unfollowed(override val userId: Int) : FollowEvent()
}
