package com.ninezero.halo.feature.store.domain.model

/** 상품 대표 이미지 한 장 */
sealed interface ProductImageRef {  // 저장된 주소 또는 새 초안
    data class Existing(val url: String) : ProductImageRef
    data class Draft(val draftId: Int) : ProductImageRef
}
