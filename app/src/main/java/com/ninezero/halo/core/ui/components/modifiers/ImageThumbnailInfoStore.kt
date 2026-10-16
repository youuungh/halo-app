package com.ninezero.halo.core.ui.components.modifiers

import coil3.compose.AsyncImagePainter
import coil3.memory.MemoryCache

/** 목록 썸네일 캐시 키·비율 저장소 */
object ImageThumbnailInfoStore {  // 뷰어 placeholder와 비율 박스용
    data class Info(
        val cacheKey: MemoryCache.Key?,
        val aspectRatio: Float?,
        val thumbnailUrl: String? = null
    )

    private val map = mutableMapOf<String, Info>()

    fun put(
        url: String,
        cacheKey: MemoryCache.Key?,
        aspectRatio: Float?,
        thumbnailUrl: String? = null
    ) {
        map[url] = Info(cacheKey, aspectRatio, thumbnailUrl)
    }

    fun get(url: String): Info? = map[url]

    /** AsyncImage onSuccess에서 캐시 키·비율 저장 */
    fun onSuccessCapture(url: String): (AsyncImagePainter.State.Success) -> Unit = { state ->
        val image = state.result.image
        // 로드 모델도 보관, 오프라인 폴백용
        val loadedThumbnailUrl = (state.result.request.data as? String)?.takeIf { it != url }
        put(
            url = url,
            cacheKey = state.result.memoryCacheKey,
            aspectRatio = if (image.height > 0) image.width.toFloat() / image.height.toFloat() else null,
            thumbnailUrl = loadedThumbnailUrl
        )
    }
}
