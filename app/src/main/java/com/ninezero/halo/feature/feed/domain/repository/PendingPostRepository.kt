package com.ninezero.halo.feature.feed.domain.repository

import android.net.Uri
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostType
import kotlinx.coroutines.flow.Flow

interface PendingPostRepository {
    /** 업로드 중 포스트 추가 */
    suspend fun addPendingPost(
        content: String,
        postType: PostType,
        mediaUris: List<Uri>?,
        authorId: Int,
        authorUsername: String,
        authorDisplayName: String?,
        authorAvatarUrl: String?,
        tagIds: List<Int>? = null,
        sectionTagId: Int? = null,
        requiredTier: String = "FREE",
        creatorId: Int? = null,
        isSecret: Boolean = false
    ): Long

    /** 내 업로드 중 포스트 관찰 */
    fun getPendingPostsByAuthor(authorId: Int): Flow<List<Post>>  // 커뮤니티 글 제외

    /** 커뮤니티 업로드 중 포스트 관찰 */
    fun getPendingCommunityPosts(authorId: Int, creatorId: Int): Flow<List<Post>>
}
