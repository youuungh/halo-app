package com.ninezero.halo.feature.profile.presentation.follow_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.feature.feed.domain.event.FollowEvent
import com.ninezero.halo.feature.feed.domain.event.FollowEventBus
import com.ninezero.halo.feature.feed.domain.usecase.ToggleAuthorFollowUseCase
import com.ninezero.halo.feature.profile.domain.repository.FollowRepository
import com.ninezero.halo.feature.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val PAGE_SIZE = 20

@OptIn(FlowPreview::class)
@HiltViewModel
class FollowListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val followRepository: FollowRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val toggleAuthorFollowUseCase: ToggleAuthorFollowUseCase,
    private val followEventBus: FollowEventBus
) : ViewModel(), OrbitContainerHost<FollowListState, FollowListState, FollowListSideEffect> {

    private val userId: Int = savedStateHandle.get<Int>("userId") ?: 0
    private val displayName: String = savedStateHandle.get<String>("displayName") ?: ""
    private val initialTab: Int = savedStateHandle.get<Int>("initialTab") ?: 0
    private val followerCount: Int = savedStateHandle.get<Int>("followerCount") ?: 0
    private val followingCount: Int = savedStateHandle.get<Int>("followingCount") ?: 0
    private val subscriberCount: Int = savedStateHandle.get<Int>("subscriberCount") ?: 0
    private val isMyProfile: Boolean = savedStateHandle.get<Boolean>("isMyProfile") ?: false
    private val showSubscribers: Boolean = savedStateHandle.get<Boolean>("showSubscribers") ?: false

    private val followersSearchQueryFlow = MutableStateFlow(SearchInput(""))
    private val followingSearchQueryFlow = MutableStateFlow(SearchInput(""))
    private val subscribersSearchQueryFlow = MutableStateFlow(SearchInput(""))

    private val followersGeneration = ListGeneration()
    private val followingGeneration = ListGeneration()
    private val subscribersGeneration = ListGeneration()

    // 지우기는 대기 없이 바로, 같은 검색어도 다시 받음
    private data class SearchInput(val query: String, val immediate: Boolean = false)

    override val container: OrbitContainer<FollowListState, FollowListState, FollowListSideEffect> =
        orbitContainer(
            FollowListState(
                displayName = displayName,
                isMyProfile = isMyProfile,
                selectedTab = when (initialTab) {
                    1 -> FollowTab.FOLLOWING
                    2 -> FollowTab.SUBSCRIBERS
                    else -> FollowTab.FOLLOWERS
                },
                followerCount = followerCount,
                followingCount = followingCount,
                subscriberCount = subscriberCount,
                showSubscribers = showSubscribers
            )
        )

    init {
        loadInitialData()
        setupDebouncedSearch()
        observeFollowEvents()
    }

    private fun observeFollowEvents() {
        followEventBus.events
            .onEach { event ->
                intent {
                    val newIsFollowing = event is FollowEvent.Followed
                    val targetId = event.userId
                    val updatedFollowers = state.followers.map { user ->
                        if (user.id == targetId) user.copy(isFollowing = newIsFollowing) else user
                    }
                    val updatedFollowing = state.following.map { user ->
                        if (user.id == targetId) user.copy(isFollowing = newIsFollowing) else user
                    }
                    val updatedSubscribers = state.subscribers.map { user ->
                        if (user.id == targetId) user.copy(isFollowing = newIsFollowing) else user
                    }
                    if (updatedFollowers != state.followers ||
                        updatedFollowing != state.following ||
                        updatedSubscribers != state.subscribers
                    ) {
                        reduce {
                            state.copy(
                                followers = updatedFollowers,
                                following = updatedFollowing,
                                subscribers = updatedSubscribers
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun setupDebouncedSearch() {
        // 조합 중 단일 자모 쿼리는 검색 생략
        followersSearchQueryFlow
            .drop(1)  // 첫 빈 검색어는 초기 로드가 이미 받음
            .debounce { if (it.immediate) 0L else 300L }
            .distinctUntilChanged { old, new -> !new.immediate && old.query == new.query }
            .filter { it.query.isBlank() || it.query.isValidSearchQuery() }
            .onEach { intent { loadFollowers(reset = true) } }
            .launchIn(viewModelScope)

        followingSearchQueryFlow
            .drop(1)  // 첫 빈 검색어는 초기 로드가 이미 받음
            .debounce { if (it.immediate) 0L else 300L }
            .distinctUntilChanged { old, new -> !new.immediate && old.query == new.query }
            .filter { it.query.isBlank() || it.query.isValidSearchQuery() }
            .onEach { intent { loadFollowing(reset = true) } }
            .launchIn(viewModelScope)

        subscribersSearchQueryFlow
            .drop(1)  // 첫 빈 검색어는 초기 로드가 이미 받음
            .debounce { if (it.immediate) 0L else 300L }
            .distinctUntilChanged { old, new -> !new.immediate && old.query == new.query }
            .filter { it.query.isBlank() || it.query.isValidSearchQuery() }
            .onEach { intent { loadSubscribers(reset = true) } }
            .launchIn(viewModelScope)
    }

    private fun String.isValidSearchQuery(): Boolean {
        val trimmed = trim()
        if (trimmed.isBlank()) return false
        return !trimmed.matches(KOREAN_JAMO_ONLY)
    }

    companion object {
        private val KOREAN_JAMO_ONLY = Regex("^[ㄱ-ㅎㅏ-ㅣ]+$")
    }

    fun onIntent(intent: FollowListIntent) {
        when (intent) {
            is FollowListIntent.NavigateBack -> navigateBack()
            is FollowListIntent.SelectTab -> selectTab(intent.tab)
            is FollowListIntent.UpdateSearchQuery -> updateSearchQuery(intent.query)
            is FollowListIntent.Search -> search()
            is FollowListIntent.ClearSearch -> clearSearch()
            is FollowListIntent.LoadMoreFollowers -> loadMoreFollowers()
            is FollowListIntent.LoadMoreFollowing -> loadMoreFollowing()
            is FollowListIntent.LoadMoreSubscribers -> loadMoreSubscribers()
            is FollowListIntent.ToggleFollow -> toggleFollow(intent.userId)
            is FollowListIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is FollowListIntent.NavigateToMessage -> navigateToMessage(intent.userId)
            is FollowListIntent.ShowUnfollowConfirmDialog -> showUnfollowConfirmDialog(intent.user)
            is FollowListIntent.ConfirmUnfollow -> confirmUnfollow()
            is FollowListIntent.DismissUnfollowConfirmDialog -> dismissUnfollowConfirmDialog()
            is FollowListIntent.Retry -> retry()
            is FollowListIntent.DismissToast -> dismissToast()
            is FollowListIntent.SnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun loadInitialData() = intent {
        reduce { state.copy(isInitialLoading = true) }

        when (state.selectedTab) {
            FollowTab.FOLLOWERS -> loadFollowers(reset = true)
            FollowTab.FOLLOWING -> loadFollowing(reset = true)
            FollowTab.SUBSCRIBERS -> loadSubscribers(reset = true)
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(FollowListSideEffect.NavigateBack)
    }

    private fun selectTab(tab: FollowTab) = intent {
        if (state.selectedTab == tab) return@intent

        reduce { state.copy(selectedTab = tab) }

        when (tab) {
            FollowTab.FOLLOWERS -> {
                if (state.followers.isEmpty() && !state.isFollowersLoading) {
                    loadFollowers(reset = true)
                }
            }

            FollowTab.FOLLOWING -> {
                if (state.following.isEmpty() && !state.isFollowingLoading) {
                    loadFollowing(reset = true)
                }
            }

            FollowTab.SUBSCRIBERS -> {
                if (state.subscribers.isEmpty() && !state.isSubscribersLoading) {
                    loadSubscribers(reset = true)
                }
            }
        }
    }

    private fun updateSearchQuery(query: String) = blockingIntent {
        when (state.selectedTab) {
            FollowTab.FOLLOWERS -> {
                reduce { state.copy(followersSearchQuery = query) }
                followersSearchQueryFlow.value = SearchInput(query)
            }

            FollowTab.FOLLOWING -> {
                reduce { state.copy(followingSearchQuery = query) }
                followingSearchQueryFlow.value = SearchInput(query)
            }

            FollowTab.SUBSCRIBERS -> {
                reduce { state.copy(subscribersSearchQuery = query) }
                subscribersSearchQueryFlow.value = SearchInput(query)
            }
        }
    }

    private fun search() = intent {
        // 검색 버튼은 즉시 검색
        when (state.selectedTab) {
            FollowTab.FOLLOWERS -> loadFollowers(reset = true)
            FollowTab.FOLLOWING -> loadFollowing(reset = true)
            FollowTab.SUBSCRIBERS -> loadSubscribers(reset = true)
        }
    }

    private fun clearSearch() = intent {
        when (state.selectedTab) {
            FollowTab.FOLLOWERS -> {
                reduce { state.copy(followersSearchQuery = "") }
                followersSearchQueryFlow.value = SearchInput("", immediate = true)  // 기다리던 검색을 이것으로 대체
            }

            FollowTab.FOLLOWING -> {
                reduce { state.copy(followingSearchQuery = "") }
                followingSearchQueryFlow.value = SearchInput("", immediate = true)  // 기다리던 검색을 이것으로 대체
            }

            FollowTab.SUBSCRIBERS -> {
                reduce { state.copy(subscribersSearchQuery = "") }
                subscribersSearchQueryFlow.value = SearchInput("", immediate = true)  // 기다리던 검색을 이것으로 대체
            }
        }
    }

    private fun loadFollowers(reset: Boolean) = intent {
        // 새 검색어는 기다리지 않음, 늦은 응답은 버림
        if (!reset && state.isFollowersLoading) return@intent
        if (!reset && !state.followersHasMore) return@intent

        val cursor = if (reset) null else state.followersNextCursor
        val generation = if (reset) followersGeneration.next() else followersGeneration.current()  // 처음부터 받으면 진행 중이던 더보기 버림
        reduce { state.copy(isFollowersLoading = true, followersError = null) }

        val query = state.followersSearchQuery.takeIf { it.isNotBlank() }
        fun isStale() = followersGeneration.current() != generation ||
            state.followersSearchQuery.takeIf { it.isNotBlank() } != query
        // 검색어만 바뀌어 버릴 땐 새 요청이 없어 로딩 표시를 직접 내림
        suspend fun dropIfStale(): Boolean {
            if (!isStale()) return false
            if (followersGeneration.current() == generation) {
                reduce { state.copy(isFollowersLoading = false, isInitialLoading = if (reset) false else state.isInitialLoading) }
            }
            return true
        }
        val result = if (state.isMyProfile) {
            followRepository.getMyFollowersWithSearch(cursor, PAGE_SIZE, query)
        } else {
            followRepository.getFollowersWithSearch(userId, cursor, PAGE_SIZE, query)
        }

        val isSearching = query != null
        result.onSuccess { listResult ->
            if (dropIfStale()) return@intent
            reduce {
                state.copy(
                    // 서버 페이징 중복 행 제거, 중복 key는 크래시
                    followers = if (reset) listResult.items else (state.followers + listResult.items).distinctBy { it.id },
                    followersNextCursor = listResult.nextCursor,
                    followersHasMore = listResult.nextCursor != null,
                    isFollowersLoading = false,
                    isInitialLoading = if (reset) false else state.isInitialLoading,
                    // 검색 중에는 탭 카운트가 검색 결과 개수로 바뀌지 않도록 유지
                    followerCount = if (isSearching) state.followerCount else listResult.totalCount,
                    followersError = null
                )
            }
        }.onFailure { error ->
            if (dropIfStale()) return@intent
            reduce {
                state.copy(
                    isFollowersLoading = false,
                    isInitialLoading = if (reset) false else state.isInitialLoading,
                    followersError = if (state.followers.isEmpty()) error.message
                        ?: "팔로워 목록을 불러올 수 없습니다" else null,
                    snackbarState = if (state.followers.isNotEmpty()) SnackbarState(
                        error.message ?: "더 불러오기 실패"
                    ) else null
                )
            }
        }
    }

    private fun loadFollowing(reset: Boolean) = intent {
        // 새 검색어는 기다리지 않음, 늦은 응답은 버림
        if (!reset && state.isFollowingLoading) return@intent
        if (!reset && !state.followingHasMore) return@intent

        val cursor = if (reset) null else state.followingNextCursor
        val generation = if (reset) followingGeneration.next() else followingGeneration.current()  // 처음부터 받으면 진행 중이던 더보기 버림
        reduce { state.copy(isFollowingLoading = true, followingError = null) }

        val query = state.followingSearchQuery.takeIf { it.isNotBlank() }
        fun isStale() = followingGeneration.current() != generation ||
            state.followingSearchQuery.takeIf { it.isNotBlank() } != query
        // 검색어만 바뀌어 버릴 땐 새 요청이 없어 로딩 표시를 직접 내림
        suspend fun dropIfStale(): Boolean {
            if (!isStale()) return false
            if (followingGeneration.current() == generation) {
                reduce { state.copy(isFollowingLoading = false, isInitialLoading = if (reset) false else state.isInitialLoading) }
            }
            return true
        }
        val result = if (state.isMyProfile) {
            followRepository.getMyFollowingWithSearch(cursor, PAGE_SIZE, query)
        } else {
            followRepository.getFollowingWithSearch(userId, cursor, PAGE_SIZE, query)
        }

        val isSearching = query != null
        result.onSuccess { listResult ->
            if (dropIfStale()) return@intent
            reduce {
                state.copy(
                    following = if (reset) listResult.items else (state.following + listResult.items).distinctBy { it.id },
                    followingNextCursor = listResult.nextCursor,
                    followingHasMore = listResult.nextCursor != null,
                    isFollowingLoading = false,
                    isInitialLoading = if (reset) false else state.isInitialLoading,
                    // 검색 중에는 탭 카운트가 검색 결과 개수로 바뀌지 않도록 유지
                    followingCount = if (isSearching) state.followingCount else listResult.totalCount,
                    followingError = null
                )
            }
        }.onFailure { error ->
            if (dropIfStale()) return@intent
            reduce {
                state.copy(
                    isFollowingLoading = false,
                    isInitialLoading = if (reset) false else state.isInitialLoading,
                    followingError = if (state.following.isEmpty()) error.message
                        ?: "팔로잉 목록을 불러올 수 없습니다" else null,
                    snackbarState = if (state.following.isNotEmpty()) SnackbarState(
                        error.message ?: "더 불러오기 실패"
                    ) else null
                )
            }
        }
    }

    private fun loadMoreFollowers() = intent {
        loadFollowers(reset = false)
    }

    private fun loadMoreFollowing() = intent {
        loadFollowing(reset = false)
    }

    private fun loadSubscribers(reset: Boolean) = intent {
        if (!state.showSubscribers) return@intent  // 구독자 탭은 크리에이터 본인만
        // 새 검색어는 기다리지 않음, 늦은 응답은 버림
        if (!reset && state.isSubscribersLoading) return@intent
        if (!reset && !state.subscribersHasMore) return@intent

        val page = if (reset) 1 else state.subscribersPage
        val generation = if (reset) subscribersGeneration.next() else subscribersGeneration.current()  // 처음부터 받으면 진행 중이던 더보기 버림
        reduce { state.copy(isSubscribersLoading = true, subscribersError = null) }

        val query = state.subscribersSearchQuery.takeIf { it.isNotBlank() }
        fun isStale() = subscribersGeneration.current() != generation ||
            state.subscribersSearchQuery.takeIf { it.isNotBlank() } != query
        // 검색어만 바뀌어 버릴 땐 새 요청이 없어 로딩 표시를 직접 내림
        suspend fun dropIfStale(): Boolean {
            if (!isStale()) return false
            if (subscribersGeneration.current() == generation) {
                reduce { state.copy(isSubscribersLoading = false, isInitialLoading = if (reset) false else state.isInitialLoading) }
            }
            return true
        }
        val result = subscriptionRepository.getMySubscribers(page, PAGE_SIZE, query)

        val isSearching = query != null
        result.onSuccess { listResult ->
            if (dropIfStale()) return@intent
            reduce {
                state.copy(
                    subscribers = if (reset) listResult.items else (state.subscribers + listResult.items).distinctBy { it.id },
                    subscribersPage = page + 1,
                    subscribersHasMore = listResult.hasNext,
                    isSubscribersLoading = false,
                    isInitialLoading = if (reset) false else state.isInitialLoading,
                    // 검색 중에는 탭 카운트가 검색 결과 개수로 바뀌지 않도록 유지
                    subscriberCount = if (isSearching) state.subscriberCount else listResult.totalCount,
                    subscribersError = null
                )
            }
        }.onFailure { error ->
            if (dropIfStale()) return@intent
            reduce {
                state.copy(
                    isSubscribersLoading = false,
                    isInitialLoading = if (reset) false else state.isInitialLoading,
                    subscribersError = if (state.subscribers.isEmpty()) error.message
                        ?: "구독자 목록을 불러올 수 없습니다" else null,
                    snackbarState = if (state.subscribers.isNotEmpty()) SnackbarState(
                        error.message ?: "더 불러오기 실패"
                    ) else null
                )
            }
        }
    }

    private fun loadMoreSubscribers() = intent {
        loadSubscribers(reset = false)
    }

    private fun toggleFollow(targetUserId: Int) = intent {
        if (state.toggleLoadingUserIds.contains(targetUserId)) return@intent

        val currentUser = state.followers.find { it.id == targetUserId }
            ?: state.following.find { it.id == targetUserId }
            ?: state.subscribers.find { it.id == targetUserId }
            ?: return@intent
        val shouldFollow = !currentUser.isFollowing

        reduce { state.copy(toggleLoadingUserIds = state.toggleLoadingUserIds + targetUserId) }

        toggleAuthorFollowUseCase(targetUserId, shouldFollow)
            .onSuccess {
                reduce { state.copy(toggleLoadingUserIds = state.toggleLoadingUserIds - targetUserId) }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        toggleLoadingUserIds = state.toggleLoadingUserIds - targetUserId,
                        snackbarState = SnackbarState(error.message ?: "팔로우 상태를 변경할 수 없습니다")
                    )
                }
            }
    }

    private fun showUnfollowConfirmDialog(user: UserSummary) = intent {
        reduce {
            state.copy(
                showUnfollowConfirmDialog = true,
                unfollowTargetUser = user
            )
        }
    }

    private fun dismissUnfollowConfirmDialog() = intent {
        reduce {
            state.copy(
                showUnfollowConfirmDialog = false,
                unfollowTargetUser = null
            )
        }
    }

    private fun confirmUnfollow() = intent {
        val targetUser = state.unfollowTargetUser ?: return@intent
        val targetUserId = targetUser.id

        reduce {
            state.copy(
                showUnfollowConfirmDialog = false,
                unfollowTargetUser = null,
                toggleLoadingUserIds = state.toggleLoadingUserIds + targetUserId
            )
        }

        toggleAuthorFollowUseCase(targetUserId, shouldFollow = false)
            .onSuccess {
                reduce {
                    state.copy(
                        followingCount = (state.followingCount - 1).coerceAtLeast(0),
                        toggleLoadingUserIds = state.toggleLoadingUserIds - targetUserId,
                        toastMessage = "팔로우를 취소했습니다"
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        toggleLoadingUserIds = state.toggleLoadingUserIds - targetUserId,
                        snackbarState = SnackbarState(error.message ?: "팔로우를 취소할 수 없습니다")
                    )
                }
            }
    }

    private fun navigateToUserProfile(targetUserId: Int) = intent {
        postSideEffect(FollowListSideEffect.NavigateToUserProfile(targetUserId))
    }

    private fun navigateToMessage(targetUserId: Int) = intent {
        val user = (state.followers + state.following + state.subscribers)
            .firstOrNull { it.id == targetUserId }
        postSideEffect(
            FollowListSideEffect.NavigateToNewChat(
                targetUserId = targetUserId,
                username = user?.username ?: "",
                displayName = user?.displayName ?: "",
                avatarUrl = (user?.avatarThumbUrl ?: user?.avatarUrl) ?: "",
                role = user?.role ?: "USER"
            )
        )
    }

    private fun retry() = intent {
        when (state.selectedTab) {
            FollowTab.FOLLOWERS -> loadFollowers(reset = true)
            FollowTab.FOLLOWING -> loadFollowing(reset = true)
            FollowTab.SUBSCRIBERS -> loadSubscribers(reset = true)
        }
    }

    private fun dismissToast() = intent {
        reduce { state.copy(toastMessage = null) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }
}
