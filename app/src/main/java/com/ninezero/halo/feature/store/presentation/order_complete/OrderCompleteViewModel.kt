package com.ninezero.halo.feature.store.presentation.order_complete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ninezero.halo.feature.store.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer
import javax.inject.Inject

@HiltViewModel
class OrderCompleteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel(), OrbitContainerHost<OrderCompleteState, OrderCompleteState, Nothing> {

    private val orderId: Int = savedStateHandle["orderId"] ?: 0
    override val container: OrbitContainer<OrderCompleteState, OrderCompleteState, Nothing> =
        orbitContainer(OrderCompleteState())

    init {
        loadOrder()
    }

    // 요약 카드용, 실패는 알리지 않음
    private fun loadOrder() = intent {
        orderRepository.getOrder(orderId).onSuccess { order ->
            reduce { state.copy(order = order) }
        }
    }
}
