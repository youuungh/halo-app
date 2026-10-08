package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.feature.feed.domain.model.MediaDraftTargetType

/** 글·댓글에 붙이기 전에 서버가 가공해 두는 미디어 초안 */
interface MediaDraftRepository {
    /** 선택 순서대로 만든 초안 ID 목록 */
    suspend fun createDrafts(targetType: MediaDraftTargetType, uris: List<String>): Result<List<Int>>  // 하나라도 실패하면 실패
}
