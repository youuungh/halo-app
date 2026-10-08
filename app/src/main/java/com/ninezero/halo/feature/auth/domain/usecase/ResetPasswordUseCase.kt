package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.util.isValidPassword
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String, newPassword: String): Result<Unit> {
        if (newPassword.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.PASSWORD_REQUIRED))
        }

        if (!newPassword.isValidPassword()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.PASSWORD_INVALID))
        }

        return repository.resetPassword(token, newPassword)
    }
}
