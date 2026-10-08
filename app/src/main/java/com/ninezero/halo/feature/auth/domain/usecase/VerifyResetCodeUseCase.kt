package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyResetCodeUseCase @Inject constructor(  // 성공 시 재설정 토큰 반환
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): Result<String> {
        if (code.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.CODE_REQUIRED))
        }

        if (code.length != Constants.Auth.VERIFICATION_CODE_LENGTH) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.CODE_LENGTH))
        }

        return repository.verifyResetCode(email, code)
    }
}
