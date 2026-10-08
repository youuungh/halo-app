package com.ninezero.halo.feature.share.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import com.ninezero.halo.feature.share.data.remote.api.ShareApi
import com.ninezero.halo.feature.share.data.remote.dto.SharePostRequest
import com.ninezero.halo.feature.share.data.remote.dto.ShareProductRequest
import com.ninezero.halo.feature.share.data.remote.dto.toDomain
import com.ninezero.halo.feature.share.domain.model.ShareResult
import com.ninezero.halo.feature.share.domain.repository.ShareRepository
import javax.inject.Inject

class ShareRepositoryImpl @Inject constructor(
    private val api: ShareApi,
    private val chatRepository: ChatRepository
) : ShareRepository {

    override suspend fun sharePost(
        postId: Int,
        recipientUserIds: List<Int>,
        message: String?,
        clientTempId: String?,
        messageTempId: String?
    ): Result<ShareResult> {
        return safeApiCall {
            api.sharePost(SharePostRequest(postId, recipientUserIds, message, clientTempId, messageTempId))
        }.map { it.toDomain() }.also { result ->
            // 방 목록 직접 동기화
            if (result.isSuccess) chatRepository.syncChatRooms()
        }
    }

    override suspend fun shareProduct(
        productId: Int,
        recipientUserIds: List<Int>,
        message: String?,
        clientTempId: String?,
        messageTempId: String?
    ): Result<ShareResult> {
        return safeApiCall {
            api.shareProduct(ShareProductRequest(productId, recipientUserIds, message, clientTempId, messageTempId))
        }.map { it.toDomain() }.also { result ->
            if (result.isSuccess) chatRepository.syncChatRooms()
        }
    }
}
