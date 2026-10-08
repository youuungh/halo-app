package com.ninezero.halo.feature.tag.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.tag.data.remote.api.TagApi
import com.ninezero.halo.feature.tag.data.remote.dto.TagRequest
import com.ninezero.halo.feature.tag.data.remote.dto.UpdateTagRequest
import com.ninezero.halo.feature.tag.data.remote.dto.toDomain
import com.ninezero.halo.feature.tag.domain.model.Tag
import com.ninezero.halo.feature.tag.domain.repository.TagRepository
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val api: TagApi
) : TagRepository {

    override suspend fun getCreatorTags(creatorId: Int, targetType: String): Result<List<Tag>> {
        return safeApiCall {
            api.getCreatorTags(creatorId, targetType)
        }.map { response -> response.tags.map { it.toDomain() } }
    }

    override suspend fun createTag(
        name: String,
        targetType: String,
        isSectionEnabled: Boolean
    ): Result<Tag> {
        return safeApiCall {
            api.createTag(TagRequest(name, targetType, isSectionEnabled))
        }.map { it.toDomain() }
    }

    override suspend fun updateTag(
        tagId: Int,
        name: String?,
        isSectionEnabled: Boolean?
    ): Result<Tag> {
        return safeApiCall {
            api.updateTag(tagId, UpdateTagRequest(name, isSectionEnabled))
        }.map { it.toDomain() }
    }

    override suspend fun deleteTag(tagId: Int): Result<Unit> {
        return safeApiCallUnit {
            api.deleteTag(tagId)
        }
    }
}
