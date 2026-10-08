package com.ninezero.halo.feature.auth.domain.repository

import com.ninezero.halo.feature.auth.domain.model.Device

interface DeviceRepository {
    /** 활성 기기 목록 조회 */
    suspend fun getActiveDevices(): Result<List<Device>>
    /** 기기 로그아웃 */
    suspend fun logoutDevice(sessionId: String): Result<Unit>
}
