package com.ninezero.halo.feature.address.data.remote.dto

import com.ninezero.halo.feature.address.domain.model.Address
import kotlinx.serialization.Serializable

@Serializable
data class AddressResponseDto(
    val id: Int,
    val recipientName: String,
    val recipientPhone: String,
    val zipCode: String,
    val address: String,
    val addressDetail: String? = null,
    val memo: String? = null,
    val isDefault: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateAddressRequestDto(
    val recipientName: String,
    val recipientPhone: String,
    val zipCode: String,
    val address: String,
    val addressDetail: String? = null,
    val memo: String? = null,
    val isDefault: Boolean = false
)

@Serializable
data class UpdateAddressRequestDto(
    val recipientName: String,
    val recipientPhone: String,
    val zipCode: String,
    val address: String,
    val addressDetail: String? = null,
    val memo: String? = null,
    val isDefault: Boolean = false
)

fun AddressResponseDto.toDomain() = Address(
    id = id,
    recipientName = recipientName,
    recipientPhone = recipientPhone,
    zipCode = zipCode,
    address = address,
    addressDetail = addressDetail,
    memo = memo,
    isDefault = isDefault
)
