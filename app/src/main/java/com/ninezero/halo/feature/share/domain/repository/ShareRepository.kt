package com.ninezero.halo.feature.share.domain.repository

import com.ninezero.halo.feature.share.domain.model.ShareResult

interface ShareRepository {
    /** 포스트 공유 */
    suspend fun sharePost(
        postId: Int,
        recipientUserIds: List<Int>,
        message: String?,
        clientTempId: String? = null,
        messageTempId: String? = null
    ): Result<ShareResult>

    /** 상품 공유 */
    suspend fun shareProduct(
        productId: Int,
        recipientUserIds: List<Int>,
        message: String?,
        clientTempId: String? = null,
        messageTempId: String? = null
    ): Result<ShareResult>
}
