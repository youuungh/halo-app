package com.ninezero.halo.feature.auth.data.remote.dto

import com.ninezero.halo.core.util.parseTimestamp
import com.ninezero.halo.feature.auth.domain.model.AuthToken
import com.ninezero.halo.feature.auth.domain.model.Device
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val fid: String? = null,
    val deviceId: String? = null
)

@Serializable
data class LoginResponse(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Long? = null
)

@Serializable
data class SocialLoginRequest(
    val provider: String,
    val token: String,  // 구글은 idToken, 카카오·네이버는 액세스 토큰
    val fid: String? = null,
    val deviceId: String? = null
)

@Serializable
data class SocialLoginResponse(  // needsUsername이면 signupToken, 아니면 auth
    val needsUsername: Boolean,
    val auth: LoginResponse? = null,
    val signupToken: String? = null,
    val suggestedUsername: String? = null
)

@Serializable
data class SocialCompleteRequest(
    val signupToken: String,
    val username: String,
    val fid: String? = null,
    val deviceId: String? = null
)

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

@Serializable
data class RegisterResponse(  // 계정 없이 코드만 발송된 상태
    val email: String,
    val username: String,
    val message: String
)

@Serializable
data class VerifyEmailCodeRequest(  // 검증 성공 시 계정 생성이라 세션 정보 포함
    val email: String,
    val code: String,
    val fid: String? = null,
    val deviceId: String? = null
)

@Serializable
data class ResendVerificationRequest(
    val email: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class VerifyResetCodeRequest(
    val email: String,
    val code: String
)

@Serializable
data class VerifyResetCodeResponse(
    val resetToken: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

@Serializable
data class FcmRegistrationRequest(
    val fid: String,
    val deviceId: String? = null
)

@Serializable
data class DeviceResponse(
    val id: String,
    val deviceType: String = "",
    val deviceName: String,
    val browser: String = "",
    val os: String,
    val ipAddress: String,
    val location: String? = null,
    val lastActive: String,
    val isCurrent: Boolean,
    val createdAt: String = ""
)

@Serializable
data class DeviceListResponse(
    val devices: List<DeviceResponse>
)

fun LoginResponse.toAuthToken(): AuthToken {
    return AuthToken(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    )
}

fun DeviceResponse.toDomain(): Device {
    return Device(
        id = id,
        deviceName = deviceName,
        os = os,
        ipAddress = ipAddress,
        location = location,
        lastActiveAt = lastActive.parseTimestamp(),
        isCurrent = isCurrent
    )
}
