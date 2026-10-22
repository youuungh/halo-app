package com.ninezero.halo.core.deeplink

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.ninezero.halo.core.ui.components.overlay.GlobalCenterToast

fun Context.copyLinkToClipboard(link: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("link", link))
    GlobalCenterToast.show("링크를 복사했습니다")
}

fun Context.copyCodeToClipboard(code: String) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
    GlobalCenterToast.show("쿠폰 코드를 복사했습니다")
}

fun Context.shareTextExternally(text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(sendIntent, null))
}
