package com.ninezero.halo.feature.feed.domain.repository

interface HiddenPostRepository {
    /** 포스트 숨김 */
    suspend fun toggleHidden(postId: Int): Result<Boolean>
}
