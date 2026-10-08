package com.ninezero.halo.feature.feed.domain.repository

import com.ninezero.halo.feature.feed.domain.model.PostUploadEvent
import kotlinx.coroutines.flow.SharedFlow

/** Worker의 업로드 결과를 ViewModel로 전달 */
interface PostUploadEventRepository {
    val uploadEvents: SharedFlow<PostUploadEvent>
}
