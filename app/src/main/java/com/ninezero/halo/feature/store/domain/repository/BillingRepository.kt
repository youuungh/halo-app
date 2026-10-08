package com.ninezero.halo.feature.store.domain.repository

import com.ninezero.halo.feature.store.domain.model.BillingCard

interface BillingRepository {
    /** customerKey 조회/발급 */
    suspend fun getCustomerKey(): Result<String>

    /** 빌링키 발급 */
    suspend fun issueBillingKey(authKey: String): Result<BillingCard>

    /** 등록 빌링 카드 조회 */
    suspend fun getMyBillingKey(): Result<BillingCard>

    /** 등록 빌링 카드 삭제 */
    suspend fun deleteBillingKey(): Result<Unit>

    /** 빌링키로 주문 결제 */
    suspend fun payByBilling(orderId: Int): Result<Unit>
}
