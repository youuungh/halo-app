package com.ninezero.halo.feature.profile.domain.repository

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.profile.domain.model.FollowListResult
import com.ninezero.halo.feature.profile.domain.model.FollowedCreatorsResult

interface FollowRepository {
    /** 팔로우 */
    suspend fun followUser(targetUserId: Int): Result<Boolean>

    /** 언팔로우 */
    suspend fun unfollowUser(userId: Int): Result<Unit>

    /** 사용자 팔로워 검색 조회 */
    suspend fun getFollowersWithSearch(
        userId: Int,
        cursor: String?,
        limit: Int,
        search: String?
    ): Result<FollowListResult>

    /** 사용자 팔로잉 검색 조회 */
    suspend fun getFollowingWithSearch(
        userId: Int,
        cursor: String?,
        limit: Int,
        search: String?
    ): Result<FollowListResult>

    /** 내 팔로워 검색 조회 */
    suspend fun getMyFollowersWithSearch(
        cursor: String?,
        limit: Int,
        search: String?
    ): Result<FollowListResult>

    /** 내 팔로잉 검색 조회 */
    suspend fun getMyFollowingWithSearch(
        cursor: String?,
        limit: Int,
        search: String?
    ): Result<FollowListResult>

    /** 팔로우 추천 조회 */
    suspend fun getFollowRecommendations(limit: Int): Result<List<UserSummary>>

    /** 크리에이터 알림 설정 */
    suspend fun setCreatorNotifications(creatorId: Int, enabled: Boolean): Result<Unit>

    /** 팔로우 크리에이터 목록 조회 */
    suspend fun getFollowedCreators(page: Int, limit: Int): Result<FollowedCreatorsResult>
}
