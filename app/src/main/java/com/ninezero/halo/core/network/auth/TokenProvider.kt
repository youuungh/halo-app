package com.ninezero.halo.core.network.auth

interface TokenProvider {
    suspend fun getAccessToken(): String?

    fun getAccessTokenBlocking(): String?  // OkHttp 계약이 동기라 blocking

    /** access token 만료 시각 */
    fun getExpiresAtBlocking(): Long?  // 선제 refresh 판정용

    suspend fun getRefreshToken(): String?

    suspend fun updateTokens(accessToken: String, refreshToken: String, expiresInSeconds: Long?)

    suspend fun clearToken()

    /** 로그인 사용자 id */
    suspend fun getUserId(): Int?

    suspend fun clearUserId()  // 남으면 로그아웃 상태를 로그인으로 오판
}
