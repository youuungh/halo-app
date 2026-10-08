package com.ninezero.halo.feature.auth.domain.repository

import com.ninezero.halo.feature.auth.domain.model.AuthSession
import com.ninezero.halo.feature.auth.domain.model.SocialLoginResult
import com.ninezero.halo.feature.auth.domain.model.User

interface AuthRepository {
    /** 로그인 */
    suspend fun login(
        email: String,
        password: String,
        fid: String?
    ): Result<AuthSession>

    /** 회원가입 인증 코드 발송 */
    suspend fun register(
        email: String,
        username: String,
        password: String
    ): Result<Unit>

    /** 회원가입 인증 코드 검증 */
    suspend fun verifyEmailCode(
        email: String,
        code: String,
        fid: String?
    ): Result<AuthSession>

    /** 인증 코드 재발송 */
    suspend fun resendVerification(email: String): Result<Unit>

    /** 간편로그인 */
    suspend fun socialLogin(
        provider: String,
        token: String,
        fid: String?
    ): Result<SocialLoginResult>

    /** 간편로그인 가입 확정 */
    suspend fun socialComplete(
        signupToken: String,
        username: String,
        fid: String?
    ): Result<AuthSession>

    /** 로그아웃 */
    suspend fun logout(): Result<Unit>

    /** 비밀번호 재설정 요청 */
    suspend fun requestPasswordReset(email: String): Result<Unit>

    /** 비밀번호 재설정 코드 검증 */
    suspend fun verifyResetCode(email: String, code: String): Result<String>

    /** 비밀번호 재설정 */
    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>

    /** 내 정보 조회 */
    suspend fun getCurrentUser(): Result<User>

    /** 비밀번호 변경 */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /** 계정 탈퇴 */
    suspend fun deleteAccount(): Result<Unit>
}
