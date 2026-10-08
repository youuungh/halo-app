package com.ninezero.halo.feature.profile.data.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.model.PaginatedResult
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.profile.data.remote.api.ProfileApi
import com.ninezero.halo.feature.profile.data.remote.dto.CommunityPostRequest
import com.ninezero.halo.feature.profile.data.remote.dto.toCommentThreadData
import com.ninezero.halo.feature.profile.data.remote.dto.toCreatorStoreSection
import com.ninezero.halo.feature.profile.data.remote.dto.toDomain
import com.ninezero.halo.feature.profile.data.remote.dto.toDto
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.CreatorProfileSection
import com.ninezero.halo.feature.profile.domain.model.CreatorStoreSection
import com.ninezero.halo.feature.profile.domain.model.UpdateProfileRequest
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi
) : ProfileRepository {

    // SSOT: 내 프로필 캐시
    private val myProfileFlow = MutableStateFlow<UserProfile?>(null)

    override suspend fun getUserProfile(userId: Int): Result<UserProfile> {
        return safeApiCall {
            api.getUserProfile(userId)
        }.map { it.toDomain() }
    }

    override suspend fun getMyProfile(): Result<UserProfile> {
        return safeApiCall {
            api.getMyProfile()
        }.map { it.toDomain() }.also { result ->
            result.getOrNull()?.let { myProfileFlow.value = it }
        }
    }

    override fun observeMyProfile(): Flow<UserProfile?> = myProfileFlow.asStateFlow()

    override fun getCachedMyProfile(): UserProfile? = myProfileFlow.value

    override fun clearMyProfileCache() {
        myProfileFlow.value = null
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile> {
        return safeApiCall {
            api.updateProfile(request.toDto())
        }.map { it.toDomain() }.also { result ->
            result.getOrNull()?.let { myProfileFlow.value = it }
        }
    }

    override suspend fun uploadAvatar(avatarPart: MultipartBody.Part): Result<UserProfile> {
        return safeApiCall {
            api.uploadAvatar(avatarPart)
        }.map { it.toDomain() }.also { result ->
            result.getOrNull()?.let { myProfileFlow.value = it }
        }
    }

    override suspend fun getUserCommentThreads(
        userId: Int,
        cursor: String?,
        limit: Int,
        sort: String?,
        contextType: String?
    ): Result<CursorPage<CommentThreadData>> {
        return safeApiCall {
            api.getUserCommentThreads(userId, cursor, limit, sort, contextType)
        }.map { response ->
            CursorPage(response.items.map { it.toCommentThreadData() }, response.nextCursor)
        }
    }

    override suspend fun getCreatorHomeSections(
        creatorId: Int,
        page: Int,
        limit: Int
    ): Result<CreatorProfileSection> {
        return safeApiCall {
            api.getCreatorHomeSections(creatorId, page, limit)
        }.map { it.toDomain() }
    }

    override suspend fun getCreatorStoreSections(
        creatorId: Int,
        page: Int,
        limit: Int,
        sort: String?
    ): Result<CreatorStoreSection> {
        return safeApiCall {
            api.getCreatorStoreSections(creatorId, page, limit, sort)
        }.map { it.toCreatorStoreSection() }
    }

    override suspend fun getPinnedPosts(creatorId: Int): Result<PaginatedResult<Post>> {
        return safeApiCall {
            api.getPinnedPosts(creatorId)
        }.map { response ->
            PaginatedResult(response.items.map { it.toDomain() }, response.hasNext)
        }
    }

    override suspend fun getPostsByTag(
        tagId: Int,
        page: Int,
        limit: Int
    ): Result<PaginatedResult<Post>> {
        return safeApiCall {
            api.getPostsByTag(tagId, page, limit)
        }.map { response ->
            PaginatedResult(response.items.map { it.toDomain() }, response.hasNext)
        }
    }

    override suspend fun getCommunityPosts(
        creatorId: Int,
        cursor: String?,
        limit: Int,
        sort: String
    ): Result<CursorPage<Post>> {
        return safeApiCall {
            api.getCommunityPosts(creatorId, cursor, limit, sort)
        }.map { response ->
            CursorPage(
                (response.pinnedPosts + response.posts.items).map { it.toDomain() },
                response.posts.nextCursor
            )
        }
    }

    override suspend fun createCommunityPost(
        creatorId: Int,
        content: String,
        isSecret: Boolean,
        mediaDraftIds: List<Int>,
        clientRequestId: String?
    ): Result<Post> {
        return safeApiCall {
            api.createCommunityPost(
                creatorId,
                CommunityPostRequest(
                    content = content,
                    isSecret = isSecret,
                    mediaDraftIds = mediaDraftIds,
                    clientRequestId = clientRequestId
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun deleteCommunityPost(postId: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deleteCommunityPost(postId)
        }
    }

    override suspend fun togglePinCommunityPost(postId: Int): Result<Boolean> {
        return safeApiCall {
            api.togglePinCommunityPost(postId)
        }.map { response ->
            response["isPinned"] ?: false
        }
    }

    override suspend fun searchUsers(query: String, page: Int, limit: Int): Result<List<UserSummary>> {
        return safeApiCall {
            api.searchUsers(query, page, limit)
        }.map { response ->
            response.items.map { it.toDomain() }
        }
    }
}
