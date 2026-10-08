package com.ninezero.halo.feature.feed.domain.repository

import android.net.Uri
import com.ninezero.halo.feature.feed.domain.model.PostDraft
import kotlinx.coroutines.flow.Flow

interface PostDraftRepository {

    /** 임시저장 목록 관찰 */
    fun getAllDrafts(): Flow<List<PostDraft>>

    /** 임시저장 저장 */
    suspend fun saveDraft(
        content: String,
        mediaUris: List<Uri>,
        authorId: Int,
        authorUsername: String,
        authorDisplayName: String?,
        authorAvatarUrl: String?
    ): Long


    /** 임시저장 삭제 */
    suspend fun deleteDraft(id: Long)

    /** 임시저장 레코드만 삭제 */
    suspend fun deleteDraftRecordOnly(id: Long)  // 사본 파일은 유지
}
