package com.ninezero.halo.feature.notification.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferenceDto(
    val socialEnabled: Boolean = true,
    val creatorActivityEnabled: Boolean = true,
    val commerceEnabled: Boolean = true,
    val chatEnabled: Boolean = true,
    val systemEnabled: Boolean = true
)

@Serializable
data class UpdateNotificationPreferenceRequest(
    val socialEnabled: Boolean? = null,
    val creatorActivityEnabled: Boolean? = null,
    val commerceEnabled: Boolean? = null,
    val chatEnabled: Boolean? = null,
    val systemEnabled: Boolean? = null
)
