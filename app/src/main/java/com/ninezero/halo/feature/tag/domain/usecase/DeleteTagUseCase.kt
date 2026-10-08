package com.ninezero.halo.feature.tag.domain.usecase

import com.ninezero.halo.feature.tag.domain.repository.TagRepository
import javax.inject.Inject

class DeleteTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(tagId: Int): Result<Unit> {
        return repository.deleteTag(tagId)
    }
}
