package com.ninezero.halo.feature.auth.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.auth.data.remote.dto.ChangePasswordRequest
import com.ninezero.halo.feature.auth.data.remote.dto.DeviceListResponse
import com.ninezero.halo.feature.auth.data.remote.dto.FcmRegistrationRequest
import com.ninezero.halo.feature.auth.data.remote.dto.ForgotPasswordRequest
import com.ninezero.halo.feature.auth.data.remote.dto.LoginRequest
import com.ninezero.halo.feature.auth.data.remote.dto.LoginResponse
import com.ninezero.halo.feature.auth.data.remote.dto.RegisterRequest
import com.ninezero.halo.feature.auth.data.remote.dto.RegisterResponse
import com.ninezero.halo.feature.auth.data.remote.dto.ResendVerificationRequest
import com.ninezero.halo.feature.auth.data.remote.dto.ResetPasswordRequest
import com.ninezero.halo.feature.auth.data.remote.dto.SocialCompleteRequest
import com.ninezero.halo.feature.auth.data.remote.dto.SocialLoginRequest
import com.ninezero.halo.feature.auth.data.remote.dto.SocialLoginResponse
import com.ninezero.halo.feature.auth.data.remote.dto.UserDto
import com.ninezero.halo.feature.auth.data.remote.dto.VerifyEmailCodeRequest
import com.ninezero.halo.feature.auth.data.remote.dto.VerifyResetCodeRequest
import com.ninezero.halo.feature.auth.data.remote.dto.VerifyResetCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    /** 로그인 (공개) */
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    /** 회원가입 인증 코드 발송 (공개) */
    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<RegisterResponse>>

    /** 회원가입 인증 코드 검증 (공개) */
    @POST("api/v1/auth/verify-email-code")
    suspend fun verifyEmailCode(
        @Body request: VerifyEmailCodeRequest
    ): Response<ApiResponse<LoginResponse>>

    /** 인증 코드 재발송 (공개) */
    @POST("api/v1/auth/resend-verification")
    suspend fun resendVerification(
        @Body request: ResendVerificationRequest
    ): Response<ApiResponse<String>>

    /** 간편로그인 (공개) */
    @POST("api/v1/auth/social/login")
    suspend fun socialLogin(
        @Body request: SocialLoginRequest
    ): Response<ApiResponse<SocialLoginResponse>>

    /** 간편로그인 가입 확정 (공개) */
    @POST("api/v1/auth/social/complete")
    suspend fun socialComplete(
        @Body request: SocialCompleteRequest
    ): Response<ApiResponse<LoginResponse>>

    /** 로그아웃 */
    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiResponse<Map<String, String>>>

    /** 비밀번호 재설정 요청 (공개) */
    @POST("api/v1/auth/forgot-password")
    suspend fun requestPasswordReset(
        @Body request: ForgotPasswordRequest
    ): Response<ApiResponse<String>>

    /** 비밀번호 재설정 코드 검증 (공개) */
    @POST("api/v1/auth/verify-reset-code")
    suspend fun verifyResetCode(
        @Body request: VerifyResetCodeRequest
    ): Response<ApiResponse<VerifyResetCodeResponse>>

    /** 비밀번호 재설정 (공개) */
    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<String>>

    /** 내 정보 조회 */
    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    /** 비밀번호 변경 */
    @POST("api/v1/auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<String>>

    /** 계정 탈퇴 */
    @DELETE("api/v1/auth/me")
    suspend fun deleteAccount(): Response<ApiResponse<String>>

    /** FCM 토큰 등록 */
    @POST("api/v1/auth/fcm-token")
    suspend fun registerFid(
        @Body request: FcmRegistrationRequest
    ): Response<ApiResponse<Map<String, String>>>

    /** FCM 토큰 삭제 */
    @DELETE("api/v1/auth/fcm-token")
    suspend fun deleteFid(
        @Query("deviceId") deviceId: String?
    ): Response<Unit>

    /** 활성 기기 목록 조회 */
    @GET("api/v1/auth/devices")
    suspend fun getActiveDevices(): Response<ApiResponse<DeviceListResponse>>

    /** 기기 로그아웃 */
    @DELETE("api/v1/auth/devices/{sessionId}")
    suspend fun logoutDevice(
        @Path("sessionId") sessionId: String
    ): Response<Unit>
}
