package com.ninezero.halo.feature.store.data.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.feature.store.data.remote.api.OrderApi
import com.ninezero.halo.feature.store.data.remote.dto.CreateDirectOrderRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.CreateOrderRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.UpdateOrderStatusRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.UpdateShippingRequestDto
import com.ninezero.halo.feature.store.data.remote.dto.toDomain
import com.ninezero.halo.feature.store.domain.event.StoreEventBus
import com.ninezero.halo.feature.store.domain.model.CreateDirectOrderRequest
import com.ninezero.halo.feature.store.domain.model.CreateOrderRequest
import com.ninezero.halo.feature.store.domain.model.CreatorMonthlySales
import com.ninezero.halo.feature.store.domain.model.CreatorSalesSummary
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse
import com.ninezero.halo.feature.store.domain.repository.OrderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApi,
    private val storeEventBus: StoreEventBus
) : OrderRepository {

    override suspend fun createOrder(request: CreateOrderRequest): Result<Order> {
        return safeApiCall {
            api.createOrder(
                CreateOrderRequestDto(
                    shippingAddress = request.shippingAddress,
                    shippingPhone = request.shippingPhone,
                    shippingName = request.shippingName,
                    paymentProvider = request.paymentProvider,
                    memo = request.memo,
                    pointsToUse = request.pointsToUse,
                    couponCodes = request.couponCodes,
                    cartItemIds = request.cartItemIds
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun createDirectOrder(request: CreateDirectOrderRequest): Result<Order> {
        return safeApiCall {
            api.createDirectOrder(
                CreateDirectOrderRequestDto(
                    productId = request.productId,
                    quantity = request.quantity,
                    shippingAddress = request.shippingAddress,
                    shippingPhone = request.shippingPhone,
                    shippingName = request.shippingName,
                    paymentProvider = request.paymentProvider,
                    memo = request.memo,
                    pointsToUse = request.pointsToUse,
                    couponCodes = request.couponCodes
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun getMyOrders(cursor: String?, limit: Int): Result<CursorPage<Order>> {
        return safeApiCall { api.getMyOrders(cursor, limit) }.map { response ->
            CursorPage(response.items.map { it.toDomain() }, response.nextCursor)
        }
    }

    override suspend fun getOrder(orderId: Int): Result<Order> {
        return safeApiCall { api.getOrder(orderId) }.map { it.toDomain() }
    }

    override suspend fun cancelOrder(orderId: Int, reason: String?): Result<Order> {
        return safeApiCall { api.cancelOrder(orderId, reason) }.map { it.toDomain() }
            .onSuccess { storeEventBus.emitOrderUpdated(it) }
    }

    override suspend fun cancelOrderGroup(orderId: Int, creatorId: Int, reason: String?): Result<Order> {
        return safeApiCall { api.cancelOrderGroup(orderId, creatorId, reason) }.map { it.toDomain() }
            .onSuccess { storeEventBus.emitOrderUpdated(it) }
    }

    override suspend fun getShippingStatus(orderId: Int, creatorId: Int?): Result<ShippingStatusResponse> {
        return safeApiCall { api.getShippingStatus(orderId, creatorId) }.map { it.toDomain() }
    }

    override suspend fun getCreatorOrders(cursor: String?, limit: Int, filter: String?): Result<CursorPage<Order>> {
        return safeApiCall { api.getCreatorOrders(cursor, limit, filter) }.map { response ->
            CursorPage(response.items.map { it.toDomain() }, response.nextCursor)
        }
    }

    override suspend fun getCreatorSalesSummary(): Result<CreatorSalesSummary> {
        return safeApiCall { api.getCreatorSalesSummary() }.map { it.toDomain() }
    }

    override suspend fun getCreatorMonthlySales(month: String?): Result<CreatorMonthlySales> {
        return safeApiCall { api.getCreatorMonthlySales(month) }.map { it.toDomain() }
    }

    override suspend fun updateOrderStatus(orderId: Int, status: String): Result<Order> {
        return safeApiCall {
            api.updateOrderStatus(orderId, UpdateOrderStatusRequestDto(status))
        }.map { it.toDomain() }
    }

    override suspend fun updateShippingInfo(orderId: Int, trackingNumber: String, carrier: String): Result<Order> {
        return safeApiCall {
            api.updateShippingInfo(orderId, UpdateShippingRequestDto(trackingNumber, carrier))
        }.map { it.toDomain() }
    }
}
