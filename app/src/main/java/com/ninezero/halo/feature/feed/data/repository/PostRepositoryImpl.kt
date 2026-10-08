package com.ninezero.halo.feature.feed.data.repository

import androidx.core.net.toUri
import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.network.dto.toDomain
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.core.network.upload.DirectUploader
import com.ninezero.halo.feature.feed.data.cache.PostStateStore
import com.ninezero.halo.feature.feed.data.remote.api.PostApi
import com.ninezero.halo.feature.feed.data.remote.dto.PostRequest
import com.ninezero.halo.feature.feed.data.remote.dto.UpdatePostRequest
import com.ninezero.halo.feature.feed.data.remote.dto.VideoUploadPrepareRequest
import com.ninezero.halo.feature.feed.data.remote.dto.toDomain
import com.ninezero.halo.feature.feed.data.remote.dto.toItemsPart
import com.ninezero.halo.feature.feed.domain.model.NewMediaItem
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostType
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val api: PostApi,
    private val postStateStore: PostStateStore,
    private val directUploader: DirectUploader
) : PostRepository {
    // SSOT

    override fun observePost(postId: Int): Flow<Post?> {
        return postStateStore.observePost(postId)
    }

    override fun observeCachedPosts(): Flow<List<Post>> {
        return postStateStore.observePosts()
    }

    override fun updateCachedPost(post: Post) {
        postStateStore.upsert(post)
    }

    override fun removeCachedPost(postId: Int) {
        postStateStore.remove(postId)
    }

    override fun updateCachedFollowState(authorId: Int, isFollowing: Boolean) {
        postStateStore.updateAuthorFollowState(authorId, isFollowing)
    }

    override fun removeCachedPostsByAuthor(authorId: Int) {
        postStateStore.removeByAuthor(authorId)
    }

    override fun clearCachedPosts() {
        postStateStore.clear()
    }

    // API 호출

    override suspend fun getPostById(postId: Int): Result<Post> {
        return safeApiCall {
            api.getPostById(postId)
        }.map { it.toDomain() }.also { result ->
            result.getOrNull()?.let { cachePost(it) }
        }
    }

    override suspend fun createPost(
        content: String,
        postType: PostType,
        productId: Int?,
        requiredTier: String,
        tagIds: List<Int>?,
        sectionTagId: Int?,
        mediaDraftIds: List<Int>,
        clientRequestId: String?
    ): Result<Post> {
        return safeApiCall {
            api.createPost(
                PostRequest(
                    content = content,
                    postType = postType.name,
                    productId = productId,
                    requiredTier = requiredTier,
                    tagIds = tagIds,
                    sectionTagId = sectionTagId,
                    mediaDraftIds = mediaDraftIds,
                    clientRequestId = clientRequestId
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun updatePost(
        postId: Int,
        content: String,
        tagIds: List<Int>?,
        sectionTagId: Int?
    ): Result<Post> {
        return safeApiCall {
            api.updatePost(
                postId,
                UpdatePostRequest(
                    content = content,
                    tagIds = tagIds,
                    sectionTagId = sectionTagId
                )
            )
        }.map { it.toDomain() }.also { result ->
            result.getOrNull()?.let { updateCachedPost(it) }
        }
    }

    override suspend fun updatePostWithMedia(
        postId: Int,
        content: String?,
        keepMediaIds: List<Int>?,
        newFiles: List<MultipartBody.Part>,
        reorder: List<Int>?,
        items: List<NewMediaItem>?
    ): Result<Post> {
        fun String.toTextBody() = toRequestBody("text/plain".toMediaType())

        return safeApiCall {
            api.updatePostWithMedia(
                postId = postId,
                content = content?.toTextBody(),
                keepMediaIds = keepMediaIds?.joinToString(",")?.toTextBody(),
                reorder = reorder?.joinToString(",")?.toTextBody(),
                items = items?.toItemsPart(),
                mediaFiles = newFiles
            )
        }.map { it.toDomain() }.also { result ->
            result.getOrNull()?.let { updateCachedPost(it) }
        }
    }

    override suspend fun deletePost(postId: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deletePost(postId)
        }.also { result ->
            if (result.isSuccess) {
                removeCachedPost(postId)
            }
        }
    }

    override suspend fun uploadPostVideo(postId: Int, uri: String): Result<Int> {
        return directUploader.uploadVideo(uri.toUri()) { mimeType, size ->
            safeApiCall {
                api.prepareUpload(postId, VideoUploadPrepareRequest(mimeType = mimeType, size = size))
            }.map { it.toDomain() }
        }
    }

    override suspend fun togglePinPost(postId: Int): Result<Boolean> {
        return safeApiCall {
            api.togglePinPost(postId)
        }.map { response ->
            response.isPinned
        }
    }

    override suspend fun getMyPosts(
        cursor: String?,
        limit: Int,
        sort: String?,
        contextType: String?
    ): Result<CursorPage<Post>> {
        return safeApiCall {
            api.getMyPosts(cursor, limit, sort, contextType)
        }.map { response ->
            CursorPage(response.items.map { it.toDomain() }, response.nextCursor)
        }.also { result ->
            result.getOrNull()?.let { cachePosts(it.items) }
        }
    }

    override suspend fun getUserPosts(
        userId: Int,
        cursor: String?,
        limit: Int,
        sort: String?,
        contextType: String?
    ): Result<CursorPage<Post>> {
        return safeApiCall {
            api.getUserPosts(userId, cursor, limit, sort, contextType)
        }.map { response ->
            CursorPage(response.items.map { it.toDomain() }, response.nextCursor)
        }.also { result ->
            result.getOrNull()?.let { cachePosts(it.items) }
        }
    }

    // 캐시
    override fun getCachedPost(postId: Int): Post? {
        return postStateStore.getPost(postId)
    }

    override fun cachePost(post: Post) {
        updateCachedPost(post)
    }

    override fun cachePosts(posts: List<Post>) {
        postStateStore.upsertAll(posts)
    }
}
