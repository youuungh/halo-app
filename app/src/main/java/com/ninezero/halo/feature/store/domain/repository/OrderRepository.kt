package com.ninezero.halo.feature.store.domain.repository

import com.ninezero.halo.core.model.CursorPage
import com.ninezero.halo.feature.store.domain.model.CreateDirectOrderRequest
import com.ninezero.halo.feature.store.domain.model.CreateOrderRequest
import com.ninezero.halo.feature.store.domain.model.CreatorMonthlySales
import com.ninezero.halo.feature.store.domain.model.CreatorSalesSummary
import com.ninezero.halo.feature.store.domain.model.Order
import com.ninezero.halo.feature.store.domain.model.ShippingStatusResponse

interface OrderRepository {
    /** 주문 생성 */
    suspend fun createOrder(request: CreateOrderRequest): Result<Order>
    /** 바로구매 주문 생성 */
    suspend fun createDirectOrder(request: CreateDirectOrderRequest): Result<Order>
    /** 내 주문 목록 조회 */
    suspend fun getMyOrders(cursor: String?, limit: Int): Result<CursorPage<Order>>
    /** 주문 상세 조회 */
    suspend fun getOrder(orderId: Int): Result<Order>
    /** 주문 취소 */
    suspend fun cancelOrder(orderId: Int, reason: String? = null): Result<Order>
    /** 크리에이터 그룹 취소 */
    suspend fun cancelOrderGroup(orderId: Int, creatorId: Int, reason: String? = null): Result<Order>
    /** 배송 상태 조회 */
    suspend fun getShippingStatus(orderId: Int, creatorId: Int? = null): Result<ShippingStatusResponse>
    /** 판매자 주문 목록 조회 */
    suspend fun getCreatorOrders(cursor: String?, limit: Int, filter: String? = null): Result<CursorPage<Order>>
    /** 판매자 매출 요약 조회 */
    suspend fun getCreatorSalesSummary(): Result<CreatorSalesSummary>
    /** 판매자 월별 매출 통계 조회 */
    suspend fun getCreatorMonthlySales(month: String?): Result<CreatorMonthlySales>
    /** 주문 상태 변경 */
    suspend fun updateOrderStatus(orderId: Int, status: String): Result<Order>
    /** 배송 정보 등록 */
    suspend fun updateShippingInfo(orderId: Int, trackingNumber: String, carrier: String): Result<Order>
}
