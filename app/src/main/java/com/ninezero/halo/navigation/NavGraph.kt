package com.ninezero.halo.navigation

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessStarted
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import androidx.navigationevent.NavigationEvent.Companion.EDGE_RIGHT
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.feature.address.presentation.address_detail.AddressDetailScreen
import com.ninezero.halo.feature.address.presentation.address_manage.AddressManageScreen
import com.ninezero.halo.feature.auth.presentation.forgot_password.ForgotPasswordScreen
import com.ninezero.halo.feature.auth.presentation.login.LoginScreen
import com.ninezero.halo.feature.auth.presentation.register.RegisterScreen
import com.ninezero.halo.feature.auth.presentation.reset_code.ResetCodeScreen
import com.ninezero.halo.feature.auth.presentation.reset_password.ResetPasswordScreen
import com.ninezero.halo.feature.auth.presentation.social_username.SocialUsernameScreen
import com.ninezero.halo.feature.auth.presentation.verify_email.VerifyEmailScreen
import com.ninezero.halo.feature.auth.presentation.welcome.WelcomeScreen
import com.ninezero.halo.feature.chat.presentation.archived_chats.ArchivedChatsScreen
import com.ninezero.halo.feature.chat.presentation.chat_room.ChatRoomScreen
import com.ninezero.halo.feature.coupon.presentation.coupon_manage.CouponManageScreen
import com.ninezero.halo.feature.coupon.presentation.my_coupons.MyCouponsScreen
import com.ninezero.halo.feature.creator.presentation.CreatorApplicationScreen
import com.ninezero.halo.feature.feed.presentation.blocked_profiles.BlockedProfilesScreen
import com.ninezero.halo.feature.feed.presentation.comment_detail.CommentDetailScreen
import com.ninezero.halo.feature.feed.presentation.create_post.CreatePostScreen
import com.ninezero.halo.feature.feed.presentation.liked_posts.LikedPostsScreen
import com.ninezero.halo.feature.feed.presentation.post_detail.PostDetailScreen
import com.ninezero.halo.feature.feed.presentation.reply.ReplyScreen
import com.ninezero.halo.feature.feed.presentation.saved_posts.SavedPostsScreen
import com.ninezero.halo.feature.main.presentation.MainScreen
import com.ninezero.halo.feature.notification.presentation.NotificationScreen
import com.ninezero.halo.feature.notification.presentation.creator_notifications.CreatorNotificationsScreen
import com.ninezero.halo.feature.notification.presentation.notification_settings.NotificationSettingsScreen
import com.ninezero.halo.feature.point.presentation.PointScreen
import com.ninezero.halo.feature.profile.domain.model.SectionType
import com.ninezero.halo.feature.profile.presentation.edit_profile.EditProfileScreen
import com.ninezero.halo.feature.profile.presentation.follow_list.FollowListScreen
import com.ninezero.halo.feature.profile.presentation.section_detail.SectionDetailScreen
import com.ninezero.halo.feature.profile.presentation.user_profile.UserProfileScreen
import com.ninezero.halo.feature.settings.presentation.SettingsScreen
import com.ninezero.halo.feature.settings.presentation.info.InfoScreen
import com.ninezero.halo.feature.settings.presentation.open_source_licenses.OpenSourceLicensesScreen
import com.ninezero.halo.feature.store.presentation.cart.CartScreen
import com.ninezero.halo.feature.store.presentation.checkout.CheckoutScreen
import com.ninezero.halo.feature.store.presentation.create_product.CreateProductScreen
import com.ninezero.halo.feature.store.presentation.create_review.CreateReviewScreen
import com.ninezero.halo.feature.store.presentation.my_products.MyProductsScreen
import com.ninezero.halo.feature.store.presentation.my_reviews.MyReviewsScreen
import com.ninezero.halo.feature.store.presentation.order_complete.OrderCompleteScreen
import com.ninezero.halo.feature.store.presentation.order_detail.OrderDetailScreen
import com.ninezero.halo.feature.store.presentation.order_list.OrderListScreen
import com.ninezero.halo.feature.store.presentation.payment_methods.PaymentMethodsScreen
import com.ninezero.halo.feature.store.presentation.product_detail.ProductDetailScreen
import com.ninezero.halo.feature.store.presentation.review_list.ReviewListScreen
import com.ninezero.halo.feature.store.presentation.sales_manage.SalesManageScreen
import com.ninezero.halo.feature.store.presentation.sales_stats.SalesStatsScreen
import com.ninezero.halo.feature.store.presentation.wishlist.WishlistScreen
import com.ninezero.halo.feature.subscription.presentation.membership_manage.MembershipManageScreen
import com.ninezero.halo.feature.subscription.presentation.my_subscriptions.MySubscriptionsScreen
import com.ninezero.halo.feature.subscription.presentation.subscription_sheet.SubscriptionSheetHost

private const val TRANSITION_DURATION = 250
private const val SHARED_AXIS_DURATION = 250
private const val SHARED_AXIS_SLIDE_DISTANCE = 90  // px, 일반 기기 기준 약 30dp

