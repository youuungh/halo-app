package com.ninezero.halo.feature.profile.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.model.PaginatedResult
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.CreatorProfileSection
import com.ninezero.halo.feature.profile.domain.model.CreatorStoreSection
import com.ninezero.halo.feature.profile.domain.model.UpdateProfileRequest
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface ProfileRepository {
    /** 프로필 조회 */
    suspend fun getUserProfile(userId: Int): Result<UserProfile>

    /** 내 프로필 조회 */
    suspend fun getMyProfile(): Result<UserProfile>

    /** 내 프로필 관찰 */
    fun observeMyProfile(): Flow<UserProfile?>  // 조회·수정·아바타 업로드 성공 시 emit

    /** 캐시 내 프로필 조회 */
    fun getCachedMyProfile(): UserProfile?

    /** 내 프로필 캐시 삭제 */
    fun clearMyProfileCache()

    /** 프로필 수정 */
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile>

    /** 프로필 이미지 업로드 */
    suspend fun uploadAvatar(avatarPart: MultipartBody.Part): Result<UserProfile>

    /** 사용자 댓글 스레드 조회 */
    suspend fun getUserCommentThreads(
        userId: Int,
        cursor: String? = null,
        limit: Int = 20,
        sort: String? = null,
        contextType: String? = null
    ): Result<CursorPage<CommentThreadData>>

    /** 크리에이터 프로필 섹션 조회 */
    suspend fun getCreatorHomeSections(
        creatorId: Int,
        page: Int = 1,
        limit: Int = 20
    ): Result<CreatorProfileSection>

    /** 크리에이터 스토어 섹션 조회 */
    suspend fun getCreatorStoreSections(
        creatorId: Int,
        page: Int = 1,
        limit: Int = 20,
        sort: String? = null
    ): Result<CreatorStoreSection>

    /** 크리에이터 고정 포스트 조회 */
    suspend fun getPinnedPosts(creatorId: Int): Result<PaginatedResult<Post>>

    /** 태그별 포스트 조회 */
    suspend fun getPostsByTag(
        tagId: Int,
        page: Int = 1,
        limit: Int = 20
    ): Result<PaginatedResult<Post>>

    /** 커뮤니티 글 목록 조회 */
    suspend fun getCommunityPosts(
        creatorId: Int,
        cursor: String? = null,
        limit: Int = 20,
        sort: String = "LATEST"
    ): Result<CursorPage<Post>>  // 고정글은 첫 페이지 앞에 붙고 다음 커서는 일반 글 기준

    /** 커뮤니티 글 작성 */
    suspend fun createCommunityPost(
        creatorId: Int,
        content: String,
        isSecret: Boolean = false,
        mediaDraftIds: List<Int> = emptyList(),  // 표시 순서대로
        clientRequestId: String? = null  // 같은 요청이 반복되면 서버가 만든 글을 그대로 응답
    ): Result<Post>

    /** 커뮤니티 글 삭제 */
    suspend fun deleteCommunityPost(postId: Int): Result<Unit>

    /** 커뮤니티 공지 고정/해제 */
    suspend fun togglePinCommunityPost(postId: Int): Result<Boolean>

    /** 사용자 검색 */
    suspend fun searchUsers(query: String, page: Int = 1, limit: Int = 20): Result<List<UserSummary>>
}
