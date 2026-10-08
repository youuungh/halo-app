package com.ninezero.halo.feature.tag.domain.usecase

import com.ninezero.halo.feature.tag.domain.model.Tag
import com.ninezero.halo.feature.tag.domain.repository.TagRepository
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(
        name: String,
        targetType: String,
        isSectionEnabled: Boolean = false
    ): Result<Tag> {
        return repository.createTag(name, targetType, isSectionEnabled)
    }
}