// Shared Axis 순차 fade, 잔상 제거
private const val SHARED_AXIS_FADE_OUT_DURATION = 75
private const val SHARED_AXIS_FADE_IN_DURATION = 175
private const val SHARED_AXIS_FADE_IN_DELAY = 75

// 모달 뒤 화면 축소 배율
private const val MODAL_BACKDROP_SCALE = 0.94f
private const val MODAL_BACKDROP_FADE_OUT_DURATION = 90  // 축소가 보이도록 일반 전환보다 길게

private const val PREDICTIVE_BACK_SCALE = 0.9f
private const val PREDICTIVE_BACK_SHIFT_FRACTION = 0.03f  // 화면 폭 대비 이동량
private const val PREDICTIVE_BACK_EXIT_SLIDE_FRACTION = 0.08f  // 사라질 때 더 밀리는 양
private const val PREDICTIVE_BACK_SHRINK_DURATION = 200
private const val PREDICTIVE_BACK_FADE_OUT_DURATION = 100
private val PREDICTIVE_BACK_CORNER_RADIUS = 28.dp  // 기기 모서리 반경을 못 읽을 때

private val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)  // 빠른 가속, 완만한 감속

// 예측형 뒤로 가기 이동
private val PredictiveBackSlideEasing = Easing { fraction ->
    val total = PREDICTIVE_BACK_SHRINK_DURATION + PREDICTIVE_BACK_FADE_OUT_DURATION
    val shrinkEnd = PREDICTIVE_BACK_SHRINK_DURATION.toFloat() / total
    val shiftPortion = PREDICTIVE_BACK_SHIFT_FRACTION /
        (PREDICTIVE_BACK_SHIFT_FRACTION + PREDICTIVE_BACK_EXIT_SLIDE_FRACTION)
    if (fraction < shrinkEnd) {
        shiftPortion * LinearOutSlowInEasing.transform(fraction / shrinkEnd)
    } else {
        shiftPortion + (1f - shiftPortion) *
            EmphasizedEasing.transform((fraction - shrinkEnd) / (1f - shrinkEnd))
    }
}

private fun NavBackStackEntry.isModalDestination(): Boolean =
    destination.hasRoute<Screen.CreatePost>() ||
            destination.hasRoute<Screen.EditPost>() ||
            destination.hasRoute<Screen.CreateCommunityPost>() ||
            destination.hasRoute<Screen.Reply>() ||
            destination.hasRoute<Screen.CreateReview>() ||
            destination.hasRoute<Screen.CreateProduct>()

// 콘텐츠 딥링크 패턴, 필수 인자는 path로 매핑
private inline fun <reified T : Screen> contentDeepLinks(path: String) = listOf(
    navDeepLink<T>(basePath = "${DeepLinks.SCHEME}://$path"),
    navDeepLink<T>(basePath = "${DeepLinks.WEB_BASE}/$path")
)

private val pushExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (targetState.isModalDestination()) scaleBackOut() else slideOutLeft()
}

private val pushPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
    {
        if (initialState.isModalDestination()) scaleBackIn() else slideInRight()
    }

