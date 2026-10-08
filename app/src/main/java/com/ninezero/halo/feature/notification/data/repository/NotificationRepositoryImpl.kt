package com.ninezero.halo.feature.notification.data.repository

import com.ninezero.halo.core.network.safeApiCall
import com.ninezero.halo.core.network.safeApiCallNoData
import com.ninezero.halo.core.network.safeApiCallUnit
import com.ninezero.halo.feature.notification.data.remote.api.NotificationApi
import com.ninezero.halo.feature.notification.data.remote.dto.MarkReadByTargetRequest
import com.ninezero.halo.feature.notification.data.remote.dto.UpdateNotificationPreferenceRequest
import com.ninezero.halo.feature.notification.data.remote.dto.toDomain
import com.ninezero.halo.feature.notification.domain.model.NotificationListResult
import com.ninezero.halo.feature.notification.domain.model.NotificationPreference
import com.ninezero.halo.feature.notification.domain.model.UpdateNotificationPreference
import com.ninezero.halo.feature.notification.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi
) : NotificationRepository {

    // 스낵바 소멸 시 커밋이라 화면을 나가도 끝까지 진행
    private val commitScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun getNotifications(cursor: String?, limit: Int): Result<NotificationListResult> {
        return safeApiCall {
            api.getNotifications(cursor = cursor, limit = limit)
        }.map { response ->
            NotificationListResult(
                notifications = response.notifications.items.map { it.toDomain() },
                unreadCount = response.unreadCount,
                nextCursor = response.notifications.nextCursor,
            )
        }
    }

    override suspend fun getUnreadCount(): Result<Int> {
        return safeApiCall {
            api.getUnreadCount()
        }.map { it.count }
    }

    override suspend fun markAsRead(notificationId: Int): Result<Unit> {
        return safeApiCallNoData {
            api.markAsRead(notificationId)
        }
    }

    override suspend fun markAsReadByTarget(type: String, targetId: Int): Result<Unit> {
        return safeApiCallNoData {
            api.markAsReadByTarget(MarkReadByTargetRequest(type = type, targetId = targetId))
        }
    }

    override suspend fun deleteNotification(notificationId: Int): Result<Unit> = commitScope.async {
        safeApiCallUnit {
            api.deleteNotification(notificationId)
        }
    }.await()

    override suspend fun getPreferences(): Result<NotificationPreference> {
        return safeApiCall {
            api.getPreferences()
        }.map { dto ->
            NotificationPreference(
                socialEnabled = dto.socialEnabled,
                creatorActivityEnabled = dto.creatorActivityEnabled,
                commerceEnabled = dto.commerceEnabled,
                chatEnabled = dto.chatEnabled,
                systemEnabled = dto.systemEnabled
            )
        }
    }

    override suspend fun updatePreferences(request: UpdateNotificationPreference): Result<NotificationPreference> {
        return safeApiCall {
            api.updatePreferences(
                UpdateNotificationPreferenceRequest(
                    socialEnabled = request.socialEnabled,
                    creatorActivityEnabled = request.creatorActivityEnabled,
                    commerceEnabled = request.commerceEnabled,
                    chatEnabled = request.chatEnabled,
                    systemEnabled = request.systemEnabled
                )
            )
        }.map { dto ->
            NotificationPreference(
                socialEnabled = dto.socialEnabled,
                creatorActivityEnabled = dto.creatorActivityEnabled,
                commerceEnabled = dto.commerceEnabled,
                chatEnabled = dto.chatEnabled,
                systemEnabled = dto.systemEnabled
            )
        }
    }
}
