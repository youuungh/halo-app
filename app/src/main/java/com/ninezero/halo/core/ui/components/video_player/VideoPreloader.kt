package com.ninezero.halo.core.ui.components.video_player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.CacheWriter
import java.util.Collections
import java.util.concurrent.Executors

/** 다음 영상 프리로더 */
@OptIn(UnstableApi::class)
object VideoPreloader {  // PREFETCH_BYTES만 받고 예측 바뀌면 취소
    private const val PREFETCH_BYTES = 2L * 1024 * 1024  // 2MB ~ 4Mbps 기준 수 초 분량

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "video-preloader").apply { isDaemon = true }
    }

    // 받아둔 url, front·full
    private val frontCached = Collections.synchronizedSet(HashSet<String>())
    private val fullyCached = Collections.synchronizedSet(HashSet<String>())

    // 배치 토큰, 최신 아니면 중단
    @Volatile
    private var generation = 0

    @Volatile
    private var activeWriter: CacheWriter? = null

    /** 영상 프리페치 */
    fun prefetch(context: Context, urls: List<String>, fullCount: Int = 2) {  // 앞 fullCount개는 전체, 나머지 앞부분만
        val targets = urls.filter { it.isNotBlank() }
        if (targets.isEmpty()) return

        val myGen = ++generation
        activeWriter?.cancel()  // 직전 배치 취소

        val factory = VideoCache.cacheDataSourceFactory(context.applicationContext)
        executor.execute {
            for ((index, url) in targets.withIndex()) {
                if (myGen != generation) return@execute  // 더 새로운 배치로 교체됨
                val full = index < fullCount
                // 충분히 받았으면 스킵
                if (full) {
                    if (url in fullyCached) continue
                } else {
                    if (url in frontCached || url in fullyCached) continue
                }
                val dataSpec = DataSpec.Builder()
                    .setUri(url)
                    .apply { if (!full) setLength(PREFETCH_BYTES) }  // full이면 length 미지정 = 끝까지
                    .build()
                val writer = CacheWriter(factory.createDataSource(), dataSpec, null, null)
                activeWriter = writer
                try {
                    writer.cache()
                    if (full) fullyCached.add(url) else frontCached.add(url)
                } catch (_: Exception) {
                    // 취소·네트워크 오류 무시
                }
            }
        }
    }
}
