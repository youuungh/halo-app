package com.ninezero.halo.feature.notification.data.remote.api

import com.ninezero.halo.core.network.ApiResponse
import com.ninezero.halo.feature.notification.data.remote.dto.MarkReadByTargetRequest
import com.ninezero.halo.feature.notification.data.remote.dto.NotificationListResponseDto
import com.ninezero.halo.feature.notification.data.remote.dto.NotificationPreferenceDto
import com.ninezero.halo.feature.notification.data.remote.dto.UnreadCountResponseDto
import com.ninezero.halo.feature.notification.data.remote.dto.UpdateNotificationPreferenceRequest
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {
    /** 알림 목록 조회 */
    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("isRead") isRead: Boolean? = null,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<NotificationListResponseDto>>

    /** 안읽은 알림 수 조회 */
    @GET("api/v1/notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<UnreadCountResponseDto>>

    /** 알림 읽음 처리 */
    @PUT("api/v1/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: Int
    ): Response<ApiResponse<Map<String, String>>>

    /** 대상 기준 알림 읽음 처리 */
    @PUT("api/v1/notifications/read-by-target")
    suspend fun markAsReadByTarget(
        @Body request: MarkReadByTargetRequest
    ): Response<ApiResponse<Map<String, String>>>

    /** 알림 삭제 */
    @DELETE("api/v1/notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: Int
    ): Response<Unit>

    /** 알림 설정 조회 */
    @GET("api/v1/notifications/preferences")
    suspend fun getPreferences(): Response<ApiResponse<NotificationPreferenceDto>>

    /** 알림 설정 수정 */
    @PUT("api/v1/notifications/preferences")
    suspend fun updatePreferences(
        @Body request: UpdateNotificationPreferenceRequest
    ): Response<ApiResponse<NotificationPreferenceDto>>
}
