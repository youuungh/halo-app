package com.ninezero.halo.feature.address.domain.repository

import com.ninezero.halo.feature.address.domain.model.Address

interface AddressRepository {
    /** 배송지 목록 조회 */
    suspend fun getAddresses(): Result<List<Address>>
    /** 배송지 조회 */
    suspend fun getAddress(id: Int): Result<Address>
    /** 배송지 추가 */
    suspend fun createAddress(
        recipientName: String,
        recipientPhone: String,
        zipCode: String,
        address: String,
        addressDetail: String?,
        memo: String?,
        isDefault: Boolean = false
    ): Result<Address>
    /** 배송지 수정 */
    suspend fun updateAddress(
        id: Int,
        recipientName: String,
        recipientPhone: String,
        zipCode: String,
        address: String,
        addressDetail: String?,
        memo: String?,
        isDefault: Boolean = false
    ): Result<Address>
    /** 배송지 삭제 */
    suspend fun deleteAddress(id: Int): Result<Unit>
}
