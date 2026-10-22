package com.ninezero.halo.feature.profile.presentation.user_profile

import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.feature.feed.domain.model.Post
import com.ninezero.halo.feature.feed.domain.model.PostContextType
import com.ninezero.halo.feature.profile.domain.model.CommentThreadData
import com.ninezero.halo.feature.profile.domain.model.PostSummaryData
import com.ninezero.halo.feature.profile.domain.model.PostTagSection
import com.ninezero.halo.feature.profile.domain.model.ProductTagSection
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.domain.model.ThreadCommentData
import com.ninezero.halo.feature.profile.domain.model.UserProfile
import com.ninezero.halo.feature.profile.presentation.components.ProductSortType
import com.ninezero.halo.feature.profile.presentation.components.ProfileTab
import com.ninezero.halo.feature.profile.presentation.components.UserPostSortType
import com.ninezero.halo.feature.store.domain.model.Product

data class UserProfileState(
    val isRefreshing: Boolean = false,
    val profile: UserProfile? = null,
    val snackbarState: SnackbarState? = null,

    // 현재 로그인한 사용자 정보
    val myUserId: Int? = null,

    // 탭, Creator HOME·일반 POSTS
    val selectedTab: ProfileTab = ProfileTab.POSTS,

    // 포스트 탭
    val userPosts: List<Post> = emptyList(),
    val isPostsLoading: Boolean = false,
    val postsNextCursor: String? = null,
    val postsHasMore: Boolean = true,
    val postsSortType: UserPostSortType = UserPostSortType.LATEST,
    val postsContextType: PostContextType? = PostContextType.GENERAL,

    // 답글 탭
    val userCommentThreads: List<CommentThreadData> = emptyList(),
    val isRepliesLoading: Boolean = false,
    val repliesNextCursor: String? = null,
    val repliesHasMore: Boolean = true,
    val repliesSortType: UserPostSortType = UserPostSortType.LATEST,
    val repliesContextType: PostContextType? = PostContextType.GENERAL,

    // 포스트 옵션
    val showPostOptionsSheet: Boolean = false,
    val selectedPost: Post? = null,

    // 댓글 옵션
    val showCommentOptionsSheet: Boolean = false,
    val selectedComment: ThreadCommentData? = null,
    val selectedCommentThread: CommentThreadData? = null,

    // 스레드 포스트 옵션
    val showThreadPostOptionsSheet: Boolean = false,
    val selectedThreadPost: PostSummaryData? = null,

    // 프로필 옵션
    val showProfileOptionsSheet: Boolean = false,

    // 알림 벨
    val showNotifyFollowDialog: Boolean = false,

    // 차단/해제 다이얼로그
    val showBlockConfirmDialog: Boolean = false,
    val showUnblockConfirmDialog: Boolean = false,
    val showBlockCompletedDialog: Boolean = false,
    val showUnblockCompletedDialog: Boolean = false,

    // 홈 탭, Creator 전용
    val isHomeSectionsLoading: Boolean = false,
    val pinnedPosts: List<Post> = emptyList(),
    val postSections: List<PostTagSection> = emptyList(),
    val productSections: List<ProductTagSection> = emptyList(),

    // 커뮤니티 탭, Creator 전용
    val communityPosts: List<Post> = emptyList(),
    val pendingCommunityPosts: List<Post> = emptyList(),
    val communityPinnedPosts: List<Post> = emptyList(),
    val isCommunityLoading: Boolean = false,
    val communityNextCursor: String? = null,
    val communityHasMore: Boolean = true,

    // 스토어 탭, Creator 전용
    val storeProducts: List<Product> = emptyList(),
    val storeProductSections: List<ProductTagSection> = emptyList(),
    val isStoreLoading: Boolean = false,
    val storePage: Int = 1,
    val storeHasMore: Boolean = true,
    val storeSortType: ProductSortType = ProductSortType.LATEST,

    // 공유
    val showShareSheet: Boolean = false,
    val sharePostId: Int? = null,
    val navigateRoomId: Int? = null
)

sealed interface UserProfileIntent {
    data object LoadProfile : UserProfileIntent
    data object RefreshProfile : UserProfileIntent
    data object RefreshOnResume : UserProfileIntent  // 표시 없이 현재 탭 갱신
    data object NavigateBack : UserProfileIntent
    data object SnackbarDismissed : UserProfileIntent

    // 팔로우 관련
    data object ToggleFollow : UserProfileIntent

    // 알림 벨
    data object NotifyBellClick : UserProfileIntent
    data object ConfirmNotifyFollow : UserProfileIntent
    data object DismissNotifyFollowDialog : UserProfileIntent

    // 메시지
    data object SendMessage : UserProfileIntent

    // 더보기 메뉴
    data object ShowMoreOptions : UserProfileIntent
    data object DismissProfileOptions : UserProfileIntent

    // 차단 관련
    data object ShowBlockConfirmDialog : UserProfileIntent
    data object DismissBlockConfirmDialog : UserProfileIntent
    data object ConfirmBlockUser : UserProfileIntent
    data object DismissBlockCompletedDialog : UserProfileIntent

    // 차단 해제 관련
    data object ShowUnblockConfirmDialog : UserProfileIntent
    data object DismissUnblockConfirmDialog : UserProfileIntent
    data object ConfirmUnblockUser : UserProfileIntent
    data object DismissUnblockCompletedDialog : UserProfileIntent

