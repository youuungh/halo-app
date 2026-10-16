package com.ninezero.halo.feature.feed.presentation.post_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.delegate.PostShareDelegate
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.CommentUploadEvent
import com.ninezero.halo.feature.feed.domain.repository.CommentUploadEventRepository
import com.ninezero.halo.feature.feed.domain.repository.PendingCommentRepository
import com.ninezero.halo.feature.feed.domain.usecase.DeleteCommentUseCase
import com.ninezero.halo.feature.feed.domain.usecase.DeletePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetCachedPostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetCommentRepliesUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetPostByIdUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetPostCommentsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObserveCachedCommentsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObservePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.RemoveCachedPostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.TogglePinPostUseCase
import com.ninezero.halo.feature.feed.presentation.BlockTargetUser
import com.ninezero.halo.feature.feed.presentation.CommentSortType
import com.ninezero.halo.feature.feed.presentation.toUnavailablePostReason
import com.ninezero.halo.feature.profile.domain.usecase.GetCachedMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.ObserveMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCachedPostUseCase: GetCachedPostUseCase,
    private val removeCachedPostUseCase: RemoveCachedPostUseCase,
    private val observePostUseCase: ObservePostUseCase,
    private val observeCachedCommentsUseCase: ObserveCachedCommentsUseCase,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase,
    private val getCommentRepliesUseCase: GetCommentRepliesUseCase,
    private val postMutationDelegate: PostMutationDelegate,
    private val postShareDelegate: PostShareDelegate,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val togglePinPostUseCase: TogglePinPostUseCase,
    private val pendingCommentRepository: PendingCommentRepository,
    private val commentUploadEventRepository: CommentUploadEventRepository,
    private val observeMyProfileUseCase: ObserveMyProfileUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    getCachedMyProfileUseCase: GetCachedMyProfileUseCase
) : ViewModel(), OrbitContainerHost<PostDetailState, PostDetailState, PostDetailSideEffect> {

    private val postId: Int = savedStateHandle.get<Int>("postId") ?: 0
    private val highlightCommentId: Int? = savedStateHandle.get<Int>("highlightCommentId")
    private var highlightCommentEnsured = false
    private var isBlockingPostAuthor = false  // 차단 완료 다이얼로그를 닫을 때 나감
    private val myProfile = getCachedMyProfileUseCase()
    private val myUserId: Int? = myProfile?.userId
    private val isCurrentUserCreator: Boolean = myProfile?.isCreator == true

    override val container: OrbitContainer<PostDetailState, PostDetailState, PostDetailSideEffect> =
        orbitContainer(
            PostDetailState(
                myUserId = myUserId,
                isCurrentUserCreator = isCurrentUserCreator,
                highlightCommentId = highlightCommentId
            )
        )

    private val commentsGeneration = ListGeneration()

    init {
        observeMyProfile()
        if (postId > 0) {
            observePost()
            observeCachedComments()
            // 캐시 먼저 표시
            getCachedPostUseCase(postId)?.let { cachedPost ->
                intent {
                    reduce {
                        state.copy(
                            post = cachedPost,
                            isFollowing = cachedPost.author.isFollowing,
                            isLoading = false
                        )
                    }
                }
            }
            // API 최신 데이터 로드
            loadPost()
            observePendingComments()
            observeUploadResults()
        }
    }

    private fun observePost() {
        observePostUseCase(postId)
            .onEach { post ->
                intent {
                    // 작성자 차단 중엔 글 유지
                    if (post == null && isBlockingPostAuthor) return@intent
                    if (post == null && state.post != null && !state.isLoading) {
                        postSideEffect(PostDetailSideEffect.NavigateBack)
                    }
                    reduce {
                        state.copy(
                            post = post,
                            isFollowing = post?.author?.isFollowing ?: false,
                            isLoading = post == null && state.isLoading
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCachedComments() {
        observeCachedCommentsUseCase()
            .onEach { cachedComments ->
                intent {
                    // 반영은 reduce 안에서, 동시 갱신 유실 방지
                    reduce {
                        state.copy(
                            comments = state.comments.mapNotNull { comment ->
                                cachedComments[comment.id]
                            },
                            expandedCommentReplies = state.expandedCommentReplies.mapValues { (_, replies) ->
                                replies.mapNotNull { reply ->
                                    cachedComments[reply.id]
                                }
                            },
                            selectedComment = state.selectedComment?.let { selected ->
                                cachedComments[selected.id]
                            },
                            replyToComment = state.replyToComment?.let { replyTo ->
                                cachedComments[replyTo.id]
                            }
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeUploadResults() {
        commentUploadEventRepository.uploadEvents
            .onEach { result ->
                // 해당 post의 결과만 처리
                when (result) {
                    is CommentUploadEvent.Success -> {
                        if (result.postId == postId) {
                            val parentCommentId = result.parentCommentId
                            // 답글 화면이 열려 있으면 스낵바는 그쪽 몫
                            if (parentCommentId == null || !commentUploadEventRepository.isCommentDetailActive(parentCommentId)) {
                                intent {
                                    reduce {
                                        state.copy(snackbarState = SnackbarState("게시됨"))
                                    }
                                }
                            }

                            // 최상위 댓글은 맨 위 고정, 답글은 스레드만 재조회
                            if (parentCommentId == null) {
                                intent {
                                    reduce { state.copy(comments = (listOf(result.comment) + state.comments).distinctBy { it.id }) }
                                }
                            } else if (container.stateFlow.value.expandedCommentReplies.containsKey(parentCommentId)) {
                                expandReplies(parentCommentId)
                            }
                        }
                    }
                    is CommentUploadEvent.Failure -> {
                        if (result.postId == postId) {
                            val parentCommentId = result.parentCommentId
                            if (parentCommentId == null || !commentUploadEventRepository.isCommentDetailActive(parentCommentId)) {
                                intent {
                                    reduce {
                                        // 서버 메시지 우선
                                        state.copy(snackbarState = SnackbarState(result.errorMessage.ifBlank { "게시 실패" }))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observePendingComments() {
        pendingCommentRepository.getPendingCommentsByPostId(postId)
            .onEach { pending ->
                val topLevel = pending.filter { it.parentCommentId == null }
                val replies = pending
                    .filter { it.parentCommentId != null }
                    .groupBy { it.parentCommentId!! }  // 부모 댓글 id별 펜딩 답글
                intent {
                    reduce { state.copy(pendingComments = topLevel, pendingReplies = replies) }
                }
            }
            .launchIn(viewModelScope)
    }

    // 내 프로필이 늦게 오면 내 글 판정 다시
    private fun observeMyProfile() {
        observeMyProfileUseCase()
            .filterNotNull()
            .onEach { profile ->
                intent { reduce { state.copy(myUserId = profile.userId, isCurrentUserCreator = profile.isCreator) } }
            }
            .launchIn(viewModelScope)
        if (myProfile == null) intent { getMyProfileUseCase() }
    }

    fun onIntent(intent: PostDetailIntent) {
        when (intent) {
            is PostDetailIntent.RefreshPost -> refreshPost()

            // 포스트 상호작용
            is PostDetailIntent.LikePost -> likePost()
            is PostDetailIntent.UnlikePost -> unlikePost()
            is PostDetailIntent.BookmarkPost -> bookmarkPost()
            is PostDetailIntent.FollowAuthor -> followAuthor()
            is PostDetailIntent.UnfollowAuthor -> unfollowAuthor()

            // 포스트 옵션
            is PostDetailIntent.ShowPostOptions -> showPostOptions()
            is PostDetailIntent.DismissPostOptions -> dismissPostOptions()
            is PostDetailIntent.DeletePost -> deletePost()
            is PostDetailIntent.EditPost -> editPost()
            is PostDetailIntent.PinPost -> pinPost()
            is PostDetailIntent.NotInterestedPost -> notInterestedPost()

            // 댓글 관련
            is PostDetailIntent.LoadMoreComments -> loadMoreComments()
            is PostDetailIntent.ChangeSortType -> changeSortType(intent.sortType)
            is PostDetailIntent.SetReplyTo -> setReplyTo(intent.comment)

            // 댓글 상호작용
            is PostDetailIntent.ToggleCommentLike -> toggleCommentLike(intent.commentId)
            is PostDetailIntent.ExpandReplies -> expandReplies(intent.commentId)
            is PostDetailIntent.LoadMoreReplies -> loadMoreReplies(intent.commentId)

            // 댓글 옵션
            is PostDetailIntent.ShowCommentOptions -> showCommentOptions(intent.commentId)
            is PostDetailIntent.DismissCommentOptions -> dismissCommentOptions()
            is PostDetailIntent.DeleteComment -> deleteComment()
            is PostDetailIntent.BookmarkComment -> bookmarkComment()

            // 차단 다이얼로그
            is PostDetailIntent.BlockAuthor -> blockAuthor()
            is PostDetailIntent.BlockCommentAuthor -> blockCommentAuthor()
            is PostDetailIntent.DismissBlockConfirmDialog -> dismissBlockConfirmDialog()
            is PostDetailIntent.ConfirmBlockUser -> confirmBlockUser()
            is PostDetailIntent.DismissBlockCompletedDialog -> dismissBlockCompletedDialog()

            // 공유
            is PostDetailIntent.SharePost -> sharePost()
            is PostDetailIntent.DismissShareSheet -> dismissShareSheet()
            is PostDetailIntent.ShareCompleted -> shareCompleted(intent.sharedRoomIds, intent.recipientCount)
            is PostDetailIntent.NavigateToChatRoom -> navigateToChatRoom(intent.roomId)

            // 네비게이션
            is PostDetailIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is PostDetailIntent.NavigateToCommentDetail -> navigateToCommentDetail(intent.commentId)

            // Snackbar
            is PostDetailIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    // 포스트

    private fun loadPost(id: Int = postId) = intent {
        // 캐시된 데이터가 없을 때만 로딩 표시
        val showLoading = state.post == null
        if (showLoading) {
            reduce { state.copy(isLoading = true, error = null) }
        }

        getPostByIdUseCase(id)
            .onSuccess { post ->
                reduce {
                    state.copy(
                        post = post,
                        isFollowing = post.author.isFollowing,
                        isLoading = false,
                        isViewCountLoaded = true,  // API 응답 완료 → 조회수 표시
                        error = null
                    )
                }
                loadComments(id)
            }
            .onFailure { error ->
                if (state.post == null || error.isPostUnavailable()) {
                    showPostError(error)
                } else {
                    // 그 외 오류는 캐시 본문 유지, 댓글은 스켈레톤 그대로
                    reduce { state.copy(snackbarState = SnackbarState(error.message ?: "포스트를 불러올 수 없습니다")) }
                }
            }
    }

    // 삭제·블라인드·차단이면 캐시에서도 뺌
    private suspend fun Syntax<PostDetailState, PostDetailSideEffect>.showPostError(error: Throwable) {
        val message = error.message ?: "포스트를 불러올 수 없습니다"
        // 글을 먼저 비워 캐시 제거 방출이 뒤로 가기로 이어지지 않음
        reduce {
            state.copy(
                post = null,
                isLoading = false,
                isRefreshing = false,
                error = message,
                isCommentsLoading = false,
                commentsError = message,
                snackbarState = SnackbarState(message)
            )
        }
        if (error.isPostUnavailable()) removeCachedPostUseCase(postId)
    }

    private fun Throwable.isPostUnavailable(): Boolean =  // 삭제·블라인드 404, 차단 403
        toUnavailablePostReason() != null

    private fun refreshPost() = intent {
        reduce { state.copy(isRefreshing = true) }

        getPostByIdUseCase(postId)
            .onSuccess { post ->
                reduce {
                    state.copy(
                        post = post,
                        isFollowing = post.author.isFollowing,
                        isRefreshing = false,
                        comments = emptyList(),
                        commentsPage = 1,
                        commentsHasMore = true,
                        expandedCommentReplies = emptyMap()
                    )
                }
                loadComments(postId)
            }
            .onFailure { error ->
                if (error.isPostUnavailable()) {
                    showPostError(error)
                    return@intent
                }
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(error.message ?: "새로고침 실패")
                    )
                }
            }
    }

    private fun likePost() = intent {
        val post = state.post ?: return@intent
        postMutationDelegate.togglePostLike(post.id, shouldLike = true)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 실패")) }
            }
    }

    private fun unlikePost() = intent {
        val post = state.post ?: return@intent
        postMutationDelegate.togglePostLike(post.id, shouldLike = false)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 취소 실패")) }
            }
    }

    private fun bookmarkPost() = intent {
        val post = state.post ?: return@intent
        val shouldBookmark = !post.isBookmarked

        reduce {
            state.copy(
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }

        postMutationDelegate.togglePostBookmark(post.id, shouldBookmark)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("저장 실패")) }
            }
    }

    private fun followAuthor() = intent {
        val authorId = state.post?.author?.id ?: return@intent
        postMutationDelegate.toggleAuthorFollow(authorId, shouldFollow = true)
            ?.onSuccess {
                // 버튼만 사라져 결과를 따로 알림
                postSideEffect(PostDetailSideEffect.ShowCenterToast("팔로우"))
            }
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("팔로우 실패")) }
            }
    }

    private fun unfollowAuthor() = intent {
        val authorId = state.post?.author?.id ?: return@intent
        postMutationDelegate.toggleAuthorFollow(authorId, shouldFollow = false)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("팔로우 취소 실패")) }
            }
    }

    // 포스트 옵션

    private fun showPostOptions() = intent {
        reduce { state.copy(showPostOptionsSheet = true) }
    }

    private fun dismissPostOptions() = intent {
        reduce { state.copy(showPostOptionsSheet = false) }
    }

    private fun deletePost() = intent {
        val post = state.post ?: return@intent
        dismissPostOptions()

        deletePostUseCase(post.id)
            .onSuccess {
                postSideEffect(PostDetailSideEffect.PostDeleted)
            }
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패")) }
            }
    }

    private fun editPost() = intent {
        val post = state.post ?: return@intent
        dismissPostOptions()
        postSideEffect(PostDetailSideEffect.NavigateToEditPost(post.id))
    }

    private fun pinPost() = intent {
        val post = state.post ?: return@intent
        val shouldPin = !post.isPinned

        reduce {
            state.copy(
                showPostOptionsSheet = false,
                snackbarState = SnackbarState(if (shouldPin) "고정됨" else "고정 해제됨")
            )
        }

        togglePinPostUseCase(post.id, shouldPin)
            .onFailure { error ->
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(error.message ?: "고정 처리 실패")
                    )
                }
            }
    }

    private fun notInterestedPost() = intent {
        val post = state.post ?: return@intent
        dismissPostOptions()

        postMutationDelegate.hidePost(post.id)
            .onSuccess {
                postSideEffect(PostDetailSideEffect.NavigateBack)
            }
    }

    private fun blockAuthor() = intent {
        val author = state.post?.author ?: return@intent
        dismissPostOptions()

        reduce {
            state.copy(
                showBlockConfirmDialog = true,
                blockTargetUser = BlockTargetUser(
                    id = author.id,
                    displayName = author.displayName ?: author.username,
                    avatarUrl = author.avatarUrl
                )
            )
        }
    }

    // 댓글

    private fun loadComments(id: Int = postId) = intent {
        reduce { state.copy(isCommentsLoading = true, commentsError = null, commentsPage = 1) }

        val sortBy = state.commentSortBy
        getPostCommentsUseCase(id, 1, 20, sortBy.apiValue)
            .onSuccess { comments ->
                if (state.commentSortBy != sortBy) return@intent
                commentsGeneration.next()
                reduce {
                    state.copy(
                        comments = comments,
                        isCommentsLoading = false,
                        isChangingSort = false,
                        commentsError = null,
                        commentsPage = 1,
                        commentsHasMore = comments.size >= 20
                    )
                }
                ensureHighlightCommentLoaded()
            }
            .onFailure { e ->
                if (state.commentSortBy != sortBy) return@intent
                // 스켈레톤 그대로, 다시 받기는 당겨서 새로고침
                reduce { state.copy(snackbarState = SnackbarState(e.message ?: "댓글을 불러오는데 실패했습니다")) }
            }
    }

    /** 하이라이트 댓글까지 페이지 로드 */
    private fun ensureHighlightCommentLoaded() = intent {
        val targetId = state.highlightCommentId ?: return@intent
        if (highlightCommentEnsured) return@intent
        highlightCommentEnsured = true

        var attempts = 0
        while (state.comments.none { it.id == targetId } && state.commentsHasMore && attempts < 20) {
            val generation = commentsGeneration.current()
            val sortBy = state.commentSortBy
            val nextPage = state.commentsPage + 1
            val comments = getPostCommentsUseCase(postId, nextPage, 20, sortBy.apiValue)
                .getOrNull() ?: break
            attempts++
            // 그사이 새로 받았으면 새 목록에서 다시 찾음
            if (commentsGeneration.current() != generation || state.commentSortBy != sortBy) continue
            reduce {
                state.copy(
                    comments = (state.comments + comments).distinctBy { it.id },
                    commentsPage = nextPage,
                    commentsHasMore = comments.size >= 20
                )
            }
        }
    }

    private fun loadMoreComments() = intent {
        if (state.isCommentsLoading || !state.commentsHasMore) return@intent

        val generation = commentsGeneration.current()
        val nextPage = state.commentsPage + 1
        val sortBy = state.commentSortBy
        reduce { state.copy(isCommentsLoading = true) }

        getPostCommentsUseCase(postId, nextPage, 20, sortBy.apiValue)
            .onSuccess { comments ->
                if (state.commentSortBy != sortBy) return@intent
                if (commentsGeneration.current() != generation) return@intent
                reduce {
                    state.copy(
                        comments = (state.comments + comments).distinctBy { it.id },
                        isCommentsLoading = false,
                        commentsPage = nextPage,
                        commentsHasMore = comments.size >= 20
                    )
                }
            }
            .onFailure { error ->
                if (commentsGeneration.current() != generation) return@intent
                reduce {
                    state.copy(
                        isCommentsLoading = false,
                        snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    private fun changeSortType(sortType: CommentSortType) = intent {
        if (state.commentSortBy == sortType) return@intent
        commentsGeneration.next()  // 옛 정렬로 진행 중이던 더보기 버림

        reduce {
            state.copy(
                commentSortBy = sortType,
                isChangingSort = true,
                comments = emptyList(),
                commentsPage = 1,
                commentsHasMore = true,
                expandedCommentReplies = emptyMap()
            )
        }
        loadComments()
    }

    private fun setReplyTo(comment: Comment?) = intent {
        reduce { state.copy(replyToComment = comment) }
    }

    // 댓글 상호작용

    private fun toggleCommentLike(commentId: Int) = intent {
        val comment = findComment(state, commentId) ?: return@intent
        postMutationDelegate.toggleCommentLike(commentId, shouldLike = !comment.isLiked)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 처리 실패")) }
            }
    }

    private fun findComment(state: PostDetailState, commentId: Int): Comment? {  // 최상위·firstReply·펼친 답글
        for (top in state.comments) {
            if (top.id == commentId) return top
            top.firstReply?.takeIf { it.id == commentId }?.let { return it }
            state.expandedCommentReplies[top.id]?.firstOrNull { it.id == commentId }?.let { return it }
        }
        return null
    }

    private fun expandReplies(commentId: Int) = intent {
        if (state.loadingRepliesCommentIds.contains(commentId)) return@intent

        reduce {
            state.copy(loadingRepliesCommentIds = state.loadingRepliesCommentIds + commentId)
        }

        val pageSize = 20
        getCommentRepliesUseCase(commentId, 1, pageSize)
            .onSuccess { replies ->
                reduce {
                    state.copy(
                        expandedCommentReplies = state.expandedCommentReplies + (commentId to replies),
                        loadingRepliesCommentIds = state.loadingRepliesCommentIds - commentId,
                        repliesPage = state.repliesPage + (commentId to 1),
                        repliesHasMore = state.repliesHasMore + (commentId to (replies.size >= pageSize))
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        loadingRepliesCommentIds = state.loadingRepliesCommentIds - commentId,
                        snackbarState = SnackbarState(error.message ?: "답글을 불러올 수 없습니다")
                    )
                }
            }
    }

    private fun loadMoreReplies(commentId: Int) = intent {
        if (state.loadingRepliesCommentIds.contains(commentId)) return@intent
        if (state.repliesHasMore[commentId] != true) return@intent

        reduce {
            state.copy(loadingRepliesCommentIds = state.loadingRepliesCommentIds + commentId)
        }

        val currentPage = state.repliesPage[commentId] ?: 1
        val nextPage = currentPage + 1
        val pageSize = 20

        getCommentRepliesUseCase(commentId, nextPage, pageSize)
            .onSuccess { newReplies ->
                val existingReplies = state.expandedCommentReplies[commentId] ?: emptyList()
                reduce {
                    state.copy(
                        expandedCommentReplies = state.expandedCommentReplies + (commentId to (existingReplies + newReplies).distinctBy { it.id }),
                        loadingRepliesCommentIds = state.loadingRepliesCommentIds - commentId,
                        repliesPage = state.repliesPage + (commentId to nextPage),
                        repliesHasMore = state.repliesHasMore + (commentId to (newReplies.size >= pageSize))
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        loadingRepliesCommentIds = state.loadingRepliesCommentIds - commentId,
                        snackbarState = SnackbarState(error.message ?: "답글을 불러올 수 없습니다")
                    )
                }
            }
    }

    // 댓글 옵션

    private fun showCommentOptions(commentId: Int) = intent {
        val comment = findComment(state, commentId) ?: return@intent
        reduce { state.copy(showCommentOptionsSheet = true, selectedComment = comment) }
    }

    private fun dismissCommentOptions() = intent {
        reduce { state.copy(showCommentOptionsSheet = false, selectedComment = null) }
    }

    private fun deleteComment() = intent {
        val selected = state.selectedComment ?: return@intent
        dismissCommentOptions()

        // 목록 반영은 캐시 구독 몫
        deleteCommentUseCase(selected.id)
            .onSuccess {
                reduce {
                    state.copy(
                        post = state.post?.copy(commentCount = maxOf(0, (state.post?.commentCount ?: 0) - 1)),
                        snackbarState = SnackbarState("댓글 삭제됨")
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패")) }
            }
    }

    private fun bookmarkComment() = intent {
        val comment = state.selectedComment ?: return@intent
        val shouldBookmark = !comment.isBookmarked
        dismissCommentOptions()

        postMutationDelegate.toggleCommentBookmark(comment.id, shouldBookmark)
            ?.onSuccess { syncedIsBookmarked ->
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(
                            if (syncedIsBookmarked) "저장됨" else "저장 취소됨"
                        )
                    )
                }
            }
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("저장 실패")) }
            }
    }

    private fun blockCommentAuthor() = intent {
        val author = state.selectedComment?.author ?: return@intent
        dismissCommentOptions()

        reduce {
            state.copy(
                showBlockConfirmDialog = true,
                blockTargetUser = BlockTargetUser(
                    id = author.id,
                    displayName = author.displayName ?: author.username,
                    avatarUrl = author.avatarUrl
                )
            )
        }
    }

    // 차단

    private fun dismissBlockConfirmDialog() = intent {
        reduce {
            state.copy(
                showBlockConfirmDialog = false,
                blockTargetUser = null
            )
        }
    }

    private fun confirmBlockUser() = intent {
        val targetUser = state.blockTargetUser ?: return@intent
        val isPostAuthor = state.post?.author?.id == targetUser.id

        reduce { state.copy(showBlockConfirmDialog = false) }

        isBlockingPostAuthor = isPostAuthor
        postMutationDelegate.blockUser(targetUser.id)
            .onFailure { isBlockingPostAuthor = false }
            .onSuccess {
                if (isPostAuthor) {
                    reduce {
                        state.copy(showBlockCompletedDialog = true)
                    }
                } else {
                    reduce {
                        state.copy(
                            comments = state.comments.filter { it.author.id != targetUser.id },
                            expandedCommentReplies = state.expandedCommentReplies.mapValues { (_, replies) ->
                                replies.filter { it.author.id != targetUser.id }
                            },
                            showBlockCompletedDialog = true
                        )
                    }
                }
            }
    }

    private fun dismissBlockCompletedDialog() = intent {
        val targetUser = state.blockTargetUser
        val isPostAuthor = state.post?.author?.id == targetUser?.id

        reduce {
            state.copy(
                showBlockCompletedDialog = false,
                blockTargetUser = null
            )
        }

        if (isPostAuthor) {
            postSideEffect(PostDetailSideEffect.NavigateBack)
        }
    }

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(PostDetailSideEffect.NavigateToUserProfile(userId))
    }

    private fun navigateToCommentDetail(commentId: Int) = intent {
        postSideEffect(PostDetailSideEffect.NavigateToCommentDetail(commentId))
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    // 공유
    private fun sharePost() = intent {
        reduce { state.copy(showShareSheet = true) }
    }

    private fun dismissShareSheet() = intent {
        reduce { state.copy(showShareSheet = false) }
    }

    private fun shareCompleted(sharedRoomIds: List<Int>, recipientCount: Int) = intent {
        val result = postShareDelegate.complete(state.post?.id, sharedRoomIds, recipientCount)
        reduce {
            state.copy(
                showShareSheet = false,
                snackbarState = SnackbarState(result.message, actionLabel = result.actionLabel),
                navigateRoomId = result.navigateRoomId
            )
        }
    }

    private fun navigateToChatRoom(roomId: Int) = intent {
        reduce { state.copy(navigateRoomId = null) }
        postSideEffect(PostDetailSideEffect.NavigateToChatRoom(roomId))
    }
}
