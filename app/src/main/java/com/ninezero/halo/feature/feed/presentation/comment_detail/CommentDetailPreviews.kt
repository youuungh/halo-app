package com.ninezero.halo.feature.feed.presentation.comment_detail

import androidx.compose.runtime.Composable
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloScreenPreview
import com.ninezero.halo.core.ui.preview.previewAuthor
import com.ninezero.halo.core.ui.preview.previewComment
import com.ninezero.halo.core.ui.preview.previewMediaItem
import com.ninezero.halo.core.ui.preview.previewPost
import com.ninezero.halo.feature.feed.domain.model.MediaType
import com.ninezero.halo.feature.feed.domain.model.UserPreview

internal fun previewCommentDetailNoImageState(): CommentDetailState {
    return CommentDetailState(
        post = previewPost(
            id = 1,
            content = "오늘 새로운 프로젝트를 시작했습니다. Kotlin과 Jetpack Compose로 만드는 소셜 앱입니다!",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 42,
            commentCount = 15,
            viewCount = 1240,
            isLiked = true,
            createdAtOffset = 3_600_000
        ),
        isPostLoading = false,
        parentComment = previewComment(
            id = 1,
            postId = 1,
            author = previewAuthor(2, "user1", "사용자1"),
            content = "정말 좋은 글이네요! 많이 배워갑니다. Compose 사용하시면서 어려운 점은 없으셨나요?",
            replyCount = 3,
            likeCount = 12,
            createdAtOffset = 1_800_000
        ),
        isParentLoading = false,
        replies = listOf(
            previewComment(
                id = 2,
                postId = 1,
                author = previewAuthor(3, "user2", "사용자2"),
                content = "저도 궁금해요!",
                parentCommentId = 1,
                likeCount = 2,
                isLiked = true,
                createdAtOffset = 900_000
            ),
            previewComment(
                id = 3,
                postId = 1,
                author = previewAuthor(1, "developer", "개발자"),
                content = "처음에는 조금 어려웠는데 익숙해지니까 정말 편해요!",
                parentCommentId = 1,
                likeCount = 5,
                createdAtOffset = 600_000
            )
        ),
        isRepliesLoading = false
    )
}

internal fun previewCommentDetailBlockedPostState(): CommentDetailState {
    return CommentDetailState(
        post = previewPost(
            id = 1,
            content = "이 포스트는 차단된 사용자의 포스트입니다",
            author = previewAuthor(1, "blocked_user", "차단된 사용자"),
            likeCount = 5,
            commentCount = 1,
            viewCount = 50,
            isBlocked = true,
            createdAtOffset = 0
        ),
        isPostLoading = false,
        parentComment = previewComment(
            id = 1,
            postId = 1,
            author = previewAuthor(2, "commenter", "댓글 작성자"),
            content = "댓글 내용입니다",
            createdAtOffset = 0
        ),
        isParentLoading = false,
        replies = emptyList(),
        isRepliesLoading = false
    )
}

internal fun previewCommentDetailWithImagesState(): CommentDetailState {
    return CommentDetailState(
        post = previewPost(
            id = 1,
            content = "제주도 성산일출봉에서 찍은 일출 사진입니다. 새벽 5시에 일어나서 찍었어요!",
            author = previewAuthor(1, "traveler", "여행자"),
            mediaAttachments = listOf(
                previewMediaItem(1, url = "https://picsum.photos/400/300"),
                previewMediaItem(2, url = "https://picsum.photos/401/300")
            ),
            likeCount = 24,
            commentCount = 5,
            viewCount = 500,
            isLiked = true,
            createdAtOffset = 86_400_000
        ),
        isPostLoading = false,
        parentComment = previewComment(
            id = 1,
            postId = 1,
            author = previewAuthor(2, "user1", "사용자1"),
            content = "정말 멋진 사진이네요! 제주도 언제 가보고 싶었는데 이 사진 보니까 더 가고 싶어졌어요",
            replyCount = 2,
            likeCount = 5,
            createdAtOffset = 3_600_000
        ),
        isParentLoading = false,
        replies = listOf(
            previewComment(
                id = 2,
                postId = 1,
                author = previewAuthor(1, "traveler", "여행자"),
                content = "감사합니다! 꼭 가보세요, 정말 좋아요!",
                parentCommentId = 1,
                likeCount = 1,
                createdAtOffset = 1_800_000
            )
        ),
        isRepliesLoading = false
    )
}

