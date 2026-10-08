package com.ninezero.halo.feature.feed.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.feed.data.remote.api.HiddenPostApi
import com.ninezero.halo.feature.feed.domain.repository.HiddenPostRepository
import javax.inject.Inject

class HiddenPostRepositoryImpl @Inject constructor(
    private val api: HiddenPostApi
) : HiddenPostRepository {

    override suspend fun toggleHidden(postId: Int): Result<Boolean> {
        return safeApiCall {
            api.toggleHiddenPost(postId)
        }.map { response ->
            response.isHidden
        }
    }
}
