package com.ninezero.halo.core.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import timber.log.Timber
import java.io.File

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.launchCustomTab(link: String) {
    try {
        val uri = if (link.startsWith("http://") || link.startsWith("https://")) {
            link.toUri()
        } else {
            "https://$link".toUri()
        }
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(this, uri)
    } catch (e: Exception) {
        Timber.e(e, "커스텀 탭 실행 실패: $link")
    }
}

fun Context.createCameraImageUri(): Uri {
    val cacheDir = File(cacheDir, "camera_photos").apply {
        if (!exists()) mkdirs()
    }
    val imageFile = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
}
