package com.ninezero.halo.feature.profile.presentation.user_profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.ListGeneration
import com.ninezero.halo.core.util.mergeFreshHead
import com.ninezero.halo.feature.feed.domain.delegate.PostMutationDelegate
import com.ninezero.halo.feature.feed.domain.delegate.PostShareDelegate
import com.ninezero.halo.feature.feed.domain.event.BlockEventBus
import com.ninezero.halo.feature.feed.domain.event.FollowEvent
import com.ninezero.halo.feature.feed.domain.event.FollowEventBus
import com.ninezero.halo.feature.feed.domain.event.SubscriptionEventBus
import com.ninezero.halo.feature.feed.domain.model.CachedCommentsTracker
import com.ninezero.halo.feature.feed.domain.model.CachedPostsTracker
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.feed.domain.model.PostUploadEvent
import com.ninezero.halo.feature.feed.domain.model.syncWithCache
import com.ninezero.halo.feature.feed.domain.repository.PendingPostRepository
import com.ninezero.halo.feature.feed.domain.repository.PostUploadEventRepository
import com.ninezero.halo.feature.feed.domain.usecase.CacheCommentsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.DeletePostUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObserveCachedCommentsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ObserveCachedPostsUseCase
import com.ninezero.halo.feature.feed.domain.usecase.ToggleAuthorFollowUseCase
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.PostSummaryData
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.domain.model.ThreadCommentData
import com.ninezero.halo.feature.profile.domain.model.applyFollow
import com.ninezero.halo.feature.profile.domain.model.mergeCached
import com.ninezero.halo.feature.profile.domain.model.syncWithCache
import com.ninezero.halo.feature.profile.domain.model.toCacheableFeedComments
import com.ninezero.halo.feature.profile.domain.model.updateComment
import com.ninezero.halo.feature.profile.domain.model.updatePost
import com.ninezero.halo.feature.profile.domain.repository.FollowRepository
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import com.ninezero.halo.feature.profile.domain.usecase.DeleteCommunityPostUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetCachedMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetCommunityPostsUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetMyProfileUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetUserCommentThreadsUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetUserPostsUseCase
import com.ninezero.halo.feature.profile.domain.usecase.GetUserProfileUseCase
import com.ninezero.halo.feature.profile.presentation.components.ProductSortType
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.UserPostSortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