@Composable
fun HaloNavGraph(
    navController: NavHostController,
    startDestination: Screen = Screen.Welcome
) {
    SubscriptionSheetHost(
        onNavigateToMySubscriptions = { navController.navigateUnlessOnTop(Screen.MySubscriptions) }
    ) {
        // 뒤로는 dropUnlessStarted, 연타로 빈 화면 방지
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // 예측형 뒤로 가기는 일반 화면만, 모달은 기존 전환
            predictivePopEnterTransition = {
                if (initialState.isModalDestination()) pushPopEnter() else EnterTransition.None
            },
            predictivePopExitTransition = { swipeEdge ->
                if (initialState.isModalDestination()) slideOutRight() else predictiveShrinkOut(swipeEdge)
            }
        ) {
            // 인증
            screen<Screen.Welcome>(
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter
            ) {
                WelcomeScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Main) {
                            popUpTo<Screen.Welcome> { inclusive = true }
                        }
                    },
                    onNavigateToUsernameOnboarding = { signupToken, suggestedUsername ->
                        navController.navigateUnlessOnTop(
                            Screen.SocialUsername(
                                signupToken = signupToken,
                                suggestedUsername = suggestedUsername
                            )
                        )
                    },
                    onNavigateToRegister = { navController.navigateUnlessOnTop(Screen.Register) },
                    onNavigateToLogin = { navController.navigateUnlessOnTop(Screen.Login) }
                )
            }

            screen<Screen.Login>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Main) {
                            popUpTo<Screen.Welcome> { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register) {
                            popUpTo<Screen.Welcome>()
                        }
                    },
                    onNavigateToForgotPassword = { navController.navigateUnlessOnTop(Screen.ForgotPassword) },
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.Register>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login) {
                            popUpTo<Screen.Welcome>()
                        }
                    },
                    onNavigateToVerifyEmail = { email ->
                        navController.navigateUnlessOnTop(Screen.VerifyEmail(email))
                    },
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.VerifyEmail>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                VerifyEmailScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToHome = {
                        // 코드 검증이 곧 로그인이라 인증 스택 비움
                        navController.navigate(Screen.Main) {
                            popUpTo<Screen.Welcome> { inclusive = true }
                        }
                    }
                )
            }

            screen<Screen.ForgotPassword>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                ForgotPasswordScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToResetCode = { email ->
                        navController.navigateUnlessOnTop(Screen.ResetCode(email))
                    }
                )
            }

            screen<Screen.ResetCode>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                ResetCodeScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToResetPassword = { resetToken ->
                        // 코드 화면으로 못 돌아가게 스택 정리
                        navController.navigate(Screen.ResetPassword(resetToken)) {
                            popUpTo<Screen.Login>()
                        }
                    }
                )
            }

            screen<Screen.ResetPassword>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                ResetPasswordScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToLogin = {
                        // 재설정 플로우 전부 제거 후 로그인
                        navController.popBackStack(Screen.Login, inclusive = false)
                    }
                )
            }

            screen<Screen.SocialUsername>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                SocialUsernameScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Main) {
                            popUpTo<Screen.Welcome> { inclusive = true }
                        }
                    },
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            // 메인
            screen<Screen.Main>(
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter
            ) {
                MainScreen(
                    onNavigateToCreatePost = {
                        navController.navigateUnlessOnTop(Screen.CreatePost)
                    },
                    onNavigateToPostDetail = { postId ->
                        navController.navigateUnlessOnTop(Screen.PostDetail(postId))
                    },
                    onNavigateToCommentDetail = { commentId ->
                        navController.navigateUnlessOnTop(Screen.CommentDetail(commentId))
                    },
                    onNavigateToProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigateUnlessOnTop(Screen.EditPost(postId))
                    },
                    onNavigateToChat = { roomId ->
                        navController.navigateUnlessOnTop(Screen.ChatRoom(roomId.toString()))
                    },
                    onNavigateToSettings = {
                        navController.navigateUnlessOnTop(Screen.Settings)
                    },
                    onNavigateToEditProfile = {
                        navController.navigateUnlessOnTop(Screen.EditProfile)
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    },
                    onNavigateToSectionDetail = { sectionType, creatorId, tagId, title ->
                        navController.navigateUnlessOnTop(
                            Screen.SectionDetail(
                                sectionType = sectionType.name,
                                creatorId = creatorId,
                                tagId = tagId,
                                title = title
                            )
                        )
                    },
                    onNavigateToCreateCommunityPost = { creatorId ->
                        navController.navigateUnlessOnTop(Screen.CreateCommunityPost(creatorId))
                    },
                    onNavigateToFollowList = { userId, displayName, initialTab, followerCount, followingCount, subscriberCount, showSubscribers ->
                        navController.navigateUnlessOnTop(
                            Screen.FollowList(
                                userId = userId,
                                displayName = displayName,
                                initialTab = initialTab,
                                followerCount = followerCount,
                                followingCount = followingCount,
                                subscriberCount = subscriberCount,
                                showSubscribers = showSubscribers,
                                isMyProfile = true
                            )
                        )
                    },
                    onNavigateToMembershipManage = {
                        navController.navigateUnlessOnTop(Screen.MembershipManage)
                    },
                    onNavigateToMySubscriptions = {
                        navController.navigateUnlessOnTop(Screen.MySubscriptions)
                    },
                    onNavigateToPoint = {
                        navController.navigateUnlessOnTop(Screen.Point)
                    },
                    onNavigateToCoupon = {
                        navController.navigateUnlessOnTop(Screen.MyCoupons)
                    },
                    onNavigateToCouponManage = {
                        navController.navigateUnlessOnTop(Screen.CouponManage)
                    },
                    onNavigateToMyReviews = {
                        navController.navigateUnlessOnTop(Screen.MyReviews)
                    },
                    onNavigateToAddress = {
                        navController.navigateUnlessOnTop(Screen.AddressManage)
                    },
                    onNavigateToWishlist = {
                        navController.navigateUnlessOnTop(Screen.Wishlist)
                    },
                    onNavigateToCart = {
                        navController.navigateUnlessOnTop(Screen.Cart)
                    },
                    onNavigateToMyOrders = {
                        navController.navigateUnlessOnTop(Screen.MyOrders)
                    },
                    onNavigateToMyProducts = {
                        navController.navigateUnlessOnTop(Screen.MyProducts)
                    },
                    onNavigateToSalesManage = {
                        navController.navigateUnlessOnTop(Screen.SalesManage)
                    },
                    onNavigateToNotifications = {
                        navController.navigateUnlessOnTop(Screen.Notifications)
                    }
                )
            }

            screen<Screen.Notifications>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                NotificationScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToPostDetail = { postId, highlightCommentId ->
                        navController.navigateUnlessOnTop(Screen.PostDetail(postId, highlightCommentId))
                    },
                    onNavigateToCommentDetail = { commentId, highlightReplyId ->
                        navController.navigateUnlessOnTop(
                            Screen.CommentDetail(
                                commentId,
                                highlightAnchor = highlightReplyId == null,
                                highlightReplyId = highlightReplyId
                            )
                        )
                    },
                    onNavigateToProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToOrderDetail = { orderId ->
                        navController.navigateUnlessOnTop(Screen.OrderDetail(orderId))
                    },
                    onNavigateToChatRoom = { roomId ->
                        navController.navigateUnlessOnTop(Screen.ChatRoom(roomId.toString()))
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    },
                    onNavigateToPoint = {
                        navController.navigateUnlessOnTop(Screen.Point)
                    },
                    onNavigateToCreatorApplication = {
                        navController.navigateUnlessOnTop(Screen.CreatorApplication)
                    },
                    onNavigateToMySubscriptions = {
                        navController.navigateUnlessOnTop(Screen.MySubscriptions)
                    },
                    onNavigateToSalesManage = {
                        navController.navigateUnlessOnTop(Screen.SalesManage)
                    },
                    onNavigateToReviewList = { productId, reviewId ->
                        navController.navigateUnlessOnTop(
                            Screen.ReviewList(
                                productId,
                                highlightReviewId = reviewId
                            )
                        )
                    }
                )
            }

            // 피드
            screen<Screen.PostDetail>(
                deepLinks = contentDeepLinks<Screen.PostDetail>(DeepLinks.PATH_POST),
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                PostDetailScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToCommentDetail = { commentId ->
                        navController.navigateUnlessOnTop(Screen.CommentDetail(commentId))
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigateUnlessOnTop(Screen.EditPost(postId))
                    },
                    onNavigateToReply = { postId, parentCommentId ->
                        navController.navigateUnlessOnTop(Screen.Reply(postId, parentCommentId))
                    },
                    onNavigateToChatRoom = { roomId ->
                        navController.navigateUnlessOnTop(Screen.ChatRoom(roomId.toString()))
                    }
                )
            }

            screen<Screen.CommentDetail>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                CommentDetailScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToCommentDetail = { commentId ->
                        navController.navigateUnlessOnTop(Screen.CommentDetail(commentId))
                    },
                    onNavigateToPost = { postId ->
                        navController.navigateUnlessOnTop(Screen.PostDetail(postId))
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigateUnlessOnTop(Screen.EditPost(postId))
                    },
                    onNavigateToReply = { postId, parentCommentId ->
                        navController.navigateUnlessOnTop(Screen.Reply(postId, parentCommentId))
                    }
                )
            }

            screen<Screen.CreatePost>(
                enterTransition = { slideInUp() },
                exitTransition = { slideOutUp() },
                popEnterTransition = { slideInDown() },
                popExitTransition = { slideOutDown() }
            ) {
                CreatePostScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.EditPost>(
                enterTransition = { slideInUp() },
                exitTransition = { slideOutUp() },
                popEnterTransition = { slideInDown() },
                popExitTransition = { slideOutDown() }
            ) {
                CreatePostScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.CreateCommunityPost>(
                enterTransition = { slideInUp() },
                exitTransition = { slideOutUp() },
                popEnterTransition = { slideInDown() },
                popExitTransition = { slideOutDown() }
            ) {
                CreatePostScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.Reply>(
                enterTransition = { slideInUp() },
                exitTransition = { slideOutUp() },
                popEnterTransition = { slideInDown() },
                popExitTransition = { slideOutDown() }
            ) {
                ReplyScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    }
                )
            }

            // 프로필
            screen<Screen.UserProfile>(
                deepLinks = contentDeepLinks<Screen.UserProfile>(DeepLinks.PATH_PROFILE),
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                UserProfileScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToPostDetail = { postId ->
                        navController.navigateUnlessOnTop(Screen.PostDetail(postId))
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigateUnlessOnTop(Screen.EditPost(postId))
                    },
                    onNavigateToCommentDetail = { commentId ->
                        navController.navigateUnlessOnTop(Screen.CommentDetail(commentId))
                    },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToMessage = { roomId ->
                        navController.navigateUnlessOnTop(Screen.ChatRoom(roomId = roomId.toString()))
                    },
                    onNavigateToNewChat = { targetUserId, username, displayName, avatarUrl, role, isBlockedByMe, isBlockedByOther ->
                        navController.navigateUnlessOnTop(
                            Screen.ChatRoom(
                                roomId = "0",
                                targetUserId = targetUserId,
                                targetUsername = username,
                                targetDisplayName = displayName,
                                targetAvatarUrl = avatarUrl,
                                targetRole = role,
                                targetBlockedByMe = isBlockedByMe,
                                targetBlockedByOther = isBlockedByOther
                            )
                        )
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    },
                    onNavigateToSectionDetail = { sectionType, creatorId, tagId, title ->
                        navController.navigateUnlessOnTop(
                            Screen.SectionDetail(
                                sectionType = sectionType.name,
                                creatorId = creatorId,
                                tagId = tagId,
                                title = title
                            )
                        )
                    },
                    onNavigateToCreateCommunityPost = { creatorId ->
                        navController.navigateUnlessOnTop(Screen.CreateCommunityPost(creatorId))
                    },
                    onNavigateToFollowList = { userId, displayName, initialTab, followerCount, followingCount, subscriberCount, showSubscribers ->
                        navController.navigateUnlessOnTop(
                            Screen.FollowList(
                                userId = userId,
                                displayName = displayName,
                                initialTab = initialTab,
                                followerCount = followerCount,
                                followingCount = followingCount,
                                subscriberCount = subscriberCount,
                                showSubscribers = showSubscribers,
                                isMyProfile = showSubscribers  // showSubscribers는 내 프로필 구독자 탭만 true
                            )
                        )
                    },
                    onNavigateToEditProfile = {
                        navController.navigateUnlessOnTop(Screen.EditProfile)
                    }
                )
            }

            screen<Screen.FollowList>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                FollowListScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToNewChat = { targetUserId, username, displayName, avatarUrl, role ->
                        navController.navigateUnlessOnTop(
                            Screen.ChatRoom(
                                roomId = "0",
                                targetUserId = targetUserId,
                                targetUsername = username,
                                targetDisplayName = displayName,
                                targetAvatarUrl = avatarUrl,
                                targetRole = role
                            )
                        )
                    }
                )
            }

            screen<Screen.SectionDetail>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.SectionDetail>()
                val sectionType = try {
                    SectionType.valueOf(route.sectionType)
                } catch (e: Exception) {
                    SectionType.PINNED_POST
                }

                SectionDetailScreen(
                    sectionType = sectionType,
                    creatorId = route.creatorId,
                    tagId = route.tagId,
                    title = route.title,
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToPostDetail = { postId ->
                        navController.navigateUnlessOnTop(Screen.PostDetail(postId))
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    }
                )
            }

            screen<Screen.EditProfile>(
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter
            ) {
                EditProfileScreen(onNavigateBack = dropUnlessStarted { navController.popBackStack() })
            }

            // 채팅
            screen<Screen.ChatRoom>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                ChatRoomScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToPostDetail = { postId ->
                        navController.navigateUnlessOnTop(Screen.PostDetail(postId))
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    }
                )
            }

            screen<Screen.ArchivedChats>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                ArchivedChatsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToChatRoom = { roomId ->
                        navController.navigateUnlessOnTop(Screen.ChatRoom(roomId.toString()))
                    }
                )
            }

            // 상품
            screen<Screen.ProductDetail>(
                deepLinks = contentDeepLinks<Screen.ProductDetail>(DeepLinks.PATH_PRODUCT),
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                ProductDetailScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToCart = {
                        navController.navigateUnlessOnTop(Screen.Cart)
                    },
                    onNavigateToDirectCheckout = { productId, quantity ->
                        navController.navigateUnlessOnTop(Screen.Checkout(directProductId = productId, directQuantity = quantity))
                    },
                    onNavigateToReviewList = { productId ->
                        navController.navigateUnlessOnTop(Screen.ReviewList(productId))
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToChatRoom = { roomId ->
                        navController.navigateUnlessOnTop(Screen.ChatRoom(roomId.toString()))
                    }
                )
            }

            screen<Screen.Wishlist>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                WishlistScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToCart = {
                        navController.navigateUnlessOnTop(Screen.Cart)
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    }
                )
            }

            // 장바구니·주문
            screen<Screen.Cart>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                CartScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToCheckout = { selectedItemIds ->
                        navController.navigateUnlessOnTop(Screen.Checkout(selectedItemIds))
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    }
                )
            }

            screen<Screen.Checkout>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) { backStackEntry ->
                val isDirect = backStackEntry.toRoute<Screen.Checkout>().directProductId != null
                CheckoutScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToAddressManage = {
                        navController.navigateUnlessOnTop(Screen.AddressManage)
                    },
                    onNavigateToOrderComplete = { orderId, orderNumber ->
                        // 결제 끝난 화면이 다시 뜨지 않게 플로우 제거
                        navController.navigate(Screen.OrderComplete(orderId, orderNumber)) {
                            if (isDirect) {
                                popUpTo<Screen.Checkout> { inclusive = true }
                            } else {
                                popUpTo<Screen.Cart> { inclusive = true }
                            }
                        }
                    }
                )
            }

            screen<Screen.OrderComplete>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.OrderComplete>()
                OrderCompleteScreen(
                    orderId = route.orderId,
                    orderNumber = route.orderNumber,
                    onNavigateToOrderDetail = { id ->
                        navController.navigate(Screen.OrderDetail(id)) {
                            popUpTo<Screen.OrderComplete> { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Main) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }

            screen<Screen.OrderDetail>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                OrderDetailScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    },
                    onNavigateToCart = {
                        navController.navigateUnlessOnTop(Screen.Cart)
                    },
                    onNavigateToCreateReview = { productId, orderId, productName, productImageUrl ->
                        navController.navigateUnlessOnTop(
                            Screen.CreateReview(
                                productId = productId,
                                orderId = orderId,
                                productName = productName,
                                productImageUrl = productImageUrl ?: ""
                            )
                        )
                    }
                )
            }

            screen<Screen.MyOrders>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                OrderListScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToOrderDetail = { orderId ->
                        navController.navigateUnlessOnTop(Screen.OrderDetail(orderId))
                    },
                    onNavigateToCart = {
                        navController.navigateUnlessOnTop(Screen.Cart)
                    },
                    onNavigateToCreateReview = { productId, orderId, productName, productImageUrl ->
                        navController.navigateUnlessOnTop(
                            Screen.CreateReview(
                                productId = productId,
                                orderId = orderId,
                                productName = productName,
                                productImageUrl = productImageUrl ?: ""
                            )
                        )
                    }
                )
            }

            // 리뷰
            screen<Screen.ReviewList>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                ReviewListScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToEditReview = { productId, orderId, productName, productImageUrl, reviewId, rating, content ->
                        navController.navigateUnlessOnTop(
                            Screen.CreateReview(
                                productId = productId,
                                orderId = orderId,
                                productName = productName,
                                productImageUrl = productImageUrl ?: "",
                                reviewId = reviewId,
                                rating = rating,
                                content = content
                            )
                        )
                    }
                )
            }

            screen<Screen.MyReviews>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                MyReviewsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    },
                    onNavigateToEditReview = { productId, orderId, productName, productImageUrl, reviewId, rating, content ->
                        navController.navigateUnlessOnTop(
                            Screen.CreateReview(
                                productId = productId,
                                orderId = orderId,
                                productName = productName,
                                productImageUrl = productImageUrl ?: "",
                                reviewId = reviewId,
                                rating = rating,
                                content = content
                            )
                        )
                    }
                )
            }

            screen<Screen.CreateReview>(
                enterTransition = { slideInUp() },
                exitTransition = { slideOutUp() },
                popEnterTransition = { slideInDown() },
                popExitTransition = { slideOutDown() }
            ) {
                CreateReviewScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onReviewSaved = { navController.popBackStack() }
                )
            }

            // 멤버십·구독
            screen<Screen.MembershipManage>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                MembershipManageScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.MySubscriptions>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                MySubscriptionsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToCreatorProfile = { creatorId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(creatorId))
                    }
                )
            }

            // 포인트·쿠폰
            screen<Screen.Point>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                PointScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.MyCoupons>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                MyCouponsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            // 주소
            screen<Screen.AddressManage>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                AddressManageScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToCreate = { zipCode, address ->
                        navController.navigateUnlessOnTop(
                            Screen.AddressDetail(
                                zipCode = zipCode,
                                address = address
                            )
                        )
                    },
                    onNavigateToEdit = { addressId ->
                        navController.navigateUnlessOnTop(Screen.AddressDetail(addressId = addressId))
                    }
                )
            }

            screen<Screen.AddressDetail>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                AddressDetailScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            // 크리에이터
            screen<Screen.CreatorApplication>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                CreatorApplicationScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.MyProducts>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                MyProductsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToCreateProduct = {
                        navController.navigateUnlessOnTop(Screen.CreateProduct())
                    },
                    onNavigateToEditProduct = { productId ->
                        navController.navigateUnlessOnTop(Screen.CreateProduct(productId))
                    },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    }
                )
            }

            screen<Screen.CreateProduct>(
                enterTransition = { slideInUp() },
                exitTransition = { slideOutUp() },
                popEnterTransition = { slideInDown() },
                popExitTransition = { slideOutDown() }
            ) {
                CreateProductScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            screen<Screen.SalesManage>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                SalesManageScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToSalesStats = { navController.navigateUnlessOnTop(Screen.SalesStats) }
                )
            }

            screen<Screen.SalesStats>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                SalesStatsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.CouponManage>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                CouponManageScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            // 설정
            screen<Screen.Settings>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                SettingsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToNotificationSettings = {
                        navController.navigateUnlessOnTop(Screen.NotificationSettings)
                    },
                    onNavigateToSavedPosts = {
                        navController.navigateUnlessOnTop(Screen.SavedPosts)
                    },
                    onNavigateToLikedPosts = {
                        navController.navigateUnlessOnTop(Screen.LikedPosts)
                    },
                    onNavigateToArchivedChats = {
                        navController.navigateUnlessOnTop(Screen.ArchivedChats)
                    },
                    onNavigateToBlockedProfiles = {
                        navController.navigateUnlessOnTop(Screen.BlockedProfiles)
                    },
                    onNavigateToPaymentMethods = {
                        navController.navigateUnlessOnTop(Screen.PaymentMethods)
                    },
                    onNavigateToInfo = {
                        navController.navigateUnlessOnTop(Screen.Info)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Welcome) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onNavigateToCreatorApplication = {
                        navController.navigateUnlessOnTop(Screen.CreatorApplication)
                    }
                )
            }

            screen<Screen.NotificationSettings>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                NotificationSettingsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToCreatorNotifications = { navController.navigateUnlessOnTop(Screen.CreatorNotifications) }
                )
            }

            screen<Screen.CreatorNotifications>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                CreatorNotificationsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.SavedPosts>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                SavedPostsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToPostDetail = { postId ->
                        navController.navigateUnlessOnTop(Screen.PostDetail(postId))
                    },
                    onNavigateToCommentDetail = { commentId ->
                        navController.navigateUnlessOnTop(Screen.CommentDetail(commentId))
                    },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigateUnlessOnTop(Screen.EditPost(postId))
                    },
                    onNavigateToChatRoom = { roomId ->
                        navController.navigateUnlessOnTop(Screen.ChatRoom(roomId.toString()))
                    }
                )
            }

            screen<Screen.LikedPosts>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                LikedPostsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToPostDetail = { postId ->
                        navController.navigateUnlessOnTop(Screen.PostDetail(postId))
                    },
                    onNavigateToCommentDetail = { commentId ->
                        navController.navigateUnlessOnTop(Screen.CommentDetail(commentId))
                    },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigateUnlessOnTop(Screen.EditPost(postId))
                    },
                    onNavigateToChatRoom = { roomId ->
                        navController.navigateUnlessOnTop(Screen.ChatRoom(roomId.toString()))
                    }
                )
            }

            screen<Screen.BlockedProfiles>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                BlockedProfilesScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToUserProfile = { userId ->
                        navController.navigateUnlessOnTop(Screen.UserProfile(userId))
                    }
                )
            }

            screen<Screen.PaymentMethods>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                PaymentMethodsScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }

            screen<Screen.Info>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                InfoScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToOpenSourceLicenses = {
                        navController.navigateUnlessOnTop(Screen.OpenSourceLicenses)
                    }
                )
            }

            screen<Screen.OpenSourceLicenses>(
                enterTransition = { slideInLeft() },
                exitTransition = pushExit,
                popEnterTransition = pushPopEnter,
                popExitTransition = { slideOutRight() }
            ) {
                OpenSourceLicensesScreen(
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() }
                )
            }
        }
    }
}

