package com.ninezero.halo.feature.profile.data.repository

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.profile.data.remote.api.FollowApi
import com.ninezero.halo.feature.profile.data.remote.dto.FollowRequestDto
import com.ninezero.halo.feature.profile.data.remote.dto.toDomain
import com.ninezero.halo.feature.profile.domain.model.FollowListResult
import com.ninezero.halo.feature.profile.domain.model.FollowedCreatorsResult
import com.ninezero.halo.feature.profile.domain.repository.FollowRepository
import javax.inject.Inject

class FollowRepositoryImpl @Inject constructor(
    private val api: FollowApi
) : FollowRepository {

    override suspend fun followUser(targetUserId: Int): Result<Boolean> {
        return safeApiCall {
            api.followUser(FollowRequestDto(targetUserId))
        }.map { response ->
            response.userId != null
        }
    }

    override suspend fun unfollowUser(userId: Int): Result<Unit> {
        return safeApiCall {
            api.unfollowUser(userId)
        }.map {}
    }

    override suspend fun getFollowersWithSearch(
        userId: Int,
        cursor: String?,
        limit: Int,
        search: String?
    ): Result<FollowListResult> {
        return safeApiCall {
            api.getFollowers(userId, cursor, limit, search)
        }.map { response ->
            FollowListResult(
                items = response.items.map { it.toDomain() },
                totalCount = response.totalCount,
                nextCursor = response.nextCursor
            )
        }
    }

    override suspend fun getFollowingWithSearch(
        userId: Int,
        cursor: String?,
        limit: Int,
        search: String?
    ): Result<FollowListResult> {
        return safeApiCall {
            api.getFollowing(userId, cursor, limit, search)
        }.map { response ->
            FollowListResult(
                items = response.items.map { it.toDomain() },
                totalCount = response.totalCount,
                nextCursor = response.nextCursor
            )
        }
    }

    override suspend fun getMyFollowersWithSearch(
        cursor: String?,
        limit: Int,
        search: String?
    ): Result<FollowListResult> {
        return safeApiCall {
            api.getMyFollowers(cursor, limit, search)
        }.map { response ->
            FollowListResult(
                items = response.items.map { it.toDomain() },
                totalCount = response.totalCount,
                nextCursor = response.nextCursor
            )
        }
    }

    override suspend fun getMyFollowingWithSearch(
        cursor: String?,
        limit: Int,
        search: String?
    ): Result<FollowListResult> {
        return safeApiCall {
            api.getMyFollowing(cursor, limit, search)
        }.map { response ->
            FollowListResult(
                items = response.items.map { it.toDomain() },
                totalCount = response.totalCount,
                nextCursor = response.nextCursor
            )
        }
    }

    override suspend fun getFollowRecommendations(limit: Int): Result<List<UserSummary>> {
        return safeApiCall {
            api.getFollowRecommendations(limit)
        }.map { users ->
            users.map { it.toDomain() }
        }
    }

    override suspend fun setCreatorNotifications(creatorId: Int, enabled: Boolean): Result<Unit> {
        return safeApiCall { api.toggleNewPostNotification(creatorId, enabled) }
            .mapCatching {
                safeApiCall { api.toggleNewProductNotification(creatorId, enabled) }.getOrThrow()
            }
            .map { }
    }

    override suspend fun getFollowedCreators(page: Int, limit: Int): Result<FollowedCreatorsResult> {
        return safeApiCall {
            api.getFollowedCreators(page, limit)
        }.map { response ->
            FollowedCreatorsResult(
                items = response.items.map { it.toDomain() },
                hasNext = response.hasNext
            )
        }
    }
}
