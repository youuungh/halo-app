package com.ninezero.halo.feature.auth.domain.model

data class Device(
    val id: String,
    val deviceName: String,
    val os: String,
    val ipAddress: String,
    val location: String?,
    val lastActiveAt: Long,
    val isCurrent: Boolean,
)
