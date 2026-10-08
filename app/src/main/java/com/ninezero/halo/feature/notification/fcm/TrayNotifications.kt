package com.ninezero.halo.feature.notification.fcm

import android.app.NotificationManager
import android.content.Context
import com.ninezero.halo.feature.notification.domain.model.Notification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrayNotifications @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val manager: NotificationManager
        get() = context.getSystemService(NotificationManager::class.java)

    /** 알림의 트레이 알림 제거 */
    fun cancel(notification: Notification) {
        manager.cancel(
            FcmNotificationHelper.trayIdOf(
                type = notification.type,
                targetId = notification.targetId,
                payloadId = notification.id
            )
        )
        // 마지막 알림을 지웠으면 묶음 요약도 함께 정리
        FcmNotificationHelper.pruneOrphanSummaries(context)
    }

    /** 채팅방 트레이 알림 제거 */
    fun cancelChatRoom(roomId: Int) {
        manager.cancel(FcmNotificationHelper.chatTrayId(roomId))
    }
}
