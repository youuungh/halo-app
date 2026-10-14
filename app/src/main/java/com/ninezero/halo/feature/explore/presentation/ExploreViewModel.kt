package com.ninezero.halo.feature.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.auth.domain.usecase.GetCurrentUserIdUseCase
import com.ninezero.halo.feature.explore.domain.model.Banner
import com.ninezero.halo.feature.explore.domain.usecase.GetBannersUseCase
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.delegate.PostShareDelegate
import com.ninezero.halo.feature.feed.domain.event.SubscriptionEventBus
import com.ninezero.halo.feature.feed.domain.model.CachedPostsTracker
import com.ninezero.halo.feature.feed.domain.model.FeedType
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.syncWithCache
import com.ninezero.halo.feature.feed.domain.usecase.DeletePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.GetFeedUseCase
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
class ExploreViewModel @Inject constructor(
    private val getBannersUseCase: GetBannersUseCase,
    private val getFeedUseCase: GetFeedUseCase,
    private val observeCachedPostsUseCase: ObserveCachedPostsUseCase,
    private val postMutationDelegate: PostMutationDelegate,
    private val postShareDelegate: PostShareDelegate,
    private val deletePostUseCase: DeletePostUseCase,
    private val togglePinPostUseCase: TogglePinPostUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val subscriptionEventBus: SubscriptionEventBus
) : ViewModel(), OrbitContainerHost<ExploreState, ExploreState, ExploreSideEffect> {

    private val cachedPostsTracker = CachedPostsTracker()

    // init 즉시 로드라 로딩 상태로 시작
    override val container: OrbitContainer<ExploreState, ExploreState, ExploreSideEffect> =
        orbitContainer(ExploreState(isBannersLoading = true, isTrendingLoading = true))

    private val trendingGeneration = ListGeneration()
    private val popularGeneration = ListGeneration()

    init {
        observeCachedPosts()
        observeSubscriptionEvents()
        loadCurrentUser()
        loadBanners()
        loadTrendingPosts()
    }

    private fun observeSubscriptionEvents() {  // 구독 시 탭 새로고침
        subscriptionEventBus.events
            .onEach { refreshCurrentTab() }
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
                            trendingPosts = state.trendingPosts.syncWithCache(cachedPostsMap, previousCachedIds),
                            popularPosts = state.popularPosts.syncWithCache(cachedPostsMap, previousCachedIds),
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

        getMyProfileUseCase()
            .onSuccess { profile ->
                reduce { state.copy(isCurrentUserCreator = profile.isCreator) }
            }
    }

    fun onIntent(intent: ExploreIntent) {
        when (intent) {
            is ExploreIntent.LoadTrendingPosts -> { retryBannersIfEmpty(); loadTrendingPosts() }
            is ExploreIntent.LoadPopularPosts -> { retryBannersIfEmpty(); loadPopularPosts() }
            is ExploreIntent.LoadMoreTrending -> loadMoreTrending()
            is ExploreIntent.LoadMorePopular -> loadMorePopular()
            is ExploreIntent.RefreshCurrentTab -> refreshCurrentTab()
            is ExploreIntent.SelectTab -> selectTab(intent.tab)
            is ExploreIntent.LikePost -> likePost(intent.postId)
            is ExploreIntent.UnlikePost -> unlikePost(intent.postId)
            is ExploreIntent.BookmarkPost -> bookmarkPost(intent.postId)
            is ExploreIntent.HidePost -> hidePost(intent.postId)
            is ExploreIntent.BlockUser -> blockUser(intent.userId)
            is ExploreIntent.FollowUser -> followUser(intent.userId)
            is ExploreIntent.UnfollowUser -> unfollowUser(intent.userId)
            is ExploreIntent.ShowPostOptions -> showPostOptions(intent.post)
            is ExploreIntent.DismissPostOptions -> dismissPostOptions()
            is ExploreIntent.EditPost -> editPost()
            is ExploreIntent.DeletePost -> deletePost()
            is ExploreIntent.PinPost -> pinPost()
            is ExploreIntent.SharePost -> sharePost(intent.postId)
            is ExploreIntent.DismissShareSheet -> dismissShareSheet()
            is ExploreIntent.ShareCompleted -> shareCompleted(intent.sharedRoomIds, intent.recipientCount)
            is ExploreIntent.NavigateToChatRoom -> navigateToChatRoom(intent.roomId)
            is ExploreIntent.NavigateToPostDetail -> navigateToPostDetail(intent.postId)
            is ExploreIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is ExploreIntent.NavigateToBannerLink -> navigateToBannerLink(intent.banner)
            is ExploreIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun loadBanners() = intent {
        reduce { state.copy(isBannersLoading = true) }

        getBannersUseCase()
            .onSuccess { banners ->
                reduce { state.copy(isBannersLoading = false, banners = banners) }
            }
            .onFailure {
                reduce { state.copy(isBannersLoading = false) }
            }
    }

    private fun retryBannersIfEmpty() = intent {  // 배너는 자체 재시도 UI 없음
        if (state.banners.isEmpty() && !state.isBannersLoading) {
            loadBanners()
        }
    }

    private fun loadTrendingPosts() = intent {
        reduce { state.copy(isTrendingLoading = true) }

        getFeedUseCase(FeedType.TRENDING, page = 1, limit = PAGE_SIZE)
            .onSuccess { result ->
                trendingGeneration.next()
                reduce {
                    state.copy(
                        isTrendingLoading = false,
                        trendingPosts = result.items,
                        trendingPage = result.currentPage,
                        trendingHasMore = result.hasNext,
                        trendingError = null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isTrendingLoading = false,
                        trendingError = error.message ?: "실시간 피드를 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun loadPopularPosts() = intent {
        if (state.popularPosts.isNotEmpty()) return@intent  // 이미 로드됨

        reduce { state.copy(isPopularLoading = true) }

        getFeedUseCase(FeedType.EXPLORE, page = 1, limit = PAGE_SIZE)
            .onSuccess { result ->
                popularGeneration.next()
                reduce {
                    state.copy(
                        isPopularLoading = false,
                        popularPosts = result.items,
                        popularPage = result.currentPage,
                        popularHasMore = result.hasNext,
                        popularError = null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isPopularLoading = false,
                        popularError = error.message ?: "인기 피드를 불러올 수 없습니다"
                    )
                }
            }
    }

    private fun loadMoreTrending() = intent {
        if (state.isTrendingLoading || state.isRefreshing || !state.trendingHasMore) return@intent

        val generation = trendingGeneration.current()
        val nextPage = state.trendingPage + 1
        reduce { state.copy(isTrendingLoading = true) }

        getFeedUseCase(FeedType.TRENDING, page = nextPage, limit = PAGE_SIZE)
            .onSuccess { result ->
                if (trendingGeneration.current() != generation) {
                    reduce { state.copy(isTrendingLoading = false) }
                    return@intent
                }
                reduce {
                    state.copy(
                        isTrendingLoading = false,
                        trendingPosts = (state.trendingPosts + result.items).distinctBy { it.id },
                        trendingPage = result.currentPage,
                        trendingHasMore = result.hasNext
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(isTrendingLoading = false, snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")) }
            }
    }

    private fun loadMorePopular() = intent {
        if (state.isPopularLoading || state.isRefreshing || !state.popularHasMore) return@intent

        val generation = popularGeneration.current()
        val nextPage = state.popularPage + 1
        reduce { state.copy(isPopularLoading = true) }

        getFeedUseCase(FeedType.EXPLORE, page = nextPage, limit = PAGE_SIZE)
            .onSuccess { result ->
                if (popularGeneration.current() != generation) {
                    reduce { state.copy(isPopularLoading = false) }
                    return@intent
                }
                reduce {
                    state.copy(
                        isPopularLoading = false,
                        popularPosts = (state.popularPosts + result.items).distinctBy { it.id },
                        popularPage = result.currentPage,
                        popularHasMore = result.hasNext
                    )
                }
            }
            .onFailure { error ->
                reduce { state.copy(isPopularLoading = false, snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")) }
            }
    }

    private fun refreshCurrentTab() = intent {
        reduce { state.copy(isRefreshing = true) }

        getBannersUseCase()
            .onSuccess { banners ->
                reduce { state.copy(banners = banners) }
            }

        when (state.selectedTab) {
            ExploreTab.TRENDING -> {
                getFeedUseCase(FeedType.TRENDING, page = 1, limit = PAGE_SIZE)
                    .onSuccess { result ->
                        trendingGeneration.next()
                        reduce {
                            state.copy(
                                isRefreshing = false,
                                trendingPosts = result.items,
                                trendingPage = result.currentPage,
                                trendingHasMore = result.hasNext
                            )
                        }
                    }
                    .onFailure {
                        reduce {
                            state.copy(
                                isRefreshing = false,
                                snackbarState = SnackbarState("새로고침 실패")
                            )
                        }
                    }
            }
            ExploreTab.POPULAR -> {
                getFeedUseCase(FeedType.EXPLORE, page = 1, limit = PAGE_SIZE)
                    .onSuccess { result ->
                        popularGeneration.next()
                        reduce {
                            state.copy(
                                isRefreshing = false,
                                popularPosts = result.items,
                                popularPage = result.currentPage,
                                popularHasMore = result.hasNext
                            )
                        }
                    }
                    .onFailure {
                        reduce {
                            state.copy(
                                isRefreshing = false,
                                snackbarState = SnackbarState("새로고침 실패")
                            )
                        }
                    }
            }
        }
    }

    private fun selectTab(tab: ExploreTab) = intent {
        reduce { state.copy(selectedTab = tab) }

        when (tab) {
            ExploreTab.TRENDING -> {
                if (state.trendingPosts.isEmpty()) loadTrendingPosts()
            }
            ExploreTab.POPULAR -> {
                if (state.popularPosts.isEmpty()) loadPopularPosts()
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
        val targetPost = state.trendingPosts.find { it.id == postId }
            ?: state.popularPosts.find { it.id == postId }
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
        postSideEffect(ExploreSideEffect.NavigateToPostDetail(postId))
    }

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(ExploreSideEffect.NavigateToUserProfile(userId))
    }

    private fun navigateToBannerLink(banner: Banner) = intent {
        postSideEffect(ExploreSideEffect.NavigateToBannerLink(banner.link))
    }

    // 포스트 옵션 시트 처리
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
        postSideEffect(ExploreSideEffect.NavigateToEditPost(postId))
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
                        trendingPosts = state.trendingPosts.filter { it.id != postId },
                        popularPosts = state.popularPosts.filter { it.id != postId },
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
                            trendingPosts = state.trendingPosts.filter { it.id != postId },
                            popularPosts = state.popularPosts.filter { it.id != postId },
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
                            trendingPosts = state.trendingPosts.filter { it.author.id != userId },
                            popularPosts = state.popularPosts.filter { it.author.id != userId },
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

    // 공유 처리
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

    private fun navigateToChatRoom(roomId: Int) = intent {
        reduce { state.copy(navigateRoomId = null) }
        postSideEffect(ExploreSideEffect.NavigateToChatRoom(roomId))
    }
}
