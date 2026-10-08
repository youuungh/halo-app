package com.ninezero.halo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NidOAuth
import com.ninezero.halo.core.database.dao.CachedMessageDao
import com.ninezero.halo.core.database.dao.PendingChatMessageDao
import com.ninezero.halo.core.database.dao.PendingCommentDao
import com.ninezero.halo.core.database.dao.PendingPostDao
import com.ninezero.halo.core.database.dao.PostDraftDao
import com.ninezero.halo.core.fcm.FcmRegistrationManager
import com.ninezero.halo.core.fcm.FcmRegistrationRepository
import com.ninezero.halo.core.network.websocket.RealtimeSocket
import com.ninezero.halo.core.ui.components.video_player.VideoPlayerHolder
import com.ninezero.halo.core.util.MediaPersistenceManager
import com.ninezero.halo.feature.auth.data.local.TokenManager
import com.ninezero.halo.feature.auth.presentation.social.clearNaverSession
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import com.ninezero.halo.feature.feed.domain.repository.CommentRepository
import com.ninezero.halo.feature.feed.domain.repository.PostRepository
import com.ninezero.halo.feature.notification.fcm.FcmNotificationHelper
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class HaloApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var postDraftDao: PostDraftDao

    @Inject
    lateinit var pendingPostDao: PendingPostDao

    @Inject
    lateinit var pendingCommentDao: PendingCommentDao

    @Inject
    lateinit var pendingChatMessageDao: PendingChatMessageDao

    @Inject
    lateinit var cachedMessageDao: CachedMessageDao

    @Inject
    lateinit var mediaPersistence: MediaPersistenceManager

    @Inject
    lateinit var realtimeSocket: RealtimeSocket

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var fcmRegistrationManager: FcmRegistrationManager

    @Inject
    lateinit var fcmRegistrationRepository: FcmRegistrationRepository

    @Inject
    lateinit var chatRepository: ChatRepository

    @Inject
    lateinit var postRepository: PostRepository

    @Inject
    lateinit var commentRepository: CommentRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // FCM 토큰 서버 동기화 여부
    @Volatile
    private var fcmSynced = false

    // 포그라운드 전용 job, onStop에서 먼저 취소
    private var foregroundJob: Job? = null

    // 이미지 전용 OkHttp, 로깅 없음
    private val imageOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }

        if (BuildConfig.NAVER_CLIENT_ID.isNotBlank()) {
            NidOAuth.initialize(
                this,
                BuildConfig.NAVER_CLIENT_ID,
                BuildConfig.NAVER_CLIENT_SECRET,
                "Halo"
            )
        }

        FcmNotificationHelper.createNotificationChannels(this)

        // FID는 onRegistered로만 도착
        fcmRegistrationManager.requestRegistration()

        // 실시간 WebSocket 수명 관리
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // 포그라운드 플래그 먼저, 아니면 소켓 안 붙음
                realtimeSocket.setForeground(true)
                VideoPlayerHolder.resumeFromBackground()  // 백그라운드로 멈춰둔 영상 재개
                // 복귀 시 남은 그룹 요약 정리
                FcmNotificationHelper.pruneOrphanSummaries(this@HaloApplication)
                foregroundJob?.cancel()
                foregroundJob = applicationScope.launch {
                    // 로그인 상태 판정 전에 토큰 한 번 로드
                    tokenManager.getAccessToken()

                    launch {
                        tokenManager.loggedIn.collect { loggedIn ->
                            if (loggedIn) realtimeSocket.connect() else realtimeSocket.disconnect()
                        }
                    }

                    // 토큰 갱신 신호로 소켓 부활
                    launch {
                        tokenManager.tokenRefreshed.collect { realtimeSocket.onTokenRefreshed() }
                    }

                    // 채팅 실시간 이벤트는 채팅 화면이 없어도 캐시에 반영
                    launch { chatRepository.applyRealtimeEvents() }

                    // 별도 collectLatest, 등록이 느려 분리
                    tokenManager.loggedIn.collectLatest { loggedIn ->
                        if (!loggedIn) {
                            fcmSynced = false
                            FcmNotificationHelper.clearUserNotifications(this@HaloApplication)  // 현재 계정의 트레이 알림 정리
                            profileRepository.clearMyProfileCache()  // 다음 계정에 이전 계정 프로필이 쓰이지 않게
                            postRepository.clearCachedPosts()  // 이전 계정 기준 글·댓글도
                            commentRepository.clearCachedComments()
                            clearNaverSession()  // 다음 네이버 로그인에서 계정을 다시 고르게
                            return@collectLatest
                        }
                        if (!fcmSynced) {
                            fcmRegistrationManager.getFid()?.let { token ->
                                fcmRegistrationRepository.registerFid(token)
                                    .onSuccess { fcmSynced = true }
                            }
                        }
                    }
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                // 플래그 먼저 닫음, cancel은 비동기
                realtimeSocket.setForeground(false)
                VideoPlayerHolder.pauseForBackground()  // 백그라운드 영상 디코드·오디오 방지
                foregroundJob?.cancel()  // 해제보다 먼저 취소, 순서 바뀌면 게이트 재개방
                foregroundJob = null
                realtimeSocket.disconnect()
            }
        })

        // 임시저장 / 업로드 큐 사본 중 DB에 없는 미참조 파일 정리
        applicationScope.launch {
            // 워커 없이 남은 대기 글·댓글, 사본은 아래에서 정리
            runCatching {
                val workManager = WorkManager.getInstance(this@HaloApplication)
                pendingPostDao.getUnqueuedIds()
                    .filter { workManager.getWorkInfosByTagFlow("post_upload_$it").first().isEmpty() }
                    .forEach { pendingPostDao.deleteById(it) }
                pendingCommentDao.getUnqueuedIds()
                    .filter { workManager.getWorkInfosByTagFlow("comment_upload_$it").first().isEmpty() }
                    .forEach { pendingCommentDao.deleteById(it) }
            }.onFailure { Timber.e(it, "대기 글 정리 실패") }

            runCatching {
                val draftIds = postDraftDao.getAllIds().toSet()
                mediaPersistence.cleanupOrphans(
                    MediaPersistenceManager.OwnerType.DRAFT,
                    draftIds
                )
                val pendingIds = pendingPostDao.getAllPendingPostIds().toSet()
                mediaPersistence.cleanupOrphans(
                    MediaPersistenceManager.OwnerType.PENDING_POST,
                    pendingIds
                )

                val pendingCommentIds = pendingCommentDao.getAllIds().toSet()
                mediaPersistence.cleanupOrphans(
                    MediaPersistenceManager.OwnerType.PENDING_COMMENT,
                    pendingCommentIds
                )

                val pendingChatIds = pendingChatMessageDao.getAllIds().toSet()
                mediaPersistence.cleanupOrphans(
                    MediaPersistenceManager.OwnerType.PENDING_CHAT,
                    pendingChatIds
                )
            }.onFailure { Timber.e(it, "고아 미디어 정리 실패") }
        }

        // 30일 이상 재조회되지 않은 채팅 메시지 캐시 정리
        applicationScope.launch {
            runCatching {
                cachedMessageDao.deleteSentOlderThan(
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
                )
            }.onFailure { Timber.e(it, "캐시된 메시지 정리 실패") }
        }

        Timber.d("HaloApplication 초기화 완료")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(200L * 1024 * 1024)  // 200 MB
                    .build()
            }
            .components {
                // 로컬 동영상 프레임 디코딩, 없으면 회색 프리뷰
                add(VideoFrameDecoder.Factory())
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { imageOkHttpClient }
                    )
                )
            }
            .crossfade(true)
            .build()
    }
}
