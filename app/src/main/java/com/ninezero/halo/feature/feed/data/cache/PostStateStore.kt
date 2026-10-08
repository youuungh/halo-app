package com.ninezero.halo.feature.feed.data.cache

import com.ninezero.halo.feature.feed.domain.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** 포스트 SSOT 캐시 */
interface PostStateStore {
    fun observePost(postId: Int): Flow<Post?>
    fun observePosts(): Flow<List<Post>>
    fun getPost(postId: Int): Post?
    fun upsert(post: Post)
    fun upsertAll(posts: List<Post>)
    fun updateAuthorFollowState(authorId: Int, isFollowing: Boolean)
    fun remove(postId: Int)
    fun removeByAuthor(authorId: Int)
    fun clear()
}

@Singleton
class PostStateStoreImpl @Inject constructor() : PostStateStore {
    // 동시 수정 유실 방지로 update만
    private val _posts = MutableStateFlow<Map<Int, Post>>(emptyMap())

    override fun observePost(postId: Int): Flow<Post?> {
        return _posts
            .map { cache -> cache[postId] }
            .distinctUntilChanged()
    }

    override fun observePosts(): Flow<List<Post>> {
        return _posts
            .map { cache -> cache.values.toList() }
            .distinctUntilChanged()
    }

    override fun getPost(postId: Int): Post? = _posts.value[postId]

    override fun upsert(post: Post) {
        _posts.update { it + (post.id to post) }
    }

    override fun upsertAll(posts: List<Post>) {
        if (posts.isEmpty()) return
        _posts.update { it + posts.associateBy { post -> post.id } }
    }

    override fun updateAuthorFollowState(authorId: Int, isFollowing: Boolean) {
        _posts.update { current ->
            current.mapValues { (_, post) ->
                if (post.author.id != authorId || post.author.isFollowing == isFollowing) {
                    post
                } else {
                    post.copy(author = post.author.copy(isFollowing = isFollowing))
                }
            }
        }
    }

    override fun remove(postId: Int) {
        _posts.update { it - postId }
    }

    override fun removeByAuthor(authorId: Int) {
        _posts.update { current -> current.filterValues { post -> post.author.id != authorId } }
    }

    override fun clear() {
        _posts.value = emptyMap()
    }
}