// 화면 전환 헬퍼
private fun slideInLeft() = slideInHorizontally(
    animationSpec = tween(SHARED_AXIS_DURATION, easing = EmphasizedEasing),
    initialOffsetX = { SHARED_AXIS_SLIDE_DISTANCE }
) + fadeIn(
    animationSpec = tween(
        durationMillis = SHARED_AXIS_FADE_IN_DURATION,
        delayMillis = SHARED_AXIS_FADE_IN_DELAY,
        easing = LinearEasing
    )
)

private fun slideOutLeft() = slideOutHorizontally(
    animationSpec = tween(SHARED_AXIS_DURATION, easing = EmphasizedEasing),
    targetOffsetX = { -SHARED_AXIS_SLIDE_DISTANCE }
) + fadeOut(animationSpec = tween(SHARED_AXIS_FADE_OUT_DURATION, easing = LinearEasing))

private fun slideInRight() = slideInHorizontally(
    animationSpec = tween(SHARED_AXIS_DURATION, easing = EmphasizedEasing),
    initialOffsetX = { -SHARED_AXIS_SLIDE_DISTANCE }
) + fadeIn(
    animationSpec = tween(
        durationMillis = SHARED_AXIS_FADE_IN_DURATION,
        delayMillis = SHARED_AXIS_FADE_IN_DELAY,
        easing = LinearEasing
    )
)

