package com.ninezero.halo.feature.auth.domain.usecase

import com.ninezero.halo.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(  // 다른 기기 전부 로그아웃
    private val repository: AuthRepository
) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        return repository.changePassword(currentPassword, newPassword)
    }
}
