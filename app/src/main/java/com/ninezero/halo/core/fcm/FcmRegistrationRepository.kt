package com.ninezero.halo.core.fcm

interface FcmRegistrationRepository {
    /** FID 등록 */
    suspend fun registerFid(fid: String): Result<Unit>
    /** FID 삭제 */
    suspend fun deleteFid(): Result<Unit>
}