private fun slideOutRight() = slideOutHorizontally(
    animationSpec = tween(SHARED_AXIS_DURATION, easing = EmphasizedEasing),
    targetOffsetX = { SHARED_AXIS_SLIDE_DISTANCE }
) + fadeOut(animationSpec = tween(SHARED_AXIS_FADE_OUT_DURATION, easing = LinearEasing))

private fun AnimatedContentTransitionScope<*>.slideInUp() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = SHARED_AXIS_FADE_IN_DURATION,
            delayMillis = SHARED_AXIS_FADE_IN_DELAY,
            easing = LinearEasing
        )
    )

private fun AnimatedContentTransitionScope<*>.slideOutUp() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(SHARED_AXIS_FADE_OUT_DURATION, easing = LinearEasing))

private fun AnimatedContentTransitionScope<*>.slideInDown() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = SHARED_AXIS_FADE_IN_DURATION,
            delayMillis = SHARED_AXIS_FADE_IN_DELAY,
            easing = LinearEasing
        )
    )

private fun AnimatedContentTransitionScope<*>.slideOutDown() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(SHARED_AXIS_FADE_OUT_DURATION, easing = LinearEasing))

// 손가락 따라 줄어들고 끝에서 밀리며 사라짐
private fun predictiveShrinkOut(swipeEdge: Int): ExitTransition = scaleOut(
    targetScale = PREDICTIVE_BACK_SCALE,
    animationSpec = tween(PREDICTIVE_BACK_SHRINK_DURATION, easing = LinearOutSlowInEasing)
) + slideOutHorizontally(
    animationSpec = tween(
        durationMillis = PREDICTIVE_BACK_SHRINK_DURATION + PREDICTIVE_BACK_FADE_OUT_DURATION,
        easing = PredictiveBackSlideEasing
    ),
    targetOffsetX = { fullWidth ->
        val distance =
            (fullWidth * (PREDICTIVE_BACK_SHIFT_FRACTION + PREDICTIVE_BACK_EXIT_SLIDE_FRACTION)).toInt()
        if (swipeEdge == EDGE_RIGHT) -distance else distance
    }
) + fadeOut(
    animationSpec = tween(
        durationMillis = PREDICTIVE_BACK_FADE_OUT_DURATION,
        delayMillis = PREDICTIVE_BACK_SHRINK_DURATION,
        easing = LinearEasing
    )
)

