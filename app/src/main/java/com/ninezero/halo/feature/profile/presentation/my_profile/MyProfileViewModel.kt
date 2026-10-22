package com.ninezero.halo.feature.profile.presentation.my_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.core.util.mergeFreshHead
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.delegate.PostShareDelegate
import com.ninezero.halo.feature.feed.domain.event.FollowEvent
import com.ninezero.halo.feature.feed.domain.event.FollowEventBus
import com.ninezero.halo.feature.feed.domain.model.CachedCommentsTracker
import com.ninezero.halo.feature.feed.domain.model.CachedPostsTracker
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.feed.domain.model.PostUploadEvent
import com.ninezero.halo.feature.feed.domain.model.syncWithCache
import com.ninezero.halo.feature.feed.domain.repository.PendingPostRepository
import com.ninezero.halo.feature.feed.domain.repository.PostUploadEventRepository
import com.ninezero.halo.feature.feed.domain.usecase.CacheCommentsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.DeleteCommentUseCase
import com.ninezero.halo.feature.feed.domain.usecase.DeletePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObserveCachedCommentsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObserveCachedPostsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.TogglePinPostUseCase
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.PostSummaryData
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.domain.model.ThreadCommentData
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.domain.model.applyFollow
import com.ninezero.halo.feature.profile.domain.model.mergeCached
import com.ninezero.halo.feature.profile.domain.model.syncWithCache
import com.ninezero.halo.feature.profile.domain.model.toCacheableFeedComments
import com.ninezero.halo.feature.profile.domain.model.updateComment
import com.ninezero.halo.feature.profile.domain.model.updatePost
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import com.ninezero.halo.feature.profile.domain.usecase.DeleteCommunityPostUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetCommunityPostsUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyCommentThreadsUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyPostsUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.ObserveMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.TogglePinCommunityPostUseCase
import com.ninezero.halo.feature.profile.presentation.components.ProductSortType
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.UserPostSortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val PAGE_SIZE = 10
private const val STORE_PAGE_SIZE = 20  // 스토어 Repository 기본값과 같음

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val observeMyProfileUseCase: ObserveMyProfileUseCase,
    private val getMyPostsUseCase: GetMyPostsUseCase,
    private val getMyCommentThreadsUseCase: GetMyCommentThreadsUseCase,
    private val observeCachedPostsUseCase: ObserveCachedPostsUseCase,
    private val observeCachedCommentsUseCase: ObserveCachedCommentsUseCase,
    private val cacheCommentsUseCase: CacheCommentsUseCase,
    private val postMutationDelegate: PostMutationDelegate,
    private val postShareDelegate: PostShareDelegate,
    private val deletePostUseCase: DeletePostUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val togglePinPostUseCase: TogglePinPostUseCase,
    private val deleteCommunityPostUseCase: DeleteCommunityPostUseCase,
    private val togglePinCommunityPostUseCase: TogglePinCommunityPostUseCase,
    private val profileRepository: ProfileRepository,
    private val getCommunityPostsUseCase: GetCommunityPostsUseCase,
    private val pendingPostRepository: PendingPostRepository,
    private val postUploadEventRepository: PostUploadEventRepository,
    private val followEventBus: FollowEventBus
) : ViewModel(), OrbitContainerHost<MyProfileState, MyProfileState, MyProfileSideEffect> {

    private val cachedPostsTracker = CachedPostsTracker()
    private val cachedCommentsTracker = CachedCommentsTracker()

    // 목록을 갈아끼울 때마다 올려 늦은 더보기를 버림
    private val postsGeneration = ListGeneration()
    private val repliesGeneration = ListGeneration()
    private val communityGeneration = ListGeneration()
    private val storeGeneration = ListGeneration()
    private val homeGeneration = ListGeneration()  // 홈 섹션은 받을 때마다 올림, 늦게 온 옛 응답은 버림

    // init보다 위에, 아래면 초기화 전에 쓰여 크래시
    private val initialTabStarted = AtomicBoolean(false)
    private var pendingPostsJob: Job? = null

    // 첫 프레임 빈 헤더 방지
    override val container: OrbitContainer<MyProfileState, MyProfileState, MyProfileSideEffect> =
        orbitContainer(MyProfileState(isLoading = true))

    init {
        observeCachedPosts()
        observeCachedComments()
        observeFollowEvents()
        observeMyProfile()
        loadProfile()
        observeUploadResults()
    }

    private fun observeFollowEvents() {
        followEventBus.events
            .onEach { event ->
                intent {
                    val authorId = event.userId
                    val isFollowing = event is FollowEvent.Followed
                    val updatedThreads = state.myCommentThreads.map { it.applyFollow(authorId, isFollowing) }
                    val updatedSelectedComment = state.selectedComment?.applyFollow(authorId, isFollowing)
                    val updatedSelectedCommentThread = state.selectedCommentThread?.applyFollow(authorId, isFollowing)
                    val updatedSelectedThreadPost = state.selectedThreadPost?.applyFollow(authorId, isFollowing)
                    // 캐시 반영을 기다리지 않고 커뮤니티 목록도 바로 갱신
                    val updatedCommunityPosts = state.communityPosts.map { it.applyFollow(authorId, isFollowing) }
                    val updatedCommunityPinnedPosts = state.communityPinnedPosts.map { it.applyFollow(authorId, isFollowing) }
                    if (
                        updatedThreads != state.myCommentThreads ||
                        updatedSelectedComment != state.selectedComment ||
                        updatedSelectedCommentThread != state.selectedCommentThread ||
                        updatedSelectedThreadPost != state.selectedThreadPost ||
                        updatedCommunityPosts != state.communityPosts ||
                        updatedCommunityPinnedPosts != state.communityPinnedPosts
                    ) {
                        reduce {
                            state.copy(
                                myCommentThreads = updatedThreads,
                                selectedComment = updatedSelectedComment,
                                selectedCommentThread = updatedSelectedCommentThread,
                                selectedThreadPost = updatedSelectedThreadPost,
                                communityPosts = updatedCommunityPosts,
                                communityPinnedPosts = updatedCommunityPinnedPosts
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMyProfile() {
        observeMyProfileUseCase()
            .onEach { profile ->
                if (profile == null) return@onEach
                intent { applyProfile(profile) }
            }
            .launchIn(viewModelScope)
    }

    // 캐시·조회 중 먼저 온 첫 프로필로 초기 탭을 한 번만 로드
    private suspend fun Syntax<MyProfileState, MyProfileSideEffect>.applyProfile(profile: UserProfile) {
        if (!initialTabStarted.compareAndSet(false, true)) {
            // 첫 조회가 실패한 뒤 도착해도 오류 화면에 남지 않게
            reduce { if (state.profile == profile) state else state.copy(profile = profile, error = null) }
            return
        }
        // 크리에이터·관리자는 HOME, 일반은 POSTS 먼저
        val initialTab = if (profile.isCreator) ProfileTab.HOME else ProfileTab.POSTS
        reduce {
            state.copy(
                profile = state.profile ?: profile,  // 더 새 프로필이 먼저 들어왔으면 그대로
                error = null,
                selectedTab = initialTab,
                // 탭 로딩을 같은 reduce에서 켜 빈 화면 깜빡임 방지
                isHomeSectionsLoading = initialTab == ProfileTab.HOME,
                isPostsLoading = initialTab == ProfileTab.POSTS
            )
        }
        if (initialTab == ProfileTab.HOME) loadHomeSections() else loadMyPosts()
        observePendingPosts(profile.userId)
    }

    // 헤더 수치를 서버 값으로, 반영은 observeMyProfile
    private fun syncProfile() = intent {
        getMyProfileUseCase()
    }

    private fun observeCachedPosts() {
        observeCachedPostsUseCase()
            .onEach { cachedPosts ->
                // 반영은 reduce 안에서, 동시 갱신 유실 방지
                val (cachedPostsMap, previousCachedIds) = cachedPostsTracker.track(cachedPosts)
                intent {
                    reduce {
                        state.copy(
                            myPosts = state.myPosts.syncWithCache(cachedPostsMap, previousCachedIds),
                            pinnedPosts = state.pinnedPosts.syncWithCache(cachedPostsMap, previousCachedIds),
                            postSections = state.postSections.syncWithCache(cachedPostsMap, previousCachedIds),
                            communityPosts = state.communityPosts.syncWithCache(cachedPostsMap, previousCachedIds),
                            communityPinnedPosts = state.communityPinnedPosts.syncWithCache(cachedPostsMap, previousCachedIds),
                            selectedPost = state.selectedPost?.syncWithCache(cachedPostsMap, previousCachedIds)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // 답글 탭 댓글 상태 자동 반영
    private fun observeCachedComments() {
        observeCachedCommentsUseCase()
            .onEach { cachedComments ->
                // 반영은 reduce 안에서, 동시 갱신 유실 방지
                val previousCachedCommentIds = cachedCommentsTracker.track(cachedComments.keys)
                intent {
                    reduce {
                        if (
                            state.myCommentThreads.isEmpty() &&
                            state.selectedComment == null &&
                            state.selectedCommentThread == null
                        ) {
                            return@reduce state
                        }
                        val authorFollowMap = cachedComments.values.associate { comment ->
                            comment.author.id to comment.author.isFollowing
                        }
                        val updatedThreads = state.myCommentThreads.mapNotNull { thread ->
                            val updatedComments = thread.comments.mapNotNull { comment ->
                                val cached = cachedComments[comment.id]
                                when {
                                    cached != null -> comment.mergeCached(cached)
                                    comment.id in previousCachedCommentIds -> null
                                    else -> comment
                                }
                            }
                            if (updatedComments.none { it.isProfileUser }) {  // 내 댓글이 안 남음
                                null
                            } else {
                                thread.copy(
                                    post = thread.post.copy(
                                        author = thread.post.author.copy(
                                            isFollowing = authorFollowMap[thread.post.author.id]
                                                ?: thread.post.author.isFollowing
                                        )
                                    ),
                                    comments = updatedComments
                                )
                            }
                        }
                        val updatedSelectedComment = state.selectedComment?.let { selected ->
                            val cached = cachedComments[selected.id]
                            when {
                                cached != null -> selected.mergeCached(cached)
                                selected.id in previousCachedCommentIds -> null
                                else -> selected
                            }
                        }
                        val updatedSelectedCommentThread = state.selectedCommentThread?.let { thread ->
                            val updatedComments = thread.comments.mapNotNull { comment ->
                                val cached = cachedComments[comment.id]
                                when {
                                    cached != null -> comment.mergeCached(cached)
                                    comment.id in previousCachedCommentIds -> null
                                    else -> comment
                                }
                            }
                            if (updatedComments.none { it.isProfileUser }) {  // 내 댓글이 안 남음
                                null
                            } else {
                                thread.copy(
                                    post = thread.post.copy(
                                        author = thread.post.author.copy(
                                            isFollowing = authorFollowMap[thread.post.author.id]
                                                ?: thread.post.author.isFollowing
                                        )
                                    ),
                                    comments = updatedComments
                                )
                            }
                        }
                        val updatedSelectedThreadPost = state.selectedThreadPost?.let { post ->
                            post.copy(
                                author = post.author.copy(
                                    isFollowing = authorFollowMap[post.author.id] ?: post.author.isFollowing
                                )
                            )
                        }
                        state.copy(
                            myCommentThreads = updatedThreads,
                            selectedComment = updatedSelectedComment,
                            selectedCommentThread = updatedSelectedCommentThread,
                            selectedThreadPost = updatedSelectedThreadPost
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun cacheThreadComments(threads: List<CommentThreadData>) {
        val comments = threads
            .flatMap { it.toCacheableFeedComments() }
            .distinctBy { it.id }
        if (comments.isNotEmpty()) {
            cacheCommentsUseCase(comments)
        }
    }

    private fun observePendingPosts(authorId: Int) {
        pendingPostsJob?.cancel()  // 다시 불려도 구독은 하나
        pendingPostsJob = viewModelScope.launch {
            pendingPostRepository.getPendingPostsByAuthor(authorId)
                .onEach { pendingPosts ->
                    intent {
                        reduce { state.copy(pendingPosts = pendingPosts) }
                    }
                }
                .launchIn(this)
            // 커뮤니티 탭 FAB로 쓴 내 커뮤니티 글
            pendingPostRepository.getPendingCommunityPosts(authorId, creatorId = authorId)
                .onEach { pending ->
                    intent {
                        reduce { state.copy(pendingCommunityPosts = pending) }
                    }
                }
                .launchIn(this)
        }
    }

    private fun observeUploadResults() {
        postUploadEventRepository.uploadEvents
            .onEach { event ->
                when (event) {
                    is PostUploadEvent.Success -> {
                        // 성공 시 새로고침, 커뮤니티 글 안내는 이 화면
                        if (event.creatorId == null) {
                            loadMyPosts()
                            syncProfile()
                        } else if (event.creatorId == container.stateFlow.value.profile?.userId) {
                            intent { reduce { state.copy(snackbarState = SnackbarState("게시됨")) } }
                            loadCommunityPosts(quiet = true)
                            syncProfile()
                        }
                    }
                    is PostUploadEvent.Failure -> {
                        // 일반 글 실패는 Main/Feed에서 처리
                        if (event.creatorId != null && event.creatorId == container.stateFlow.value.profile?.userId) {
                            intent { reduce { state.copy(snackbarState = SnackbarState("게시 실패")) } }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: MyProfileIntent) {
        when (intent) {
            is MyProfileIntent.LoadProfile -> loadProfile()
            is MyProfileIntent.RefreshProfile -> refreshProfile()
            is MyProfileIntent.RefreshOnResume -> refreshOnResume()
            is MyProfileIntent.NavigateToEditProfile -> navigateToEditProfile()
            is MyProfileIntent.NavigateToSettings -> navigateToSettings()
            is MyProfileIntent.NavigateToMembershipManage -> navigateToMembershipManage()
            is MyProfileIntent.NavigateToMySubscriptions -> navigateToMySubscriptions()
            is MyProfileIntent.NavigateToPoint -> navigateToPoint()
            is MyProfileIntent.NavigateToCoupon -> navigateToCoupon()
            is MyProfileIntent.NavigateToCouponManage -> navigateToCouponManage()
            is MyProfileIntent.NavigateToMyReviews -> navigateToMyReviews()
            is MyProfileIntent.NavigateToAddress -> navigateToAddress()
            is MyProfileIntent.NavigateToWishlist -> navigateToWishlist()
            is MyProfileIntent.NavigateToCart -> navigateToCart()
            is MyProfileIntent.NavigateToMyOrders -> navigateToMyOrders()
            is MyProfileIntent.NavigateToMyProducts -> navigateToMyProducts()
            is MyProfileIntent.NavigateToSalesManage -> navigateToSalesManage()
            is MyProfileIntent.SnackbarDismissed -> dismissSnackbar()
            // 탭 관련
            is MyProfileIntent.SelectTab -> selectTab(intent.tab)
            // 포스트 탭
            is MyProfileIntent.LoadMorePosts -> loadMorePosts()
            is MyProfileIntent.ChangePostsSortType -> changePostsSortType(intent.sortType)
            is MyProfileIntent.ChangePostsContextType -> changePostsContextType(intent.contextType)
            is MyProfileIntent.LikePost -> likePost(intent.postId)
            is MyProfileIntent.UnlikePost -> unlikePost(intent.postId)
            is MyProfileIntent.NavigateToPostDetail -> navigateToPostDetail(intent.postId)
            is MyProfileIntent.ShowPostOptions -> showPostOptions(intent.post)
            is MyProfileIntent.DismissPostOptions -> dismissPostOptions()
            is MyProfileIntent.DeletePost -> deletePost()
            is MyProfileIntent.EditPost -> editPost()
            is MyProfileIntent.PinPost -> pinPost()
            is MyProfileIntent.BookmarkPost -> bookmarkPost()
            is MyProfileIntent.BlockPostAuthor -> blockPostAuthor()
            // 답글 탭
            is MyProfileIntent.LoadMoreCommentThreads -> loadMoreCommentThreads()
            is MyProfileIntent.ChangeRepliesSortType -> changeRepliesSortType(intent.sortType)
            is MyProfileIntent.ChangeRepliesContextType -> changeRepliesContextType(intent.contextType)
            is MyProfileIntent.LikeComment -> likeComment(intent.commentId)
            is MyProfileIntent.UnlikeComment -> unlikeComment(intent.commentId)
            is MyProfileIntent.ToggleThreadPostLike -> toggleThreadPostLike(intent.postId)
            is MyProfileIntent.NavigateToCommentDetail -> navigateToCommentDetail(intent.commentId)
            is MyProfileIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is MyProfileIntent.ShowDeletedPostSnackbar -> showDeletedPostSnackbar()
            is MyProfileIntent.ShowHiddenPostSnackbar -> showHiddenPostSnackbar()
            // 팔로우
            is MyProfileIntent.FollowUser -> followUser(intent.userId)
            is MyProfileIntent.UnfollowUser -> unfollowUser(intent.userId)
            // 댓글 옵션 시트
            is MyProfileIntent.ShowCommentOptions -> showCommentOptions(intent.comment, intent.thread)
            is MyProfileIntent.DismissCommentOptions -> dismissCommentOptions()
            is MyProfileIntent.DeleteComment -> deleteComment()
            is MyProfileIntent.BookmarkComment -> bookmarkComment()
            is MyProfileIntent.BlockCommentAuthor -> blockCommentAuthor()
            // 스레드 포스트 옵션 시트
            is MyProfileIntent.ShowThreadPostOptions -> showThreadPostOptions(intent.post)
            is MyProfileIntent.DismissThreadPostOptions -> dismissThreadPostOptions()
            is MyProfileIntent.BookmarkThreadPost -> bookmarkThreadPost()
            is MyProfileIntent.BlockThreadPostAuthor -> blockThreadPostAuthor()
            // 홈 탭, Creator 전용
            is MyProfileIntent.NavigateToSectionDetail -> navigateToSectionDetail(
                intent.sectionType,
                intent.tagId,
                intent.title
            )
            is MyProfileIntent.NavigateToProductDetail -> navigateToProductDetail(intent.productId)
            // 커뮤니티 탭, Creator 전용
            is MyProfileIntent.LoadCommunityPosts -> loadCommunityPosts()
            is MyProfileIntent.LoadMoreCommunityPosts -> loadMoreCommunityPosts()
            is MyProfileIntent.LikeCommunityPost -> likeCommunityPost(intent.postId)
            is MyProfileIntent.ShowCommunityPostOptions -> showCommunityPostOptions(intent.post)
            is MyProfileIntent.EditCommunityPost -> editCommunityPost()
            is MyProfileIntent.DeleteCommunityPost -> deleteCommunityPost()
            is MyProfileIntent.PinCommunityPost -> pinCommunityPost()
            is MyProfileIntent.CreateCommunityPost -> createCommunityPost()
            // 공유
            is MyProfileIntent.SharePost -> sharePost(intent.postId)
            is MyProfileIntent.DismissShareSheet -> dismissShareSheet()
            is MyProfileIntent.ShareCompleted -> shareCompleted(intent.sharedRoomIds, intent.recipientCount)
            is MyProfileIntent.NavigateToChatRoom -> navigateToChatRoom(intent.roomId)
            // 스토어 탭, Creator 전용
            is MyProfileIntent.LoadStoreProducts -> loadStoreProducts()
            is MyProfileIntent.LoadMoreStoreProducts -> loadMoreStoreProducts()
            is MyProfileIntent.ChangeStoreSortType -> changeStoreSortType(intent.sortType)
        }
    }

    private fun loadProfile() = intent {
        reduce { state.copy(isLoading = true, error = null) }

        getMyProfileUseCase()
            .onSuccess { profile ->
                // 캐시가 먼저 왔으면 고른 탭 유지
                applyProfile(profile)
                reduce { state.copy(isLoading = false, error = null) }
            }
            .onFailure { error ->
                val message = error.message ?: "프로필을 불러올 수 없습니다"
                reduce { state.copy(isLoading = false, error = message, snackbarState = SnackbarState(message)) }
            }
    }

    private fun refreshProfile() = intent {
        reduce { state.copy(isRefreshing = true) }

        getMyProfileUseCase()
            .onSuccess { profile ->
                val currentTab = state.selectedTab
                reduce {
                    state.copy(
                        profile = profile,
                        isRefreshing = false
                    )
                }

                when (currentTab) {
                    ProfileTab.POSTS -> loadMyPosts()
                    ProfileTab.REPLIES -> loadMyCommentThreads()
                    ProfileTab.HOME -> loadHomeSections()
                    ProfileTab.COMMUNITY -> loadCommunityPosts()
                    ProfileTab.STORE -> loadStoreProducts()
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isRefreshing = false,
                        snackbarState = SnackbarState(error.message ?: "새로고침 실패")
                    )
                }
            }
    }

    // 화면 복귀, 표시 없이 현재 탭 갱신
    private fun refreshOnResume() = intent {
        if (state.isRefreshing || state.isLoading) return@intent
        if (state.profile == null) {  // 첫 로드 실패 화면, 다시 시도
            loadProfile()
            return@intent
        }
        syncProfile()
        when (state.selectedTab) {
            ProfileTab.POSTS -> refreshPostsQuietly()
            ProfileTab.REPLIES -> refreshCommentThreadsQuietly()
            // 홈 스켈레톤은 비었을 때만, 스켈레톤으로 남은 홈은 그대로
            ProfileTab.HOME -> if (!state.isHomeSectionsLoading) loadHomeSections(quiet = true)
            ProfileTab.COMMUNITY -> refreshCommunityPostsQuietly()
            ProfileTab.STORE -> reloadLoadedStoreProducts()
        }
    }

    private fun selectTab(tab: ProfileTab) = intent {
        if (state.selectedTab == tab) return@intent

        reduce { state.copy(selectedTab = tab) }

        when (tab) {
            ProfileTab.POSTS -> {
                if (state.myPosts.isEmpty()) loadMyPosts()
            }
            ProfileTab.REPLIES -> {
                if (state.myCommentThreads.isEmpty()) loadMyCommentThreads()
            }
            ProfileTab.HOME -> {
                if (state.pinnedPosts.isEmpty() && state.postSections.isEmpty() && state.productSections.isEmpty()) {
                    loadHomeSections()
                }
            }
            ProfileTab.COMMUNITY -> {
                if (state.communityPosts.isEmpty()) loadCommunityPosts()
            }
            ProfileTab.STORE -> {
                if (state.storeProducts.isEmpty() && state.storeProductSections.isEmpty()) loadStoreProducts()
            }
        }
    }

    // 포스트 탭

    private fun loadMyPosts() = intent {
        reduce { state.copy(isPostsLoading = true) }

        val sort = state.postsSortType
        val contextType = state.postsContextType
        getMyPostsUseCase(
            limit = PAGE_SIZE,
            sort = sort.apiValue,
            contextType = contextType?.apiValue
        )
            .onSuccess { (posts, nextCursor) ->
                if (state.postsSortType != sort || state.postsContextType != contextType) return@intent
                postsGeneration.next()
                reduce {
                    state.copy(
                        myPosts = posts,
                        isPostsLoading = false,
                        postsNextCursor = nextCursor,
                        postsHasMore = nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                // 받아 둔 글이 없으면 스켈레톤 그대로
                if (state.myPosts.isNotEmpty()) reduce { state.copy(isPostsLoading = false) }
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "포스트를 불러올 수 없습니다")) }
            }
    }

    private fun loadMorePosts(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (state.isPostsLoading || !state.postsHasMore) return@intent

        val cursor = state.postsNextCursor
        val generation = postsGeneration.current()
        reduce { state.copy(isPostsLoading = true) }

        val sort = state.postsSortType
        val contextType = state.postsContextType
        getMyPostsUseCase(
            cursor = cursor,
            limit = PAGE_SIZE,
            sort = sort.apiValue,
            contextType = contextType?.apiValue
        )
            .onSuccess { (posts, nextCursor) ->
                if (state.postsSortType != sort || state.postsContextType != contextType) return@intent
                if (postsGeneration.current() != generation) {
                    // 정렬을 바꿔 비운 목록이면 새 1페이지가 맡음
                    if (!state.isPostsLoading && state.myPosts.isNotEmpty()) loadMorePosts()  // 목록 수가 그대로면 트리거가 안 걸려 다시 요청
                    return@intent
                }
                reduce {
                    state.copy(
                        myPosts = (state.myPosts + posts).distinctBy { it.id },
                        isPostsLoading = false,
                        postsNextCursor = nextCursor,
                        postsHasMore = nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                if (postsGeneration.current() != generation) return@intent
                reduce {
                    state.copy(
                        isPostsLoading = false,
                        snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    // 첫 페이지만 받아 앞에 합침

    private fun refreshPostsQuietly() = intent {
        if (state.isPostsLoading) return@intent
        val sort = state.postsSortType
        val contextType = state.postsContextType
        getMyPostsUseCase(
            limit = PAGE_SIZE,
            sort = sort.apiValue,
            contextType = contextType?.apiValue
        ).onSuccess { (posts, nextCursor) ->
            if (state.postsSortType != sort || state.postsContextType != contextType) return@intent
            if (state.isPostsLoading) return@intent  // 그사이 더보기·새로고침이 시작됨
            reduce {
                val merged = state.myPosts.mergeFreshHead(posts) { it.id }
                // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                val keepsTail = merged.size > posts.size
                state.copy(
                    myPosts = merged,
                    postsHasMore = if (keepsTail) state.postsHasMore else nextCursor != null,
                    postsNextCursor = if (keepsTail) state.postsNextCursor else nextCursor
                )
            }
        }
    }

    private fun changePostsSortType(sortType: UserPostSortType) = intent {
        if (state.postsSortType == sortType) return@intent
        postsGeneration.next()  // 옛 정렬로 진행 중이던 더보기 버림
        reduce {
            state.copy(
                postsSortType = sortType,
                myPosts = emptyList(),
                postsNextCursor = null,
                postsHasMore = true
            )
        }
        loadMyPosts()
    }

    private fun changePostsContextType(contextType: PostContextType?) = intent {
        if (state.postsContextType == contextType) return@intent
        postsGeneration.next()  // 옛 범위로 진행 중이던 더보기 버림
        reduce {
            state.copy(
                postsContextType = contextType,
                myPosts = emptyList(),
                postsNextCursor = null,
                postsHasMore = true
            )
        }
        loadMyPosts()
    }

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

    private fun showPostOptions(post: Post) = intent {
        reduce { state.copy(showPostOptionsSheet = true, selectedPost = post) }
    }

    private fun dismissPostOptions() = intent {
        reduce { state.copy(showPostOptionsSheet = false, selectedPost = null) }
    }

    private fun deletePost() = intent {
        val postId = state.selectedPost?.id ?: return@intent
        dismissPostOptions()

        deletePostUseCase(postId)
            .onSuccess {
                reduce {
                    state.copy(
                        myPosts = state.myPosts.filter { it.id != postId },
                        snackbarState = SnackbarState("포스트가 삭제되었습니다")
                    )
                }
                syncProfile()
            }
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패")) }
            }
    }

    private fun editPost() = intent {
        val postId = state.selectedPost?.id ?: return@intent
        dismissPostOptions()
        postSideEffect(MyProfileSideEffect.NavigateToEditPost(postId))
    }

    private fun pinPost() = intent {
        val post = state.selectedPost ?: return@intent
        val shouldPin = !post.isPinned

        // 낙관적 갱신
        reduce {
            state.copy(
                showPostOptionsSheet = false,
                selectedPost = null,
                myPosts = state.myPosts.map { if (it.id == post.id) it.copy(isPinned = shouldPin) else it },
                snackbarState = SnackbarState(if (shouldPin) "고정됨" else "고정 해제됨")
            )
        }

        togglePinPostUseCase(post.id, shouldPin)
            .onSuccess {
                // 홈 탭 고정 줄은 서버 순서라 다시 받음
                if (state.pinnedPosts.isNotEmpty() || state.postSections.isNotEmpty() || state.productSections.isNotEmpty()) {
                    loadHomeSections()
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        myPosts = state.myPosts.map { if (it.id == post.id) it.copy(isPinned = !shouldPin) else it },
                        snackbarState = SnackbarState(error.message ?: "고정 처리 실패")
                    )
                }
            }
    }

    private fun navigateToPostDetail(postId: Int) = intent {
        postSideEffect(MyProfileSideEffect.NavigateToPostDetail(postId))
    }

    // 답글 탭

    private fun loadMyCommentThreads() = intent {
        reduce { state.copy(isRepliesLoading = true) }

        val sort = state.repliesSortType
        val contextType = state.repliesContextType
        getMyCommentThreadsUseCase(
            sort = sort.apiValue,
            contextType = contextType?.apiValue
        )
            .onSuccess { (threads, nextCursor) ->
                if (state.repliesSortType != sort || state.repliesContextType != contextType) return@intent
                cacheThreadComments(threads)
                repliesGeneration.next()
                // 다 걸러진 페이지면 이어 받는 동안 로딩 표시 유지
                val chain = threads.isEmpty() && nextCursor != null
                reduce {
                    state.copy(
                        myCommentThreads = threads.distinctBy { it.post.id to (it.comments.lastOrNull()?.id ?: 0) },
                        isRepliesLoading = chain,
                        repliesNextCursor = nextCursor,
                        repliesHasMore = nextCursor != null
                    )
                }
                if (chain) loadMoreCommentThreads(chained = true)
            }
            .onFailure { error ->
                // 받아 둔 답글이 없으면 스켈레톤 그대로
                if (state.myCommentThreads.isNotEmpty()) reduce { state.copy(isRepliesLoading = false) }
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "답글을 불러올 수 없습니다")) }
            }
    }

    // 다 걸러진 페이지 이어 받기면 로딩 가드 건너뜀

    private fun loadMoreCommentThreads(chained: Boolean = false): Job = intent {  // 자기를 다시 불러 타입 명시
        if ((state.isRepliesLoading && !chained) || !state.repliesHasMore) return@intent

        val cursor = state.repliesNextCursor
        val generation = repliesGeneration.current()
        reduce { state.copy(isRepliesLoading = true) }

        val sort = state.repliesSortType
        val contextType = state.repliesContextType
        getMyCommentThreadsUseCase(
            cursor = cursor,
            sort = sort.apiValue,
            contextType = contextType?.apiValue
        )
            .onSuccess { (threads, nextCursor) ->
                if (state.repliesSortType != sort || state.repliesContextType != contextType) return@intent
                if (repliesGeneration.current() != generation) {
                    // 정렬을 바꿔 비운 목록이면 새 1페이지가 맡음
                    if (!state.isRepliesLoading && state.myCommentThreads.isNotEmpty()) loadMoreCommentThreads()  // 목록 수가 그대로면 트리거가 안 걸려 다시 요청
                    return@intent
                }
                cacheThreadComments(threads)
                val chain = threads.isEmpty() && nextCursor != null
                reduce {
                    state.copy(
                        myCommentThreads = (state.myCommentThreads + threads).distinctBy { it.post.id to (it.comments.lastOrNull()?.id ?: 0) },
                        isRepliesLoading = chain,
                        repliesNextCursor = nextCursor,
                        repliesHasMore = nextCursor != null
                    )
                }
                if (chain) loadMoreCommentThreads(chained = true)
            }
            .onFailure { error ->
                if (repliesGeneration.current() != generation) return@intent
                reduce {
                    state.copy(
                        isRepliesLoading = false,
                        snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    // 첫 페이지만 받아 앞에 합침

    private fun refreshCommentThreadsQuietly() = intent {
        if (state.isRepliesLoading) return@intent
        val sort = state.repliesSortType
        val contextType = state.repliesContextType
        getMyCommentThreadsUseCase(
            sort = sort.apiValue,
            contextType = contextType?.apiValue
        ).onSuccess { (threads, nextCursor) ->
            if (state.repliesSortType != sort || state.repliesContextType != contextType) return@intent
            if (state.isRepliesLoading) return@intent  // 그사이 더보기·새로고침이 시작됨
            if (threads.isEmpty() && nextCursor != null) return@intent  // 첫 페이지가 다 걸러짐, 받아 둔 목록 유지
            cacheThreadComments(threads)
            val threadKey = { thread: CommentThreadData -> thread.post.id to (thread.comments.lastOrNull()?.id ?: 0) }
            reduce {
                val fresh = threads.distinctBy(threadKey)
                val merged = state.myCommentThreads.mergeFreshHead(fresh, threadKey)
                // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                val keepsTail = merged.size > fresh.size
                state.copy(
                    myCommentThreads = merged,
                    repliesHasMore = if (keepsTail) state.repliesHasMore else nextCursor != null,
                    repliesNextCursor = if (keepsTail) state.repliesNextCursor else nextCursor
                )
            }
        }
    }

    private fun changeRepliesSortType(sortType: UserPostSortType) = intent {
        if (state.repliesSortType == sortType) return@intent
        repliesGeneration.next()  // 옛 정렬로 진행 중이던 더보기 버림
        reduce {
            state.copy(
                repliesSortType = sortType,
                myCommentThreads = emptyList(),
                repliesNextCursor = null,
                repliesHasMore = true
            )
        }
        loadMyCommentThreads()
    }

    private fun changeRepliesContextType(contextType: PostContextType?) = intent {
        if (state.repliesContextType == contextType) return@intent
        repliesGeneration.next()  // 옛 범위로 진행 중이던 더보기 버림
        reduce {
            state.copy(
                repliesContextType = contextType,
                myCommentThreads = emptyList(),
                repliesNextCursor = null,
                repliesHasMore = true
            )
        }
        loadMyCommentThreads()
    }

    private fun likeComment(commentId: Int) = toggleThreadCommentLike(commentId, shouldLike = true)

    private fun unlikeComment(commentId: Int) = toggleThreadCommentLike(commentId, shouldLike = false)

    private fun toggleThreadCommentLike(commentId: Int, shouldLike: Boolean) = intent {
        val prev = state.myCommentThreads.firstNotNullOfOrNull { thread -> thread.comments.find { it.id == commentId } }
            ?: return@intent
        // 스레드 맨 앞 부모 댓글은 캐시 밖이라 목록을 직접 갱신
        if (prev.isLiked != shouldLike) {
            reduce {
                state.copy(
                    myCommentThreads = state.myCommentThreads.updateComment(commentId) {
                        it.copy(isLiked = shouldLike, likeCount = maxOf(0, it.likeCount + if (shouldLike) 1 else -1))
                    }
                )
            }
        }
        postMutationDelegate.toggleCommentLike(commentId, shouldLike)
            ?.onSuccess { result ->
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.updateComment(commentId) {
                            it.copy(isLiked = result.isLiked, likeCount = result.likeCount)
                        }
                    )
                }
            }
            ?.onFailure {
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.updateComment(commentId) {
                            it.copy(isLiked = prev.isLiked, likeCount = prev.likeCount)
                        },
                        snackbarState = SnackbarState("좋아요 처리 실패")
                    )
                }
            }
    }

    // 답글 탭 원글은 캐시 밖이라 목록을 직접 갱신
    private fun toggleThreadPostLike(postId: Int) = intent {
        val prev = state.myCommentThreads.firstOrNull { it.post.id == postId }?.post ?: return@intent
        val shouldLike = !prev.isLiked
        reduce {
            state.copy(
                myCommentThreads = state.myCommentThreads.updatePost(postId) {
                    it.copy(isLiked = shouldLike, likeCount = maxOf(0, prev.likeCount + if (shouldLike) 1 else -1))
                }
            )
        }
        postMutationDelegate.togglePostLike(postId, shouldLike)
            ?.onSuccess { result ->
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.updatePost(postId) {
                            it.copy(isLiked = result.isLiked, likeCount = result.likeCount)
                        }
                    )
                }
            }
            ?.onFailure {
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.updatePost(postId) {
                            it.copy(isLiked = prev.isLiked, likeCount = prev.likeCount)
                        },
                        snackbarState = SnackbarState("좋아요 처리 실패")
                    )
                }
            }
    }

    private fun navigateToCommentDetail(commentId: Int) = intent {
        postSideEffect(MyProfileSideEffect.NavigateToCommentDetail(commentId))
    }

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(MyProfileSideEffect.NavigateToUserProfile(userId))
    }

    private fun showDeletedPostSnackbar() = intent {
        reduce {
            state.copy(
                snackbarState = SnackbarState("삭제된 포스트입니다")
            )
        }
    }

    private fun showHiddenPostSnackbar() = intent {
        reduce {
            state.copy(
                snackbarState = SnackbarState("유효하지 않은 포스트입니다")
            )
        }
    }

    private fun showCommentOptions(
        comment: ThreadCommentData,
        thread: CommentThreadData
    ) = intent {
        reduce {
            state.copy(
                showCommentOptionsSheet = true,
                selectedComment = comment,
                selectedCommentThread = thread
            )
        }
    }

    private fun dismissCommentOptions() = intent {
        reduce {
            state.copy(
                showCommentOptionsSheet = false,
                selectedComment = null,
                selectedCommentThread = null
            )
        }
    }

    private fun deleteComment() = intent {
        val commentId = state.selectedComment?.id ?: return@intent
        dismissCommentOptions()

        deleteCommentUseCase(commentId)
            .onSuccess {
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.mapNotNull { thread ->
                            val filteredComments = thread.comments.filter { it.id != commentId }
                            // 내 댓글이 안 남은 스레드는 뺌
                            if (filteredComments.none { it.isProfileUser }) null
                            else thread.copy(comments = filteredComments)
                        },
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

        // 부모 댓글은 캐시 밖이라 목록을 직접 갱신
        reduce {
            state.copy(
                myCommentThreads = state.myCommentThreads.updateComment(comment.id) {
                    it.copy(isBookmarked = shouldBookmark)
                },
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }

        postMutationDelegate.toggleCommentBookmark(comment.id, shouldBookmark)
            ?.onFailure {
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.updateComment(comment.id) {
                            it.copy(isBookmarked = comment.isBookmarked)
                        },
                        snackbarState = SnackbarState("저장 실패")
                    )
                }
            }
    }

    private fun blockCommentAuthor() = intent {
        val authorId = state.selectedComment?.author?.id ?: return@intent
        dismissCommentOptions()

        postMutationDelegate.blockUser(authorId)
            .onSuccess {
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.mapNotNull { thread ->
                            val filteredComments = thread.comments.filter { it.author.id != authorId }
                            if (filteredComments.isEmpty()) null
                            else thread.copy(comments = filteredComments)
                        }
                    )
                }
            }
    }

    private fun navigateToEditProfile() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToEditProfile)
    }

    private fun navigateToSettings() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToSettings)
    }

    private fun navigateToMembershipManage() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToMembershipManage)
    }

    private fun navigateToMySubscriptions() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToMySubscriptions)
    }

    private fun navigateToPoint() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToPoint)
    }

    private fun navigateToCoupon() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToCoupon)
    }

    private fun navigateToCouponManage() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToCouponManage)
    }

    private fun navigateToMyProducts() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToMyProducts)
    }

    private fun navigateToSalesManage() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToSalesManage)
    }

    private fun navigateToMyReviews() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToMyReviews)
    }

    private fun navigateToAddress() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToAddress)
    }

    private fun navigateToWishlist() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToWishlist)
    }

    private fun navigateToCart() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToCart)
    }

    private fun navigateToMyOrders() = intent {
        postSideEffect(MyProfileSideEffect.NavigateToMyOrders)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    // 스레드 포스트 옵션

    private fun showThreadPostOptions(post: PostSummaryData) = intent {
        reduce { state.copy(showThreadPostOptionsSheet = true, selectedThreadPost = post) }
    }

    private fun dismissThreadPostOptions() = intent {
        reduce { state.copy(showThreadPostOptionsSheet = false, selectedThreadPost = null) }
    }

    private fun bookmarkThreadPost() = intent {
        val postId = state.selectedThreadPost?.id ?: return@intent
        val prevIsBookmarked = state.selectedThreadPost?.isBookmarked ?: false
        val shouldBookmark = !prevIsBookmarked
        dismissThreadPostOptions()

        reduce {
            state.copy(
                myCommentThreads = state.myCommentThreads.map { thread ->
                    if (thread.post.id == postId) {
                        thread.copy(post = thread.post.copy(isBookmarked = shouldBookmark))
                    } else thread
                },
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }

        postMutationDelegate.togglePostBookmark(postId, shouldBookmark)
            ?.onFailure {
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.map { thread ->
                            if (thread.post.id == postId) {
                                thread.copy(post = thread.post.copy(isBookmarked = prevIsBookmarked))
                            } else thread
                        },
                        snackbarState = SnackbarState("저장 실패")
                    )
                }
            }
    }

    private fun blockThreadPostAuthor() = intent {
        val authorId = state.selectedThreadPost?.author?.id ?: return@intent
        dismissThreadPostOptions()

        postMutationDelegate.blockUser(authorId)
            .onSuccess {
                reduce {
                    state.copy(
                        myCommentThreads = state.myCommentThreads.filter { it.post.author.id != authorId }
                    )
                }
            }
    }

    // 팔로우

    private fun followUser(userId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow = true)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("팔로우 실패")) }
            }
    }

    private fun unfollowUser(userId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow = false)
            ?.onFailure {
                reduce { state.copy(snackbarState = SnackbarState("팔로우 취소 실패")) }
            }
    }

    // 홈 탭, Creator 전용

    private fun loadHomeSections(quiet: Boolean = false) = intent {
        val creatorId = state.profile?.userId ?: return@intent
        if (!state.profile!!.isCreator) return@intent

        val generation = homeGeneration.next()
        reduce { state.copy(isHomeSectionsLoading = true) }

        val postSectionsResult = profileRepository.getCreatorHomeSections(creatorId)
        val storeSectionsResult = profileRepository.getCreatorStoreSections(creatorId)
        if (homeGeneration.current() != generation) return@intent  // 그사이 다시 받기 시작함, 로딩 표시는 그쪽 몫
        var postErrorMessage: String? = null
        var storeErrorMessage: String? = null

        postSectionsResult.onSuccess { sections ->
            reduce {
                state.copy(
                    pinnedPosts = sections.pinnedPosts,
                    postSections = sections.postSections
                )
            }
        }.onFailure { error ->
            postErrorMessage = error.message ?: "포스트 섹션을 불러올 수 없습니다"
        }

        storeSectionsResult.onSuccess { storeSection ->
            reduce {
                state.copy(productSections = storeSection.tagSections)
            }
        }.onFailure { error ->
            storeErrorMessage = error.message ?: "상품 섹션을 불러올 수 없습니다"
        }

        val failed = postErrorMessage != null || storeErrorMessage != null
        val isEmpty = state.pinnedPosts.isEmpty() && state.postSections.isEmpty() && state.productSections.isEmpty()
        // 못 받아 보여줄 게 없으면 스켈레톤 그대로
        if (quiet || !failed || !isEmpty) reduce { state.copy(isHomeSectionsLoading = false) }
        if (!quiet && failed) {  // 복귀 새로고침 실패는 조용히
            val message = when {
                postErrorMessage != null && storeErrorMessage != null -> "홈 콘텐츠를 불러올 수 없습니다"
                postErrorMessage != null -> postErrorMessage
                else -> storeErrorMessage
            } ?: "홈 콘텐츠를 불러올 수 없습니다"
            reduce { state.copy(snackbarState = SnackbarState(message)) }
        }
    }

    private fun navigateToSectionDetail(
        sectionType: SectionType,
        tagId: Int?,
        title: String
    ) = intent {
        val creatorId = state.profile?.userId ?: return@intent
        postSideEffect(
            MyProfileSideEffect.NavigateToSectionDetail(
                sectionType = sectionType,
                creatorId = creatorId,
                tagId = tagId,
                title = title
            )
        )
    }

    private fun navigateToProductDetail(productId: Int) = intent {
        postSideEffect(MyProfileSideEffect.NavigateToProductDetail(productId))
    }

    // 커뮤니티 탭, Creator 전용

    private fun loadCommunityPosts(quiet: Boolean = false) = intent {
        val creatorId = state.profile?.userId ?: return@intent
        if (!state.profile!!.isCreator) return@intent

        if (!quiet) reduce { state.copy(isCommunityLoading = true) }

        getCommunityPostsUseCase(creatorId, limit = PAGE_SIZE)
            .onSuccess { (posts, nextCursor) ->
                val pinnedPosts = posts.filter { it.isPinned }
                val normalPosts = posts.filter { !it.isPinned }

                communityGeneration.next()
                reduce {
                    state.copy(
                        communityPinnedPosts = pinnedPosts,
                        communityPosts = normalPosts,
                        isCommunityLoading = false,
                        communityNextCursor = nextCursor,
                        communityHasMore = nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        // 받아 둔 글이 없으면 스켈레톤 그대로
                        isCommunityLoading = state.isCommunityLoading &&
                            state.communityPosts.isEmpty() && state.communityPinnedPosts.isEmpty(),
                        snackbarState = SnackbarState(error.message ?: "커뮤니티를 불러올 수 없습니다")
                    )
                }
            }
    }

    private fun loadMoreCommunityPosts(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (state.isCommunityLoading || !state.communityHasMore) return@intent

        val creatorId = state.profile?.userId ?: return@intent
        val cursor = state.communityNextCursor
        val generation = communityGeneration.current()

        reduce { state.copy(isCommunityLoading = true) }

        getCommunityPostsUseCase(creatorId, cursor = cursor, limit = PAGE_SIZE)
            .onSuccess { (posts, nextCursor) ->
                if (communityGeneration.current() != generation) {
                    // 로딩 표시는 갈아끼운 쪽 몫
                    if (!state.isCommunityLoading && state.communityPosts.isNotEmpty()) loadMoreCommunityPosts()  // 목록 수가 그대로면 트리거가 안 걸려 다시 요청
                    return@intent
                }
                val normalPosts = posts.filter { !it.isPinned }
                reduce {
                    state.copy(
                        communityPosts = (state.communityPosts + normalPosts).distinctBy { it.id },
                        isCommunityLoading = false,
                        communityNextCursor = nextCursor,
                        communityHasMore = nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                if (communityGeneration.current() != generation) return@intent
                reduce {
                    state.copy(
                        isCommunityLoading = false,
                        snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                    )
                }
            }
    }

    // 첫 페이지만 받아 앞에 합침

    private fun refreshCommunityPostsQuietly() = intent {
        val creatorId = state.profile?.userId ?: return@intent
        if (!state.profile!!.isCreator || state.isCommunityLoading) return@intent
        getCommunityPostsUseCase(creatorId, limit = PAGE_SIZE)
            .onSuccess { (posts, nextCursor) ->
                if (state.isCommunityLoading) return@intent  // 그사이 더보기·새로고침이 시작됨
                val pinnedPosts = posts.filter { it.isPinned }
                val normalPosts = posts.filter { !it.isPinned }
                reduce {
                    val pinnedIds = pinnedPosts.map { it.id }.toSet()
                    val merged = state.communityPosts.mergeFreshHead(normalPosts) { it.id }.filter { it.id !in pinnedIds }
                    // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                    val keepsTail = merged.size > normalPosts.size
                    state.copy(
                        communityPinnedPosts = pinnedPosts,
                        communityPosts = merged,
                        communityHasMore = if (keepsTail) state.communityHasMore else nextCursor != null,
                        communityNextCursor = if (keepsTail) state.communityNextCursor else nextCursor
                    )
                }
            }
    }

    private fun likeCommunityPost(postId: Int) = intent {
        val targetPost = state.communityPosts.find { it.id == postId }
            ?: state.communityPinnedPosts.find { it.id == postId }
        val prevIsLiked = targetPost?.isLiked ?: false
        val prevCount = targetPost?.likeCount ?: 0
        val newIsLiked = !prevIsLiked
        val newCount = if (newIsLiked) prevCount + 1 else maxOf(0, prevCount - 1)

        reduce {
            state.copy(
                communityPosts = state.communityPosts.map { post ->
                    if (post.id == postId) post.copy(isLiked = newIsLiked, likeCount = newCount) else post
                },
                communityPinnedPosts = state.communityPinnedPosts.map { post ->
                    if (post.id == postId) post.copy(isLiked = newIsLiked, likeCount = newCount) else post
                }
            )
        }

        postMutationDelegate.togglePostLike(postId, shouldLike = newIsLiked)
            ?.onSuccess { result ->
                reduce {
                    state.copy(
                        communityPosts = state.communityPosts.map { post ->
                            if (post.id == postId) post.copy(isLiked = result.isLiked, likeCount = result.likeCount) else post
                        },
                        communityPinnedPosts = state.communityPinnedPosts.map { post ->
                            if (post.id == postId) post.copy(isLiked = result.isLiked, likeCount = result.likeCount) else post
                        }
                    )
                }
            }
            ?.onFailure {
                reduce {
                    state.copy(
                        communityPosts = state.communityPosts.map { post ->
                            if (post.id == postId) post.copy(isLiked = prevIsLiked, likeCount = prevCount) else post
                        },
                        communityPinnedPosts = state.communityPinnedPosts.map { post ->
                            if (post.id == postId) post.copy(isLiked = prevIsLiked, likeCount = prevCount) else post
                        },
                        snackbarState = SnackbarState("좋아요 처리 실패")
                    )
                }
            }
    }

    private fun showCommunityPostOptions(post: Post) = intent {
        reduce {
            state.copy(
                showPostOptionsSheet = true,
                selectedPost = post
            )
        }
    }

    private fun editCommunityPost() = intent {
        val post = state.selectedPost ?: return@intent
        reduce {
            state.copy(
                showPostOptionsSheet = false,
                selectedPost = null
            )
        }
        postSideEffect(MyProfileSideEffect.NavigateToEditPost(post.id))
    }

    private fun deleteCommunityPost() = intent {
        val post = state.selectedPost ?: return@intent
        val postId = post.id

        reduce {
            state.copy(
                showPostOptionsSheet = false,
                selectedPost = null
            )
        }

        reduce {
            state.copy(
                communityPosts = state.communityPosts.filter { it.id != postId },
                communityPinnedPosts = state.communityPinnedPosts.filter { it.id != postId }
            )
        }

        deleteCommunityPostUseCase(postId)
            .onSuccess {
                reduce {
                    state.copy(
                        myPosts = state.myPosts.filter { it.id != postId },  // 포스트 탭 커뮤니티 필터에서 지운 경우
                        snackbarState = SnackbarState("포스트가 삭제되었습니다")
                    )
                }
                syncProfile()
            }
            .onFailure { e ->
                loadCommunityPosts()
                reduce { state.copy(snackbarState = SnackbarState(e.message ?: "삭제에 실패했습니다")) }
            }
    }

    private fun pinCommunityPost() = intent {
        val post = state.selectedPost ?: return@intent
        val postId = post.id
        val prevIsPinned = post.isPinned

        reduce {
            state.copy(
                showPostOptionsSheet = false,
                selectedPost = null
            )
        }

        // 낙관적 갱신
        val optimisticMessage = if (prevIsPinned) "고정이 해제되었습니다" else "포스트가 고정되었습니다"
        reduce {
            state.applyCommunityPin(postId, pinned = !prevIsPinned)
                .copy(snackbarState = SnackbarState(optimisticMessage))
        }

        togglePinCommunityPostUseCase(postId)
            .onSuccess { isPinned ->
                // 서버는 토글이라 다른 기기에서 먼저 바꿨으면 결과가 반대
                if (isPinned == prevIsPinned) {
                    reduce {
                        state.applyCommunityPin(postId, pinned = isPinned).copy(
                            snackbarState = SnackbarState(if (isPinned) "포스트가 고정되었습니다" else "고정이 해제되었습니다")
                        )
                    }
                }
            }
            .onFailure { e ->
                reduce {
                    state.applyCommunityPin(postId, pinned = prevIsPinned)
                        .copy(snackbarState = SnackbarState(e.message ?: "고정 변경에 실패했습니다"))
                }
            }
    }

    // 커뮤니티 목록에 있던 글만 고정·일반 사이 이동
    private fun MyProfileState.applyCommunityPin(postId: Int, pinned: Boolean): MyProfileState {
        val existing = (communityPosts + communityPinnedPosts).find { it.id == postId }
        val moved = when {
            existing == null -> this
            pinned -> copy(
                communityPosts = communityPosts.filter { it.id != postId },
                communityPinnedPosts = (communityPinnedPosts + existing.copy(isPinned = true)).distinctBy { it.id }
            )
            else -> copy(
                communityPinnedPosts = communityPinnedPosts.filter { it.id != postId },
                communityPosts = (communityPosts + existing.copy(isPinned = false))
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAt }
            )
        }
        return moved.copy(myPosts = moved.myPosts.map { if (it.id == postId) it.copy(isPinned = pinned) else it })
    }

    // 커뮤니티 멤버 글, 목록도 바로 갱신
    private fun MyProfileState.updateCommunityPost(postId: Int, transform: (Post) -> Post) = copy(
        communityPosts = communityPosts.map { if (it.id == postId) transform(it) else it },
        communityPinnedPosts = communityPinnedPosts.map { if (it.id == postId) transform(it) else it }
    )

    private fun bookmarkPost() = intent {
        val post = state.selectedPost ?: return@intent
        val shouldBookmark = !post.isBookmarked
        reduce {
            state.updateCommunityPost(post.id) { it.copy(isBookmarked = shouldBookmark) }.copy(
                showPostOptionsSheet = false,
                selectedPost = null,
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }
        postMutationDelegate.togglePostBookmark(post.id, shouldBookmark)
            ?.onFailure {
                reduce {
                    state.updateCommunityPost(post.id) { it.copy(isBookmarked = post.isBookmarked) }
                        .copy(snackbarState = SnackbarState("저장 실패"))
                }
            }
    }

    private fun blockPostAuthor() = intent {
        val authorId = state.selectedPost?.author?.id ?: return@intent
        reduce { state.copy(showPostOptionsSheet = false, selectedPost = null) }
        postMutationDelegate.blockUser(authorId)
            .onSuccess { isBlocked ->
                if (isBlocked) {
                    reduce {
                        state.copy(
                            communityPosts = state.communityPosts.filter { it.author.id != authorId },
                            communityPinnedPosts = state.communityPinnedPosts.filter { it.author.id != authorId },
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

    private fun createCommunityPost() = intent {
        val creatorId = state.profile?.userId ?: return@intent
        postSideEffect(MyProfileSideEffect.NavigateToCreateCommunityPost(creatorId))
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

    private fun navigateToChatRoom(roomId: Int) = intent {
        reduce { state.copy(navigateRoomId = null) }
        postSideEffect(MyProfileSideEffect.NavigateToChatRoom(roomId))
    }

    // 스토어 탭, Creator 전용

    private fun loadStoreProducts(sortType: ProductSortType? = null) = intent {
        val creatorId = state.profile?.userId ?: return@intent
        if (!state.profile!!.isCreator) return@intent

        val currentSort = sortType ?: state.storeSortType
        reduce { state.copy(isStoreLoading = true) }

        profileRepository.getCreatorStoreSections(creatorId, sort = currentSort.apiValue)
            .onSuccess { storeSection ->
                if (state.storeSortType != currentSort) return@intent
                storeGeneration.next()
                reduce {
                    state.copy(
                        storeProductSections = storeSection.tagSections,
                        storeProducts = storeSection.allProducts,
                        isStoreLoading = false,
                        storePage = 1,
                        storeHasMore = storeSection.hasNext
                    )
                }
            }
            .onFailure { error ->
                if (state.storeSortType != currentSort) return@intent
                reduce {
                    state.copy(
                        // 받아 둔 상품이 없으면 스켈레톤 그대로
                        isStoreLoading = state.storeProducts.isEmpty() && state.storeProductSections.isEmpty(),
                        snackbarState = SnackbarState(error.message ?: "스토어를 불러올 수 없습니다")
                    )
                }
            }
    }

    private fun reloadLoadedStoreProducts() = intent {
        val creatorId = state.profile?.userId ?: return@intent
        if (!state.profile!!.isCreator || state.isStoreLoading) return@intent
        val basePage = state.storePage
        val generation = storeGeneration.current()
        val sort = state.storeSortType
        val loadedCount = basePage * STORE_PAGE_SIZE
        profileRepository.getCreatorStoreSections(
            creatorId = creatorId,
            limit = minOf(loadedCount, Constants.MAX_PAGE_LIMIT),
            sort = sort.apiValue
        ).onSuccess { storeSection ->
            if (state.storeSortType != sort || storeGeneration.current() != generation || state.storePage != basePage) return@intent  // 그사이 정렬·더보기
            reduce {
                if (loadedCount <= Constants.MAX_PAGE_LIMIT) {
                    state.copy(
                        storeProductSections = storeSection.tagSections,
                        storeProducts = storeSection.allProducts,
                        storeHasMore = storeSection.hasNext
                    )
                } else {
                    // 순위가 바뀌는 목록이라 받아 둔 목록 유지
                    state.copy(storeProductSections = storeSection.tagSections)
                }
            }
        }
    }

    private fun changeStoreSortType(sortType: ProductSortType) = intent {
        if (state.storeSortType == sortType) return@intent
        storeGeneration.next()  // 옛 정렬로 진행 중이던 더보기 버림
        reduce {
            state.copy(
                storeSortType = sortType,
                storeProducts = emptyList(),
                storePage = 1,
                storeHasMore = true
            )
        }
        loadStoreProducts(sortType)
    }

    private fun loadMoreStoreProducts(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (state.isStoreLoading || !state.storeHasMore) return@intent

        val creatorId = state.profile?.userId ?: return@intent
        val basePage = state.storePage
        val generation = storeGeneration.current()
        val nextPage = basePage + 1
        val sort = state.storeSortType
        reduce { state.copy(isStoreLoading = true) }

        profileRepository.getCreatorStoreSections(
            creatorId = creatorId,
            page = nextPage,
            sort = sort.apiValue
        )
            .onSuccess { storeSection ->
                if (state.storeSortType != sort) return@intent
                if (storeGeneration.current() != generation) {
                    if (!state.isStoreLoading && state.storeProducts.isNotEmpty()) loadMoreStoreProducts()  // 목록 수가 그대로면 트리거가 안 걸려 다시 요청
                    return@intent
                }
                reduce {
                    state.copy(
                        storeProducts = (state.storeProducts + storeSection.allProducts).distinctBy { it.id },
                        isStoreLoading = false,
                        storePage = nextPage,
                        storeHasMore = storeSection.hasNext
                    )
                }
            }
            .onFailure { error ->
                if (storeGeneration.current() != generation) return@intent
                if (state.storeSortType != sort) return@intent
                reduce { state.copy(isStoreLoading = false, snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")) }
            }
    }
}
