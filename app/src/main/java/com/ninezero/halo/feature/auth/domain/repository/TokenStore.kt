package com.ninezero.halo.feature.auth.domain.repository

import com.ninezero.halo.feature.auth.domain.model.AuthToken

interface TokenStore {
    /** 로그인 결과 저장 */
    suspend fun saveLoginResult(token: AuthToken, userId: Int)
    /** 저장된 사용자 id 조회 */
    suspend fun getUserId(): Int?
    /** 토큰 삭제 */
    suspend fun clearToken()
    /** 사용자 id 삭제 */
    suspend fun clearUserId()
}