    // 탭 관련
    data class SelectTab(val tab: ProfileTab) : UserProfileIntent

    // 포스트 탭
    data object LoadMorePosts : UserProfileIntent
    data class ChangePostsSortType(val sortType: UserPostSortType) : UserProfileIntent
    data class ChangePostsContextType(val contextType: PostContextType?) : UserProfileIntent
    data class LikePost(val postId: Int) : UserProfileIntent
    data class UnlikePost(val postId: Int) : UserProfileIntent
    data class NavigateToPostDetail(val postId: Int) : UserProfileIntent
    data class ShowPostOptions(val post: Post) : UserProfileIntent
    data object DismissPostOptions : UserProfileIntent
    data object BookmarkPost : UserProfileIntent
    data object BlockPostAuthor : UserProfileIntent
    // 내 글, 내 프로필 진입 또는 남의 커뮤니티에 쓴 글
    data object EditPost : UserProfileIntent
    data object DeletePost : UserProfileIntent

    // 답글 탭
    data object LoadMoreCommentThreads : UserProfileIntent
    data class ChangeRepliesSortType(val sortType: UserPostSortType) : UserProfileIntent
    data class ChangeRepliesContextType(val contextType: PostContextType?) : UserProfileIntent
    data class LikeComment(val commentId: Int) : UserProfileIntent
    data class UnlikeComment(val commentId: Int) : UserProfileIntent
    data class ToggleThreadPostLike(val postId: Int) : UserProfileIntent
    data class NavigateToCommentDetail(val commentId: Int) : UserProfileIntent
    data class NavigateToUserProfile(val userId: Int) : UserProfileIntent
    data object ShowDeletedPostSnackbar : UserProfileIntent
    data class ShowRestrictedContentSnackbar(val message: String) : UserProfileIntent

    // 다른 사용자 팔로우
    data class FollowOtherUser(val userId: Int) : UserProfileIntent
    data class UnfollowOtherUser(val userId: Int) : UserProfileIntent
    // 댓글 옵션
    data class ShowCommentOptions(val comment: ThreadCommentData, val thread: CommentThreadData) : UserProfileIntent
    data object DismissCommentOptions : UserProfileIntent
    data object BookmarkComment : UserProfileIntent
    data object BlockCommentAuthor : UserProfileIntent

    // 스레드 포스트 옵션
    data class ShowThreadPostOptions(val post: PostSummaryData) : UserProfileIntent
    data object DismissThreadPostOptions : UserProfileIntent
    data object BookmarkThreadPost : UserProfileIntent
    data object BlockThreadPostAuthor : UserProfileIntent

    // 홈 탭, Creator 전용
    data class NavigateToSectionDetail(
        val sectionType: SectionType,
        val tagId: Int? = null,
        val title: String
    ) : UserProfileIntent
    data class NavigateToProductDetail(val productId: Int) : UserProfileIntent

    // 커뮤니티 탭, Creator 전용
    data object LoadCommunityPosts : UserProfileIntent
    data object LoadMoreCommunityPosts : UserProfileIntent
    data class LikeCommunityPost(val postId: Int) : UserProfileIntent
    data class ShowCommunityPostOptions(val post: Post) : UserProfileIntent
    data object CreateCommunityPost : UserProfileIntent

    // 공유
    data class SharePost(val postId: Int) : UserProfileIntent
    data object DismissShareSheet : UserProfileIntent
    data class ShareCompleted(val sharedRoomIds: List<Int>, val recipientCount: Int) : UserProfileIntent
    data class NavigateToChatRoom(val roomId: Int) : UserProfileIntent

    // 스토어 탭, Creator 전용
    data object LoadStoreProducts : UserProfileIntent
    data object LoadMoreStoreProducts : UserProfileIntent
    data class ChangeStoreSortType(val sortType: ProductSortType) : UserProfileIntent
}

sealed interface UserProfileSideEffect {
    data object NavigateBack : UserProfileSideEffect
    data class ShowToast(val message: String) : UserProfileSideEffect
    data class NavigateToPostDetail(val postId: Int) : UserProfileSideEffect
    data class NavigateToCommentDetail(val commentId: Int) : UserProfileSideEffect
    data class NavigateToEditPost(val postId: Int) : UserProfileSideEffect
    data class NavigateToUserProfile(val userId: Int) : UserProfileSideEffect
    data class NavigateToMessage(val roomId: Int) : UserProfileSideEffect
    data class NavigateToNewChat(  // draft 진입, 첫 메시지 때 방 생성
        val targetUserId: Int,
        val username: String = "",
        val displayName: String = "",
        val avatarUrl: String = "",
        val role: String = "USER",
        val isBlockedByMe: Boolean = false,
        val isBlockedByOther: Boolean = false
    ) : UserProfileSideEffect
    data class NavigateToProductDetail(val productId: Int) : UserProfileSideEffect
    data class NavigateToCreateCommunityPost(val creatorId: Int) : UserProfileSideEffect
    data class NavigateToSectionDetail(
        val sectionType: SectionType,
        val creatorId: Int,
        val tagId: Int?,
        val title: String
    ) : UserProfileSideEffect
}
