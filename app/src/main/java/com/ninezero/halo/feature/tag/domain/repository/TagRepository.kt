package com.ninezero.halo.feature.tag.domain.repository

import com.ninezero.halo.feature.tag.domain.model.Tag

interface TagRepository {
    /** 크리에이터 태그 목록 조회 */
    suspend fun getCreatorTags(creatorId: Int, targetType: String): Result<List<Tag>>

    /** 태그 생성 */
    suspend fun createTag(name: String, targetType: String, isSectionEnabled: Boolean): Result<Tag>

    /** 태그 수정 */
    suspend fun updateTag(tagId: Int, name: String?, isSectionEnabled: Boolean?): Result<Tag>

    /** 태그 삭제 */
    suspend fun deleteTag(tagId: Int): Result<Unit>
}
