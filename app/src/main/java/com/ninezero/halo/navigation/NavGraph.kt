package com.ninezero.halo.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
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

// 콘텐츠 딥링크 패턴, 필수 인자는 path로 매핑
private inline fun <reified T : Screen> contentDeepLinks(path: String) = listOf(
    navDeepLink<T>(basePath = "${DeepLinks.SCHEME}://$path"),
    navDeepLink<T>(basePath = "${DeepLinks.WEB_BASE}/$path")
)

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
            predictivePopExitTransition = {
                if (initialState.isModalDestination()) slideOutRight() else predictiveBackHold()
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
                    onNavigateBack = dropUnlessStarted { navController.popBackStack() },
                    onNavigateToProductDetail = { productId ->
                        navController.navigateUnlessOnTop(Screen.ProductDetail(productId))
                    }
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

/** 예측형 뒤로 가기 미리 보기를 얹는 composable 래퍼 */
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
    PredictiveBackLayer(entry) { content(entry) }
}

/** 맨 위가 같은 화면·같은 인자면 스킵 */
fun NavHostController.navigateUnlessOnTop(route: Screen) {  // 연타 중복 이동 방지
    val top = currentBackStackEntry
    val isSameOnTop = top != null &&
        top.destination.hasRoute(route::class) &&
        runCatching { top.toRoute<Screen>(route::class) }.getOrNull() == route
    if (!isSameOnTop) navigate(route)
}
