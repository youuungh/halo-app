package com.ninezero.halo.feature.feed.data.worker

import com.ninezero.halo.feature.feed.domain.model.CommentUploadEvent
import com.ninezero.halo.feature.feed.domain.repository.CommentUploadEventRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentUploadResultManager @Inject constructor() : CommentUploadEventRepository {

    private val _uploadEvents = MutableSharedFlow<CommentUploadEvent>(extraBufferCapacity = 10)
    override val uploadEvents: SharedFlow<CommentUploadEvent> = _uploadEvents.asSharedFlow()

    // 중첩 진입 있어 List
    private val activeCommentDetailIds = mutableListOf<Int>()

    override fun registerActiveCommentDetail(commentId: Int) {
        activeCommentDetailIds.add(commentId)
    }

    override fun unregisterActiveCommentDetail(commentId: Int) {
        activeCommentDetailIds.remove(commentId)
    }

    override fun isCommentDetailActive(commentId: Int): Boolean =
        commentId in activeCommentDetailIds

    suspend fun emitResult(result: CommentUploadEvent) {
        _uploadEvents.emit(result)
    }
}
