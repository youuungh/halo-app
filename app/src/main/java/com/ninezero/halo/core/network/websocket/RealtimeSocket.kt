package com.ninezero.halo.core.network.websocket

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.ninezero.halo.core.network.auth.TokenProvider
import com.ninezero.halo.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_AUTH_FAILURES = 3
private const val PING_INTERVAL_SECONDS = 20L

/** 채팅·알림 실시간 소켓 */
@Singleton
class RealtimeSocket @Inject constructor(  // 포그라운드에서만 연결, 연결 중엔 서버가 푸시 생략
    okHttpClient: OkHttpClient,
    private val tokenProvider: TokenProvider,
    private val json: Json,
    @param:ApplicationContext private val context: Context
) {
    private val wsClient = okHttpClient.newBuilder()
        .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var webSocket: WebSocket? = null

    // 버퍼 없으면 전송 항상 실패
    private val _frames = MutableSharedFlow<RealtimeFrame>(replay = 0, extraBufferCapacity = 64)
    val frames: SharedFlow<RealtimeFrame> = _frames.asSharedFlow()


    @Volatile
    private var isOpen = false // 완전히 열렸는지, 정상 종료·취소 판별용

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 연결은 한 번에 하나, 겹치면 소켓 둘
    private val connectMutex = Mutex()

    // 소켓 일련번호, 옛 응답 무시용
    private val generation = AtomicInteger(0)

    // 재연결을 계속할지 여부, 여러 스레드가 함께 읽음
    @Volatile
    private var shouldReconnect = true
    private val reconnectAttempts = AtomicInteger(0)

    // 포그라운드 여부, 정리 틈에 재연결 방지
    // 소켓 생성 직전에 이 값을 한 번 더 확인해 차단
    @Volatile
    private var isForeground = false

    // 401 누적, 성공 시 0
    private val authFailures = AtomicInteger(0)

    @Volatile
    private var networkCallbackRegistered = false

    fun setForeground(foreground: Boolean) {  // 내려갈 때는 정리보다 먼저 false
        isForeground = foreground
    }

    suspend fun connect() {
        // 취소 여부 먼저 확인, 아니면 소켓 부활
        currentCoroutineContext().ensureActive()
        registerNetworkCallbackOnce()
        reconnectAttempts.set(0)
        authFailures.set(0)
        shouldReconnect = true
        doConnect()
    }

    /** 새 토큰으로 재연결 */
    fun onTokenRefreshed() {
        if (!isForeground || shouldReconnect) return
        Timber.d("Realtime: 토큰 갱신 감지 — 재연결 재개")
        authFailures.set(0)
        reconnectAttempts.set(0)
        shouldReconnect = true
        scope.launch { doConnect() }
    }

    /** 네트워크 복귀 즉시 재연결 등록 */
    private fun registerNetworkCallbackOnce() {
        if (networkCallbackRegistered) return
        networkCallbackRegistered = true
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                reconnectAttempts.set(0)
                // 사전 필터
                if (shouldReconnect && webSocket == null) {
                    Timber.d("Realtime: 네트워크 사용 가능 — 즉시 재연결")
                    scope.launch { doConnect() }
                }
            }
        })
    }

    private suspend fun doConnect() {
        connectMutex.withLock {
            // disconnect 후 재연결 차단
            if (!shouldReconnect) return

            // 백그라운드면 여기서 차단
            if (!isForeground) return

            // 소켓  "연결됨"과 "연결 중" 동시 판별
            if (webSocket != null) {
                Timber.d("Realtime: 이미 연결됨 또는 연결 중")
                return
            }

            val token = tokenProvider.getAccessToken()
            if (token == null) {
                // 토큰 없으면 무한 재연결 방지
                Timber.w("Realtime: access token 없음 — 재연결 중단")
                shouldReconnect = false
                return
            }

            // 토큰 읽기는 1회, 백그라운드 재확인
            if (!shouldReconnect) return

            val request = Request.Builder()
                .url("${Constants.WS_BASE_URL}ws/realtime")
                .header("Authorization", "Bearer $token")
                .build()

            val listener = SocketListener(generation.incrementAndGet())
            val socket = wsClient.newWebSocket(request, listener)
            webSocket = socket

            // 대입 전 실패·끊기 확인, 안 하면 재연결 막힘
            if (!shouldReconnect || listener.dead) {
                webSocket = null
                socket.cancel()
            }
        }
    }

    fun disconnect() {
        // 재연결 먼저 꺼 연결 중인 쪽이 취소
        shouldReconnect = false
        generation.incrementAndGet()  // 일련번호도 올려 옛 응답 무시
        // 열린 소켓은 정상 종료, 연결 중이면 취소
        // 안 그러면 연결이 완료돼 서버에 세션이 남음
        if (isOpen) {
            webSocket?.close(1000, "User disconnected")
        } else {
            webSocket?.cancel()
        }
        webSocket = null
        isOpen = false
        Timber.d("Realtime: 연결 해제됨")
    }

    /** 일련번호 달린 소켓 리스너 */
    private inner class SocketListener(private val gen: Int) : WebSocketListener() {
        @Volatile
        var dead = false  // 옛 소켓 응답 필터
            private set

        private fun isCurrent() = gen == generation.get()

        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (!isCurrent()) {
                // 안 닫으면 서버가 푸시를 계속 생략
                Timber.d("Realtime: 교체된 소켓(gen=$gen) — 닫는다")
                webSocket.close(1000, "Superseded")
                return
            }

            reconnectAttempts.set(0)
            authFailures.set(0)
            isOpen = true
            Timber.d("Realtime: 연결됨")
            emit(RealtimeFrame(type = RealtimeFrameType.CONNECTED))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // 옛 소켓 메시지까지 흘리면 같은 이벤트 두 번 처리됨
            if (!isCurrent()) return

            Timber.d("Realtime: 수신 - $text")
            val frame = try {
                json.decodeFromString<RealtimeFrame>(text)
            } catch (e: Exception) {
                Timber.d("Realtime: 파싱할 수 없는 프레임 - $text")
                return
            }
            // 서버 CONNECTED는 버림, 중복 조회 방지
            if (frame.type == RealtimeFrameType.CONNECTED) return
            emit(frame)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            // 옛 소켓 확인보다 먼저 표시
            dead = true
            if (!isCurrent()) return

            isOpen = false
            Timber.e(t, "Realtime: 연결 실패 (httpCode=${response?.code})")
            emit(RealtimeFrame(type = RealtimeFrameType.ERROR, message = t.message ?: "연결 실패"))
            this@RealtimeSocket.webSocket = null

            // 여러 번 실패해야 중단
            if (response?.code == 401) {
                val failures = authFailures.incrementAndGet()
                if (failures >= MAX_AUTH_FAILURES) {
                    Timber.w("Realtime: 인증 거부 ${failures}회 — 재연결 중단")
                    shouldReconnect = false
                    return
                }
                Timber.w("Realtime: 인증 거부됨 (401) — 재시도 ($failures/$MAX_AUTH_FAILURES)")
            }
            scheduleReconnect("failure")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("Realtime: 종료 중 - code: $code, reason: $reason")
            // 서버가 끊어도 close 호출 필수, 안 하면 이후 연결 차단
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            dead = true
            if (!isCurrent()) return

            isOpen = false
            Timber.d("Realtime: 종료됨 - code: $code, reason: $reason")
            this@RealtimeSocket.webSocket = null

            // 1008 = 서버가 인증 실패로 끊음
            if (code == 1008) {
                Timber.w("Realtime: 정책 위반 (1008) — 재연결 중단")
                shouldReconnect = false
                return
            }
            scheduleReconnect("close")
        }
    }

    private fun emit(frame: RealtimeFrame) {
        if (!_frames.tryEmit(frame)) {
            Timber.e("Realtime: 프레임 드롭 - ${frame.type}")
        }
    }

    /** 지수 백오프 재연결 예약 */
    private fun scheduleReconnect(cause: String) {  // 횟수 제한 없음
        if (!shouldReconnect) return

        // 증가값을 지역 변수로, 필드 재읽기는 크래시
        val attempt = reconnectAttempts.incrementAndGet()
        // 1s → 2s → 4s → 8s → 16s, 30s 캡
        val baseMs = (1000L shl (attempt - 1).coerceAtMost(5)).coerceAtMost(30_000L)
        val delayMs = baseMs + ThreadLocalRandom.current().nextLong(baseMs / 2)
        scope.launch {
            Timber.d("Realtime: $cause 이후 ${delayMs}ms 뒤 재연결 (시도 ${attempt}회)")
            delay(delayMs)
            doConnect()
        }
    }
}
