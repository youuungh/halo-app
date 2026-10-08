package com.ninezero.halo.feature.store.domain.event

import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.Review

/** 하위 화면에서 바뀐 항목을 받아 둔 목록에 반영 */
sealed class StoreEvent {
    data class OrderUpdated(val order: Order) : StoreEvent()
    data class ReviewCreated(val review: Review) : StoreEvent()
    data class ReviewUpdated(val review: Review) : StoreEvent()
    data class ReviewDeleted(val reviewId: Int) : StoreEvent()
    data class ProductUpdated(val product: Product) : StoreEvent()
    data class WishlistRemoved(val productId: Int) : StoreEvent()
}
