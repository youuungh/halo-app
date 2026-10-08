package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.feature.auth.domain.model.AuthSession
import com.ninezero.halo.feature.auth.domain.model.AuthValidationMessages
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import javax.inject.Inject

class SocialCompleteUseCase @Inject constructor(  // 계정 생성 후 토큰 저장
    private val repository: AuthRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(
        signupToken: String,
        username: String,
        fid: String? = null
    ): Result<AuthSession> {
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.USERNAME_REQUIRED))
        }
        if (username.trim().length < Constants.Auth.MIN_USERNAME_LENGTH) {
            return Result.failure(IllegalArgumentException(AuthValidationMessages.USERNAME_TOO_SHORT))
        }

        return repository.socialComplete(signupToken, username.trim(), fid)
            .onSuccess { session ->
                tokenStore.saveLoginResult(session.token, session.user.id)
            }
    }
}
