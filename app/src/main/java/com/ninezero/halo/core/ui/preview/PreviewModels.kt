package com.ninezero.halo.core.ui.preview

import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.feature.feed.domain.model.Author
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.UserPreview
import com.ninezero.halo.feature.profile.domain.model.UserProfile

internal fun previewAuthor(
    id: Int = 1,
    username: String = "username",
    displayName: String = "표시이름",
    avatarUrl: String? = null,
    isFollowing: Boolean = false
): Author = Author(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    isFollowing = isFollowing
)

internal fun previewPost(
    id: Int = 1,
    content: String = "프리뷰 포스트 내용",
    author: Author = previewAuthor(),
    mediaAttachments: List<MediaItem> = emptyList(),
    likeCount: Int = 0,
    commentCount: Int = 0,
    viewCount: Int = 0,
    isLiked: Boolean = false,
    isBookmarked: Boolean = false,
    isBlocked: Boolean = false,
    createdAtOffset: Long = 3_600_000,
    now: Long = System.currentTimeMillis()
): Post = Post(
    id = id,
    content = content,
    author = author,
    mediaAttachments = mediaAttachments,
    likeCount = likeCount,
    commentCount = commentCount,
    viewCount = viewCount,
    isLiked = isLiked,
    isBookmarked = isBookmarked,
    isBlocked = isBlocked,
    createdAt = now - createdAtOffset,
)

internal fun previewComment(
    id: Int = 1,
    postId: Int = 1,
    author: Author = previewAuthor(),
    content: String = "프리뷰 댓글",
    mediaAttachments: List<MediaItem> = emptyList(),
    parentCommentId: Int? = null,
    replyCount: Int = 0,
    likeCount: Int = 0,
    isLiked: Boolean = false,
    isBookmarked: Boolean = false,
    isBlocked: Boolean = false,
    firstReply: Comment? = null,
    replyPreviewUsers: List<UserPreview> = emptyList(),
    createdAtOffset: Long = 0,
    now: Long = System.currentTimeMillis()
): Comment = Comment(
    id = id,
    postId = postId,
    author = author,
    content = content,
    mediaAttachments = mediaAttachments,
    parentCommentId = parentCommentId,
    replyCount = replyCount,
    likeCount = likeCount,
    isLiked = isLiked,
    isBookmarked = isBookmarked,
    isBlocked = isBlocked,
    firstReply = firstReply,
    replyPreviewUsers = replyPreviewUsers,
    createdAt = now - createdAtOffset,
)

internal fun previewMediaItem(
    id: Int = 1,
    type: MediaType = MediaType.IMAGE,
    url: String = "https://example.com/image.jpg",
    thumbnailUrl: String? = null
): MediaItem = MediaItem(
    id = id,
    type = type,
    url = url,
    thumbnailUrl = thumbnailUrl
)

internal fun previewUserSummary(
    id: Int = 1,
    username: String = "username",
    displayName: String? = "표시이름",
    avatarUrl: String? = null,
    role: String = "USER",
    followerCount: Int = 0,
    followingCount: Int = 0,
    postCount: Int = 0,
    isFollowing: Boolean = false
): UserSummary = UserSummary(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    role = role,
    followerCount = followerCount,
    followingCount = followingCount,
    postCount = postCount,
    isFollowing = isFollowing
)

internal fun previewUserProfile(
    userId: Int = 1,
    role: String = "USER",
    displayName: String = "나",
    bio: String? = null,
    avatarUrl: String? = null,
    location: String? = null,
    website: String? = null,
    followerCount: Int = 0,
    followingCount: Int = 0,
    postCount: Int = 0,
    subscriberCount: Int? = null,
    hasSubscriptionPlans: Boolean = false,
    isCreator: Boolean = false,
    isBlocked: Boolean = false,
    username: String = "me",
    now: Long = System.currentTimeMillis()
): UserProfile = UserProfile(
    userId = userId,
    role = role,
    displayName = displayName,
    bio = bio,
    avatarUrl = avatarUrl,
    location = location,
    website = website,
    followerCount = followerCount,
    followingCount = followingCount,
    postCount = postCount,
    subscriberCount = subscriberCount,
    hasSubscriptionPlans = hasSubscriptionPlans,
    isCreator = isCreator,
    isBlocked = isBlocked,
    createdAt = now,
    updatedAt = now,
    username = username
)
