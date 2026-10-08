package com.ninezero.halo.feature.notification.domain.model

data class NotificationPreference(
    val socialEnabled: Boolean = true,
    val creatorActivityEnabled: Boolean = true,
    val commerceEnabled: Boolean = true,
    val chatEnabled: Boolean = true,
    val systemEnabled: Boolean = true
)

data class UpdateNotificationPreference(
    val socialEnabled: Boolean? = null,
    val creatorActivityEnabled: Boolean? = null,
    val commerceEnabled: Boolean? = null,
    val chatEnabled: Boolean? = null,
    val systemEnabled: Boolean? = null
)
