
package com.ninezero.halo.core.ui.components.media_viewer

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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.ViewerBottomSheet
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.ImageThumbnailInfoStore
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.mediaSharedKey
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.video_player.VideoMuteController
import com.ninezero.halo.core.ui.components.video_player.VideoPlayerHolder
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

/** 뷰어 미디어 항목 */
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

data class MediaViewerState(
    val isVisible: Boolean = false,
    val items: List<ViewerMediaItem> = emptyList(),
    val initialIndex: Int = 0,
    val ownerKey: String? = null
)

/** 이미지·동영상 통합 풀스크린 뷰어 */
@Composable
fun MediaViewerOverlay(  // 안착 페이지만 공유 플레이어 점유
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
    var duration by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    // 길게 눌러 일시정지
    var isHolding by remember { mutableStateOf(false) }

    val currentItem = items.getOrNull(pagerState.currentPage)
    val currentIsVideo = currentItem?.type == ViewerMediaType.VIDEO
    val settledItem = items.getOrNull(pagerState.settledPage)

    // 영상 페이지 첫 도달 시 음소거 해제
    // 닫힐 때 직전 상태로 복원 → 피드로 돌아가면 다시 음소거
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
        // 재생/일시정지 아이콘 동기화
        DisposableEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
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
            duration = 0L
            while (true) {
                if (!isSeeking) {
                    currentPosition = exoPlayer.currentPosition
                    // duration 유효값일 때만 갱신
                    val d = exoPlayer.duration
                    if (d > 0) duration = d
                }
                delay(100)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 미디어 페이저
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // 탭 컨트롤 토글은 이미지 페이지만
                            if (items.getOrNull(pagerState.currentPage)?.type != ViewerMediaType.VIDEO) {
                                controlsVisible = !controlsVisible
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
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

            if (item.type == ViewerMediaType.VIDEO && exoPlayer != null) {
                VideoPage(
                    item = item,
                    isActive = pagerState.settledPage == page,
                    exoPlayer = exoPlayer,
                    // 탭 = 일시정지/재생 토글
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
                var effectiveUrl by remember(url) { mutableStateOf(url) }

                // 열었던 페이지만 morph
                val pageSharedModifier = if (
                    sharedTransitionScope != null &&
                    animatedVisibilityScope != null &&
                    page == pagerState.currentPage &&
                    !isDragging &&
                    !hasLeftInitialPage
                ) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = mediaSharedKey(url, ownerKey)),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier

                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // 비율 알면 레터박스 박스
                    // 비율 미상이면 fillMaxSize
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
                            .data(effectiveUrl)
                            // 썸네일을 placeholder로
                            .placeholderMemoryCacheKey(thumbInfo?.cacheKey)
                            .build(),
                        contentDescription = "이미지 ${page + 1}",
                        modifier = sizeModifier.then(pageSharedModifier),
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
                                    // 원본이 캐시에도 네트워크에도 없으면 피드 썸네일로 폴백
                                    val thumb = thumbInfo?.thumbnailUrl
                                    if (thumb != null && effectiveUrl != thumb) effectiveUrl = thumb
                                }
                                else -> {}
                            }
                        }
                    )
                }
            }
        }

        // 상단 크롬
        AnimatedVisibility(
            visible = !isDragging && !isHolding && (currentIsVideo || controlsVisible),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopStart)
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
                        .haloBlur(state = hazeState) {
                            style = HaloBlurStyle(
                                backgroundColor = Color.Black,
                                tint = HaloBlurTint(Color.White.copy(alpha = 0.1f)),
                                blurRadius = 16.dp,
                                noiseFactor = 0f
                            )
                        }
                        .clickable { onDismiss() },
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
                if (items.size > 1) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .haloBlur(state = hazeState) {
                                style = HaloBlurStyle(
                                    backgroundColor = Color.Black,
                                    tint = HaloBlurTint(Color.White.copy(alpha = 0.1f)),
                                    blurRadius = 16.dp,
                                    noiseFactor = 0f
                                )
                            }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${items.size}",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 더보기, 영상은 채팅만
                if (!currentIsVideo || showVideoMoreButton) {
                    Box(
                        modifier = Modifier
                            .bounceClick()
                            .size(36.dp)
                            .clip(CircleShape)
                            .haloBlur(state = hazeState) {
                                style = HaloBlurStyle(
                                    backgroundColor = Color.Black,
                                    tint = HaloBlurTint(Color.White.copy(alpha = 0.1f)),
                                    blurRadius = 16.dp,
                                    noiseFactor = 0f
                                )
                            }
                            .clickable { showBottomSheet = true },
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
                        .clip(RoundedCornerShape(24.dp))
                        .haloBlur(state = hazeState) {
                            style = HaloBlurStyle(
                                backgroundColor = Color.Black,
                                tint = HaloBlurTint(Color.White.copy(alpha = 0.1f)),
                                blurRadius = 16.dp,
                                noiseFactor = 0f
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 재생/일시정지 버튼
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
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
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 현재 시간
                    Text(
                        text = currentPosition.toVideoDuration(),
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    // 진행 막대
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                        onValueChange = { value ->
                            isSeeking = true
                            currentPosition = (value * duration).toLong()
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(currentPosition)
                            isSeeking = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    // 총 시간
                    Text(
                        text = duration.toVideoDuration(),
                        color = Color.White,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

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
                            modifier = Modifier.size(24.dp)
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

/** 동영상 페이지 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun VideoPage(  // 안착 시에만 공유 플레이어 점유
    item: ViewerMediaItem,
    isActive: Boolean,
    exoPlayer: ExoPlayer,
    onTogglePlay: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isActive) {
                if (!isActive) return@pointerInput
                detectTapGestures(
                    onTap = { onTogglePlay() },
                    // 길게 누르면 일시정지, 떼면 재생
                    onLongPress = { onHoldStart() },
                    onPress = {
                        tryAwaitRelease()
                        onHoldEnd()
                    }
                )
            }
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

            // 음소거 동기화, 현재 주체만
            LaunchedEffect(VideoMuteController.muted) {
                VideoPlayerHolder.setMuted(owner, VideoMuteController.muted)
            }

            // 첫 프레임 감지 + 표면 부착
            DisposableEffect(Unit) {
                val listener = object : Player.Listener {
                    override fun onRenderedFirstFrame() {
                        // 새 영상의 첫 프레임이 표면에 그려지면 포스터 제거
                        showPoster = false
                    }
                }
                exoPlayer.addListener(listener)
                VideoPlayerHolder.attach(playerView)

                onDispose {
                    exoPlayer.removeListener(listener)
                    VideoPlayerHolder.detach(playerView)
                    // 다른 주체 없으면 일시정지
                    VideoPlayerHolder.release(owner)
                }
            }

            // TextureView 백엔드 PlayerView
            AndroidView(
                factory = { playerView },
                modifier = Modifier.fillMaxSize()
            )

            if (showPoster) {
                VideoPoster(item.thumbnailUrl)
            }
        } else {
            VideoPoster(item.thumbnailUrl)
        }
    }
}

/** 동영상 포스터 */
@Composable
private fun VideoPoster(thumbnailUrl: String?) {  // 검정 바탕으로 잔상 방지
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


