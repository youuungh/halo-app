package com.ninezero.halo.feature.tag.domain.usecase

import com.ninezero.halo.feature.tag.domain.model.Tag
import com.ninezero.halo.feature.tag.domain.repository.TagRepository
import javax.inject.Inject

class UpdateTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(
        tagId: Int,
        name: String? = null,
        isSectionEnabled: Boolean? = null
    ): Result<Tag> {
        return repository.updateTag(tagId, name, isSectionEnabled)
    }
}
