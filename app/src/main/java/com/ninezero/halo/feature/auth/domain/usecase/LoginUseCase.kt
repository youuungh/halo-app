package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.feature.auth.domain.model.AuthSession
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        fid: String? = null
    ): Result<AuthSession> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.EMAIL_REQUIRED))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.PASSWORD_REQUIRED))
        }

        return repository.login(email, password, fid).onSuccess { session ->
            tokenStore.saveLoginResult(session.token, session.user.id)
        }
    }
}
