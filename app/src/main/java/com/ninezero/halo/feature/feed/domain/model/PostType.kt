package com.ninezero.halo.feature.feed.domain.model

enum class PostType {
    TEXT,
    MEDIA,  // 클라이언트 임시 분류, 서버 전송 안 함
    IMAGE,
    VIDEO,
    PRODUCT
}
