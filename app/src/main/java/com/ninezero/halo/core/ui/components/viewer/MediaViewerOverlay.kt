
package com.ninezero.halo.core.ui.components.viewer

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.gif.AnimatedImageDecoder
import coil3.request.ImageRequest
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.ViewerBottomSheet
import com.ninezero.halo.core.ui.components.loading.LoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.media.ImageThumbnailInfoStore
import com.ninezero.halo.core.ui.components.media.VideoMuteController
import com.ninezero.halo.core.ui.components.media.VideoPlayerHolder
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.mediaSharedKey
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.util.toVideoDuration
import com.ninezero.halo.feature.feed.domain.model.MediaItem
import com.ninezero.halo.feature.feed.domain.model.MediaType
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.net.URL
import kotlin.math.abs

private const val DOUBLE_TAP_ZOOM = 2.5f
private const val MAX_ZOOM = 4f

private val ScrimStops = arrayOf(
    0f to Color.Black.copy(alpha = 0.65f),
    0.5f to Color.Black.copy(alpha = 0.3f),
    1f to Color.Transparent
)

data class ViewerMediaItem(
    val url: String,
    val thumbnailUrl: String? = null,  // 동영상 포스터, 이미지는 불필요
    val type: ViewerMediaType = ViewerMediaType.IMAGE
)

enum class ViewerMediaType {
    IMAGE,
    VIDEO
}

/** 도메인 미디어를 뷰어 항목으로 변환 */
private fun MediaItem.toViewerItem(): ViewerMediaItem = ViewerMediaItem(
    url = url,
    thumbnailUrl = thumbnailUrl,
    type = if (type == MediaType.VIDEO) ViewerMediaType.VIDEO else ViewerMediaType.IMAGE
)

/** 미디어 목록을 뷰어 항목으로 변환 */
fun List<MediaItem>.toViewerItems(): List<ViewerMediaItem> = map { it.toViewerItem() }

/** 미디어 뷰어 열림 · 항목 · 시작 위치 */
data class MediaViewerState(
    val isVisible: Boolean = false,
    val items: List<ViewerMediaItem> = emptyList(),
    val initialIndex: Int = 0,
    val ownerKey: String? = null
)

