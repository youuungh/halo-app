package com.ninezero.halo.core.ui.components.modifiers

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/** 미디어 morph용 SharedTransitionScope */
val LocalMediaSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }  // null이면 morph 없음

/** 뷰어에 떠 있는 미디어 키 */
val LocalActiveViewerMediaKey = compositionLocalOf<String?> { null }  // 닫혀 있으면 null

/** 미디어 morph 키 */
fun mediaSharedKey(url: String, ownerKey: String? = null): String =  // 같은 URL 중복 렌더 시 ownerKey로 구분
    if (ownerKey == null) "media-viewer:$url" else "media-viewer:$ownerKey:$url"

/** 미디어 썸네일 shared element */
@Composable
fun Modifier.mediaSharedElement(  // scope null이면 그대로, 활성 키면 자리 숨김
    scope: SharedTransitionScope?,
    activeKey: String?,
    url: String,
    ownerKey: String? = null
): Modifier {
    if (scope == null) return this
    val key = mediaSharedKey(url, ownerKey)
    return with(scope) {
        this@mediaSharedElement.sharedElementWithCallerManagedVisibility(
            sharedContentState = rememberSharedContentState(key = key),
            visible = activeKey != key
        )
    }
}
