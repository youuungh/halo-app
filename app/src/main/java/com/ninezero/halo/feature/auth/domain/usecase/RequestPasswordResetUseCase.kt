package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.util.isValidEmail
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RequestPasswordResetUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.EMAIL_REQUIRED))
        }

        if (!email.isValidEmail()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.EMAIL_INVALID))
        }

        return repository.requestPasswordReset(email)
    }
}
