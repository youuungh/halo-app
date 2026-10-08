package com.ninezero.halo.feature.notification.domain.repository

import com.ninezero.halo.feature.notification.domain.model.NotificationListResult
import com.ninezero.halo.feature.notification.domain.model.NotificationPreference
import com.ninezero.halo.feature.notification.domain.model.UpdateNotificationPreference

interface NotificationRepository {
    /** 알림 목록 조회 */
    suspend fun getNotifications(cursor: String?, limit: Int): Result<NotificationListResult>
    /** 안읽은 알림 수 조회 */
    suspend fun getUnreadCount(): Result<Int>
    /** 알림 읽음 처리 */
    suspend fun markAsRead(notificationId: Int): Result<Unit>
    /** 대상 기준 알림 읽음 처리 */
    suspend fun markAsReadByTarget(type: String, targetId: Int): Result<Unit>
    /** 알림 삭제 */
    suspend fun deleteNotification(notificationId: Int): Result<Unit>
    /** 알림 설정 조회 */
    suspend fun getPreferences(): Result<NotificationPreference>
    /** 알림 설정 수정 */
    suspend fun updatePreferences(request: UpdateNotificationPreference): Result<NotificationPreference>
}
