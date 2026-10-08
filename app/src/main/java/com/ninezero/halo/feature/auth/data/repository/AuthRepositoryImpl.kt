package com.ninezero.halo.feature.auth.data.repository

import com.ninezero.halo.core.fcm.FcmRegistrationRepository
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallNoData
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.auth.data.local.DeviceIdProvider
import com.ninezero.halo.feature.auth.data.remote.api.AuthApi
import com.ninezero.halo.feature.auth.data.remote.dto.ChangePasswordRequest
import com.ninezero.halo.feature.auth.data.remote.dto.FcmRegistrationRequest
import com.ninezero.halo.feature.auth.data.remote.dto.ForgotPasswordRequest
import com.ninezero.halo.feature.auth.data.remote.dto.LoginRequest
import com.ninezero.halo.feature.auth.data.remote.dto.RegisterRequest
import com.ninezero.halo.feature.auth.data.remote.dto.ResendVerificationRequest
import com.ninezero.halo.feature.auth.data.remote.dto.ResetPasswordRequest
import com.ninezero.halo.feature.auth.data.remote.dto.SocialCompleteRequest
import com.ninezero.halo.feature.auth.data.remote.dto.SocialLoginRequest
import com.ninezero.halo.feature.auth.data.remote.dto.VerifyEmailCodeRequest
import com.ninezero.halo.feature.auth.data.remote.dto.VerifyResetCodeRequest
import com.ninezero.halo.feature.auth.data.remote.dto.toAuthToken
import com.ninezero.halo.feature.auth.data.remote.dto.toDomain
import com.ninezero.halo.feature.auth.domain.model.AuthSession
import com.ninezero.halo.feature.auth.domain.model.Device
import com.ninezero.halo.feature.auth.domain.model.SocialLoginResult
import com.ninezero.halo.feature.auth.domain.model.User
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.DeviceRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val deviceIdProvider: DeviceIdProvider
) : AuthRepository, FcmRegistrationRepository, DeviceRepository {

    override suspend fun login(
        email: String,
        password: String,
        fid: String?
    ): Result<AuthSession> {
        return safeApiCall {
            api.login(
                LoginRequest(
                    email = email,
                    password = password,
                    fid = fid,
                    deviceId = deviceIdProvider.getDeviceId()
                )
            )
        }.map { response ->
            AuthSession(
                user = response.user.toDomain(),
                token = response.toAuthToken()
            )
        }
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String
    ): Result<Unit> {
        return safeApiCallNoData {
            api.register(
                RegisterRequest(
                    email = email,
                    username = username,
                    password = password
                )
            )
        }
    }

    override suspend fun verifyEmailCode(
        email: String,
        code: String,
        fid: String?
    ): Result<AuthSession> {
        return safeApiCall {
            api.verifyEmailCode(
                VerifyEmailCodeRequest(
                    email = email,
                    code = code,
                    fid = fid,
                    deviceId = deviceIdProvider.getDeviceId()
                )
            )
        }.map { response ->
            AuthSession(
                user = response.user.toDomain(),
                token = response.toAuthToken()
            )
        }
    }

    override suspend fun resendVerification(email: String): Result<Unit> {
        return safeApiCallNoData {
            api.resendVerification(ResendVerificationRequest(email = email))
        }
    }

    override suspend fun socialLogin(
        provider: String,
        token: String,
        fid: String?
    ): Result<SocialLoginResult> {
        return safeApiCall {
            api.socialLogin(
                SocialLoginRequest(
                    provider = provider,
                    token = token,
                    fid = fid,
                    deviceId = deviceIdProvider.getDeviceId()
                )
            )
        }.mapCatching { response ->
            if (response.needsUsername) {
                SocialLoginResult.NeedsUsername(
                    signupToken = requireNotNull(response.signupToken) { "signupToken 누락" },
                    suggestedUsername = response.suggestedUsername.orEmpty()
                )
            } else {
                val auth = requireNotNull(response.auth) { "auth 누락" }
                SocialLoginResult.Success(
                    user = auth.user.toDomain(),
                    token = auth.toAuthToken()
                )
            }
        }
    }

    override suspend fun socialComplete(
        signupToken: String,
        username: String,
        fid: String?
    ): Result<AuthSession> {
        return safeApiCall {
            api.socialComplete(
                SocialCompleteRequest(
                    signupToken = signupToken,
                    username = username,
                    fid = fid,
                    deviceId = deviceIdProvider.getDeviceId()
                )
            )
        }.map { response ->
            AuthSession(response.user.toDomain(), response.toAuthToken())
        }
    }

    override suspend fun logout(): Result<Unit> {
        return safeApiCallNoData {
            api.logout()
        }
    }

    override suspend fun requestPasswordReset(email: String): Result<Unit> {
        return safeApiCallNoData {
            api.requestPasswordReset(
                ForgotPasswordRequest(email)
            )
        }
    }

    override suspend fun verifyResetCode(email: String, code: String): Result<String> {
        return safeApiCall {
            api.verifyResetCode(
                VerifyResetCodeRequest(email = email, code = code)
            )
        }.map { response ->
            response.resetToken
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        return safeApiCallNoData {
            api.resetPassword(
                ResetPasswordRequest(token = token, newPassword = newPassword)
            )
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return safeApiCall {
            api.getCurrentUser()
        }.map { userDto ->
            userDto.toDomain()
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return safeApiCall {
            api.changePassword(
                ChangePasswordRequest(currentPassword = currentPassword, newPassword = newPassword)
            )
        }.map { }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return safeApiCall {
            api.deleteAccount()
        }.map { }
    }

    // FcmRegistrationRepository

    override suspend fun registerFid(fid: String): Result<Unit> {
        return safeApiCallNoData {
            api.registerFid(
                FcmRegistrationRequest(fid = fid, deviceId = deviceIdProvider.getDeviceId())
            )
        }
    }

    override suspend fun deleteFid(): Result<Unit> {
        return safeApiCallUnit {
            // 이 기기 토큰만 삭제
            api.deleteFid(deviceId = deviceIdProvider.getDeviceId())
        }
    }

    // DeviceRepository

    override suspend fun getActiveDevices(): Result<List<Device>> {
        return safeApiCall {
            api.getActiveDevices()
        }.map { response ->
            response.devices.map { it.toDomain() }
        }
    }

    override suspend fun logoutDevice(sessionId: String): Result<Unit> {
        return safeApiCallUnit {
            api.logoutDevice(sessionId)
        }
    }
}
