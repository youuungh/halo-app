package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.feature.auth.domain.model.SocialLoginResult
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import javax.inject.Inject

class SocialLoginUseCase @Inject constructor(  // Success면 토큰 저장, NeedsUsername은 확정 후
    private val repository: AuthRepository,
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(
        provider: String,
        token: String,
        fid: String? = null
    ): Result<SocialLoginResult> {
        return repository.socialLogin(provider, token, fid).onSuccess { result ->
            if (result is SocialLoginResult.Success) {
                tokenStore.saveLoginResult(result.token, result.user.id)
            }
        }
    }
}
