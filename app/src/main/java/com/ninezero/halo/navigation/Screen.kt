package com.ninezero.halo.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    // 인증
    @Serializable data object Welcome : Screen
    @Serializable data object Login : Screen
    @Serializable data object Register : Screen
    @Serializable data class VerifyEmail(val email: String) : Screen
    @Serializable data object ForgotPassword : Screen
    @Serializable data class ResetCode(val email: String) : Screen
    @Serializable data class ResetPassword(val resetToken: String) : Screen
    @Serializable data class SocialUsername(val signupToken: String, val suggestedUsername: String) : Screen

    // 메인
    @Serializable data object Main : Screen
    @Serializable data object Notifications : Screen

    // 피드
    @Serializable data class PostDetail(val postId: Int, val highlightCommentId: Int? = null) : Screen
    @Serializable data class CommentDetail(
        val commentId: Int,
        val highlightAnchor: Boolean = false,
        val highlightReplyId: Int? = null
    ) : Screen
    @Serializable data object CreatePost : Screen
    @Serializable data class EditPost(val postId: Int) : Screen
    @Serializable data class CreateCommunityPost(val creatorId: Int) : Screen
    @Serializable data class Reply(val postId: Int, val parentCommentId: Int? = null) : Screen

    // 프로필
    @Serializable data class UserProfile(val userId: Int) : Screen
    @Serializable data class FollowList(
        val userId: Int,
        val displayName: String = "",
        val initialTab: Int = 0,
        val followerCount: Int = 0,
        val followingCount: Int = 0,
        val subscriberCount: Int = 0,
        val showSubscribers: Boolean = false,
        val isMyProfile: Boolean = false
    ) : Screen
    @Serializable data class SectionDetail(
        val sectionType: String,
        val creatorId: Int,
        val tagId: Int? = null,
        val title: String = ""
    ) : Screen
    @Serializable data object EditProfile : Screen

    // 채팅
    @Serializable data class ChatRoom(
        val roomId: String = "0",
        val targetUserId: Int = 0,
        val targetUsername: String = "",
        val targetDisplayName: String = "",
        val targetAvatarUrl: String = "",
        val targetRole: String = "USER",
        val targetBlockedByMe: Boolean = false,
        val targetBlockedByOther: Boolean = false
    ) : Screen
    @Serializable data object ArchivedChats : Screen

    // 상품
    @Serializable data class ProductDetail(val productId: Int) : Screen
    @Serializable data object Wishlist : Screen

    // 장바구니·주문
    @Serializable data object Cart : Screen
    @Serializable data class Checkout(
        val selectedItemIds: List<Int> = emptyList(),
        val directProductId: Int? = null,
        val directQuantity: Int? = null
    ) : Screen
    @Serializable data class OrderComplete(val orderId: Int, val orderNumber: String) : Screen
    @Serializable data class OrderDetail(val orderId: Int) : Screen
    @Serializable data object MyOrders : Screen

    // 리뷰
    @Serializable data class ReviewList(val productId: Int, val highlightReviewId: Int? = null) : Screen
    @Serializable data object MyReviews : Screen
    @Serializable data class CreateReview(
        val productId: Int,
        val orderId: Int,
        val productName: String,
        val productImageUrl: String = "",
        val reviewId: Int? = null,
        val rating: Int? = null,
        val content: String? = null
    ) : Screen

    // 멤버십·구독
    @Serializable data object MembershipManage : Screen
    @Serializable data object MySubscriptions : Screen

    // 포인트·쿠폰
    @Serializable data object Point : Screen
    @Serializable data object MyCoupons : Screen

    // 주소
    @Serializable data object AddressManage : Screen
    @Serializable data class AddressDetail(
        val addressId: Int = 0,
        val zipCode: String? = null,
        val address: String? = null
    ) : Screen

    // 크리에이터
    @Serializable data object CreatorApplication : Screen
    @Serializable data object MyProducts : Screen
    @Serializable data class CreateProduct(val productId: Int = 0) : Screen
    @Serializable data object SalesManage : Screen
    @Serializable data object SalesStats : Screen
    @Serializable data object CouponManage : Screen

    // 설정
    @Serializable data object Settings : Screen
    @Serializable data object NotificationSettings : Screen
    @Serializable data object CreatorNotifications : Screen
    @Serializable data object SavedPosts : Screen
    @Serializable data object LikedPosts : Screen
    @Serializable data object BlockedProfiles : Screen
    @Serializable data object PaymentMethods : Screen
    @Serializable data object Info : Screen
    @Serializable data object OpenSourceLicenses : Screen
}
