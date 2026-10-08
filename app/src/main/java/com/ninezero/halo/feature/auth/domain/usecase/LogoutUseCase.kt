package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.core.database.HaloDatabase
import com.ninezero.halo.core.fcm.FcmRegistrationRepository
import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import com.ninezero.halo.feature.store.domain.repository.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val fcmRegistrationRepository: FcmRegistrationRepository,
    private val tokenStore: TokenStore,
    private val cartRepository: CartRepository,
    private val database: HaloDatabase
) {
    suspend operator fun invoke(): Result<Unit> {
        // FCM 토큰 해제, 실패해도 진행
        runCatching { fcmRegistrationRepository.deleteFid() }

        // 로그아웃 성공 결과와 무관하게 로컬을 정리
        val serverResult = repository.logout()
        serverResult.onFailure { Timber.w(it, "로그아웃 서버 요청 실패") }

        tokenStore.clearToken()
        tokenStore.clearUserId()
        cartRepository.resetCartCount()

        // 계정 로컬 데이터 전부 삭제
        runCatching {
            withContext(Dispatchers.IO) { database.clearAllTables() }
        }

        return Result.success(Unit)
    }
}
