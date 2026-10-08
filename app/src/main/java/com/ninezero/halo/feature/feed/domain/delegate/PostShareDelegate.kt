package com.ninezero.halo.feature.feed.domain.delegate

import com.ninezero.halo.feature.feed.domain.usecase.IncrementPostShareCountUseCase
import javax.inject.Inject

/** 포스트 공유 완료 공통 처리 */
class PostShareDelegate @Inject constructor(
    private val incrementPostShareCountUseCase: IncrementPostShareCountUseCase
) {
    data class ShareResult(
        val message: String,
        val actionLabel: String?,
        val navigateRoomId: Int?
    )

    fun complete(postId: Int?, sharedRoomIds: List<Int>, recipientCount: Int): ShareResult {  // postId 있으면 카운트 증가
        if (postId != null) {
            incrementPostShareCountUseCase(postId)
        }
        return if (recipientCount == 1) {
            ShareResult(
                message = "메시지를 보냈습니다",
                actionLabel = "채팅 열기",
                navigateRoomId = sharedRoomIds.first()
            )
        } else {
            ShareResult(
                message = "${recipientCount}명에게 메시지를 보냈습니다",
                actionLabel = null,
                navigateRoomId = null
            )
        }
    }
}
