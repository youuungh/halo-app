package com.ninezero.halo.feature.feed.data.worker

import com.ninezero.halo.feature.feed.domain.model.PostUploadEvent
import com.ninezero.halo.feature.feed.domain.repository.PostUploadEventRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostUploadResultManager @Inject constructor() : PostUploadEventRepository {

    private val _uploadResults = MutableSharedFlow<PostUploadEvent>(extraBufferCapacity = 10)
    override val uploadEvents: SharedFlow<PostUploadEvent> = _uploadResults.asSharedFlow()

    suspend fun emitResult(result: PostUploadEvent) {
        _uploadResults.emit(result)
    }
}
