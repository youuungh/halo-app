package com.ninezero.halo.feature.feed.presentation

import com.ninezero.halo.core.network.ApiException

/** 포스트 조회 거절 사유 */
enum class UnavailablePostReason {
    NOT_FOUND,  // 삭제·블라인드·숨김 404
    BLOCKED     // 차단 403
}

/** 볼 수 없는 포스트면 그 사유, 그 외 실패는 null */
fun Throwable.toUnavailablePostReason(): UnavailablePostReason? =
    when ((this as? ApiException)?.code) {
        404 -> UnavailablePostReason.NOT_FOUND
        403 -> UnavailablePostReason.BLOCKED
        else -> null
    }
