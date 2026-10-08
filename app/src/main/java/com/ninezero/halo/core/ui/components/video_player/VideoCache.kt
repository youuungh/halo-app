package com.ninezero.halo.core.ui.components.video_player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import java.io.File

/** 비디오 디스크 캐시 */
@OptIn(UnstableApi::class)
object VideoCache {  // 인라인·뷰어 공유
    private const val MAX_BYTES = 200L * 1024 * 1024  // 200MB

    @Volatile
    private var cache: SimpleCache? = null

    private fun cache(context: Context): SimpleCache {
        return cache ?: synchronized(this) {
            cache ?: SimpleCache(
                File(context.applicationContext.cacheDir, "video_cache"),
                LeastRecentlyUsedCacheEvictor(MAX_BYTES),
                StandaloneDatabaseProvider(context.applicationContext)
            ).also { cache = it }
        }
    }

    /** 캐시 DataSource 팩토리 */
    fun cacheDataSourceFactory(context: Context): CacheDataSource.Factory =
        CacheDataSource.Factory()
            .setCache(cache(context))
            .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context.applicationContext))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    fun mediaSourceFactory(context: Context): MediaSource.Factory =
        DefaultMediaSourceFactory(cacheDataSourceFactory(context))
}
