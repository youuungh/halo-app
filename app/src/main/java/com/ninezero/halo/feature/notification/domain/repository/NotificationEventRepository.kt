package com.ninezero.halo.feature.notification.domain.repository

import com.ninezero.halo.feature.notification.domain.model.NotificationEvent
import kotlinx.coroutines.flow.Flow

interface NotificationEventRepository {
    val events: Flow<NotificationEvent>
}
