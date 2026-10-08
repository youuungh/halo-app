package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.auth.domain.model.AuthSession
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import javax.inject.Inject

class VerifyEmailCodeUseCase @Inject constructor(  // 검증 성공 시 세션 저장
    private val repository: AuthRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(
        email: String,
        code: String,
        fid: String? = null
    ): Result<AuthSession> {
        if (code.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.CODE_REQUIRED))
        }

        if (code.length != Constants.Auth.VERIFICATION_CODE_LENGTH) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.CODE_LENGTH))
        }

        return repository.verifyEmailCode(email, code, fid).onSuccess { session ->
            tokenStore.saveLoginResult(session.token, session.user.id)
        }
    }
}
