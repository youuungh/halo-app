package com.ninezero.halo.feature.feed.domain.model

data class BlockRelation(
    val isBlockedByMe: Boolean,  // 차단 해제 버튼 표시
    val isBlockedByOther: Boolean  // 안내 문구 표시
)
