package com.ninezero.halo.core.network.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/** 401 시 토큰 재발급 후 재시도 */
@Singleton
class TokenAuthenticator @Inject constructor(  // 재발급은 TokenRefresher가 1회
    private val tokenRefresher: TokenRefresher
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (PUBLIC_AUTH_PATHS.any { path.endsWith(it) }) return null

        // OkHttp가 같은 요청을 무한 재시도하지 않도록 카운트 제한
        if (responseCount(response) >= 2) return null

        val originalAccessToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")?.trim()

        // 동기 계약이라 runBlocking, OkHttp 스레드
        val newToken = runBlocking { tokenRefresher.refresh(originalAccessToken) } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        // 인증 없는 주소, 401은 비밀번호 오류
        val PUBLIC_AUTH_PATHS = listOf(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/verify-email-code",
            "/api/v1/auth/resend-verification",
            "/api/v1/auth/social/login",
            "/api/v1/auth/social/complete",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/verify-reset-code",
            "/api/v1/auth/reset-password",
        )
    }
}
