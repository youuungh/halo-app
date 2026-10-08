package com.ninezero.halo.core.network.auth

import com.ninezero.halo.core.network.ApiResponse
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenRefreshApi {
    /** 토큰 재발급 (공개) */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<RefreshTokenResponse>>
}

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long? = null
)
