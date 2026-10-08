package com.ninezero.halo.feature.address.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.address.data.remote.api.AddressApi
import com.ninezero.halo.feature.address.data.remote.dto.CreateAddressRequestDto
import com.ninezero.halo.feature.address.data.remote.dto.UpdateAddressRequestDto
import com.ninezero.halo.feature.address.data.remote.dto.toDomain
import com.ninezero.halo.feature.address.domain.model.Address
import com.ninezero.halo.feature.address.domain.repository.AddressRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepositoryImpl @Inject constructor(
    private val api: AddressApi
) : AddressRepository {

    override suspend fun getAddresses(): Result<List<Address>> {
        return safeApiCall { api.getAddresses() }
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getAddress(id: Int): Result<Address> {
        return safeApiCall { api.getAddress(id) }
            .map { it.toDomain() }
    }

    override suspend fun createAddress(
        recipientName: String,
        recipientPhone: String,
        zipCode: String,
        address: String,
        addressDetail: String?,
        memo: String?,
        isDefault: Boolean
    ): Result<Address> {
        return safeApiCall {
            api.createAddress(
                CreateAddressRequestDto(
                    recipientName = recipientName,
                    recipientPhone = recipientPhone,
                    zipCode = zipCode,
                    address = address,
                    addressDetail = addressDetail,
                    memo = memo,
                    isDefault = isDefault
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun updateAddress(
        id: Int,
        recipientName: String,
        recipientPhone: String,
        zipCode: String,
        address: String,
        addressDetail: String?,
        memo: String?,
        isDefault: Boolean
    ): Result<Address> {
        return safeApiCall {
            api.updateAddress(
                id,
                UpdateAddressRequestDto(
                    recipientName = recipientName,
                    recipientPhone = recipientPhone,
                    zipCode = zipCode,
                    address = address,
                    addressDetail = addressDetail,
                    memo = memo,
                    isDefault = isDefault
                )
            )
        }.map { it.toDomain() }
    }

    override suspend fun deleteAddress(id: Int): Result<Unit> {
        return safeApiCallUnit { api.deleteAddress(id) }
    }
}
