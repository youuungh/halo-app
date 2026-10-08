package com.ninezero.halo.feature.feed.presentation.preview

import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewComment
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.feature.feed.domain.model.BookmarkItem
import com.ninezero.halo.feature.feed.domain.model.BookmarkTargetType
import com.ninezero.halo.feature.feed.domain.model.LikeTargetType
import com.ninezero.halo.feature.feed.domain.model.LikedItem

private val author1 = previewAuthor(1, "test_user", "테스트 유저")
private val author2 = previewAuthor(2, "design_lover", "디자인러버")
private val author3 = previewAuthor(3, "creator_test", "크리에이터 테스트")

internal val previewSavedItems = listOf(
    BookmarkItem(
        targetType = BookmarkTargetType.POST,
        targetId = 1,
        post = previewPost(
            id = 1,
            content = "오늘의 일상 기록. 카페에서 커피 한잔하며 여유로운 오후를 보내고 있어요.",
            author = author1,
            likeCount = 24,
            commentCount = 5,
            createdAtOffset = 3600000
        )
    ),
    BookmarkItem(
        targetType = BookmarkTargetType.COMMENT,
        targetId = 10,
        comment = previewComment(
            id = 10,
            postId = 2,
            author = author2,
            content = "정말 좋은 글이네요! 저도 비슷한 경험이 있어서 공감이 됩니다.",
            likeCount = 8,
            isBookmarked = true,
            createdAtOffset = 7200000
        )
    ),
    BookmarkItem(
        targetType = BookmarkTargetType.POST,
        targetId = 3,
        post = previewPost(
            id = 3,
            content = "새로운 프로젝트를 시작했습니다. 많은 응원 부탁드려요!",
            author = author3,
            likeCount = 56,
            commentCount = 12,
            createdAtOffset = 86400000
        )
    ),
    BookmarkItem(
        targetType = BookmarkTargetType.COMMENT,
        targetId = 20,
        comment = previewComment(
            id = 20,
            postId = 4,
            author = author1,
            content = "이 부분은 조금 다르게 생각해볼 수 있을 것 같아요.",
            replyCount = 3,
            likeCount = 2,
            isLiked = true,
            isBookmarked = true,
            createdAtOffset = 172800000
        )
    ),
    BookmarkItem(
        targetType = BookmarkTargetType.POST,
        targetId = 5,
        post = previewPost(
            id = 5,
            content = "주말 나들이 사진 모음. 날씨가 너무 좋아서 산책하기 딱이었어요.",
            author = author2,
            likeCount = 89,
            commentCount = 23,
            createdAtOffset = 259200000
        )
    )
)

internal val previewLikedItems = listOf(
    LikedItem(
        targetType = LikeTargetType.POST,
        targetId = 1,
        post = previewPost(
            id = 1,
            content = "봄이 오니까 기분이 좋아지네요. 날씨가 따뜻해지면 더 많이 산책해야겠어요.",
            author = author1,
            likeCount = 42,
            commentCount = 7,
            createdAtOffset = 1800000
        )
    ),
    LikedItem(
        targetType = LikeTargetType.COMMENT,
        targetId = 15,
        comment = previewComment(
            id = 15,
            postId = 2,
            author = author2,
            content = "완전 공감이에요! 저도 같은 생각이었어요.",
            replyCount = 1,
            likeCount = 12,
            isLiked = true,
            createdAtOffset = 5400000
        )
    ),
    LikedItem(
        targetType = LikeTargetType.POST,
        targetId = 3,
        post = previewPost(
            id = 3,
            content = "새로운 작업물 공유합니다. 피드백 환영해요!",
            author = author3,
            likeCount = 128,
            commentCount = 34,
            createdAtOffset = 43200000
        )
    ),
    LikedItem(
        targetType = LikeTargetType.COMMENT,
        targetId = 25,
        comment = previewComment(
            id = 25,
            postId = 4,
            author = author1,
            content = "이 기능 진짜 유용하네요. 감사합니다!",
            likeCount = 5,
            isLiked = true,
            createdAtOffset = 86400000
        )
    ),
    LikedItem(
        targetType = LikeTargetType.POST,
        targetId = 5,
        post = previewPost(
            id = 5,
            content = "맛집 탐방 시리즈 첫 번째! 오늘은 이태원 파스타 맛집을 다녀왔어요.",
            author = author2,
            likeCount = 67,
            commentCount = 15,
            createdAtOffset = 172800000
        )
    )
)
