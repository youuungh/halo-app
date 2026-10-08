package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.database.HaloDatabase
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(  // 성공 시 로컬 세션 정리
    private val repository: AuthRepository,
    private val tokenStore: TokenStore,
    private val cartRepository: CartRepository,
    private val database: HaloDatabase
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.deleteAccount().onSuccess {
            tokenStore.clearToken()
            tokenStore.clearUserId()
            cartRepository.resetCartCount()

            // 계정 로컬 데이터 전부 삭제
            runCatching {
                withContext(Dispatchers.IO) { database.clearAllTables() }
            }
        }
    }
}
