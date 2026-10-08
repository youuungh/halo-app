package com.ninezero.halo.core.network.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Authorization 헤더 부착 인터셉터 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val tokenRefresher: TokenRefresher
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val initial = tokenProvider.getAccessTokenBlocking()

        val token = if (initial != null && isNearExpiry()) {
            // 만료 임박이면 선제 갱신, 실패 시 기존 토큰
            runBlocking { tokenRefresher.refresh(initial) } ?: initial
        } else {
            initial
        }

        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }

    private fun isNearExpiry(): Boolean {
        val expiresAt = tokenProvider.getExpiresAtBlocking() ?: return false
        return System.currentTimeMillis() >= expiresAt - REFRESH_THRESHOLD_MS
    }

    companion object {
        private const val REFRESH_THRESHOLD_MS = 60_000L
    }
}