// 모달 뒤 화면 축소, 순차 fade로 가장자리 틈을 가림
private fun scaleBackOut(): ExitTransition = scaleOut(
    targetScale = MODAL_BACKDROP_SCALE,
    animationSpec = tween(SHARED_AXIS_DURATION, easing = EmphasizedEasing)
) + fadeOut(animationSpec = tween(MODAL_BACKDROP_FADE_OUT_DURATION, easing = LinearEasing))

private fun scaleBackIn(): EnterTransition = scaleIn(
    initialScale = MODAL_BACKDROP_SCALE,
    animationSpec = tween(SHARED_AXIS_DURATION, easing = EmphasizedEasing)
) + fadeIn(
    animationSpec = tween(
        durationMillis = SHARED_AXIS_FADE_IN_DURATION,
        delayMillis = SHARED_AXIS_FADE_IN_DELAY,
        easing = LinearEasing
    )
)

/** 예측형 뒤로 가기 둥근 모서리를 얹는 composable 래퍼 */
private inline fun <reified T : Any> NavGraphBuilder.screen(
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
        enterTransition,
    noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
        exitTransition,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) = composable<T>(
    deepLinks = deepLinks,
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition
) { entry ->
    PredictiveBackCorners(entry) { content(entry) }
}

/** 전환 API에 없는 예측형 뒤로 가기 둥근 모서리 */
@Composable
private fun AnimatedContentScope.PredictiveBackCorners(
    entry: NavBackStackEntry,
    content: @Composable () -> Unit
) {
    val dispatcherOwner = LocalNavigationEventDispatcherOwner.current
    // 모달 화면은 예측형 축소 대상이 아님
    if (dispatcherOwner == null || entry.isModalDestination()) {
        content()
        return
    }

    val gestureState by dispatcherOwner.navigationEventDispatcher.transitionState.collectAsState()
    val gestureInProgress = gestureState is NavigationEventTransitionState.InProgress
    val exiting = transition.targetState == EnterExitState.PostExit

    // 제스처로 시작된 퇴장만, 손을 뗀 뒤 남은 전환까지 유지
    var predictiveExit by remember { mutableStateOf(false) }
    SideEffect { predictiveExit = exiting && (gestureInProgress || predictiveExit) }

    val cornerFraction = transition.animateFloat(
        transitionSpec = { tween(PREDICTIVE_BACK_SHRINK_DURATION, easing = LinearOutSlowInEasing) },
        label = "predictiveBackCorner"
    ) { state ->
        if (state == EnterExitState.PostExit && predictiveExit) 1f else 0f
    }

    // 기기 모서리 반경, 못 읽으면 기본값
    val view = LocalView.current
    val deviceCorners = remember(view, predictiveExit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val insets = view.rootWindowInsets
            val top = insets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius ?: 0
            val bottom = insets?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)?.radius ?: 0
            if (top > 0 || bottom > 0) top.toFloat() to bottom.toFloat() else null
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (predictiveExit) {
                    Modifier.graphicsLayer {
                        val fallback = PREDICTIVE_BACK_CORNER_RADIUS.toPx()
                        val top = (deviceCorners?.first ?: fallback) * cornerFraction.value
                        val bottom = (deviceCorners?.second ?: fallback) * cornerFraction.value
                        clip = true
                        shape = RoundedCornerShape(
                            topStart = top,
                            topEnd = top,
                            bottomEnd = bottom,
                            bottomStart = bottom
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

/** 맨 위가 같은 화면·같은 인자면 스킵 */
fun NavHostController.navigateUnlessOnTop(route: Screen) {  // 연타 중복 이동 방지
    val top = currentBackStackEntry
    val isSameOnTop = top != null &&
        top.destination.hasRoute(route::class) &&
        runCatching { top.toRoute<Screen>(route::class) }.getOrNull() == route
    if (!isSameOnTop) navigate(route)
}
