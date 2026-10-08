package com.ninezero.halo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ninezero.halo.core.deeplink.DeepLinks
import com.ninezero.halo.core.local.PreferencesManager
import com.ninezero.halo.core.local.ThemeMode
import com.ninezero.halo.core.network.auth.SessionEventBus
import com.ninezero.halo.core.ui.components.overlay.GlobalCenterToastHost
import com.ninezero.halo.core.ui.components.snackbar.LocalSnackbarRelay
import com.ninezero.halo.core.ui.components.snackbar.SnackbarRelay
import com.ninezero.halo.feature.auth.data.local.TokenManager
import com.ninezero.halo.feature.feed.presentation.report.ReportHost
import com.ninezero.halo.feature.notification.domain.event.NotificationSyncBus
import com.ninezero.halo.feature.notification.domain.model.NotificationDestination
import com.ninezero.halo.feature.notification.domain.model.NotificationType
import com.ninezero.halo.feature.notification.domain.model.resolveNotificationDestination
import com.ninezero.halo.feature.notification.domain.repository.NotificationRepository
import com.ninezero.halo.feature.notification.fcm.FcmNotificationHelper
import com.ninezero.halo.navigation.HaloNavGraph
import com.ninezero.halo.navigation.Screen
import com.ninezero.halo.navigation.navigateUnlessOnTop
import com.ninezero.halo.ui.theme.HaloTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var sessionEventBus: SessionEventBus

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var notificationSyncBus: NotificationSyncBus

    private var navController: NavHostController? = null

    // 그리기 전에 온 알림·링크, 컴포지션 뒤 처리
    private var pendingNewIntent: Intent? = null

    // 미로그인 딥링크, 로그인 후 재개
    private var pendingDeepLink: Uri? = null

    // Android 13+ 알림 권한
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private var startDestination by mutableStateOf<Screen?>(null)

    private var initialThemeMode by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 재생성돼도 로그인 대기 딥링크 유지
        savedInstanceState?.let {
            pendingDeepLink = BundleCompat.getParcelable(it, KEY_PENDING_DEEP_LINK, Uri::class.java)
        }

        splashScreen.setKeepOnScreenCondition { startDestination == null || initialThemeMode == null }

        lifecycleScope.launch {
            val loggedIn = tokenManager.hasToken()
            if (!loggedIn) {
                // 콜드스타트 딥링크는 data 비워 자동 처리 차단
                intent?.takeIf { it.action == Intent.ACTION_VIEW }?.data
                    ?.takeIf { DeepLinks.isContentUri(it) }
                    ?.also {
                        intent.data = null
                        pendingDeepLink = it
                    }
            }
            startDestination = if (loggedIn) Screen.Main else Screen.Welcome
        }
        lifecycleScope.launch {
            initialThemeMode = preferencesManager.observeThemeMode().first()
        }

        enableEdgeToEdge()
        setContent {
            val loadedThemeMode = initialThemeMode ?: return@setContent
            val themeMode by preferencesManager.observeThemeMode()
                .collectAsState(initial = loadedThemeMode)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                )
                onDispose {}
            }

            HaloTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val destination = startDestination
                    if (destination != null) {
                        val controller = rememberNavController()
                        navController = controller

                        LaunchedEffect(controller) {
                            // 복원된 액티비티면 원래 인텐트 무시
                            if (savedInstanceState == null) handleNotificationIntent(intent, controller)
                            pendingNewIntent?.let { pending ->
                                pendingNewIntent = null
                                handleNewIntent(pending, controller)
                            }
                        }

                        LaunchedEffect(controller) {
                            controller.currentBackStackEntryFlow.collect { entry ->
                                if (entry.destination.hasRoute<Screen.Main>()) {
                                    ensureNotificationPermission()
                                }
                            }
                        }

                        // 미로그인 딥링크 재개
                        LaunchedEffect(controller) {
                            controller.currentBackStackEntryFlow.collect { entry ->
                                if (entry.destination.hasRoute<Screen.Main>()) {
                                    pendingDeepLink?.let { uri ->
                                        pendingDeepLink = null
                                        navigateToContentLink(controller, uri)
                                    }
                                }
                            }
                        }

                        LaunchedEffect(controller) {
                            // 놓친 세션 만료면 복원된 메인 대신 첫 화면
                            val restoredMain = runCatching { controller.getBackStackEntry<Screen.Main>() }.isSuccess
                            if (savedInstanceState != null && restoredMain && !tokenManager.hasToken()) {
                                controller.navigate(Screen.Welcome) { popUpTo(0) { inclusive = true } }
                            }
                            sessionEventBus.sessionExpired.collect {
                                controller.navigate(Screen.Welcome) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }

                        // 전역 overscroll 제거
                        CompositionLocalProvider(
                            LocalOverscrollFactory provides null,
                            LocalSnackbarRelay provides remember { SnackbarRelay() }
                        ) {
                            // 신고는 전역 호스트가 받음
                            ReportHost {
                                HaloNavGraph(
                                    navController = controller,
                                    startDestination = destination
                                )
                            }
                        }
                    }

                    GlobalCenterToastHost()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pendingDeepLink?.let { outState.putParcelable(KEY_PENDING_DEEP_LINK, it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val controller = navController
        if (controller == null) {
            pendingNewIntent = intent
            return
        }
        handleNewIntent(intent, controller)
    }

    private fun handleNewIntent(intent: Intent, controller: NavHostController) {
        val contentUri = intent.takeIf { it.action == Intent.ACTION_VIEW }?.data
            ?.takeIf { DeepLinks.isContentUri(it) }
        if (contentUri != null) {
            lifecycleScope.launch {
                if (tokenManager.hasToken()) {
                    navigateToContentLink(controller, contentUri)
                } else {
                    pendingDeepLink = contentUri
                }
            }
        } else {
            handleNotificationIntent(intent, controller)
        }
    }

    // 실행 중 딥링크는 현재 스택 위에 쌓음
    private fun navigateToContentLink(navController: NavHostController, uri: Uri) {
        try {
            navController.navigate(uri)
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "처리되지 않은 딥링크: $uri")
        }
    }

    private var notificationPermissionRequested = false  // 같은 프로세스에서 중복 요청 방지

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (notificationPermissionRequested) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionRequested = true
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun handleNotificationIntent(intent: Intent?, navController: NavHostController) {
        if (intent == null) return

        if (!tokenManager.loggedIn.value) return  // 미로그인은 트레이 탭 무시, 시작 뒤 로그인도 반영

        intent.getStringExtra(FcmNotificationHelper.EXTRA_SUMMARY_GROUP)?.let {
            intent.removeExtra(FcmNotificationHelper.EXTRA_SUMMARY_GROUP)
            runCatching { navController.navigate(Screen.Notifications) { launchSingleTop = true } }
                .onFailure { e -> Timber.e(e, "요약 알림 화면 이동 실패") }
            return
        }

        // 트레이에서 탭해 들어왔으면 그 알림은 확인된 것으로 봄
        val notificationType = intent.getStringExtra("type")
        val targetType = intent.getStringExtra("targetType")
        val targetId = intent.getStringExtra("targetId")?.toIntOrNull()
        markTappedNotificationRead(intent, notificationType, targetId)
        val deepLink = intent.getStringExtra("deepLink")

        Timber.d("FCM 딥링크: targetType=$targetType, targetId=$targetId")

        // extras 소비, 중복 방지
        intent.removeExtra("type")
        intent.removeExtra("targetType")
        intent.removeExtra("targetId")
        intent.removeExtra("deepLink")

        val destination = resolveNotificationDestination(
            targetType = targetType,
            targetId = targetId,
            deepLink = deepLink,
            notificationType = notificationType
        ) ?: return

        val route: Screen = when (destination) {
            is NotificationDestination.Post -> Screen.PostDetail(destination.postId, destination.highlightCommentId)
            is NotificationDestination.Comment -> Screen.CommentDetail(
                destination.commentId,
                highlightAnchor = destination.highlightReplyId == null,
                highlightReplyId = destination.highlightReplyId
            )
            is NotificationDestination.User -> Screen.UserProfile(destination.userId)
            is NotificationDestination.Order -> Screen.OrderDetail(destination.orderId)
            is NotificationDestination.ChatRoom -> Screen.ChatRoom(destination.roomId.toString())
            is NotificationDestination.Product -> Screen.ProductDetail(destination.productId)
            NotificationDestination.Point -> Screen.Point
            NotificationDestination.CreatorApplication -> Screen.CreatorApplication
            NotificationDestination.MySubscriptions -> Screen.MySubscriptions
            NotificationDestination.SalesManage -> Screen.SalesManage
            is NotificationDestination.ProductReviews ->
                Screen.ReviewList(destination.productId, highlightReviewId = destination.reviewId)
        }

        try {
            navController.navigateUnlessOnTop(route)
        } catch (e: Exception) {
            Timber.e(e, "FCM 딥링크 화면 이동 실패")
        }
    }

    private fun markTappedNotificationRead(intent: Intent, type: String?, targetId: Int?) {
        val notificationId = intent.getStringExtra("notificationId")?.toIntOrNull()
        intent.removeExtra("notificationId")

        if (type == NotificationType.CHAT_MESSAGE) return  // 채팅은 알림 목록에 없음
        if (notificationId == null && (type == null || targetId == null)) return

        lifecycleScope.launch {
            if (!tokenManager.hasToken()) return@launch
            val result = if (notificationId != null) {
                notificationRepository.markAsRead(notificationId)
            } else {
                notificationRepository.markAsReadByTarget(type!!, targetId!!)
            }
            result.onSuccess { notificationSyncBus.notifyChanged() }
        }
    }

    companion object {
        private const val KEY_PENDING_DEEP_LINK = "pending_deep_link"
    }
}
