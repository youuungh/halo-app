package com.ninezero.halo.core.ui.components.video_player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.PlayerView

/** 공유 ExoPlayer 홀더 */
@OptIn(UnstableApi::class)
object VideoPlayerHolder {  // owner 토큰으로 재생 주체 추적
    private var player: ExoPlayer? = null
    private var currentUrl: String? = null
    private var owner: Any? = null
    private var attachedView: PlayerView? = null
    private var pausedForBackground = false

    /** 공유 플레이어 */
    fun obtain(context: Context): ExoPlayer {
        return player ?: ExoPlayer.Builder(context.applicationContext)
            .setMediaSourceFactory(VideoCache.mediaSourceFactory(context))
            .setLoadControl(seekFriendlyLoadControl())
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setSeekParameters(SeekParameters.EXACT)
            }
            .also { player = it }
    }

    /** seek 친화 LoadControl */
    private fun seekFriendlyLoadControl(): LoadControl =  // 버퍼 요구를 낮춰 seek 후 빠른 재개
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                1_000,              // 기본 2500, seek 후 1초치면 재개
                2_000       // 기본 5000
            )
            .setBackBuffer(10_000, true)  // 직전 10초 유지 → 뒤로 seek 즉시 이어짐
            .build()

    /** 재생 요청 */
    fun play(context: Context, requester: Any, url: String, muted: Boolean) {  // 같은 url이면 이어 재생
        val p = obtain(context)
        owner = requester
        if (currentUrl != url) {
            p.setMediaItem(MediaItem.fromUri(url))
            p.prepare()
            currentUrl = url
        }
        p.volume = if (muted) 0f else 1f
        p.playWhenReady = true
    }

    /** 음소거 갱신 */
    fun setMuted(requester: Any, muted: Boolean) {  // 현재 주체일 때만
        if (owner === requester) player?.volume = if (muted) 0f else 1f
    }

    /** url 로드 중 여부 */
    fun isCurrent(url: String): Boolean = currentUrl == url

    /** 재생 주체 해제 */
    fun release(requester: Any) {  // 다른 주체가 없을 때만 일시정지
        if (owner === requester) {
            player?.playWhenReady = false
            owner = null
        }
    }

    /** 백그라운드 일시정지 */
    fun pauseForBackground() {  // owner·url·위치 보존
        val p = player ?: return
        if (p.playWhenReady) {
            pausedForBackground = true
            p.playWhenReady = false
        }
    }

    /** 포그라운드 복귀 재개 */
    fun resumeFromBackground() {  // 백그라운드로 멈춘 경우만
        val p = player ?: return
        if (pausedForBackground) {
            pausedForBackground = false
            if (owner != null) p.playWhenReady = true
        }
    }

    /** 뷰 부착 */
    fun attach(view: PlayerView) {  // 이전 뷰 자동 분리
        if (attachedView !== view) {
            attachedView?.player = null
            view.player = player
            attachedView = view
        }
    }

    /** 뷰 분리 */
    fun detach(view: PlayerView) {  // 현재 부착 뷰일 때만
        if (attachedView === view) {
            view.player = null
            attachedView = null
        }
    }
}
