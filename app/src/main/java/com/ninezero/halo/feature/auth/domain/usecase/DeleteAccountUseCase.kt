package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.database.LocalAccountData
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(  // 성공 시 로컬 세션 정리
    private val repository: AuthRepository,
    private val tokenStore: TokenStore,
    private val localAccountData: LocalAccountData
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.deleteAccount().onSuccess {
            tokenStore.clearToken()
            tokenStore.clearUserId()
            localAccountData.clear()
        }
    }
}