internal fun previewCommentDetailWithVideoState(): CommentDetailState {
    return CommentDetailState(
        post = previewPost(
            id = 1,
            content = "새로운 영상을 공유합니다!",
            author = previewAuthor(1, "creator", "크리에이터"),
            mediaAttachments = listOf(
                previewMediaItem(
                    id = 1,
                    type = MediaType.VIDEO,
                    url = "https://example.com/video.mp4",
                    thumbnailUrl = "https://example.com/thumbnail.jpg"
                )
            ),
            likeCount = 100,
            commentCount = 10,
            viewCount = 1000,
            createdAtOffset = 3_600_000
        ),
        isPostLoading = false,
        parentComment = previewComment(
            id = 1,
            postId = 1,
            author = previewAuthor(2, "user1", "사용자1"),
            content = "영상 잘 봤어요!",
            replyCount = 1,
            likeCount = 3,
            createdAtOffset = 1_800_000
        ),
        isParentLoading = false,
        replies = emptyList(),
        isRepliesLoading = false
    )
}

internal fun previewCommentDetailLoadingState(): CommentDetailState {
    return CommentDetailState(
        isPostLoading = true,
        isParentLoading = true,
        isRepliesLoading = true
    )
}

internal fun previewCommentDetailNoRepliesState(): CommentDetailState {
    return CommentDetailState(
        post = previewPost(
            id = 1,
            content = "테스트 포스트입니다",
            author = previewAuthor(1, "developer", "개발자"),
            likeCount = 10,
            commentCount = 1,
            viewCount = 100,
            createdAtOffset = 0
        ),
        isPostLoading = false,
        parentComment = previewComment(
            id = 1,
            postId = 1,
            author = previewAuthor(2, "commenter", "댓글 작성자"),
            content = "첫 번째 댓글입니다!",
            createdAtOffset = 0
        ),
        isParentLoading = false,
        replies = emptyList(),
        isRepliesLoading = false
    )
}

