package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.isValidEmail
import com.ninezero.halo.core.util.isValidPassword
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.EMAIL_REQUIRED))
        }
        if (!email.isValidEmail()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.EMAIL_INVALID))
        }
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.USERNAME_REQUIRED))
        }
        if (username.trim().length < Constants.Auth.MIN_USERNAME_LENGTH) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.USERNAME_TOO_SHORT))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.PASSWORD_REQUIRED))
        }
        if (!password.isValidPassword()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.PASSWORD_INVALID))
        }
        if (password != confirmPassword) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.PASSWORD_MISMATCH))
        }

        return repository.register(email, username.trim(), password)
    }
}
