package com.ninezero.halo.feature.feed.data.repository

import com.ninezero.halo.core.database.dao.CachedChatRoomDao
import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.feed.data.remote.api.BlockApi
import com.ninezero.halo.feature.feed.domain.event.BlockEventBus
import com.ninezero.halo.feature.feed.domain.model.BlockRelation
import com.ninezero.halo.feature.feed.domain.repository.BlockRepository
import javax.inject.Inject

class BlockRepositoryImpl @Inject constructor(
    private val api: BlockApi,
    private val blockEventBus: BlockEventBus,
    private val cachedChatRoomDao: CachedChatRoomDao
) : BlockRepository {

    override suspend fun toggleBlock(userId: Int): Result<Boolean> {
        return safeApiCall {
            api.toggleBlock(userId)
        }.map { response ->
            response.isBlocked
        }.onSuccess { isBlocked ->
            // 채팅방 진입 시 차단 안내를 바로 그리도록 캐시 먼저
            cachedChatRoomDao.setBlockedByMe(userId, isBlocked)
            if (isBlocked) {
                blockEventBus.emitBlocked(userId)
            } else {
                blockEventBus.emitUnblocked(userId)
            }
        }
    }

    override suspend fun getBlockRelation(userId: Int): Result<BlockRelation> {
        return safeApiCall {
            api.getBlockRelation(userId)
        }.map { response ->
            BlockRelation(
                isBlockedByMe = response.isBlockedByMe,
                isBlockedByOther = response.isBlockedByOther
            )
        }.onSuccess { relation ->
            cachedChatRoomDao.setBlockRelation(userId, relation.isBlockedByMe, relation.isBlockedByOther)
        }
    }

    override suspend fun getMyBlockedUsers(cursor: String?, limit: Int): Result<CursorPage<UserSummary>> {
        return safeApiCall {
            api.getMyBlockedUsers(cursor, limit)
        }.map { response ->
            CursorPage(response.items.map { it.toDomain() }, response.nextCursor)
        }
    }
}