/** 이미지·동영상 통합 풀스크린 뷰어 */
@Composable
fun MediaViewerOverlay(
    state: MediaViewerState,
    onDismiss: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    onPageChanged: (Int) -> Unit = {},
    showVideoMoreButton: Boolean = false
) {
    val view = LocalView.current
    val context = LocalContext.current

    // Immersive: 시스템 바 숨김
    DisposableEffect(state.isVisible) {
        // 안 보일 때는 캡처·복원 모두 안 함
        if (!state.isVisible) {
            return@DisposableEffect onDispose {}
        }
        val window = (context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
            ?: return@DisposableEffect onDispose {}
        val prevLightStatus = insetsController.isAppearanceLightStatusBars
        val prevLightNav = insetsController.isAppearanceLightNavigationBars
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            insetsController.isAppearanceLightStatusBars = prevLightStatus
            insetsController.isAppearanceLightNavigationBars = prevLightNav
        }
    }

    // 이미지 스크림 80ms, 영상 200ms
    val initialIsVideo = state.items.getOrNull(state.initialIndex)?.type == ViewerMediaType.VIDEO
    val fadeDuration = if (initialIsVideo) 200 else 80

    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn(tween(fadeDuration)),
        exit = fadeOut(tween(fadeDuration))
    ) {
        // 열릴 때 등록해야 NavHost보다 먼저 받음
        BackHandler(enabled = state.isVisible) { onDismiss() }
        // AnimatedVisibilityScope, morph 동기화
        MediaViewerContent(
            items = state.items,
            initialIndex = state.initialIndex,
            onDismiss = onDismiss,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this,
            onPageChanged = onPageChanged,
            ownerKey = state.ownerKey,
            showVideoMoreButton = showVideoMoreButton
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun MediaViewerContent(
    items: List<ViewerMediaItem>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onPageChanged: (Int) -> Unit = {},
    ownerKey: String? = null,
    showVideoMoreButton: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { items.size }
    )

    // 열었던 이미지만 morph, 옮긴 페이지는 fade
    var hasLeftInitialPage by remember { mutableStateOf(false) }

    // 현재 페이지를 호스트에 보고
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
        if (pagerState.currentPage != initialIndex) hasLeftInitialPage = true
    }

    // morph 중 Crop, 끝나면 Fit
    val isMorphing = sharedTransitionScope?.isTransitionActive == true

    var showBottomSheet by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var toastVisible by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // 드래그 dismiss 상태
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val dismissThreshold = 200f

    // 동영상 재생 상태, 안착 페이지만
    val hasVideo = items.any { it.type == ViewerMediaType.VIDEO }
    val exoPlayer = if (hasVideo) remember { VideoPlayerHolder.obtain(context) } else null

    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var isHolding by remember { mutableStateOf(false) }  // 길게 눌러 일시정지
    var isBuffering by remember { mutableStateOf(false) }

    // 확대 중인 페이지, 넘기기·끌어 닫기 제한
    var zoomedPage by remember { mutableStateOf<Int?>(null) }
    val isZoomed = zoomedPage == pagerState.currentPage

    val currentItem = items.getOrNull(pagerState.currentPage)
    val currentIsVideo = currentItem?.type == ViewerMediaType.VIDEO
    val settledItem = items.getOrNull(pagerState.settledPage)

    // 영상 페이지 첫 도달 시 음소거 해제, 피드로 돌아가면 다시 음소거
    var mutedBeforeViewer by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(settledItem?.type) {
        if (settledItem?.type == ViewerMediaType.VIDEO && mutedBeforeViewer == null) {
            mutedBeforeViewer = VideoMuteController.muted
            VideoMuteController.updateMuted(false)
        }
    }
    DisposableEffect(Unit) {
        onDispose { mutedBeforeViewer?.let { VideoMuteController.updateMuted(it) } }
    }

    if (exoPlayer != null) {
        // 재생·일시정지 아이콘 동기화
        DisposableEffect(exoPlayer) {
            isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    isBuffering = playbackState == Player.STATE_BUFFERING
                }
            }
            exoPlayer.addListener(listener)
            onDispose { exoPlayer.removeListener(listener) }
        }

        // 재생 위치 폴링, 영상 페이지 안착 중만
        val settledVideoUrl = if (settledItem?.type == ViewerMediaType.VIDEO) settledItem.url else null
        LaunchedEffect(settledVideoUrl) {
            if (settledVideoUrl == null) return@LaunchedEffect
            currentPosition = 0L
            bufferedPosition = 0L
            duration = 0L
            while (true) {
                bufferedPosition = exoPlayer.bufferedPosition
                if (!isSeeking) {
                    currentPosition = exoPlayer.currentPosition
                    // duration 유효값일 때만 갱신
                    val playerDuration = exoPlayer.duration
                    if (playerDuration > 0) duration = playerDuration
                }
                delay(100)
            }
        }
    }

    // 영상 버퍼링 중일 때만 로딩 표시
    val waitingForVideo = isBuffering && settledItem?.type == ViewerMediaType.VIDEO &&
        pagerState.settledPage == pagerState.currentPage
    var showLoading by remember { mutableStateOf(false) }
    LaunchedEffect(waitingForVideo) {
        if (waitingForVideo) {
            delay(400)
            showLoading = true
        } else {
            showLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 블러 재료에 그림자 포함, 버튼이 튀지 않게
        Box(modifier = Modifier.fillMaxSize().hazeSource(hazeState)) {
            // 미디어 페이저
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isZoomed,
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                    .pointerInput(isZoomed) {
                        // 확대 중엔 끌어 닫기 대신 화면 안 이동
                        if (isZoomed) return@pointerInput
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                                controlsVisible = false
                            },
                            onDragEnd = {
                                isDragging = false
                                // 충분히 끌면 닫힘
                                if (abs(offsetX) > dismissThreshold || abs(offsetY) > dismissThreshold) {
                                    onDismiss()
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                    controlsVisible = true
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                offsetX = 0f
                                offsetY = 0f
                                controlsVisible = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        )
                    }
            ) { page ->
                val item = items[page]

                // 확대 배율
                val zoom = remember(item.url) { ZoomState() }
                LaunchedEffect(zoom.isZoomed) {
                    if (zoom.isZoomed) zoomedPage = page
                    else if (zoomedPage == page) zoomedPage = null
                }
                // 다른 페이지에 안착하면 원래 크기
                LaunchedEffect(pagerState.settledPage) {
                    if (pagerState.settledPage != page) zoom.reset()
                }

                if (item.type == ViewerMediaType.VIDEO && exoPlayer != null) {
                    VideoPage(
                        item = item,
                        isActive = pagerState.settledPage == page,
                        exoPlayer = exoPlayer,
                        zoom = zoom,
                        // 탭 = 일시정지·재생 토글
                        onTogglePlay = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        // 재생 중일 때만 hold-pause
                        onHoldStart = {
                            if (isPlaying) {
                                isHolding = true
                                exoPlayer.pause()
                            }
                        },
                        onHoldEnd = {
                            if (isHolding) {
                                isHolding = false
                                exoPlayer.play()
                            }
                        }
                    )
                } else {
                    val url = item.url
                    // 목록 썸네일이 남긴 캐시 키/비율
                    val thumbInfo = remember(url) { ImageThumbnailInfoStore.get(url) }
                    var imageRatio by remember(url) { mutableStateOf(thumbInfo?.aspectRatio) }
                    // 로드 URL, 실패 시 썸네일 폴백
                    var actualUrl by remember(url) { mutableStateOf(url) }

                    // 열었던 페이지만 morph, 확대 중엔 fade
                    val pageSharedModifier = if (
                        sharedTransitionScope != null &&
                        animatedVisibilityScope != null &&
                        page == pagerState.currentPage &&
                        !isDragging &&
                        !hasLeftInitialPage &&
                        !zoom.isZoomed
                    ) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = mediaSharedKey(url, ownerKey)),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    } else Modifier

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(zoom) {
                                detectTapGestures(
                                    onTap = { controlsVisible = !controlsVisible },
                                    onDoubleTap = { tap -> scope.launch { zoom.toggleAt(tap, size) } }
                                )
                            }
                            .pinchZoom(zoom),
                        contentAlignment = Alignment.Center
                    ) {
                        // 비율 알면 레터박스 박스, 미상이면 fillMaxSize
                        val ratio = imageRatio
                        val sizeModifier = if (ratio != null && ratio > 0f) {
                            if (ratio >= maxWidth.value / maxHeight.value) {
                                Modifier.fillMaxWidth().aspectRatio(ratio)
                            } else {
                                Modifier.fillMaxHeight().aspectRatio(ratio)
                            }
                        } else {
                            Modifier.fillMaxSize()
                        }

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(actualUrl)
                                .placeholderMemoryCacheKey(thumbInfo?.cacheKey)  // 썸네일을 placeholder로
                                .decoderFactory(AnimatedImageDecoder.Factory())
                                .build(),
                            contentDescription = "이미지 ${page + 1}",
                            modifier = sizeModifier
                                .then(pageSharedModifier)
                                .onSizeChanged { zoom.contentSize = it }
                                .zoomTransform(zoom),
                            contentScale = if (ratio != null) ContentScale.Crop
                            else if (isMorphing) ContentScale.Crop
                            else ContentScale.Fit,
                            onState = { state ->
                                when (state) {
                                    is AsyncImagePainter.State.Success -> {
                                        val image = state.result.image
                                        if (image.height > 0) imageRatio = image.width.toFloat() / image.height.toFloat()
                                    }
                                    is AsyncImagePainter.State.Error -> {
                                        // 원본이 캐시, 네트워크 둘다 없으면 피드 썸네일로 폴백
                                        val thumb = thumbInfo?.thumbnailUrl
                                        if (thumb != null && actualUrl != thumb) actualUrl = thumb
                                    }
                                    else -> {}
                                }
                            }
                        )
                    }
                }
            }

            // 이미지 위 그림자
            AnimatedVisibility(
                visible = !currentIsVideo && controlsVisible && !isDragging,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                val statusBarTop = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(statusBarTop + 200.dp)
                        .background(Brush.verticalGradient(*ScrimStops))
                )
            }

            // 영상 아래 그림자
            AnimatedVisibility(
                visible = currentIsVideo && !isHolding && !isDragging,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                val navBarBottom = WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(navBarBottom + 220.dp)
                        .background(Brush.verticalGradient(*ScrimStops, startY = Float.POSITIVE_INFINITY, endY = 0f))
                )
            }
        }

        // 로딩 인디케이터
        AnimatedVisibility(
            visible = showLoading && !isDragging,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            LoadingIndicator(size = LoadingIndicatorSize.Large, color = Color.White)
        }

        // 상단 크롬
        AnimatedVisibility(
            visible = !isDragging && !isHolding && (currentIsVideo || controlsVisible),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            ViewerTopBar(
                hazeState = hazeState,
                currentPage = pagerState.currentPage,
                pageCount = items.size,
                showMoreButton = !currentIsVideo || showVideoMoreButton,
                onClose = onDismiss,
                onMoreClick = { showBottomSheet = true }
            )
        }

        // 하단 재생 컨트롤, 영상만
        if (exoPlayer != null) {
            AnimatedVisibility(
                visible = currentIsVideo && !isHolding && !isDragging,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .haloBlur(hazeState, HaloBlurs.MediaControl)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 재생·일시정지 버튼
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                            ),
                            contentDescription = if (isPlaying) "일시정지" else "재생",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 현재 시간
                    Text(
                        text = currentPosition.toVideoDuration(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")  // 늘어나는 시간에 막대 폭이 흔들리지 않게
                    )

                    VideoSeekBar(
                        progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                        bufferedProgress = if (duration > 0) bufferedPosition.toFloat() / duration.toFloat() else 0f,
                        onSeek = { fraction ->
                            isSeeking = true
                            currentPosition = (fraction * duration).toLong()
                        },
                        onSeekFinished = {
                            exoPlayer.seekTo(currentPosition)
                            isSeeking = false
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // 총 시간
                    Text(
                        text = duration.toVideoDuration(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")
                    )

                    // 음소거 토글 버튼
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { VideoMuteController.toggle() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (VideoMuteController.muted) R.drawable.ic_speaker_x_mark_filled
                                else R.drawable.ic_speaker_wave_filled
                            ),
                            contentDescription = if (VideoMuteController.muted) "음소거 해제" else "음소거",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet && currentItem != null) {
        ViewerBottomSheet(
            mediaType = currentItem.type,
            onDismiss = { showBottomSheet = false },
            onSave = {
                scope.launch {
                    if (currentItem.type == ViewerMediaType.VIDEO) {
                        val result = saveVideoToGallery(context, currentItem.url)
                        toastMessage = if (result) "동영상이 저장되었습니다" else "동영상 저장에 실패했습니다"
                    } else {
                        val result = saveImageToGallery(context, currentItem.url)
                        toastMessage = if (result) "이미지가 저장되었습니다" else "이미지 저장에 실패했습니다"
                    }
                    toastVisible = true
                }
            }
        )
    }

    CenterToast(
        visible = toastVisible,
        message = toastMessage,
        onDismiss = { toastVisible = false }
    )
}

/** 확대한 그림이 화면 밖으로 안 빠지는 이동 범위 */
private fun clampZoomPan(pan: Offset, scale: Float, contentSize: IntSize, containerSize: IntSize): Offset {
    val maxX = ((contentSize.width * scale - containerSize.width) / 2f).coerceAtLeast(0f)
    val maxY = ((contentSize.height * scale - containerSize.height) / 2f).coerceAtLeast(0f)
    return Offset(pan.x.coerceIn(-maxX, maxX), pan.y.coerceIn(-maxY, maxY))
}

/** 확대 배율·이동, 배율 1이면 원래 크기 */
@Stable
private class ZoomState {
    var scale by mutableFloatStateOf(1f)
    var pan by mutableStateOf(Offset.Zero)

    /** 이동 범위를 자르는 기준, 화면 안 실제 그림 크기 */
    var contentSize by mutableStateOf(IntSize.Zero)

    val isZoomed: Boolean get() = scale > 1f

    fun reset() {
        scale = 1f
        pan = Offset.Zero
    }

    /** 두 번 탭: 그 자리로 확대, 확대 중이면 원래 크기 */
    suspend fun toggleAt(tap: Offset, containerSize: IntSize) {
        val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
        val zoomedNow = isZoomed
        val targetScale = if (zoomedNow) 1f else DOUBLE_TAP_ZOOM
        val targetPan = if (zoomedNow) Offset.Zero
        else clampZoomPan((tap - center) * (1f - DOUBLE_TAP_ZOOM), DOUBLE_TAP_ZOOM, contentSize, containerSize)
        val startScale = scale
        val startPan = pan
        animate(0f, 1f, animationSpec = tween(250)) { t, _ ->
            scale = lerp(startScale, targetScale, t)
            pan = lerp(startPan, targetPan, t)
        }
    }
}

/** 두 손가락·확대 중에만 받는 확대 제스처 */
private fun Modifier.pinchZoom(zoom: ZoomState): Modifier = pointerInput(zoom) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val pressedCount = event.changes.count { it.pressed }
            if (pressedCount >= 2 || zoom.isZoomed) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val newScale = (zoom.scale * event.calculateZoom()).coerceIn(1f, MAX_ZOOM)
                val centroid = event.calculateCentroid(useCurrent = false)
                val focus = if (centroid == Offset.Unspecified) Offset.Zero else centroid - center
                // 손가락 가운데를 기준으로 커지게
                val newPan = focus + (zoom.pan - focus) * (newScale / zoom.scale) + event.calculatePan()
                zoom.scale = newScale
                zoom.pan = clampZoomPan(newPan, newScale, zoom.contentSize, size)
                event.changes.forEach { if (it.positionChanged()) it.consume() }
            }
        } while (event.changes.any { it.pressed })
        if (zoom.scale < 1.01f) zoom.reset()
    }
}

