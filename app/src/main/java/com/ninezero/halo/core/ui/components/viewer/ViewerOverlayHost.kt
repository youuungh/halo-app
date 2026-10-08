package com.ninezero.halo.core.ui.components.viewer

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.ninezero.halo.core.ui.components.media_viewer.MediaViewerOverlay
import com.ninezero.halo.core.ui.components.media_viewer.MediaViewerState
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaType
import com.ninezero.halo.core.ui.components.modifiers.mediaSharedKey

class ViewerOverlayHostState {
    var mediaViewerState by mutableStateOf(MediaViewerState())
        internal set

    /** 뷰어 현재 페이지 */
    var mediaViewerCurrentIndex by mutableIntStateOf(0)
        internal set

    /** 뷰어 표시 중 미디어 키 */
    val activeMediaKey: String?  // 닫혀 있으면 null
        get() = if (mediaViewerState.isVisible) {
            mediaViewerState.items.getOrNull(mediaViewerCurrentIndex)
                ?.let { mediaSharedKey(it.url, mediaViewerState.ownerKey) }
        } else null

    /** 영상 뷰어 표시 여부 */
    val suppressInlineVideo: Boolean
        get() = mediaViewerState.isVisible &&
            mediaViewerState.items.any { it.type == ViewerMediaType.VIDEO }

    /** 미디어 뷰어 열기 */
    fun showMediaViewer(
        items: List<ViewerMediaItem>,
        initialIndex: Int = 0,
        ownerKey: String? = null
    ) {
        mediaViewerCurrentIndex = initialIndex
        mediaViewerState = MediaViewerState(
            isVisible = true,
            items = items,
            initialIndex = initialIndex,
            ownerKey = ownerKey
        )
    }

    /** 이미지 URL 목록 편의 오버로드 */
    fun showImageViewer(imageUrls: List<String>, initialIndex: Int = 0, ownerKey: String? = null) {
        showMediaViewer(imageUrls.map { ViewerMediaItem(url = it) }, initialIndex, ownerKey)
    }

    internal fun updateMediaViewerIndex(index: Int) {
        mediaViewerCurrentIndex = index
    }

    fun dismissMediaViewer() {
        // isVisible만 끔, 리셋하면 exit 중 빈 페이저
        mediaViewerState = mediaViewerState.copy(isVisible = false)
    }
}

/** 뷰어 오버레이 호스트 상태 */
@Composable
fun rememberViewerOverlayHostState(): ViewerOverlayHostState {
    return remember { ViewerOverlayHostState() }
}

/** 상위 뷰어 호스트 공유 CompositionLocal */
val LocalViewerOverlayHostState = staticCompositionLocalOf<ViewerOverlayHostState?> { null }  // null이면 화면 자체 호스트

/** 미디어 뷰어 오버레이 호스트 */
@Composable
fun ViewerOverlayHost(
    state: ViewerOverlayHostState,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    MediaViewerOverlay(
        state = state.mediaViewerState,
        onDismiss = { state.dismissMediaViewer() },
        sharedTransitionScope = sharedTransitionScope,
        onPageChanged = { state.updateMediaViewerIndex(it) }
    )
}
