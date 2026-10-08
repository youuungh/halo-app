package com.ninezero.halo.feature.tag.domain.usecase

import com.ninezero.halo.feature.tag.domain.model.Tag
import com.ninezero.halo.feature.tag.domain.repository.TagRepository
import javax.inject.Inject

class GetCreatorTagsUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(creatorId: Int, targetType: String): Result<List<Tag>> {
        return repository.getCreatorTags(creatorId, targetType)
    }
}
