package com.ninezero.halo.feature.store.domain.event

import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.Product
import com.ninezero.halo.feature.store.domain.model.Review
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<StoreEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val events: SharedFlow<StoreEvent> = _events.asSharedFlow()

    suspend fun emitOrderUpdated(order: Order) {
        _events.emit(StoreEvent.OrderUpdated(order))
    }

    suspend fun emitReviewCreated(review: Review) {
        _events.emit(StoreEvent.ReviewCreated(review))
    }

    suspend fun emitReviewUpdated(review: Review) {
        _events.emit(StoreEvent.ReviewUpdated(review))
    }

    suspend fun emitReviewDeleted(reviewId: Int) {
        _events.emit(StoreEvent.ReviewDeleted(reviewId))
    }

    suspend fun emitProductUpdated(product: Product) {
        _events.emit(StoreEvent.ProductUpdated(product))
    }

    suspend fun emitWishlistRemoved(productId: Int) {
        _events.emit(StoreEvent.WishlistRemoved(productId))
    }
}