private const val PAGE_SIZE = 10
private const val REPLIES_PAGE_SIZE = 20
private const val STORE_PAGE_SIZE = 20  // 스토어 Repository 기본값과 같음

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getCachedMyProfileUseCase: GetCachedMyProfileUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val getUserCommentThreadsUseCase: GetUserCommentThreadsUseCase,
    private val postMutationDelegate: PostMutationDelegate,
    private val postShareDelegate: PostShareDelegate,
    private val deletePostUseCase: DeletePostUseCase,
    private val deleteCommunityPostUseCase: DeleteCommunityPostUseCase,
    private val toggleAuthorFollowUseCase: ToggleAuthorFollowUseCase,
    private val profileRepository: ProfileRepository,
    private val getCommunityPostsUseCase: GetCommunityPostsUseCase,
    private val followRepository: FollowRepository,
    private val observeCachedPostsUseCase: ObserveCachedPostsUseCase,
    private val observeCachedCommentsUseCase: ObserveCachedCommentsUseCase,
    private val cacheCommentsUseCase: CacheCommentsUseCase,
    private val blockEventBus: BlockEventBus,
    private val followEventBus: FollowEventBus,
    private val subscriptionEventBus: SubscriptionEventBus,
    private val pendingPostRepository: PendingPostRepository,
    private val postUploadEventRepository: PostUploadEventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), OrbitContainerHost<UserProfileState, UserProfileState, UserProfileSideEffect> {

    // Optimistic UI 중복 요청 방지 플래그
    private var isFollowInProgress = false
    private val cachedPostsTracker = CachedPostsTracker()
    private var isBlockingFromHere = false  // 곧 나갈 화면이라 차단 이벤트로 다시 부르지 않음
    private val cachedCommentsTracker = CachedCommentsTracker()

    // 목록을 갈아끼울 때마다 올려 늦은 더보기를 버림
    private val postsGeneration = ListGeneration()
    private val repliesGeneration = ListGeneration()
    private val communityGeneration = ListGeneration()
    private val storeGeneration = ListGeneration()
    private val homeGeneration = ListGeneration()  // 홈 섹션은 받을 때마다 올림, 늦게 온 옛 응답은 버림

    private val userId: Int? = savedStateHandle.get<Int>("userId")

    override val container: OrbitContainer<UserProfileState, UserProfileState, UserProfileSideEffect> =
        orbitContainer(
            UserProfileState(
                myUserId = getCachedMyProfileUseCase()?.userId
            )
        )

    init {
        loadProfile()
        observeCachedPosts()
        observeCachedComments()
        observeBlockEvents()
        observeFollowEvents()
        observeSubscriptionEvents()
        observeMyProfileUpdates()
        observePendingCommunityPosts()
        observeCommunityUploadResults()
    }

    private fun observePendingCommunityPosts() {
        val creatorId = userId ?: return
        val myUserId = container.stateFlow.value.myUserId ?: return
        pendingPostRepository.getPendingCommunityPosts(myUserId, creatorId)
            .onEach { pending ->
                intent { reduce { state.copy(pendingCommunityPosts = pending) } }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCommunityUploadResults() {
        postUploadEventRepository.uploadEvents
            .onEach { event ->
                when (event) {
                    is PostUploadEvent.Success -> if (event.creatorId == userId) {
                        intent { reduce { state.copy(snackbarState = SnackbarState("게시됨")) } }
                        loadCommunityPosts(quiet = true)
                    }

                    is PostUploadEvent.Failure -> if (event.creatorId == userId) {
                        intent { reduce { state.copy(snackbarState = SnackbarState("게시 실패")) } }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSubscriptionEvents() {  // 구독 시 잠금 글 언락 위해 재조회
        subscriptionEventBus.events
            .onEach { event ->
                val targetId = userId ?: return@onEach
                if (event.creatorId == targetId) {
                    refreshProfile()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMyProfileUpdates() {
        profileRepository.observeMyProfile()
            .onEach { myProfile ->
                intent {
                    val current = state.profile ?: return@intent
                    if (myProfile == null || !current.isOwnProfile) return@intent
                    if (current.userId != myProfile.userId) return@intent
                    reduce {
                        state.copy(
                            profile = current.copy(
                                displayName = myProfile.displayName,
                                bio = myProfile.bio,
                                avatarUrl = myProfile.avatarUrl,
                                avatarThumbUrl = myProfile.avatarThumbUrl,
                                location = myProfile.location,
                                website = myProfile.website
                            )
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFollowEvents() {
        followEventBus.events
            .onEach { event ->
                intent {
                    val authorId = event.userId
                    val newIsFollowing = event is FollowEvent.Followed

                    val profile = state.profile
                    val updatedProfile = if (
                        profile != null && profile.userId == authorId && profile.isFollowing != newIsFollowing
                    ) {
                        profile.copy(
                            isFollowing = newIsFollowing,
                            followerCount = if (newIsFollowing) {
                                profile.followerCount + 1
                            } else {
                                maxOf(0, profile.followerCount - 1)
                            }
                        )
                    } else {
                        profile
                    }

                    val updatedThreads =
                        state.userCommentThreads.map { it.applyFollow(authorId, newIsFollowing) }
                    val updatedSelectedComment =
                        state.selectedComment?.applyFollow(authorId, newIsFollowing)
                    val updatedSelectedCommentThread =
                        state.selectedCommentThread?.applyFollow(authorId, newIsFollowing)
                    val updatedSelectedThreadPost =
                        state.selectedThreadPost?.applyFollow(authorId, newIsFollowing)
                    val updatedCommunityPosts =
                        state.communityPosts.map { it.applyFollow(authorId, newIsFollowing) }
                    val updatedCommunityPinnedPosts =
                        state.communityPinnedPosts.map { it.applyFollow(authorId, newIsFollowing) }

                    if (
                        updatedProfile != state.profile ||
                        updatedThreads != state.userCommentThreads ||
                        updatedSelectedComment != state.selectedComment ||
                        updatedSelectedCommentThread != state.selectedCommentThread ||
                        updatedSelectedThreadPost != state.selectedThreadPost ||
                        updatedCommunityPosts != state.communityPosts ||
                        updatedCommunityPinnedPosts != state.communityPinnedPosts
                    ) {
                        reduce {
                            state.copy(
                                profile = updatedProfile,
                                userCommentThreads = updatedThreads,
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

    private fun observeBlockEvents() {
        blockEventBus.events
            .onEach { event ->
                val targetId = userId ?: return@onEach
                if (event.userId == targetId && !isBlockingFromHere) {
                    refreshProfile()
                }
            }
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
                            userPosts = state.userPosts.syncWithCache(cachedPostsMap, previousCachedIds),
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

    private fun observeCachedComments() {
        observeCachedCommentsUseCase()
            .onEach { cachedComments ->
                // 반영은 reduce 안에서, 동시 갱신 유실 방지
                val previousCachedCommentIds = cachedCommentsTracker.track(cachedComments.keys)
                intent {
                    reduce {
                        if (
                            state.userCommentThreads.isEmpty() &&
                            state.selectedComment == null &&
                            state.selectedCommentThread == null
                        ) {
                            return@reduce state
                        }
                        val authorFollowMap = cachedComments.values.associate { comment ->
                            comment.author.id to comment.author.isFollowing
                        }
                        val updatedThreads = state.userCommentThreads.mapNotNull { thread ->
                            val updatedComments = thread.comments.mapNotNull { comment ->
                                val cached = cachedComments[comment.id]
                                when {
                                    cached != null -> comment.mergeCached(cached)
                                    comment.id in previousCachedCommentIds -> null
                                    else -> comment
                                }
                            }
                            if (updatedComments.none { it.isProfileUser }) {  // 프로필 주인 댓글이 안 남음
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
                            if (updatedComments.none { it.isProfileUser }) {  // 프로필 주인 댓글이 안 남음
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
                                    isFollowing = authorFollowMap[post.author.id]
                                        ?: post.author.isFollowing
                                )
                            )
                        }
                        state.copy(
                            userCommentThreads = updatedThreads,
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

    fun onIntent(intent: UserProfileIntent) {
        when (intent) {
            is UserProfileIntent.LoadProfile -> loadProfile()
            is UserProfileIntent.RefreshProfile -> refreshProfile()
            is UserProfileIntent.RefreshOnResume -> refreshOnResume()
            is UserProfileIntent.NavigateBack -> navigateBack()
            is UserProfileIntent.SnackbarDismissed -> dismissSnackbar()
            is UserProfileIntent.ToggleFollow -> toggleFollow()
            // 알림 벨
            is UserProfileIntent.NotifyBellClick -> notifyBellClick()
            is UserProfileIntent.ConfirmNotifyFollow -> confirmNotifyFollow()
            is UserProfileIntent.DismissNotifyFollowDialog -> dismissNotifyFollowDialog()
            // 메시지
            is UserProfileIntent.SendMessage -> sendMessage()
            // 더보기 메뉴
            is UserProfileIntent.ShowMoreOptions -> showProfileOptions()
            is UserProfileIntent.DismissProfileOptions -> dismissProfileOptions()
            // 차단 관련
            is UserProfileIntent.ShowBlockConfirmDialog -> showBlockConfirmDialog()
            is UserProfileIntent.DismissBlockConfirmDialog -> dismissBlockConfirmDialog()
            is UserProfileIntent.ConfirmBlockUser -> confirmBlockUser()
            is UserProfileIntent.DismissBlockCompletedDialog -> dismissBlockCompletedDialog()
            // 차단 해제 관련
            is UserProfileIntent.ShowUnblockConfirmDialog -> showUnblockConfirmDialog()
            is UserProfileIntent.DismissUnblockConfirmDialog -> dismissUnblockConfirmDialog()
            is UserProfileIntent.ConfirmUnblockUser -> confirmUnblockUser()
            is UserProfileIntent.DismissUnblockCompletedDialog -> dismissUnblockCompletedDialog()
            // 탭 관련
            is UserProfileIntent.SelectTab -> selectTab(intent.tab)
            // 포스트 탭
            is UserProfileIntent.LoadMorePosts -> loadMorePosts()
            is UserProfileIntent.ChangePostsSortType -> changePostsSortType(intent.sortType)
            is UserProfileIntent.ChangePostsContextType -> changePostsContextType(intent.contextType)
            is UserProfileIntent.LikePost -> likePost(intent.postId)
            is UserProfileIntent.UnlikePost -> unlikePost(intent.postId)
            is UserProfileIntent.NavigateToPostDetail -> navigateToPostDetail(intent.postId)
            is UserProfileIntent.ShowPostOptions -> showPostOptions(intent.post)
            is UserProfileIntent.DismissPostOptions -> dismissPostOptions()
            is UserProfileIntent.BookmarkPost -> bookmarkPost()
            is UserProfileIntent.NotInterestedPost -> notInterestedPost()
            is UserProfileIntent.BlockPostAuthor -> blockPostAuthor()
            is UserProfileIntent.EditPost -> editPost()
            is UserProfileIntent.DeletePost -> deletePost()
            // 답글 탭
            is UserProfileIntent.LoadMoreCommentThreads -> loadMoreCommentThreads()
            is UserProfileIntent.ChangeRepliesSortType -> changeRepliesSortType(intent.sortType)
            is UserProfileIntent.ChangeRepliesContextType -> changeRepliesContextType(intent.contextType)
            is UserProfileIntent.LikeComment -> likeComment(intent.commentId)
            is UserProfileIntent.UnlikeComment -> unlikeComment(intent.commentId)
            is UserProfileIntent.ToggleThreadPostLike -> toggleThreadPostLike(intent.postId)
            is UserProfileIntent.NavigateToCommentDetail -> navigateToCommentDetail(intent.commentId)
            is UserProfileIntent.NavigateToUserProfile -> navigateToUserProfile(intent.userId)
            is UserProfileIntent.ShowDeletedPostSnackbar -> showDeletedPostSnackbar()
            is UserProfileIntent.ShowRestrictedContentSnackbar -> showRestrictedSnackbar(intent.message)
            // 다른 사용자 팔로우
            is UserProfileIntent.FollowOtherUser -> followOtherUser(intent.userId)
            is UserProfileIntent.UnfollowOtherUser -> unfollowOtherUser(intent.userId)
            // 댓글 옵션 시트
            is UserProfileIntent.ShowCommentOptions -> showCommentOptions(
                intent.comment,
                intent.thread
            )

            is UserProfileIntent.DismissCommentOptions -> dismissCommentOptions()
            is UserProfileIntent.BookmarkComment -> bookmarkComment()
            is UserProfileIntent.BlockCommentAuthor -> blockCommentAuthor()
            // 스레드 포스트 옵션 시트
            is UserProfileIntent.ShowThreadPostOptions -> showThreadPostOptions(intent.post)
            is UserProfileIntent.DismissThreadPostOptions -> dismissThreadPostOptions()
            is UserProfileIntent.BookmarkThreadPost -> bookmarkThreadPost()
            is UserProfileIntent.NotInterestedThreadPost -> notInterestedThreadPost()
            is UserProfileIntent.BlockThreadPostAuthor -> blockThreadPostAuthor()
            // 홈 탭, Creator 전용
            is UserProfileIntent.NavigateToSectionDetail -> navigateToSectionDetail(
                intent.sectionType,
                intent.tagId,
                intent.title
            )

            is UserProfileIntent.NavigateToProductDetail -> navigateToProductDetail(intent.productId)
            // 커뮤니티 탭, Creator 전용
            is UserProfileIntent.LoadCommunityPosts -> loadCommunityPosts()
            is UserProfileIntent.LoadMoreCommunityPosts -> loadMoreCommunityPosts()
            is UserProfileIntent.LikeCommunityPost -> likeCommunityPost(intent.postId)
            is UserProfileIntent.ShowCommunityPostOptions -> showCommunityPostOptions(intent.post)
            is UserProfileIntent.CreateCommunityPost -> createCommunityPost()
            // 공유
            is UserProfileIntent.SharePost -> sharePost(intent.postId)
            is UserProfileIntent.DismissShareSheet -> dismissShareSheet()
            is UserProfileIntent.ShareCompleted -> shareCompleted(
                intent.sharedRoomIds,
                intent.recipientCount
            )

            is UserProfileIntent.NavigateToChatRoom -> navigateToChatRoom(intent.roomId)
            // 스토어 탭, Creator 전용
            is UserProfileIntent.LoadStoreProducts -> loadStoreProducts()
            is UserProfileIntent.LoadMoreStoreProducts -> loadMoreStoreProducts()
            is UserProfileIntent.ChangeStoreSortType -> changeStoreSortType(intent.sortType)
        }
    }

    private fun loadProfile() = intent {
        val result = if (userId == null || userId == 0) {
            getMyProfileUseCase()
        } else {
            getUserProfileUseCase(userId)
        }

        result
            .onSuccess { profile ->
                val ownProfile = profile.copy(
                    isOwnProfile = (userId == null || userId == 0) ||
                            (state.myUserId != null && profile.userId == state.myUserId)
                )
                // 첫 탭은 크리에이터 HOME / 일반 POSTS
                val initialTab = if (ownProfile.isCreator) ProfileTab.HOME else ProfileTab.POSTS
                reduce {
                    state.copy(
                        profile = ownProfile,
                        selectedTab = initialTab,
                        isHomeSectionsLoading = initialTab == ProfileTab.HOME,
                        isPostsLoading = initialTab == ProfileTab.POSTS,
                        // 복귀로 다시 받아 성공하면 첫 실패의 재시도 스낵바 정리
                        snackbarState = state.snackbarState?.takeUnless { it.actionLabel == "재시도" })
                }
                if (initialTab == ProfileTab.HOME) loadHomeSections() else loadUserPosts()
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        snackbarState = SnackbarState(
                            message = error.message ?: "연결에 실패했습니다",
                            actionLabel = "재시도",
                            persistent = true
                        )
                    )
                }
            }
    }

    private fun refreshProfile() = intent {
        reduce { state.copy(isRefreshing = true) }

        val result = if (userId == null || userId == 0) {
            getMyProfileUseCase()
        } else {
            getUserProfileUseCase(userId)
        }

        result.onSuccess { profile ->
            val ownProfile = profile.copy(
                isOwnProfile = (userId == null || userId == 0) ||
                        (state.myUserId != null && profile.userId == state.myUserId)
            )
            val currentTab = state.selectedTab
            reduce {
                state.copy(
                    profile = ownProfile,
                    isRefreshing = false,
                    snackbarState = null  // 첫 로드 실패의 재시도 스낵바 정리
                )
            }

            when (currentTab) {
                ProfileTab.POSTS -> loadUserPosts()
                ProfileTab.REPLIES -> loadUserCommentThreads()
                ProfileTab.HOME -> loadHomeSections()
                ProfileTab.COMMUNITY -> loadCommunityPosts()
                ProfileTab.STORE -> loadStoreProducts()
            }
        }.onFailure {
            reduce { state.copy(isRefreshing = false) }
        }
    }

    // 화면 복귀, 표시 없이 현재 탭 갱신
    private fun refreshOnResume() = intent {
        if (state.isRefreshing) return@intent
        if (state.profile == null) {  // 첫 로드 실패, 초기 탭부터 다시
            loadProfile()
            return@intent
        }

        val result = if (userId == null || userId == 0) {
            getMyProfileUseCase()
        } else {
            getUserProfileUseCase(userId)
        }

        result.onSuccess { profile ->
            val ownProfile = profile.copy(
                isOwnProfile = (userId == null || userId == 0) ||
                        (state.myUserId != null && profile.userId == state.myUserId)
            )
            reduce { state.copy(profile = ownProfile) }

            when (state.selectedTab) {
                ProfileTab.POSTS -> refreshPostsQuietly()
                ProfileTab.REPLIES -> refreshCommentThreadsQuietly()
                ProfileTab.HOME -> loadHomeSections(quiet = true)  // 홈 스켈레톤은 비었을 때만
                ProfileTab.COMMUNITY -> refreshCommunityPostsQuietly()
                ProfileTab.STORE -> reloadLoadedStoreProducts()
            }
        }
    }

    private fun toggleFollow() = intent {
        if (isFollowInProgress) return@intent
        isFollowInProgress = true

        try {
            val profile = state.profile ?: return@intent
            if (profile.isOwnProfile) return@intent

            val prevIsFollowing = profile.isFollowing
            val prevFollowerCount = profile.followerCount
            val prevNotifyEnabled = profile.isNotifyEnabled
            val shouldFollow = !prevIsFollowing
            val newFollowerCount =
                if (shouldFollow) prevFollowerCount + 1 else maxOf(0, prevFollowerCount - 1)

            reduce {
                state.copy(
                    profile = profile.copy(
                        isFollowing = shouldFollow,
                        followerCount = newFollowerCount,
                        isNotifyEnabled = shouldFollow
                    )
                )
            }

            toggleAuthorFollowUseCase(profile.userId, shouldFollow)
                .onSuccess { syncedIsFollowing ->
                    if (syncedIsFollowing != shouldFollow) {
                        val syncedFollowerCount = if (syncedIsFollowing) {
                            prevFollowerCount + 1
                        } else {
                            maxOf(0, prevFollowerCount - 1)
                        }
                        reduce {
                            state.copy(
                                profile = state.profile?.copy(
                                    isFollowing = syncedIsFollowing,
                                    followerCount = syncedFollowerCount,
                                    isNotifyEnabled = syncedIsFollowing
                                )
                            )
                        }
                    }
                }
                .onFailure { error ->
                    reduce {
                        state.copy(
                            profile = state.profile?.copy(
                                isFollowing = prevIsFollowing,
                                followerCount = prevFollowerCount,
                                isNotifyEnabled = prevNotifyEnabled
                            )
                        )
                    }
                    reduce {
                        state.copy(
                            snackbarState = SnackbarState(
                                error.message ?: "팔로우 처리 실패"
                            )
                        )
                    }
                }
        } finally {
            isFollowInProgress = false
        }
    }

    // 알림 벨

    private fun notifyBellClick() = intent {
        val profile = state.profile ?: return@intent
        if (profile.isOwnProfile || !profile.isCreator) return@intent
        if (profile.isFollowing) {
            setNotifyEnabled(!profile.isNotifyEnabled)
        } else {
            // 미팔로우면 팔로우 유도 다이얼로그
            reduce { state.copy(showNotifyFollowDialog = true) }
        }
    }

    private fun dismissNotifyFollowDialog() = intent {
        reduce { state.copy(showNotifyFollowDialog = false) }
    }

    private fun confirmNotifyFollow() = intent {
        reduce { state.copy(showNotifyFollowDialog = false) }
        if (isFollowInProgress) return@intent
        isFollowInProgress = true

        try {
            val profile = state.profile ?: return@intent
            if (profile.isFollowing) return@intent

            toggleAuthorFollowUseCase(profile.userId, true)
                .onSuccess {
                    reduce {
                        state.copy(
                            profile = state.profile?.copy(
                                isFollowing = true,
                                followerCount = (state.profile?.followerCount ?: 0) + 1,
                                isNotifyEnabled = true
                            ),
                            snackbarState = SnackbarState("알림이 켜졌습니다")
                        )
                    }
                }
                .onFailure { error ->
                    reduce {
                        state.copy(
                            snackbarState = SnackbarState(
                                error.message ?: "팔로우 처리 실패"
                            )
                        )
                    }
                }
        } finally {
            isFollowInProgress = false
        }
    }

    private var isNotifyToggleInProgress = false

    private fun setNotifyEnabled(enabled: Boolean) = intent {
        if (isNotifyToggleInProgress) return@intent
        isNotifyToggleInProgress = true

        try {
            val profile = state.profile ?: return@intent
            followRepository.setCreatorNotifications(profile.userId, enabled)
                .onSuccess {
                    reduce {
                        state.copy(
                            profile = state.profile?.copy(isNotifyEnabled = enabled),
                            snackbarState = SnackbarState(if (enabled) "알림이 켜졌습니다" else "알림이 꺼졌습니다")
                        )
                    }
                }
                .onFailure { error ->
                    reduce {
                        state.copy(
                            snackbarState = SnackbarState(
                                error.message ?: "알림 설정 실패"
                            )
                        )
                    }
                }
        } finally {
            isNotifyToggleInProgress = false
        }
    }

    private fun navigateBack() = intent {
        postSideEffect(UserProfileSideEffect.NavigateBack)
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    private fun selectTab(tab: ProfileTab) = intent {
        reduce { state.copy(selectedTab = tab) }

        when (tab) {
            ProfileTab.POSTS -> {
                if (state.userPosts.isEmpty()) loadUserPosts()
            }

            ProfileTab.REPLIES -> {
                if (state.userCommentThreads.isEmpty()) loadUserCommentThreads()
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

    private fun sendMessage() = intent {
        val profile = state.profile ?: return@intent
        // 채팅방은 첫 메시지 전송 시점에 생성
        postSideEffect(
            UserProfileSideEffect.NavigateToNewChat(
                targetUserId = profile.userId,
                username = profile.username,
                displayName = profile.displayName,
                avatarUrl = (profile.avatarThumbUrl ?: profile.avatarUrl) ?: "",
                role = profile.role
            )
        )
    }

    // 프로필 옵션

    private fun showProfileOptions() = intent {
        reduce { state.copy(showProfileOptionsSheet = true) }
    }

    private fun dismissProfileOptions() = intent {
        reduce { state.copy(showProfileOptionsSheet = false) }
    }

    // 차단 관련

    private fun showBlockConfirmDialog() = intent {
        reduce {
            state.copy(
                showProfileOptionsSheet = false,
                showBlockConfirmDialog = true
            )
        }
    }

    private fun dismissBlockConfirmDialog() = intent {
        reduce { state.copy(showBlockConfirmDialog = false) }
    }

    private fun confirmBlockUser() = intent {
        val profile = state.profile ?: return@intent

        reduce { state.copy(showBlockConfirmDialog = false) }

        isBlockingFromHere = true
        postMutationDelegate.blockUser(profile.userId)
            .onSuccess { isBlocked ->
                if (isBlocked) {
                    reduce {
                        // isBlockedByMe true, 양방향도 갱신
                        state.copy(
                            profile = profile.copy(
                                isBlockedByMe = true,
                                isBlocked = true
                            ),
                            showBlockCompletedDialog = true
                        )
                    }
                }
            }
            .onFailure { error ->
                isBlockingFromHere = false
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "차단 실패"))
                }
            }
    }

    private fun dismissBlockCompletedDialog() = intent {
        reduce { state.copy(showBlockCompletedDialog = false) }
        postSideEffect(UserProfileSideEffect.NavigateBack)
    }

    // 차단 해제 관련

    private fun showUnblockConfirmDialog() = intent {
        reduce {
            state.copy(
                showProfileOptionsSheet = false,
                showUnblockConfirmDialog = true
            )
        }
    }

    private fun dismissUnblockConfirmDialog() = intent {
        reduce { state.copy(showUnblockConfirmDialog = false) }
    }

    private fun confirmUnblockUser() = intent {
        val profile = state.profile ?: return@intent

        reduce { state.copy(showUnblockConfirmDialog = false) }

        postMutationDelegate.blockUser(profile.userId)
            .onSuccess { isBlocked ->
                if (!isBlocked) {
                    // 차단 해제, 양방향은 상대 차단 여부
                    reduce {
                        state.copy(
                            profile = profile.copy(
                                isBlockedByMe = false,
                                isBlocked = profile.isBlockedByOther
                            ),
                            showUnblockCompletedDialog = true
                        )
                    }
                    // 프로필·탭 재조회는 차단 해제 이벤트가 한 번 함
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "차단 해제 실패"))
                }
            }
    }

    private fun dismissUnblockCompletedDialog() = intent {
        reduce { state.copy(showUnblockCompletedDialog = false) }
    }

    // 포스트 탭

    private fun loadUserPosts() = intent {
        val targetUserId = userId ?: state.profile?.userId ?: return@intent
        reduce { state.copy(isPostsLoading = true) }

        val sort = state.postsSortType.apiValue
        val contextType = state.postsContextType?.apiValue

        getUserPostsUseCase(
            userId = targetUserId,
            limit = PAGE_SIZE,
            sort = sort,
            contextType = contextType
        )
            .onSuccess { (posts, nextCursor) ->
                if (state.postsSortType.apiValue != sort || state.postsContextType?.apiValue != contextType) return@intent
                postsGeneration.next()
                reduce {
                    state.copy(
                        userPosts = posts,
                        isPostsLoading = false,
                        postsNextCursor = nextCursor,
                        postsHasMore = nextCursor != null
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isPostsLoading = false,
                        snackbarState = SnackbarState(error.message ?: "포스트를 불러올 수 없습니다")
                    )
                }
            }
    }

    private fun loadMorePosts(): Job = intent {  // 자기를 다시 불러 타입 명시
        if (state.isPostsLoading || !state.postsHasMore) return@intent

        val targetUserId = userId ?: state.profile?.userId ?: return@intent
        val cursor = state.postsNextCursor
        val generation = postsGeneration.current()
        reduce { state.copy(isPostsLoading = true) }

        val sort = state.postsSortType.apiValue
        val contextType = state.postsContextType?.apiValue

        getUserPostsUseCase(
            userId = targetUserId,
            cursor = cursor,
            limit = PAGE_SIZE,
            sort = sort,
            contextType = contextType
        )
            .onSuccess { (posts, nextCursor) ->
                if (state.postsSortType.apiValue != sort || state.postsContextType?.apiValue != contextType) return@intent
                if (postsGeneration.current() != generation) {
                    // 정렬을 바꿔 비운 목록이면 새 1페이지가 맡음
                    if (!state.isPostsLoading && state.userPosts.isNotEmpty()) loadMorePosts()  // 목록 수가 그대로면 트리거가 안 걸려 다시 요청
                    return@intent
                }
                reduce {
                    state.copy(
                        userPosts = (state.userPosts + posts).distinctBy { it.id },
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
        val targetUserId = userId ?: state.profile?.userId ?: return@intent
        if (state.isPostsLoading) return@intent
        val sort = state.postsSortType.apiValue
        val contextType = state.postsContextType?.apiValue
        getUserPostsUseCase(
            userId = targetUserId,
            limit = PAGE_SIZE,
            sort = sort,
            contextType = contextType
        ).onSuccess { (posts, nextCursor) ->
            if (state.postsSortType.apiValue != sort || state.postsContextType?.apiValue != contextType) return@intent
            if (state.isPostsLoading) return@intent  // 그사이 더보기·새로고침이 시작됨
            reduce {
                val merged = state.userPosts.mergeFreshHead(posts) { it.id }
                // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                val keepsTail = merged.size > posts.size
                state.copy(
                    userPosts = merged,
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
                userPosts = emptyList(),
                postsNextCursor = null,
                postsHasMore = true
            )
        }
        loadUserPosts()
    }

    private fun changePostsContextType(contextType: PostContextType?) = intent {
        if (state.postsContextType == contextType) return@intent
        postsGeneration.next()  // 옛 범위로 진행 중이던 더보기 버림
        reduce {
            state.copy(
                postsContextType = contextType,
                userPosts = emptyList(),
                postsNextCursor = null,
                postsHasMore = true
            )
        }
        loadUserPosts()
    }

    private fun likePost(postId: Int) = intent {
        postMutationDelegate.togglePostLike(postId, shouldLike = true)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("좋아요 실패")) } }
    }

    private fun unlikePost(postId: Int) = intent {
        postMutationDelegate.togglePostLike(postId, shouldLike = false)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("좋아요 취소 실패")) } }
    }

    private fun navigateToPostDetail(postId: Int) = intent {
        postSideEffect(UserProfileSideEffect.NavigateToPostDetail(postId))
    }

    private fun showPostOptions(post: Post) = intent {
        reduce {
            state.copy(
                showPostOptionsSheet = true,
                selectedPost = post
            )
        }
    }

    private fun dismissPostOptions() = intent {
        reduce {
            state.copy(
                showPostOptionsSheet = false,
                selectedPost = null
            )
        }
    }

    private fun editPost() = intent {
        val postId = state.selectedPost?.id ?: return@intent
        dismissPostOptions()
        postSideEffect(UserProfileSideEffect.NavigateToEditPost(postId))
    }

    // 커뮤니티 글은 커뮤니티 삭제로
    private fun deletePost() = intent {
        val post = state.selectedPost ?: return@intent
        dismissPostOptions()
        val result = if (post.contextType == PostContextType.COMMUNITY) {
            deleteCommunityPostUseCase(post.id)
        } else {
            deletePostUseCase(post.id)
        }
        result
            .onSuccess {
                reduce {
                    state.copy(
                        userPosts = state.userPosts.filter { it.id != post.id },
                        pinnedPosts = state.pinnedPosts.filter { it.id != post.id },
                        communityPosts = state.communityPosts.filter { it.id != post.id },
                        communityPinnedPosts = state.communityPinnedPosts.filter { it.id != post.id },
                        snackbarState = SnackbarState("포스트가 삭제되었습니다")
                    )
                }
                // 내 프로필로 들어왔으면 포스트 수도 서버 값으로
                if (state.profile?.isOwnProfile == true) {
                    getMyProfileUseCase().onSuccess { profile ->
                        reduce { state.copy(profile = state.profile?.copy(postCount = profile.postCount)) }
                    }
                }
            }
            .onFailure { error ->
                reduce { state.copy(snackbarState = SnackbarState(error.message ?: "삭제 실패")) }
            }
    }

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

    // 캐시 반영을 기다리지 않고 커뮤니티 목록도 바로 갱신
    private fun UserProfileState.updateCommunityPost(postId: Int, transform: (Post) -> Post) = copy(
        communityPosts = communityPosts.map { if (it.id == postId) transform(it) else it },
        communityPinnedPosts = communityPinnedPosts.map { if (it.id == postId) transform(it) else it }
    )

    private fun notInterestedPost() = intent {
        val post = state.selectedPost ?: return@intent
        val postId = post.id

        reduce {
            state.copy(
                showPostOptionsSheet = false,
                selectedPost = null
            )
        }

        postMutationDelegate.hidePost(postId)
            .onSuccess { isHidden ->
                if (isHidden) {
                    reduce {
                        state.copy(
                            userPosts = state.userPosts.filter { it.id != postId },
                            communityPosts = state.communityPosts.filter { it.id != postId },
                            communityPinnedPosts = state.communityPinnedPosts.filter { it.id != postId },
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

    private fun blockPostAuthor() = intent {
        val post = state.selectedPost ?: return@intent
        val authorId = post.author.id

        reduce {
            state.copy(
                showPostOptionsSheet = false,
                selectedPost = null
            )
        }

        postMutationDelegate.blockUser(authorId)
            .onSuccess { isBlocked ->
                if (isBlocked) {
                    reduce {
                        state.copy(
                            userPosts = state.userPosts.filter { it.author.id != authorId },
                            communityPosts = state.communityPosts.filter { it.author.id != authorId },
                            communityPinnedPosts = state.communityPinnedPosts.filter { it.author.id != authorId },
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

    // 답글 탭

    private fun loadUserCommentThreads() = intent {
        val targetUserId = userId ?: state.profile?.userId ?: return@intent
        reduce { state.copy(isRepliesLoading = true) }

        val sort = state.repliesSortType.apiValue
        val contextType = state.repliesContextType?.apiValue

        getUserCommentThreadsUseCase(
            userId = targetUserId,
            limit = REPLIES_PAGE_SIZE,
            sort = sort,
            contextType = contextType
        )
            .onSuccess { (threads, nextCursor) ->
                if (state.repliesSortType.apiValue != sort || state.repliesContextType?.apiValue != contextType) return@intent
                cacheThreadComments(threads)
                repliesGeneration.next()
                // 다 걸러진 페이지면 이어 받는 동안 로딩 표시 유지
                val chain = threads.isEmpty() && nextCursor != null
                reduce {
                    state.copy(
                        userCommentThreads = threads.distinctBy {
                            it.post.id to (it.comments.lastOrNull()?.id ?: 0)
                        },
                        isRepliesLoading = chain,
                        repliesNextCursor = nextCursor,
                        repliesHasMore = nextCursor != null
                    )
                }
                if (chain) loadMoreCommentThreads(chained = true)
            }
            .onFailure { error ->
                reduce {
                    state.copy(
                        isRepliesLoading = false,
                        snackbarState = SnackbarState(error.message ?: "답글을 불러올 수 없습니다")
                    )
                }
            }
    }

    // 다 걸러진 페이지 이어 받기면 로딩 가드 건너뜀

    private fun loadMoreCommentThreads(chained: Boolean = false): Job = intent {  // 자기를 다시 불러 타입 명시
        if ((state.isRepliesLoading && !chained) || !state.repliesHasMore) return@intent

        val targetUserId = userId ?: state.profile?.userId ?: return@intent
        val cursor = state.repliesNextCursor
        val generation = repliesGeneration.current()
        reduce { state.copy(isRepliesLoading = true) }

        val sort = state.repliesSortType.apiValue
        val contextType = state.repliesContextType?.apiValue

        getUserCommentThreadsUseCase(
            userId = targetUserId,
            cursor = cursor,
            limit = REPLIES_PAGE_SIZE,
            sort = sort,
            contextType = contextType
        )
            .onSuccess { (threads, nextCursor) ->
                if (state.repliesSortType.apiValue != sort || state.repliesContextType?.apiValue != contextType) return@intent
                if (repliesGeneration.current() != generation) {
                    // 정렬을 바꿔 비운 목록이면 새 1페이지가 맡음
                    if (!state.isRepliesLoading && state.userCommentThreads.isNotEmpty()) loadMoreCommentThreads()  // 목록 수가 그대로면 트리거가 안 걸려 다시 요청
                    return@intent
                }
                cacheThreadComments(threads)
                val chain = threads.isEmpty() && nextCursor != null
                reduce {
                    state.copy(
                        userCommentThreads = (state.userCommentThreads + threads).distinctBy {
                            it.post.id to (it.comments.lastOrNull()?.id ?: 0)
                        },
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
        val targetUserId = userId ?: state.profile?.userId ?: return@intent
        if (state.isRepliesLoading) return@intent
        val sort = state.repliesSortType.apiValue
        val contextType = state.repliesContextType?.apiValue
        getUserCommentThreadsUseCase(
            userId = targetUserId,
            limit = REPLIES_PAGE_SIZE,
            sort = sort,
            contextType = contextType
        ).onSuccess { (threads, nextCursor) ->
            if (state.repliesSortType.apiValue != sort || state.repliesContextType?.apiValue != contextType) return@intent
            if (state.isRepliesLoading) return@intent  // 그사이 더보기·새로고침이 시작됨
            if (threads.isEmpty() && nextCursor != null) return@intent  // 첫 페이지가 다 걸러짐, 받아 둔 목록 유지
            cacheThreadComments(threads)
            val threadKey = { thread: CommentThreadData -> thread.post.id to (thread.comments.lastOrNull()?.id ?: 0) }
            reduce {
                val fresh = threads.distinctBy(threadKey)
                val merged = state.userCommentThreads.mergeFreshHead(fresh, threadKey)
                // 뒷부분을 이어 붙였으면 다음 커서도 그대로
                val keepsTail = merged.size > fresh.size
                state.copy(
                    userCommentThreads = merged,
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
                userCommentThreads = emptyList(),
                repliesNextCursor = null,
                repliesHasMore = true
            )
        }
        loadUserCommentThreads()
    }

    private fun changeRepliesContextType(contextType: PostContextType?) = intent {
        if (state.repliesContextType == contextType) return@intent
        repliesGeneration.next()  // 옛 범위로 진행 중이던 더보기 버림
        reduce {
            state.copy(
                repliesContextType = contextType,
                userCommentThreads = emptyList(),
                repliesNextCursor = null,
                repliesHasMore = true
            )
        }
        loadUserCommentThreads()
    }

    private fun likeComment(commentId: Int) = toggleThreadCommentLike(commentId, shouldLike = true)

    private fun unlikeComment(commentId: Int) =
        toggleThreadCommentLike(commentId, shouldLike = false)

    private fun toggleThreadCommentLike(commentId: Int, shouldLike: Boolean) = intent {
        val prev = state.userCommentThreads.firstNotNullOfOrNull { thread -> thread.comments.find { it.id == commentId } }
            ?: return@intent
        // 스레드 맨 앞 부모 댓글은 캐시 밖이라 목록을 직접 갱신
        if (prev.isLiked != shouldLike) {
            reduce {
                state.copy(
                    userCommentThreads = state.userCommentThreads.updateComment(commentId) {
                        it.copy(isLiked = shouldLike, likeCount = maxOf(0, it.likeCount + if (shouldLike) 1 else -1))
                    }
                )
            }
        }
        postMutationDelegate.toggleCommentLike(commentId, shouldLike)
            ?.onSuccess { result ->
                reduce {
                    state.copy(
                        userCommentThreads = state.userCommentThreads.updateComment(commentId) {
                            it.copy(isLiked = result.isLiked, likeCount = result.likeCount)
                        }
                    )
                }
            }
            ?.onFailure {
                reduce {
                    state.copy(
                        userCommentThreads = state.userCommentThreads.updateComment(commentId) {
                            it.copy(isLiked = prev.isLiked, likeCount = prev.likeCount)
                        },
                        snackbarState = SnackbarState("좋아요 처리 실패")
                    )
                }
            }
    }

    // 답글 탭 원글은 캐시 밖이라 목록을 직접 갱신
    private fun toggleThreadPostLike(postId: Int) = intent {
        val prev = state.userCommentThreads.firstOrNull { it.post.id == postId }?.post ?: return@intent
        val shouldLike = !prev.isLiked
        reduce {
            state.copy(
                userCommentThreads = state.userCommentThreads.updatePost(postId) {
                    it.copy(isLiked = shouldLike, likeCount = maxOf(0, prev.likeCount + if (shouldLike) 1 else -1))
                }
            )
        }
        postMutationDelegate.togglePostLike(postId, shouldLike)
            ?.onSuccess { result ->
                reduce {
                    state.copy(
                        userCommentThreads = state.userCommentThreads.updatePost(postId) {
                            it.copy(isLiked = result.isLiked, likeCount = result.likeCount)
                        }
                    )
                }
            }
            ?.onFailure {
                reduce {
                    state.copy(
                        userCommentThreads = state.userCommentThreads.updatePost(postId) {
                            it.copy(isLiked = prev.isLiked, likeCount = prev.likeCount)
                        },
                        snackbarState = SnackbarState("좋아요 처리 실패")
                    )
                }
            }
    }

    private fun navigateToCommentDetail(commentId: Int) = intent {
        postSideEffect(UserProfileSideEffect.NavigateToCommentDetail(commentId))
    }

    private fun navigateToUserProfile(userId: Int) = intent {
        postSideEffect(UserProfileSideEffect.NavigateToUserProfile(userId))
    }

    private fun showDeletedPostSnackbar() = intent {
        postSideEffect(UserProfileSideEffect.ShowToast("삭제된 포스트입니다"))
    }

    private fun showRestrictedSnackbar(message: String) = intent {
        reduce { state.copy(snackbarState = SnackbarState(message)) }
    }

    // 댓글 옵션

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

    private fun bookmarkComment() = intent {
        val comment = state.selectedComment ?: return@intent
        val shouldBookmark = !comment.isBookmarked

        // 부모 댓글은 캐시 밖이라 목록을 직접 갱신
        reduce {
            state.copy(
                userCommentThreads = state.userCommentThreads.updateComment(comment.id) {
                    it.copy(isBookmarked = shouldBookmark)
                },
                showCommentOptionsSheet = false,
                selectedComment = null,
                selectedCommentThread = null,
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }

        postMutationDelegate.toggleCommentBookmark(comment.id, shouldBookmark)
            ?.onFailure {
                reduce {
                    state.copy(
                        userCommentThreads = state.userCommentThreads.updateComment(comment.id) {
                            it.copy(isBookmarked = comment.isBookmarked)
                        },
                        snackbarState = SnackbarState("저장 실패")
                    )
                }
            }
    }

    private fun blockCommentAuthor() = intent {
        val comment = state.selectedComment ?: return@intent
        val authorId = comment.author.id

        reduce {
            state.copy(
                showCommentOptionsSheet = false,
                selectedComment = null,
                selectedCommentThread = null
            )
        }

        postMutationDelegate.blockUser(authorId)
            .onSuccess { isBlocked ->
                if (isBlocked) {
                    reduce {
                        state.copy(
                            userCommentThreads = state.userCommentThreads.map { thread ->
                                thread.copy(
                                    comments = thread.comments.filter { it.author.id != authorId }
                                )
                            }.filter { it.comments.isNotEmpty() },
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

    // 스레드 포스트 옵션

    private fun showThreadPostOptions(
        post: PostSummaryData
    ) = intent {
        reduce {
            state.copy(
                showThreadPostOptionsSheet = true,
                selectedThreadPost = post
            )
        }
    }

    private fun dismissThreadPostOptions() = intent {
        reduce {
            state.copy(
                showThreadPostOptionsSheet = false,
                selectedThreadPost = null
            )
        }
    }

    private fun bookmarkThreadPost() = intent {
        val post = state.selectedThreadPost ?: return@intent
        val prevIsBookmarked = post.isBookmarked
        val shouldBookmark = !prevIsBookmarked

        reduce {
            state.copy(
                showThreadPostOptionsSheet = false,
                selectedThreadPost = null,
                userCommentThreads = state.userCommentThreads.map { thread ->
                    if (thread.post.id == post.id) {
                        thread.copy(post = thread.post.copy(isBookmarked = shouldBookmark))
                    } else thread
                },
                snackbarState = SnackbarState(if (shouldBookmark) "저장됨" else "저장 취소됨")
            )
        }

        postMutationDelegate.togglePostBookmark(post.id, shouldBookmark)
            ?.onFailure {
                reduce {
                    state.copy(
                        userCommentThreads = state.userCommentThreads.map { thread ->
                            if (thread.post.id == post.id) {
                                thread.copy(post = thread.post.copy(isBookmarked = prevIsBookmarked))
                            } else thread
                        },
                        snackbarState = SnackbarState("저장 실패")
                    )
                }
            }
    }

    private fun notInterestedThreadPost() = intent {
        val post = state.selectedThreadPost ?: return@intent
        val postId = post.id

        reduce {
            state.copy(
                showThreadPostOptionsSheet = false,
                selectedThreadPost = null
            )
        }

        postMutationDelegate.hidePost(postId)
            .onSuccess { isHidden ->
                if (isHidden) {
                    reduce {
                        state.copy(
                            userCommentThreads = state.userCommentThreads.filter { it.post.id != postId },
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

    private fun blockThreadPostAuthor() = intent {
        val post = state.selectedThreadPost ?: return@intent
        val authorId = post.author.id

        reduce {
            state.copy(
                showThreadPostOptionsSheet = false,
                selectedThreadPost = null
            )
        }

        postMutationDelegate.blockUser(authorId)
            .onSuccess { isBlocked ->
                if (isBlocked) {
                    reduce {
                        state.copy(
                            userCommentThreads = state.userCommentThreads.filter { it.post.author.id != authorId },
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

    // 홈 탭, Creator 전용

    private fun loadHomeSections(quiet: Boolean = false) = intent {
        val profile = state.profile ?: return@intent
        if (!profile.isCreator) return@intent
        val creatorId = profile.userId

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

        reduce { state.copy(isHomeSectionsLoading = false) }
        if (!quiet && (postErrorMessage != null || storeErrorMessage != null)) {  // 복귀 새로고침 실패는 조용히
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
            UserProfileSideEffect.NavigateToSectionDetail(
                sectionType = sectionType,
                creatorId = creatorId,
                tagId = tagId,
                title = title
            )
        )
    }

    private fun navigateToProductDetail(productId: Int) = intent {
        postSideEffect(UserProfileSideEffect.NavigateToProductDetail(productId))
    }

    private fun followOtherUser(userId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow = true)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("팔로우 실패")) } }
    }

    private fun unfollowOtherUser(userId: Int) = intent {
        postMutationDelegate.toggleAuthorFollow(userId, shouldFollow = false)
            ?.onFailure { reduce { state.copy(snackbarState = SnackbarState("팔로우 취소 실패")) } }
    }

    // 커뮤니티 탭, Creator 전용

    private fun loadCommunityPosts(quiet: Boolean = false) = intent {
        val profile = state.profile ?: return@intent
        if (!profile.isCreator) return@intent
        val creatorId = profile.userId

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
                        isCommunityLoading = false,
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
        val profile = state.profile ?: return@intent
        if (!profile.isCreator || state.isCommunityLoading) return@intent
        getCommunityPostsUseCase(profile.userId, limit = PAGE_SIZE)
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
                    if (post.id == postId) post.copy(
                        isLiked = newIsLiked,
                        likeCount = newCount
                    ) else post
                },
                communityPinnedPosts = state.communityPinnedPosts.map { post ->
                    if (post.id == postId) post.copy(
                        isLiked = newIsLiked,
                        likeCount = newCount
                    ) else post
                }
            )
        }

        postMutationDelegate.togglePostLike(postId, shouldLike = newIsLiked)
            ?.onSuccess { result ->
                reduce {
                    state.copy(
                        communityPosts = state.communityPosts.map { post ->
                            if (post.id == postId) post.copy(
                                isLiked = result.isLiked,
                                likeCount = result.likeCount
                            ) else post
                        },
                        communityPinnedPosts = state.communityPinnedPosts.map { post ->
                            if (post.id == postId) post.copy(
                                isLiked = result.isLiked,
                                likeCount = result.likeCount
                            ) else post
                        }
                    )
                }
            }
            ?.onFailure {
                reduce {
                    state.copy(
                        communityPosts = state.communityPosts.map { post ->
                            if (post.id == postId) post.copy(
                                isLiked = prevIsLiked,
                                likeCount = prevCount
                            ) else post
                        },
                        communityPinnedPosts = state.communityPinnedPosts.map { post ->
                            if (post.id == postId) post.copy(
                                isLiked = prevIsLiked,
                                likeCount = prevCount
                            ) else post
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

    private fun createCommunityPost() = intent {
        val creatorId = state.profile?.userId ?: return@intent
        postSideEffect(UserProfileSideEffect.NavigateToCreateCommunityPost(creatorId))
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
        postSideEffect(UserProfileSideEffect.NavigateToMessage(roomId))
    }

    // 스토어 탭, Creator 전용

    private fun loadStoreProducts(sortType: ProductSortType? = null) = intent {
        val profile = state.profile ?: return@intent
        if (!profile.isCreator) return@intent
        val creatorId = profile.userId

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
                        isStoreLoading = false,
                        snackbarState = SnackbarState(error.message ?: "스토어를 불러올 수 없습니다")
                    )
                }
            }
    }

    private fun reloadLoadedStoreProducts() = intent {
        val profile = state.profile ?: return@intent
        if (!profile.isCreator || state.isStoreLoading) return@intent
        val basePage = state.storePage
        val generation = storeGeneration.current()
        val sort = state.storeSortType
        val loadedCount = basePage * STORE_PAGE_SIZE
        profileRepository.getCreatorStoreSections(
            creatorId = profile.userId,
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
                reduce {
                    state.copy(
                        isStoreLoading = false,
                        snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                    )
                }
            }
    }
}