internal fun previewCommentDetailWithParentChainState(): CommentDetailState {
    return CommentDetailState(
        post = previewPost(
            id = 1,
            content = "오늘 카페에서 작업하다가 찍은 사진들! 분위기 너무 좋았어요",
            author = previewAuthor(1, "coffee_lover", "커피러버", "https://picsum.photos/100/100"),
            mediaAttachments = listOf(
                previewMediaItem(1, url = "https://picsum.photos/400/300"),
                previewMediaItem(2, url = "https://picsum.photos/401/300")
            ),
            likeCount = 234,
            commentCount = 45,
            viewCount = 1520,
            isLiked = true,
            isBookmarked = true,
            createdAtOffset = 7_200_000
        ),
        isPostLoading = false,
        parentChain = listOf(
            previewComment(
                id = 1,
                postId = 1,
                author = previewAuthor(2, "photo_master", "사진장인", "https://picsum.photos/101/101"),
                content = "우와 여기 어디에요? 인테리어 진짜 예쁘다!",
                mediaAttachments = listOf(
                    previewMediaItem(1, url = "https://picsum.photos/300/200")
                ),
                replyCount = 12,
                likeCount = 45,
                isLiked = true,
                createdAtOffset = 6_000_000
            ),
            previewComment(
                id = 2,
                postId = 1,
                author = previewAuthor(1, "coffee_lover", "커피러버", "https://picsum.photos/100/100"),
                content = "성수동에 새로 생긴 카페에요! '모먼트 커피'라고 검색하시면 나와요 👍",
                parentCommentId = 1,
                replyCount = 8,
                likeCount = 23,
                createdAtOffset = 5_400_000
            ),
            previewComment(
                id = 3,
                postId = 1,
                author = previewAuthor(3, "cafe_hunter", "카페헌터"),
                content = "오 저도 거기 가봤어요! 라떼가 진짜 맛있더라구요. 특히 바닐라 라떼 추천합니다. 가격도 괜찮고 자리도 넓어서 작업하기 좋았어요.",
                parentCommentId = 2,
                replyCount = 5,
                likeCount = 15,
                isLiked = true,
                createdAtOffset = 4_800_000
            ),
            previewComment(
                id = 4,
                postId = 1,
                author = previewAuthor(4, "vanilla_fan", "바닐라팬", "https://picsum.photos/102/102"),
                content = "바닐라 라떼 저도 좋아해요!",
                parentCommentId = 3,
                replyCount = 3,
                likeCount = 8,
                createdAtOffset = 4_200_000
            )
        ),
        parentComment = previewComment(
            id = 5,
            postId = 1,
            author = previewAuthor(5, "dessert_lover", "디저트러버", "https://picsum.photos/103/103"),
            content = "저는 여기 티라미수도 먹어봤는데 진짜 맛있었어요! 사진 첨부합니다 ㅎㅎ",
            mediaAttachments = listOf(
                previewMediaItem(1, url = "https://picsum.photos/302/200"),
                previewMediaItem(2, url = "https://picsum.photos/303/200")
            ),
            parentCommentId = 4,
            replyCount = 4,
            likeCount = 18,
            isLiked = true,
            createdAtOffset = 3_600_000
        ),
        isParentLoading = false,
        replies = listOf(
            previewComment(
                id = 6,
                postId = 1,
                author = previewAuthor(1, "coffee_lover", "커피러버", "https://picsum.photos/100/100"),
                content = "티라미수 맛있죠! 저도 좋아해요",
                parentCommentId = 5,
                replyCount = 2,
                likeCount = 12,
                firstReply = previewComment(
                    id = 7,
                    postId = 1,
                    author = previewAuthor(5, "dessert_lover", "디저트러버", "https://picsum.photos/103/103"),
                    content = "다음에 같이 가요!",
                    parentCommentId = 6,
                    replyCount = 1,
                    likeCount = 5,
                    isLiked = true,
                    createdAtOffset = 2_400_000
                ),
                replyPreviewUsers = listOf(
                    UserPreview(id = 6, avatarUrl = "https://picsum.photos/104/104")
                ),
                createdAtOffset = 3_000_000
            ),
            previewComment(
                id = 8,
                postId = 1,
                author = previewAuthor(6, "foodie_kim", "푸디킴"),
                content = "저도 다녀왔어요! 크로플도 맛있더라구요",
                mediaAttachments = listOf(
                    previewMediaItem(1, url = "https://picsum.photos/304/200")
                ),
                parentCommentId = 5,
                likeCount = 7,
                createdAtOffset = 1_800_000
            ),
            previewComment(
                id = 9,
                postId = 1,
                author = previewAuthor(7, "simple_guy", "심플가이", "https://picsum.photos/105/105"),
                content = "맛있겠다",
                parentCommentId = 5,
                likeCount = 25,
                isLiked = true,
                createdAtOffset = 1_200_000
            ),
            previewComment(
                id = 10,
                postId = 1,
                author = previewAuthor(8, "review_master", "리뷰마스터", "https://picsum.photos/106/106"),
                content = "저는 개인적으로 아메리카노가 제일 맛있었어요. 원두가 좋은 것 같더라구요. 산미가 적당하고 고소한 맛이 일품이었습니다. 다음에 또 방문할 예정이에요!",
                parentCommentId = 5,
                replyCount = 3,
                likeCount = 9,
                firstReply = previewComment(
                    id = 11,
                    postId = 1,
                    author = previewAuthor(1, "coffee_lover", "커피러버", "https://picsum.photos/100/100"),
                    content = "맞아요 원두 직접 로스팅한다고 하더라구요!",
                    parentCommentId = 10,
                    replyCount = 2,
                    likeCount = 6,
                    createdAtOffset = 300_000
                ),
                replyPreviewUsers = listOf(
                    UserPreview(id = 9, avatarUrl = "https://picsum.photos/107/107"),
                    UserPreview(id = 10, avatarUrl = null)
                ),
                createdAtOffset = 600_000
            )
        ),
        isRepliesLoading = false
    )
}

@HaloLightDarkPreview
@Composable
private fun CommentDetailScreenNoImagePreview() {
    HaloScreenPreview {
        CommentDetailScreenContent(
            state = previewCommentDetailNoImageState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentDetailScreenBlockedPostPreview() {
    HaloScreenPreview {
        CommentDetailScreenContent(
            state = previewCommentDetailBlockedPostState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentDetailScreenWithImagesPreview() {
    HaloScreenPreview {
        CommentDetailScreenContent(
            state = previewCommentDetailWithImagesState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentDetailScreenWithVideoPreview() {
    HaloScreenPreview {
        CommentDetailScreenContent(
            state = previewCommentDetailWithVideoState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentDetailScreenLoadingPreview() {
    HaloScreenPreview {
        CommentDetailScreenContent(
            state = previewCommentDetailLoadingState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentDetailScreenNoRepliesPreview() {
    HaloScreenPreview {
        CommentDetailScreenContent(
            state = previewCommentDetailNoRepliesState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentDetailScreenWithParentChainPreview() {
    HaloScreenPreview {
        CommentDetailScreenContent(
            state = previewCommentDetailWithParentChainState(),
            onNavigateBack = {},
            onIntent = {}
        )
    }
}
