package com.ninezero.halo.feature.profile.presentation.my_profile

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

data class MyProfileState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val snackbarState: SnackbarState? = null,

    // 탭, 크리에이터 5탭·일반 2탭
    val selectedTab: ProfileTab = ProfileTab.POSTS,

    // 포스트 탭
    val pendingPosts: List<Post> = emptyList(),
    val myPosts: List<Post> = emptyList(),
    val isPostsLoading: Boolean = false,
    val postsNextCursor: String? = null,
    val postsHasMore: Boolean = true,
    val postsSortType: UserPostSortType = UserPostSortType.LATEST,
    val postsContextType: PostContextType? = PostContextType.GENERAL,

    // 답글 탭
    val myCommentThreads: List<CommentThreadData> = emptyList(),
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

    // 홈 탭, Creator 전용
    val isHomeSectionsLoading: Boolean = false,
    val pinnedPosts: List<Post> = emptyList(),
    val postSections: List<PostTagSection> = emptyList(),
    val productSections: List<ProductTagSection> = emptyList(),

    // 커뮤니티 탭, Creator 전용
    val communityPosts: List<Post> = emptyList(),
    val pendingCommunityPosts: List<Post> = emptyList(),  // 업로드 중인 커뮤니티 글
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

sealed interface MyProfileIntent {
    data object LoadProfile : MyProfileIntent
    data object RefreshProfile : MyProfileIntent
    data object RefreshOnResume : MyProfileIntent  // 표시 없이 현재 탭 갱신
    data object NavigateToEditProfile : MyProfileIntent
    data object NavigateToSettings : MyProfileIntent
    data object NavigateToMembershipManage : MyProfileIntent
    data object NavigateToMySubscriptions : MyProfileIntent
    data object NavigateToPoint : MyProfileIntent
    data object NavigateToCoupon : MyProfileIntent
    data object NavigateToCouponManage : MyProfileIntent
    data object NavigateToMyReviews : MyProfileIntent
    data object NavigateToAddress : MyProfileIntent
    data object NavigateToWishlist : MyProfileIntent
    data object NavigateToCart : MyProfileIntent
    data object NavigateToMyOrders : MyProfileIntent
    data object NavigateToMyProducts : MyProfileIntent
    data object NavigateToSalesManage : MyProfileIntent
    data object SnackbarDismissed : MyProfileIntent

    // 탭 관련
    data class SelectTab(val tab: ProfileTab) : MyProfileIntent

    // 포스트 탭
    data object LoadMorePosts : MyProfileIntent
    data class ChangePostsSortType(val sortType: UserPostSortType) : MyProfileIntent
    data class ChangePostsContextType(val contextType: PostContextType?) : MyProfileIntent
    data class LikePost(val postId: Int) : MyProfileIntent
    data class UnlikePost(val postId: Int) : MyProfileIntent
    data class NavigateToPostDetail(val postId: Int) : MyProfileIntent
    data class ShowPostOptions(val post: Post) : MyProfileIntent
    data object DismissPostOptions : MyProfileIntent
    data object DeletePost : MyProfileIntent
    data object EditPost : MyProfileIntent
    data object PinPost : MyProfileIntent
    // 커뮤니티 탭 멤버 글
    data object BookmarkPost : MyProfileIntent
    data object BlockPostAuthor : MyProfileIntent

    // 답글 탭
    data object LoadMoreCommentThreads : MyProfileIntent
    data class ChangeRepliesSortType(val sortType: UserPostSortType) : MyProfileIntent
    data class ChangeRepliesContextType(val contextType: PostContextType?) : MyProfileIntent
    data class LikeComment(val commentId: Int) : MyProfileIntent
    data class UnlikeComment(val commentId: Int) : MyProfileIntent
    data class ToggleThreadPostLike(val postId: Int) : MyProfileIntent
    data class NavigateToCommentDetail(val commentId: Int) : MyProfileIntent
    data class NavigateToUserProfile(val userId: Int) : MyProfileIntent
    data object ShowDeletedPostSnackbar : MyProfileIntent
    data object ShowHiddenPostSnackbar : MyProfileIntent
    // 팔로우
    data class FollowUser(val userId: Int) : MyProfileIntent
    data class UnfollowUser(val userId: Int) : MyProfileIntent

