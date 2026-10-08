package com.ninezero.halo.core.deeplink

import android.net.Uri

object DeepLinks {
    const val SCHEME = "halo"

    const val WEB_HOST = "youuungh.github.io"  // WEB_HOST 변경 시 Manifest host 함께 변경
    const val WEB_BASE = "https://$WEB_HOST"

    const val PATH_POST = "post"
    const val PATH_PRODUCT = "product"
    const val PATH_PROFILE = "profile"

    fun postLink(postId: Int) = "$WEB_BASE/$PATH_POST/$postId"
    fun productLink(productId: Int) = "$WEB_BASE/$PATH_PRODUCT/$productId"
    fun profileLink(userId: Int) = "$WEB_BASE/$PATH_PROFILE/$userId"

    fun isContentUri(uri: Uri): Boolean =
        uri.scheme == SCHEME || (uri.scheme == "https" && uri.host == WEB_HOST)
}
