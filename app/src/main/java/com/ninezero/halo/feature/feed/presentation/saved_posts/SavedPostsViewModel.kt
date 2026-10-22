package com.ninezero.halo.feature.feed.presentation.saved_posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.auth.domain.usecase.GetCurrentUserIdUseCase
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.delegate.PostShareDelegate
import com.ninezero.halo.feature.feed.domain.model.BookmarkItem
import com.ninezero.halo.feature.feed.domain.model.BookmarkTargetType
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.repository.BookmarkRepository
import com.ninezero.halo.feature.feed.domain.repository.CommentRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.feed.domain.usecase.CacheCommentsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.DeleteCommentUseCase
import com.ninezero.halo.feature.feed.domain.usecase.DeletePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.TogglePinPostUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val PAGE_SIZE = 10

@HiltViewModel
class SavedPostsViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val postMutationDelegate: PostMutationDelegate,
    private val postShareDelegate: PostShareDelegate,
    private val deletePostUseCase: DeletePostUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val togglePinPostUseCase: TogglePinPostUseCase,
    private val cacheCommentsUseCase: CacheCommentsUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : ViewModel(), OrbitContainerHost<SavedPostsState, SavedPostsState, SavedPostsSideEffect> {

    // init에서 즉시 로드하므로 초기 상태를 로딩으로 둠
    override val container: OrbitContainer<SavedPostsState, SavedPostsState, SavedPostsSideEffect> =
        orbitContainer(SavedPostsState(isLoading = true))

    private val listGeneration = ListGeneration()  // 새로고침 뒤 늦게 온 더보기 응답 버림

    // 빠진 항목 판별용, init보다 앞에 둘 것
    private var cachedPostIds: Set<Int> = emptySet()
    private var cachedCommentIds: Set<Int> = emptySet()

    init {
        observeCache()
        loadCurrentUser()
        loadItems()
    }

    fun onIntent(intent: SavedPostsIntent) {
        when (intent) {
            is SavedPostsIntent.LoadItems -> loadItems()
            is SavedPostsIntent.RefreshItems -> refreshItems()
            is SavedPostsIntent.LoadMoreItems -> loadMoreItems()
            is SavedPostsIntent.SelectTab -> selectTab(intent.tabIndex)

            is SavedPostsIntent.LikePost -> likePost(intent.postId)
            is SavedPostsIntent.UnlikePost -> unlikePost(intent.postId)
            is SavedPostsIntent.BookmarkPost -> bookmarkPost(intent.postId)
            is SavedPostsIntent.BlockUser -> blockUser(intent.userId)
            is SavedPostsIntent.FollowUser -> followUser(intent.userId)
            is SavedPostsIntent.UnfollowUser -> unfollowUser(intent.userId)

            is SavedPostsIntent.ShowPostOptions -> showPostOptions(intent.post)
            is SavedPostsIntent.DismissPostOptions -> dismissPostOptions()
            is SavedPostsIntent.EditPost -> editPost()
            is SavedPostsIntent.DeletePost -> deletePost()
            is SavedPostsIntent.PinPost -> pinPost()

            is SavedPostsIntent.LikeComment -> likeComment(intent.commentId)
            is SavedPostsIntent.UnlikeComment -> unlikeComment(intent.commentId)

            is SavedPostsIntent.ShowCommentOptions -> showCommentOptions(intent.comment)
            is SavedPostsIntent.DismissCommentOptions -> dismissCommentOptions()
            is SavedPostsIntent.DeleteComment -> deleteComment()
            is SavedPostsIntent.BookmarkComment -> bookmarkComment()
            is SavedPostsIntent.BlockCommentAuthor -> blockUser(intent.userId)

            is SavedPostsIntent.SharePost -> sharePost(intent.postId)
            is SavedPostsIntent.DismissShareSheet -> dismissShareSheet()
            is SavedPostsIntent.ShareCompleted -> shareCompleted(intent.sharedRoomIds, intent.recipientCount)

            is SavedPostsIntent.NavigateToPostDetail -> navigateToPostDetail(intent.postId)
            is SavedPostsIntent.NavigateToCommentDetail -> navigateToCommentDetail(intent.commentId)
            is SavedPostsIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)

            is SavedPostsIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadCurrentUser() = intent {
        val userId = getCurrentUserIdUseCase()
        reduce { state.copy(currentUserId = userId) }

        getMyProfileUseCase()
            .onSuccess { profile ->
                reduce { state.copy(isCurrentUserCreator = profile.isCreator) }
            }
    }

    private fun loadItems() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        val tab = state.selectedTab
        val targetType = tabFilterValue(tab)
        bookmarkRepository.getMyBookmarks(targetType, limit = PAGE_SIZE)
            .onSuccess { (items, nextCursor) ->
                if (state.selectedTab != tab) return@intent
                listGeneration.next()
                cacheLoadedItems(items)
                // 다 걸러진 페이지면 로딩 표시를 유지한 채 이어 받음
                val chain = items.isEmpty() && nextCursor != null
                reduce {
                    state.copy(
                        items = bindItems(items),
                        isLoading = chain,
                        nextCursor = nextCursor,
                        hasMore = nextCursor != null
                    )
                }
                if (chain) loadMoreItems(chained = true)
            }
            .onFailure { e ->
                if (state.selectedTab != tab) return@intent
                reduce {
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "북마크를 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun refreshItems() = intent {
        reduce { state.copy(isRefreshing = true, error = null) }

        val tab = state.selectedTab
        val targetType = tabFilterValue(tab)
        bookmarkRepository.getMyBookmarks(targetType, limit = PAGE_SIZE)
            .onSuccess { (items, nextCursor) ->
                if (state.selectedTab != tab) {
                    reduce { state.copy(isRefreshing = false) }
                    return@intent
                }
                listGeneration.next()
                cacheLoadedItems(items)
                // 목록을 갈아끼운 쪽이 로딩 표시를 맡음
                val chain = items.isEmpty() && nextCursor != null
                reduce {
                    state.copy(
                        items = bindItems(items),
                        isRefreshing = false,
                        isLoading = chain,
                        nextCursor = nextCursor,
                        hasMore = nextCursor != null
                    )
                }
                if (chain) loadMoreItems(chained = true)
            }
            .onFailure { e ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(message = e.message ?: "새로고침 실패")
                    )
                }
            }
    }

    // chained는 갈아끼운 쪽의 이어 받기, 로딩 가드 건너뜀
    private fun loadMoreItems(chained: Boolean = false): Job = intent {  // 자기를 다시 불러 타입 명시
        if ((state.isLoading && !chained) || !state.hasMore) return@intent

        reduce { state.copy(isLoading = true) }

        val tab = state.selectedTab
        val targetType = tabFilterValue(tab)
        val generation = listGeneration.current()
        val cursor = state.nextCursor
        bookmarkRepository.getMyBookmarks(targetType, cursor = cursor, limit = PAGE_SIZE)
            .onSuccess { (newItems, nextCursor) ->
                if (state.selectedTab != tab) return@intent
                if (listGeneration.current() != generation) {
                    // 목록 수가 그대로면 트리거가 안 걸려 다시 요청
                    if (!state.isLoading && state.items.isNotEmpty()) loadMoreItems()
                    return@intent
                }
                cacheLoadedItems(newItems)
                // 목록 수가 그대로면 스크롤 트리거가 다시 안 걸려 이어 받음
                val chain = newItems.isEmpty() && nextCursor != null
                reduce {
                    state.copy(
                        items = bindItems((state.items + newItems).distinctBy { it.targetType to it.targetId }),
                        isLoading = chain,
                        nextCursor = nextCursor,
                        hasMore = nextCursor != null
                    )
                }
                if (chain) loadMoreItems(chained = true)
            }
            .onFailure { e ->
                if (state.selectedTab != tab || listGeneration.current() != generation) return@intent
                reduce {
                    state.copy(
                        isLoading = false,
                        snackbarState = SnackbarState(message = e.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    private fun selectTab(tabIndex: Int) = intent {
        if (state.selectedTab == tabIndex) return@intent
        listGeneration.next()  // 탭을 빠르게 오가도 옛 탭 더보기가 붙지 않게
        reduce { state.copy(selectedTab = tabIndex, items = emptyList(), nextCursor = null, hasMore = true) }
        loadItems()
    }

    // 포스트 상호작용

    private fun likePost(postId: Int) = intent {
        postMutationDelegate.togglePostLike(postId, shouldLike = true)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 실패")) }
            }
    }

    private fun unlikePost(postId: Int) = intent {
        postMutationDelegate.togglePostLike(postId, shouldLike = false)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 취소 실패")) }
            }
    }

    private fun bookmarkPost(postId: Int) = intent {
        val targetItem = state.items.find {
            it.targetType == BookmarkTargetType.POST && it.targetId == postId
        }
        val shouldBookmark = !(targetItem?.post?.isBookmarked ?: false)
        val index = state.items.indexOf(targetItem)

        postMutationDelegate.togglePostBookmark(postId, shouldBookmark)
            ?.onSuccess {
                if (!shouldBookmark) {
                    reduce {
                        state.copy(
                            items = state.items.filter {
                                !(it.targetType == BookmarkTargetType.POST && it.targetId == postId)
                            },
                            snackbarState = SnackbarState("저장 취소됨")
                        )
                    }
                } else {
                    reduce { state.copy(snackbarState = SnackbarState("저장됨")) }
                }
            }
            ?.onFailure {
                reduce {
                    state.copy(
                        items = if (shouldBookmark) state.items else state.items.restoredIfMissing(index, targetItem),
                        snackbarState = SnackbarState("저장 실패")
                    )
                }
            }
    }

    // 포스트 옵션

    private fun showPostOptions(post: Post) = intent {
        reduce { state.copy(selectedPost = postRepository.getCachedPost(post.id) ?: post, showPostOptionsSheet = true) }
    }

    private fun dismissPostOptions() = intent {
        reduce { state.copy(selectedPost = null, showPostOptionsSheet = false) }
    }

    private fun editPost() = intent {
        val postId = state.selectedPost?.id ?: return@intent
        reduce { state.copy(selectedPost = null, showPostOptionsSheet = false) }
        postSideEffect(SavedPostsSideEffect.NavigateToEditPost(postId))
    }

    private fun deletePost() = intent {
        val postId = state.selectedPost?.id ?: return@intent
        reduce { state.copy(selectedPost = null, showPostOptionsSheet = false) }

        deletePostUseCase(postId)
            .onSuccess {
                reduce {
                    state.copy(
                        items = state.items.filter {
                            !(it.targetType == BookmarkTargetType.POST && it.targetId == postId)
                        },
                        snackbarState = SnackbarState("포스트가 삭제되었습니다")
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패")) }
            }
    }

    private fun pinPost() = intent {
        val post = state.selectedPost ?: return@intent
        val shouldPin = !post.isPinned
        reduce {
            state.copy(
                showPostOptionsSheet = false,
                selectedPost = null,
                snackbarState = SnackbarState(if (shouldPin) "고정됨" else "고정 해제됨")
            )
        }

        togglePinPostUseCase(post.id, shouldPin)
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "고정 처리 실패")) }
            }
    }

    private fun blockUser(userId: Int) = intent {
        reduce {
            state.copy(
                selectedPost = null,
                showPostOptionsSheet = false,
                selectedComment = null,
                showCommentOptionsSheet = false
            )
        }

        postMutationDelegate.blockUser(userId)
            .onSuccess { isBlocked ->
                if (isBlocked) {
                    reduce {
                        state.copy(
                            items = state.items.filter {
                                val authorId = when (it.targetType) {
                                    BookmarkTargetType.POST -> it.post?.author?.id
                                    BookmarkTargetType.COMMENT -> it.comment?.author?.id
                                }
                                authorId != userId
                            },
                            snackbarState = SnackbarState("사용자를 차단했습니다")
                        )
                    }
                } else {
                    reduce { state.copy(snackbarState = SnackbarState("차단 해제되었습니다")) }
                }
            }
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "차단 실패")) }
            }
    }

    // 팔로우

    private fun followUser(userId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow = true)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("팔로우 실패")) } }
    }

    private fun unfollowUser(userId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow = false)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("팔로우 취소 실패")) } }
    }

    // 댓글 상호작용

    private fun likeComment(commentId: Int) = intent {
        postMutationDelegate.toggleCommentLike(commentId, shouldLike = true)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 실패")) }
            }
    }

    private fun unlikeComment(commentId: Int) = intent {
        postMutationDelegate.toggleCommentLike(commentId, shouldLike = false)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("좋아요 취소 실패")) }
            }
    }

    // 댓글 옵션

    private fun showCommentOptions(comment: Comment) = intent {
        reduce {
            state.copy(
                selectedComment = commentRepository.getCachedComment(comment.id) ?: comment,
                showCommentOptionsSheet = true
            )
        }
    }

    private fun dismissCommentOptions() = intent {
        reduce { state.copy(selectedComment = null, showCommentOptionsSheet = false) }
    }

    private fun deleteComment() = intent {
        val commentId = state.selectedComment?.id ?: return@intent
        reduce { state.copy(selectedComment = null, showCommentOptionsSheet = false) }

        deleteCommentUseCase(commentId)
            .onSuccess {
                reduce {
                    state.copy(
                        items = state.items.filter {
                            !(it.targetType == BookmarkTargetType.COMMENT && it.targetId == commentId)
                        },
                        snackbarState = SnackbarState("댓글이 삭제되었습니다")
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
        val index = state.items.indexOfFirst { it.targetType == BookmarkTargetType.COMMENT && it.targetId == comment.id }
        val targetItem = state.items.getOrNull(index)
        reduce { state.copy(selectedComment = null, showCommentOptionsSheet = false) }

        postMutationDelegate.toggleCommentBookmark(comment.id, shouldBookmark)
            ?.onSuccess {
                if (!shouldBookmark) {
                    // 북마크 해제 → 저장됨 화면에서 제거
                    reduce {
                        state.copy(
                            items = state.items.filter {
                                !(it.targetType == BookmarkTargetType.COMMENT && it.targetId == comment.id)
                            },
                            snackbarState = SnackbarState("저장 취소됨")
                        )
                    }
                } else {
                    reduce { state.copy(snackbarState = SnackbarState("저장됨")) }
                }
            }
            ?.onFailure {
                reduce {
                    state.copy(
                        items = if (shouldBookmark) state.items else state.items.restoredIfMissing(index, targetItem),
                        snackbarState = SnackbarState("저장 실패")
                    )
                }
            }
    }

    // 공유

    private fun sharePost(postId: Int) = intent {
        reduce { state.copy(showShareSheet = true, sharePostId = postId) }
    }

    private fun dismissShareSheet() = intent {
        reduce { state.copy(showShareSheet = false, sharePostId = null) }
    }

    private fun shareCompleted(sharedRoomIds: List<Int>, recipientCount: Int) = intent {
        val result = postShareDelegate.complete(state.sharePostId, sharedRoomIds, recipientCount)
        reduce {
            state.copy(
                showShareSheet = false,
                sharePostId = null,
                snackbarState = SnackbarState(result.message, actionLabel = result.actionLabel),
                navigateRoomId = result.navigateRoomId
            )
        }
    }

    private fun navigateToPostDetail(postId: Int) = intent {
        postSideEffect(SavedPostsSideEffect.NavigateToPostDetail(postId))
    }

    private fun navigateToCommentDetail(commentId: Int) = intent {
        postSideEffect(SavedPostsSideEffect.NavigateToCommentDetail(commentId))
    }

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(SavedPostsSideEffect.NavigateToUserProfile(userId))
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null, navigateRoomId = null) }
    }

    private fun tabFilterValue(tab: Int): String? {
        return when (tab) {
            0 -> null       // 전체
            1 -> "POST"     // 포스트
            2 -> "COMMENT"  // 댓글
            else -> null
        }
    }

    // 낙관적 반영으로 빠진 항목을 실패 시 제자리에 되돌림
    private fun List<BookmarkItem>.restoredIfMissing(index: Int, item: BookmarkItem?): List<BookmarkItem> =
        if (item == null || any { it.targetType == item.targetType && it.targetId == item.targetId }) {
            this
        } else {
            toMutableList().apply { add(index.coerceIn(0, size), item) }
        }

    private fun observeCache() {
        viewModelScope.launch {
            combine(
                postRepository.observeCachedPosts(),
                commentRepository.observeCachedComments()
            ) { posts, comments ->
                posts.associateBy { it.id } to comments
            }.collect { (postsById, commentsById) ->
                // 직전에 있다 빠진 항목은 다른 화면에서 제거됨
                val previousPostIds = cachedPostIds
                val previousCommentIds = cachedCommentIds
                cachedPostIds = postsById.keys
                cachedCommentIds = commentsById.keys
                intent {
                    reduce {
                        state.syncWithCache(
                            postsById = postsById,
                            commentsById = commentsById,
                            previousPostIds = previousPostIds,
                            previousCommentIds = previousCommentIds
                        )
                    }
                }
            }
        }
    }

    private fun cacheLoadedItems(items: List<BookmarkItem>) {
        postRepository.cachePosts(items.mapNotNull { it.post })
        // 캐시의 첫 답글·미리보기는 유지
        cacheCommentsUseCase(items.mapNotNull { it.comment })
    }

    private fun bindItems(items: List<BookmarkItem>): List<BookmarkItem> {
        return items.mapNotNull(::bindItem)
    }

    private fun bindItem(item: BookmarkItem): BookmarkItem? {
        return when (item.targetType) {
            BookmarkTargetType.POST -> {
                val post = postRepository.getCachedPost(item.targetId) ?: item.post ?: return null
                if (!post.isBookmarked) null else item.copy(post = post)
            }

            BookmarkTargetType.COMMENT -> {
                val comment = commentRepository.getCachedComment(item.targetId) ?: item.comment ?: return null
                if (!comment.isBookmarked) null else item.copy(comment = comment)
            }
        }
    }

    private fun SavedPostsState.syncWithCache(
        postsById: Map<Int, Post>,
        commentsById: Map<Int, Comment>,
        previousPostIds: Set<Int>,
        previousCommentIds: Set<Int>
    ): SavedPostsState {
        val syncedItems = items.mapNotNull { item ->
            when (item.targetType) {
                BookmarkTargetType.POST -> {
                    if (item.targetId !in postsById && item.targetId in previousPostIds) return@mapNotNull null
                    val post = postsById[item.targetId] ?: item.post ?: return@mapNotNull null
                    if (!post.isBookmarked) null else item.copy(post = post)
                }

                BookmarkTargetType.COMMENT -> {
                    if (item.targetId !in commentsById && item.targetId in previousCommentIds) return@mapNotNull null
                    val comment = commentsById[item.targetId] ?: item.comment ?: return@mapNotNull null
                    if (!comment.isBookmarked) null else item.copy(comment = comment)
                }
            }
        }

        val syncedSelectedPost = selectedPost?.id?.let { postId ->
            postsById[postId] ?: syncedItems.firstOrNull {
                it.targetType == BookmarkTargetType.POST && it.targetId == postId
            }?.post
        }
        val syncedSelectedComment = selectedComment?.id?.let { commentId ->
            commentsById[commentId] ?: syncedItems.firstOrNull {
                it.targetType == BookmarkTargetType.COMMENT && it.targetId == commentId
            }?.comment
        }

        return copy(
            items = syncedItems,
            selectedPost = syncedSelectedPost,
            showPostOptionsSheet = showPostOptionsSheet && syncedSelectedPost != null,
            selectedComment = syncedSelectedComment,
            showCommentOptionsSheet = showCommentOptionsSheet && syncedSelectedComment != null
        )
    }
}
