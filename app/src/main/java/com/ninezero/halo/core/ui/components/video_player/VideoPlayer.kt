package com.ninezero.halo.core.ui.components.video_player

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.util.toVideoDuration
import kotlinx.coroutines.delay

/** 인라인 비디오 플레이어 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    thumbnailUrl: String?,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showMuteButton: Boolean = true,
    fixedHeight: Dp = 200.dp,
    maxWidth: Dp? = null,
    preferredAspectRatio: Float? = null,
    minAspectRatio: Float = 4f / 5f,
    maxAspectRatio: Float = 16f / 9f,
    durationMs: Long? = null
) {
    val isPreview = LocalInspectionMode.current

    // 프리뷰 모드에서는 썸네일만 표시
    if (isPreview) {
        VideoPlayerPreviewContent(
            thumbnailUrl = thumbnailUrl,
            onClick = onClick,
            showMuteButton = showMuteButton,
            fixedHeight = fixedHeight,
            maxWidth = maxWidth,
            durationMs = durationMs,
            modifier = modifier
        )
        return
    }

    var aspectRatio by remember(preferredAspectRatio) { mutableFloatStateOf(preferredAspectRatio ?: 1f) }

    // 비율에 따른 너비, 최소·최대 적용
    val clampedRatio = aspectRatio.coerceIn(
        minimumValue = minOf(minAspectRatio, maxAspectRatio),
        maximumValue = maxOf(minAspectRatio, maxAspectRatio)
    )
    val calculatedWidth = fixedHeight * clampedRatio

    // maxWidth 초과 시 비율 유지하며 축소
    val (targetWidth, targetHeight) = if (maxWidth != null && calculatedWidth > maxWidth) {
        val scaledHeight = maxWidth / clampedRatio
        maxWidth to scaledHeight
    } else {
        calculatedWidth to fixedHeight
    }

    Box(
        modifier = modifier
            .width(targetWidth)
            .height(targetHeight)
            // 정지/로딩 자리는 검정 배경
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // 재생 중일 때만 ExoPlayer, 아니면 썸네일
        if (isPlaying) {
            VideoSurface(
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                onAspectRatioChange = { aspectRatio = it }
            )
        } else if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "동영상 썸네일",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (durationMs != null && durationMs > 0L) {
            VideoDurationBadge(
                videoUrl = videoUrl,
                isPlaying = isPlaying,
                durationMs = durationMs,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }

        if (showMuteButton) {
            // 음소거 토글
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { VideoMuteController.toggle() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (VideoMuteController.muted) R.drawable.ic_speaker_x_mark
                        else R.drawable.ic_speaker_wave
                    ),
                    contentDescription = if (VideoMuteController.muted) "음소거 해제" else "음소거",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoDurationBadge(
    videoUrl: String,
    isPlaying: Boolean,
    durationMs: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var text by remember(durationMs) { mutableStateOf(durationMs.toVideoDuration()) }

    LaunchedEffect(isPlaying, videoUrl, durationMs) {
        if (!isPlaying) {
            text = durationMs.toVideoDuration()
            return@LaunchedEffect
        }
        val player = VideoPlayerHolder.obtain(context)
        while (true) {
            // 전환 중엔 전체 길이, READY 후 카운트다운
            text = if (VideoPlayerHolder.isCurrent(videoUrl) && player.playbackState == Player.STATE_READY) {
                val total = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: durationMs
                (total - player.currentPosition).coerceAtLeast(0L).toVideoDuration()
            } else {
                durationMs.toVideoDuration()
            }
            delay(250L)
        }
    }

    Box(
        modifier = modifier
            .padding(8.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/** 렌더 표면 */
@OptIn(UnstableApi::class)
@Composable
private fun VideoSurface(  // isPlaying일 때만 그림
    videoUrl: String,
    thumbnailUrl: String?,
    onAspectRatioChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val owner = remember { Any() }
    // 공유 ExoPlayer 재사용
    val player = remember { VideoPlayerHolder.obtain(context) }
    var showVideo by remember(videoUrl) {
        mutableStateOf(VideoPlayerHolder.isCurrent(videoUrl) && player.playbackState == Player.STATE_READY)
    }

    val playerView = remember {
        (LayoutInflater.from(context).inflate(R.layout.view_texture_player, null) as PlayerView).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)  // 준비 전 검은 셔터 → 투명
            // 렌더 전용, 터치 미소비
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // 공유 플레이어로 재생, 같은 url이면 이어서
    LaunchedEffect(videoUrl) {
        VideoPlayerHolder.play(context, owner, videoUrl, VideoMuteController.muted)
    }
    LaunchedEffect(VideoMuteController.muted) {
        VideoPlayerHolder.setMuted(owner, VideoMuteController.muted)
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            // 이 영상으로 READY일 때만 노출
            private fun revealIfReady() {
                if (VideoPlayerHolder.isCurrent(videoUrl) && player.playbackState == Player.STATE_READY) {
                    showVideo = true
                }
            }

            override fun onRenderedFirstFrame() = revealIfReady()

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) revealIfReady()
            }

            // 비율 반영 READY일 때만
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (VideoPlayerHolder.isCurrent(videoUrl) &&
                    player.playbackState == Player.STATE_READY &&
                    videoSize.width > 0 && videoSize.height > 0
                ) {
                    onAspectRatioChange(videoSize.width.toFloat() / videoSize.height.toFloat())
                }
            }
        }
        player.addListener(listener)
        VideoPlayerHolder.attach(playerView)

        onDispose {
            player.removeListener(listener)
            VideoPlayerHolder.detach(playerView)
            // 다른 주체 없으면 일시정지
            VideoPlayerHolder.release(owner)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 썸네일을 항상 뒤에 깔아둠
        if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = "동영상 썸네일",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        AndroidView(
            factory = { playerView },
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (showVideo) 1f else 0f)
        )
    }
}

@Composable
private fun VideoPlayerPreviewContent(
    thumbnailUrl: String?,
    onClick: () -> Unit,
    fixedHeight: Dp,
    modifier: Modifier = Modifier,
    showMuteButton: Boolean = true,
    maxWidth: Dp? = null,
    durationMs: Long? = null
) {
    val placeholderColor = Color.DarkGray
    // 프리뷰는 16:9, 화면 폭을 넘지 않게
    val previewRatio = 16f / 9f
    val calculatedWidth = fixedHeight * previewRatio
    // maxWidth 없으면 fixedHeight 기준
    val effectiveMaxWidth = maxWidth ?: (fixedHeight * 1.5f)

    // maxWidth 초과 시 비율 유지하며 축소
    val (previewWidth, previewHeight) = if (calculatedWidth > effectiveMaxWidth) {
        val scaledHeight = effectiveMaxWidth / previewRatio
        effectiveMaxWidth to scaledHeight
    } else {
        calculatedWidth to fixedHeight
    }

    Box(
        modifier = modifier
            .width(previewWidth)
            .height(previewHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // 썸네일 또는 placeholder
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = "동영상 썸네일",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(placeholderColor),
            error = ColorPainter(placeholderColor)
        )

        // 동영상 길이
        if (durationMs != null && durationMs > 0L) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = durationMs.toVideoDuration(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        if (showMuteButton) {
            // 음소거 토글
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { VideoMuteController.toggle() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        if (VideoMuteController.muted) R.drawable.ic_speaker_x_mark
                        else R.drawable.ic_speaker_wave
                    ),
                    contentDescription = if (VideoMuteController.muted) "음소거 해제" else "음소거",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