    // 댓글 옵션
    data class ShowCommentOptions(val comment: ThreadCommentData, val thread: CommentThreadData) : MyProfileIntent
    data object DismissCommentOptions : MyProfileIntent
    data object DeleteComment : MyProfileIntent
    data object BookmarkComment : MyProfileIntent
    data object BlockCommentAuthor : MyProfileIntent

    // 스레드 포스트 옵션
    data class ShowThreadPostOptions(val post: PostSummaryData) : MyProfileIntent
    data object DismissThreadPostOptions : MyProfileIntent
    data object BookmarkThreadPost : MyProfileIntent
    data object BlockThreadPostAuthor : MyProfileIntent

    // 홈 탭, Creator 전용
    data class NavigateToSectionDetail(
        val sectionType: SectionType,
        val tagId: Int? = null,
        val title: String
    ) : MyProfileIntent
    data class NavigateToProductDetail(val productId: Int) : MyProfileIntent

    // 커뮤니티 탭, Creator 전용
    data object LoadCommunityPosts : MyProfileIntent
    data object LoadMoreCommunityPosts : MyProfileIntent
    data class LikeCommunityPost(val postId: Int) : MyProfileIntent
    data class ShowCommunityPostOptions(val post: Post) : MyProfileIntent
    data object EditCommunityPost : MyProfileIntent
    data object DeleteCommunityPost : MyProfileIntent
    data object PinCommunityPost : MyProfileIntent
    data object CreateCommunityPost : MyProfileIntent

    // 공유
    data class SharePost(val postId: Int) : MyProfileIntent
    data object DismissShareSheet : MyProfileIntent
    data class ShareCompleted(val sharedRoomIds: List<Int>, val recipientCount: Int) : MyProfileIntent
    data class NavigateToChatRoom(val roomId: Int) : MyProfileIntent

    // 스토어 탭, Creator 전용
    data object LoadStoreProducts : MyProfileIntent
    data object LoadMoreStoreProducts : MyProfileIntent
    data class ChangeStoreSortType(val sortType: ProductSortType) : MyProfileIntent
}

sealed interface MyProfileSideEffect {
    data object NavigateToEditProfile : MyProfileSideEffect
    data object NavigateToSettings : MyProfileSideEffect
    data object NavigateToMembershipManage : MyProfileSideEffect
    data object NavigateToMySubscriptions : MyProfileSideEffect
    data object NavigateToPoint : MyProfileSideEffect
    data object NavigateToCoupon : MyProfileSideEffect
    data object NavigateToCouponManage : MyProfileSideEffect
    data object NavigateToMyReviews : MyProfileSideEffect
    data object NavigateToAddress : MyProfileSideEffect
    data object NavigateToWishlist : MyProfileSideEffect
    data object NavigateToCart : MyProfileSideEffect
    data object NavigateToMyOrders : MyProfileSideEffect
    data object NavigateToMyProducts : MyProfileSideEffect
    data object NavigateToSalesManage : MyProfileSideEffect
    data class NavigateToPostDetail(val postId: Int) : MyProfileSideEffect
    data class NavigateToCommentDetail(val commentId: Int) : MyProfileSideEffect
    data class NavigateToUserProfile(val userId: Int) : MyProfileSideEffect
    data class NavigateToEditPost(val postId: Int) : MyProfileSideEffect
    data class NavigateToProductDetail(val productId: Int) : MyProfileSideEffect
    data class NavigateToCreateCommunityPost(val creatorId: Int) : MyProfileSideEffect
    data class NavigateToChatRoom(val roomId: Int) : MyProfileSideEffect
    data class NavigateToSectionDetail(
        val sectionType: SectionType,
        val creatorId: Int,
        val tagId: Int?,
        val title: String
    ) : MyProfileSideEffect
}
