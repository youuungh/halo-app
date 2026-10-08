package com.ninezero.halo.feature.chat.data.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import com.ninezero.halo.R
import kotlin.math.abs

object ChatTransferNotifications {

    // 로그아웃 정리용 채널 id
    private const val CHANNEL_ID = "halo_media_transfer"
    // 서버·트레이 알림 id와 안 겹치는 음수 대역
    private const val UPLOAD_ID_BASE = -5_000_001
    private const val DOWNLOAD_ID_BASE = -5_001_001
    private const val COMPLETE_ID_BASE = -5_002_001

    fun uploadNotificationId(pendingMessageId: Long): Int =
        UPLOAD_ID_BASE - (pendingMessageId % 1_000).toInt()

    fun downloadNotificationId(fileUrl: String): Int =
        DOWNLOAD_ID_BASE - abs(fileUrl.hashCode()) % 1_000

    private fun completeNotificationId(fileUrl: String): Int =
        COMPLETE_ID_BASE - abs(fileUrl.hashCode()) % 1_000

    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "파일 전송/다운로드",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "채팅 미디어 전송 및 파일 다운로드 진행률" }
        )
    }

    /** FGS 승격용 초기 진행률 알림 */
    fun foregroundInfo(
        context: Context,
        notificationId: Int,
        title: String,
        text: String? = null
    ): ForegroundInfo {
        ensureChannel(context)
        return ForegroundInfo(
            notificationId,
            progressNotification(context, title, text, percent = -1),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    fun notifyProgress(
        context: Context,
        notificationId: Int,
        title: String,
        text: String?,
        percent: Int
    ) {
        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, progressNotification(context, title, text, percent))
    }

    private fun progressNotification(
        context: Context,
        title: String,
        text: String?,
        percent: Int
    ): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .apply { text?.let { setContentText(it) } }
            .setProgress(100, percent.coerceAtLeast(0), percent < 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    fun notifyDownloadComplete(
        context: Context,
        fileUrl: String,
        fileName: String,
        contentUri: Uri?,
        mimeType: String?
    ) {
        ensureChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("다운로드 완료")
            .setContentText(fileName)
            .setAutoCancel(true)
        if (contentUri != null) {
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            builder.setContentIntent(
                PendingIntent.getActivity(
                    context,
                    completeNotificationId(fileUrl),
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        context.getSystemService(NotificationManager::class.java)
            .notify(completeNotificationId(fileUrl), builder.build())
    }

    fun notifyDownloadFailed(context: Context, fileUrl: String, fileName: String) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("다운로드 실패")
            .setContentText(fileName)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(completeNotificationId(fileUrl), notification)
    }
}
