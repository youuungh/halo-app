package com.ninezero.halo.core.deeplink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ninezero.halo.core.ui.components.overlay.GlobalCenterToast

fun Context.copyLinkToClipboard(link: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("link", link))
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) GlobalCenterToast.show("링크를 복사했습니다")
}

fun Context.copyCodeToClipboard(code: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) GlobalCenterToast.show("쿠폰 코드를 복사했습니다")
}

fun Context.copyTextToClipboard(text: String, message: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("text", text))
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) GlobalCenterToast.show(message)
}

fun Context.shareTextExternally(text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(sendIntent, null))
}
