package com.ninezero.halo.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.auth.domain.usecase.GetCurrentUserIdUseCase
import com.ninezero.halo.feature.explore.domain.usecase.GetPopularCreatorsUseCase
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.delegate.PostShareDelegate
import com.ninezero.halo.feature.feed.domain.event.SubscriptionEventBus
import com.ninezero.halo.feature.feed.domain.model.CachedPostsTracker
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostUploadEvent
import com.ninezero.halo.feature.feed.domain.model.syncWithCache
import com.ninezero.halo.feature.feed.domain.repository.PendingPostRepository
import com.ninezero.halo.feature.feed.domain.repository.PostUploadEventRepository
import com.ninezero.halo.feature.feed.domain.usecase.DeletePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetHomeFeedUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObserveCachedPostsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.TogglePinPostUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val PAGE_SIZE = 10

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getHomeFeedUseCase: GetHomeFeedUseCase,
    private val getPopularCreatorsUseCase: GetPopularCreatorsUseCase,
    private val observeCachedPostsUseCase: ObserveCachedPostsUseCase,
    private val postMutationDelegate: PostMutationDelegate,
    private val postShareDelegate: PostShareDelegate,
    private val deletePostUseCase: DeletePostUseCase,
    private val togglePinPostUseCase: TogglePinPostUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val subscriptionEventBus: SubscriptionEventBus,
    private val pendingPostRepository: PendingPostRepository,
    private val postUploadEventRepository: PostUploadEventRepository
) : ViewModel(), OrbitContainerHost<FeedState, FeedState, FeedSideEffect> {

    private val cachedPostsTracker = CachedPostsTracker()

    // init 즉시 로드라 초기 로딩
    override val container: OrbitContainer<FeedState, FeedState, FeedSideEffect> =
        orbitContainer(FeedState(isLoading = true))

    private val listGeneration = ListGeneration()  // 새로고침 뒤 늦게 온 더보기 응답 버림

    init {
        observeCachedPosts()
        observeSubscriptionEvents()
        loadCurrentUser()
        loadRecommendedCreators()
        loadFeed()
        observeUploadResults()
    }

    private fun observeSubscriptionEvents() {
        subscriptionEventBus.events
            .onEach { refreshFeed() }
            .launchIn(viewModelScope)
    }

    private fun observeCachedPosts() {
        observeCachedPostsUseCase()
            .onEach { cachedPosts ->
                // 반영은 reduce 안에서, 동시 갱신 유실 방지
                val (cachedPostsMap, previousCachedIds) = cachedPostsTracker.track(cachedPosts)
                intent {
                    reduce {
                        state.copy(
                            posts = state.posts.syncWithCache(cachedPostsMap, previousCachedIds),
                            selectedPost = state.selectedPost?.syncWithCache(cachedPostsMap, previousCachedIds)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCurrentUser() = intent {
        val userId = getCurrentUserIdUseCase()
        reduce { state.copy(currentUserId = userId) }
        userId?.let { observePendingPosts(it) }

        getMyProfileUseCase()
            .onSuccess { profile ->
                reduce { state.copy(isCurrentUserCreator = profile.isCreator) }
            }
    }

    private fun observePendingPosts(authorId: Int) {
        pendingPostRepository.getPendingPostsByAuthor(authorId)
            .onEach { pendingPosts ->
                intent { reduce { state.copy(pendingPosts = pendingPosts) } }
            }
            .launchIn(viewModelScope)
    }

    private fun observeUploadResults() {
        postUploadEventRepository.uploadEvents
            .onEach { event ->
                // 커뮤니티 글은 피드에 없어 재로드 불필요
                if (event is PostUploadEvent.Success && event.creatorId == null) {
                    mergeFirstPageQuietly()
                }
            }
            .launchIn(viewModelScope)
    }

    // 새 1페이지를 앞에 합쳐 받아 둔 글과 스크롤 위치 유지
    private fun mergeFirstPageQuietly() = intent {
        getHomeFeedUseCase(limit = PAGE_SIZE)
            .onSuccess { result ->
                reduce { state.copy(posts = (result.items + state.posts).distinctBy { it.id }) }
            }
    }

    fun onIntent(intent: FeedIntent) {
        when (intent) {
            is FeedIntent.LoadFeed -> { loadRecommendedCreators(); loadFeed() }
            is FeedIntent.RefreshFeed -> refreshFeed()
            is FeedIntent.LoadMorePosts -> loadMorePosts()
            is FeedIntent.LikePost -> likePost(intent.postId)
            is FeedIntent.UnlikePost -> unlikePost(intent.postId)
            is FeedIntent.BookmarkPost -> bookmarkPost(intent.postId)
            is FeedIntent.HidePost -> hidePost(intent.postId)
            is FeedIntent.BlockUser -> blockUser(intent.userId)
            is FeedIntent.FollowUser -> followUser(intent.userId)
            is FeedIntent.UnfollowUser -> unfollowUser(intent.userId)
            is FeedIntent.ShowPostOptions -> showPostOptions(intent.post)
            is FeedIntent.DismissPostOptions -> dismissPostOptions()
            is FeedIntent.EditPost -> editPost()
            is FeedIntent.DeletePost -> deletePost()
            is FeedIntent.PinPost -> pinPost()
            is FeedIntent.SharePost -> sharePost(intent.postId)
            is FeedIntent.DismissShareSheet -> dismissShareSheet()
            is FeedIntent.ShareCompleted -> shareCompleted(intent.sharedRoomIds, intent.recipientCount)
            is FeedIntent.NavigateToPostDetail -> navigateToPostDetail(intent.postId)
            is FeedIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is FeedIntent.NavigateToCreatorProfile -> navigateToCreatorProfile(intent.creatorId)
            is FeedIntent.SnackbarDismissed -> dismissSnackbar()
            is FeedIntent.DismissCenterToast -> dismissCenterToast()
        }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun dismissCenterToast() = intent {
        reduce { state.copy(centerToastMessage = null) }
    }

    private fun loadRecommendedCreators() = intent {
        reduce { state.copy(isCreatorsLoading = true) }

        getPopularCreatorsUseCase(limit = 10)
            .onSuccess { creators ->
                reduce {
                    state.copy(
                        isCreatorsLoading = false,
                        recommendedCreators = creators
                    )
                }
            }
            .onFailure {
                reduce { state.copy(isCreatorsLoading = false) }
            }
    }

    private fun loadFeed() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        getHomeFeedUseCase(limit = PAGE_SIZE)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isLoading = false,
                        posts = result.items,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null,
                        error = null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "알 수 없는 오류가 발생했습니다",
                        centerToastMessage = "피드를 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun refreshFeed() = intent {
        reduce { state.copy(isRefreshing = true) }

        getPopularCreatorsUseCase(limit = 10)
            .onSuccess { creators ->
                reduce { state.copy(recommendedCreators = creators) }
            }

        getHomeFeedUseCase(limit = PAGE_SIZE)
            .onSuccess { result ->
                listGeneration.next()
                reduce {
                    state.copy(
                        isRefreshing = false,
                        posts = result.items,
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null,
                        error = null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        centerToastMessage = "피드 새로고침 실패"
                    )
                }
            }
    }

    private fun loadMorePosts() = intent {
        if (state.isLoading || !state.hasMore) return@intent

        val cursor = state.nextCursor
        val generation = listGeneration.current()
        reduce { state.copy(isLoading = true) }

        getHomeFeedUseCase(cursor = cursor, limit = PAGE_SIZE)
            .onSuccess { result ->
                if (listGeneration.current() != generation) {
                    reduce { state.copy(isLoading = false) }
                    return@intent
                }
                reduce {
                    state.copy(
                        isLoading = false,
                        posts = (state.posts + result.items).distinctBy { it.id },
                        nextCursor = result.nextCursor,
                        hasMore = result.nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isLoading = false,
                        centerToastMessage = "더 불러오기 실패"
                    )
                }
            }
    }

    private fun likePost(postId: Int) = intent {
        postMutationDelegate.togglePostLike(postId, shouldLike = true)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("좋아요 실패")) } }
    }

    private fun unlikePost(postId: Int) = intent {
        postMutationDelegate.togglePostLike(postId, shouldLike = false)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("좋아요 취소 실패")) } }
    }

    private fun bookmarkPost(postId: Int) = intent {
        val targetPost = state.posts.find { it.id == postId }
        val shouldBookmark = !(targetPost?.isBookmarked ?: false)

        reduce {
            state.copy(
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }

        postMutationDelegate.togglePostBookmark(postId, shouldBookmark)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("저장 실패")) } }
    }

    private fun navigateToPostDetail(postId: Int) = intent {
        postSideEffect(FeedSideEffect.NavigateToPostDetail(postId))
    }

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(FeedSideEffect.NavigateToUserProfile(userId))
    }

    private fun navigateToCreatorProfile(creatorId: Int) = intent {
        postSideEffect(FeedSideEffect.NavigateToCreatorProfile(creatorId))
    }

    private fun showPostOptions(post: Post) = intent {
        reduce {
            state.copy(
                selectedPost = post,
                showPostOptionsSheet = true
            )
        }
    }

    private fun dismissPostOptions() = intent {
        reduce {
            state.copy(
                selectedPost = null,
                showPostOptionsSheet = false
            )
        }
    }

    private fun editPost() = intent {
        val postId = state.selectedPost?.id ?: return@intent
        reduce {
            state.copy(
                selectedPost = null,
                showPostOptionsSheet = false
            )
        }
        postSideEffect(FeedSideEffect.NavigateToEditPost(postId))
    }

    private fun deletePost() = intent {
        val postId = state.selectedPost?.id ?: return@intent
        reduce {
            state.copy(
                selectedPost = null,
                showPostOptionsSheet = false
            )
        }

        deletePostUseCase(postId)
            .onSuccess {
                reduce {
                    state.copy(
                        posts = state.posts.filter { it.id != postId },
                        snackbarState = SnackbarState("포스트가 삭제되었습니다")
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패"))
                }
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
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(error.message ?: "고정 처리 실패")
                    )
                }
            }
    }

    private fun hidePost(postId: Int) = intent {
        reduce {
            state.copy(
                selectedPost = null,
                showPostOptionsSheet = false
            )
        }

        postMutationDelegate.hidePost(postId)
            .onSuccess { isHidden ->
                if (isHidden) {
                    reduce {
                        state.copy(
                            posts = state.posts.filter { it.id != postId },
                            snackbarState = SnackbarState("관심없음 처리되었습니다")
                        )
                    }
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "처리 실패"))
                }
            }
    }

    private fun blockUser(userId: Int) = intent {
        reduce {
            state.copy(
                selectedPost = null,
                showPostOptionsSheet = false
            )
        }

        postMutationDelegate.blockUser(userId)
            .onSuccess { isBlocked ->
                if (isBlocked) {
                    reduce {
                        state.copy(
                            posts = state.posts.filter { it.author.id != userId },
                            snackbarState = SnackbarState("사용자를 차단했습니다")
                        )
                    }
                } else {
                    reduce {
                        state.copy(snackbarState = SnackbarState("차단 해제되었습니다"))
                    }
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "차단 실패"))
                }
            }
    }

    private fun followUser(userId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow = true)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("팔로우 실패")) } }
    }

    private fun unfollowUser(userId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow = false)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("팔로우 취소 실패")) } }
    }

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
}
