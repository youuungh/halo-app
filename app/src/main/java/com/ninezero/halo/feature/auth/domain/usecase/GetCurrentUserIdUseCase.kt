package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(  // API 호출 없음
    private val tokenStore: TokenStore
) {
    suspend operator fun invoke(): Int? {
        return tokenStore.getUserId()
    }
}
