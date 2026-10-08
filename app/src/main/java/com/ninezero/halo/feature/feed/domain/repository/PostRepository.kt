package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.feed.domain.model.NewMediaItem
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostType
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface PostRepository {
    //  SSOT

    /** 포스트 관찰 */
    fun observePost(postId: Int): Flow<Post?>

    /** 포스트 캐시 관찰 */
    fun observeCachedPosts(): Flow<List<Post>>

    /** 캐시 포스트 갱신 */
    fun updateCachedPost(post: Post)

    /** 캐시 포스트 제거 */
    fun removeCachedPost(postId: Int)

    /** 캐시 작성자 팔로우 상태 갱신 */
    fun updateCachedFollowState(authorId: Int, isFollowing: Boolean)

    /** 작성자 포스트 캐시 제거 */
    fun removeCachedPostsByAuthor(authorId: Int)

    /** 포스트 캐시 전체 삭제 */
    fun clearCachedPosts()

    // API 호출

    /** 포스트 조회 */
    suspend fun getPostById(postId: Int): Result<Post>

    /** 포스트 작성 */
    suspend fun createPost(
        content: String,
        postType: PostType = PostType.TEXT,
        productId: Int? = null,
        requiredTier: String = "FREE",
        tagIds: List<Int>? = null,
        sectionTagId: Int? = null,
        mediaDraftIds: List<Int> = emptyList(),  // 표시 순서대로
        clientRequestId: String? = null  // 같은 요청이 반복되면 서버가 만든 글을 그대로 응답
    ): Result<Post>

    /** 포스트 수정 */
    suspend fun updatePost(
        postId: Int,
        content: String,
        tagIds: List<Int>? = null,
        sectionTagId: Int? = null
    ): Result<Post>

    /** 미디어 포함 포스트 수정 */
    suspend fun updatePostWithMedia(
        postId: Int,
        content: String?,
        keepMediaIds: List<Int>?,
        newFiles: List<MultipartBody.Part>,
        reorder: List<Int>?,
        items: List<NewMediaItem>? = null  // 새 미디어 순서, 없으면 파일 파트 순서
    ): Result<Post>

    /** 포스트 삭제 */
    suspend fun deletePost(postId: Int): Result<Unit>

    /** 포스트 영상 직접 업로드 */
    suspend fun uploadPostVideo(postId: Int, uri: String): Result<Int>

    /** 포스트 피드 고정/해제 */
    suspend fun togglePinPost(postId: Int): Result<Boolean>

    /** 내 포스트 목록 조회 */
    suspend fun getMyPosts(
        cursor: String?,
        limit: Int,
        sort: String? = null,
        contextType: String? = null
    ): Result<CursorPage<Post>>

    /** 사용자 포스트 목록 조회 */
    suspend fun getUserPosts(
        userId: Int,
        cursor: String?,
        limit: Int,
        sort: String? = null,
        contextType: String? = null
    ): Result<CursorPage<Post>>

    // 캐시

    /** 캐시 포스트 조회 */
    fun getCachedPost(postId: Int): Post?

    /** 포스트 캐시 저장 */
    fun cachePost(post: Post)

    /** 포스트 일괄 캐시 저장 */
    fun cachePosts(posts: List<Post>)
}