/** 확대 배율·이동을 그림에 입힘 */
private fun Modifier.zoomTransform(zoom: ZoomState): Modifier = graphicsLayer {
    scaleX = zoom.scale
    scaleY = zoom.scale
    translationX = zoom.pan.x
    translationY = zoom.pan.y
}

/** 레터박스 안 영상 크기, 비율 미상이면 화면 */
private fun fitContentSize(ratio: Float, containerSize: IntSize): IntSize {
    if (ratio <= 0f || containerSize.height == 0) return containerSize
    val containerRatio = containerSize.width.toFloat() / containerSize.height
    return if (ratio > containerRatio) {
        IntSize(containerSize.width, (containerSize.width / ratio).toInt())
    } else {
        IntSize((containerSize.height * ratio).toInt(), containerSize.height)
    }
}

/** 영상 진행 막대, 받아 둔 부분 표시 */
@Composable
private fun VideoSeekBar(
    progress: Float,
    bufferedProgress: Float,
    onSeek: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isScrubbing by remember { mutableStateOf(false) }
    val thumbSize by animateDpAsState(if (isScrubbing) 18.dp else 14.dp, label = "seekThumb")

    Canvas(
        modifier = modifier
            .height(32.dp)
            .pointerInput(Unit) {
                // 누른 자리로 바로 이동, 끄는 동안 따라감
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    isScrubbing = true
                    onSeek((down.position.x / size.width).coerceIn(0f, 1f))
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            onSeek((change.position.x / size.width).coerceIn(0f, 1f))
                            change.consume()
                        }
                    } while (change.pressed)
                    isScrubbing = false
                    onSeekFinished()
                }
            }
    ) {
        val trackHeight = 4.dp.toPx()
        val radius = CornerRadius(trackHeight / 2f)
        val top = (size.height - trackHeight) / 2f
        val played = progress.coerceIn(0f, 1f)
        val buffered = bufferedProgress.coerceIn(played, 1f)

        drawRoundRect(Color.White.copy(alpha = 0.22f), Offset(0f, top), Size(size.width, trackHeight), radius)
        drawRoundRect(Color.White.copy(alpha = 0.4f), Offset(0f, top), Size(size.width * buffered, trackHeight), radius)
        drawRoundRect(Color.White, Offset(0f, top), Size(size.width * played, trackHeight), radius)

        // 재생 위치 핸들
        val thumbRadius = thumbSize.toPx() / 2f
        val thumbCenter = Offset((size.width * played).coerceIn(thumbRadius, size.width - thumbRadius), size.height / 2f)
        drawCircle(Color.Black.copy(alpha = 0.25f), thumbRadius + 1.dp.toPx(), thumbCenter + Offset(0f, 1.dp.toPx()))
        drawCircle(Color.White, thumbRadius, thumbCenter)
    }
}

