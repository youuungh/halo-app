package com.ninezero.halo.feature.address.domain.model

data class Address(
    val id: Int,
    val recipientName: String,
    val recipientPhone: String,
    val zipCode: String,
    val address: String,
    val addressDetail: String?,
    val memo: String?,
    val isDefault: Boolean = false
)
