package com.ninezero.halo.core.network.auth

import com.ninezero.halo.core.database.HaloDatabase
import com.ninezero.halo.core.di.RefreshAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** 토큰 재발급 직렬화기 */
@Singleton
class TokenRefresher @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val sessionEventBus: SessionEventBus,
    private val database: HaloDatabase,
    @param:RefreshAuth private val refreshApi: TokenRefreshApi
) {
    private val mutex = Mutex()

    suspend fun refresh(tokenBeforeRefresh: String?): String? = mutex.withLock {  // null이면 실패, 서버 거부 시에만 로그아웃
        // 이미 갱신됐으면 그 토큰 사용
        val current = tokenProvider.getAccessToken()
        if (current != null && current != tokenBeforeRefresh) {
            return@withLock current
        }

        val refreshToken = tokenProvider.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            handleSessionExpired()
            return@withLock null
        }

        val response = try {
            refreshApi.refresh(RefreshTokenRequest(refreshToken))
        } catch (e: Exception) {
            // 네트워크 오류면 로그아웃 안 함
            Timber.w(e, "Token refresh 호출 실패(일시적) - 세션 유지")
            return@withLock null
        }

        val body = response.body()
        when {
            response.isSuccessful && body?.success == true && body.data != null -> {
                tokenProvider.updateTokens(
                    accessToken = body.data.accessToken,
                    refreshToken = body.data.refreshToken,
                    expiresInSeconds = body.data.expiresIn
                )
                body.data.accessToken
            }
            // 서버가 거부한 경우만 로그아웃, 400·401
            response.code() == 400 || response.code() == 401 -> {
                handleSessionExpired()
                null
            }
            // 429·5xx는 로그인 유지
            else -> {
                Timber.w("Token refresh 일시 실패(code=${response.code()}) - 세션 유지")
                null
            }
        }
    }

    private suspend fun handleSessionExpired() {
        val hadSession = tokenProvider.getAccessToken() != null ||
                tokenProvider.getRefreshToken() != null

        tokenProvider.clearToken()
        tokenProvider.clearUserId()  // userId 남기면 로그인으로 오인

        // 현재 계정의 로컬 데이터 정리
        runCatching {
            withContext(Dispatchers.IO) { database.clearAllTables() }
        }

        // 로그아웃 뒤 늦은 401 차단
        if (hadSession) {
            sessionEventBus.notifySessionExpired()
        }
    }
}