/** 뷰어 상단: 닫기·페이지 번호·더보기 */
@Composable
private fun ViewerTopBar(
    hazeState: HazeState,
    currentPage: Int,
    pageCount: Int,
    showMoreButton: Boolean,  // 영상은 채팅에서만
    onClose: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 닫기 버튼
        Box(
            modifier = Modifier
                .bounceClick()
                .size(36.dp)
                .clip(CircleShape)
                .haloBlur(hazeState, HaloBlurs.MediaControl)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "닫기",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 페이지 인디케이터
        if (pageCount > 1) {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .haloBlur(hazeState, HaloBlurs.MediaControl)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${currentPage + 1}/$pageCount",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 더보기, 영상은 채팅만
        if (showMoreButton) {
            Box(
                modifier = Modifier
                    .bounceClick()
                    .size(36.dp)
                    .clip(CircleShape)
                    .haloBlur(hazeState, HaloBlurs.MediaControl)
                    .clickable { onMoreClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ellipsis_horizontal),
                    contentDescription = "더보기",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

/** 동영상 페이지 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun VideoPage(
    item: ViewerMediaItem,
    isActive: Boolean,
    exoPlayer: ExoPlayer,
    zoom: ZoomState,
    onTogglePlay: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit
) {
    val zoomScope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    // 이동 범위 = 레터박스 안 영상 크기
    var videoRatio by remember(item.url) { mutableFloatStateOf(0f) }
    LaunchedEffect(videoRatio, containerSize) {
        zoom.contentSize = fitContentSize(videoRatio, containerSize)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(isActive) {
                if (!isActive) return@pointerInput
                detectTapGestures(
                    onTap = { onTogglePlay() },
                    // 두 번 탭 확대
                    onDoubleTap = { tap -> zoomScope.launch { zoom.toggleAt(tap, size) } },
                    // 길게 누르면 일시정지, 떼면 재생
                    onLongPress = { onHoldStart() },
                    onPress = {
                        tryAwaitRelease()
                        onHoldEnd()
                    }
                )
            }
            .then(if (isActive) Modifier.pinchZoom(zoom) else Modifier)
    ) {
        if (isActive) {
            val context = LocalContext.current
            val owner = remember { Any() }

            // 새 영상만 포스터로 가림
            var showPoster by remember(item.url) { mutableStateOf(!VideoPlayerHolder.isCurrent(item.url)) }

            val playerView = remember {
                (LayoutInflater.from(context).inflate(R.layout.view_texture_player, null) as PlayerView).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }

            // 재생을 표면 부착보다 먼저
            DisposableEffect(item.url) {
                showPoster = !VideoPlayerHolder.isCurrent(item.url)
                VideoPlayerHolder.play(context, owner, item.url, muted = VideoMuteController.muted)
                onDispose {}
            }

            // 현재 주체 음소거 동기화
            LaunchedEffect(VideoMuteController.muted) {
                VideoPlayerHolder.setMuted(owner, VideoMuteController.muted)
            }

            // 첫 프레임 감지 + 표면 부착
            DisposableEffect(Unit) {
                fun updateRatio(videoSize: VideoSize) {
                    if (videoSize.width == 0 || videoSize.height == 0) return
                    videoRatio = videoSize.width * videoSize.pixelWidthHeightRatio / videoSize.height
                }
                val listener = object : Player.Listener {
                    override fun onRenderedFirstFrame() {
                        // 새 영상의 첫 프레임이 표면에 그려지면 포스터 제거
                        showPoster = false
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) = updateRatio(videoSize)
                }
                updateRatio(exoPlayer.videoSize)
                exoPlayer.addListener(listener)
                VideoPlayerHolder.attach(playerView)

                onDispose {
                    exoPlayer.removeListener(listener)
                    VideoPlayerHolder.detach(playerView)
                    // 다른 주체 없으면 일시정지
                    VideoPlayerHolder.release(owner)
                }
            }

            // TextureView라 확대 가능
            Box(modifier = Modifier.fillMaxSize().zoomTransform(zoom)) {
                AndroidView(
                    factory = { playerView },
                    modifier = Modifier.fillMaxSize()
                )

                if (showPoster) {
                    VideoPoster(item.thumbnailUrl)
                }
            }
        } else {
            VideoPoster(item.thumbnailUrl)
        }
    }
}

/** 동영상 포스터 */
@Composable
private fun VideoPoster(thumbnailUrl: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private suspend fun saveImageToGallery(context: Context, imageUrl: String): Boolean = saveUrlToGallery(
    context = context,
    url = imageUrl,
    collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    relativePath = Environment.DIRECTORY_PICTURES + "/Halo",
    fallbackExtension = "jpg",
    fallbackMime = "image/jpeg"
)

private suspend fun saveVideoToGallery(context: Context, videoUrl: String): Boolean = saveUrlToGallery(
    context = context,
    url = videoUrl,
    collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
    relativePath = Environment.DIRECTORY_MOVIES + "/Halo",
    fallbackExtension = "mp4",
    fallbackMime = "video/mp4"
)

/** URL 원본을 갤러리에 저장 */
private suspend fun saveUrlToGallery(
    context: Context,
    url: String,
    collection: Uri,
    relativePath: String,
    fallbackExtension: String,
    fallbackMime: String
): Boolean = withContext(Dispatchers.IO) {
    var insertedUri: Uri? = null  // 실패 시 갤러리에 남는 미완성 항목 삭제용
    try {
        val connection = URL(url).openConnection().apply {
            // 무한 대기 방지
            connectTimeout = 30_000
            readTimeout = 30_000
        }
        connection.connect()

        val mimeType = connection.contentType?.substringBefore(';')?.trim()?.takeIf { it.isNotEmpty() } ?: fallbackMime
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: fallbackExtension
        val fileName = "Halo_${System.currentTimeMillis()}.$extension"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = context.contentResolver.insert(collection, values) ?: return@withContext false
        insertedUri = uri
        context.contentResolver.openOutputStream(uri)?.use { output ->
            BufferedInputStream(connection.getInputStream()).use { input ->
                input.copyTo(output)
            }
        }

        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        context.contentResolver.update(uri, values, null, null)
        true
    } catch (e: CancellationException) {
        insertedUri?.let { context.contentResolver.delete(it, null, null) }
        throw e
    } catch (e: Exception) {
        insertedUri?.let { context.contentResolver.delete(it, null, null) }
        false
    }
}
