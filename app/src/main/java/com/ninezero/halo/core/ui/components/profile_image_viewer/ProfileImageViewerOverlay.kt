package com.ninezero.halo.core.ui.components.profile_image_viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class ProfileImageViewerState(
    val isVisible: Boolean = false,
    val imageUrl: String? = null
)

/** 프로필 이미지 풀스크린 오버레이 */
@Composable
fun ProfileImageViewerOverlay(
    state: ProfileImageViewerState,
    onDismiss: () -> Unit,
    hazeState: HazeState,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200))
    ) {
        // AnimatedVisibilityScope, morph 동기화
        ProfileImageViewerContent(
            imageUrl = state.imageUrl,
            onDismiss = onDismiss,
            hazeState = hazeState,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = this
        )
    }
}

@Composable
private fun ProfileImageViewerContent(
    imageUrl: String?,
    onDismiss: () -> Unit,
    hazeState: HazeState,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val coroutineScope = rememberCoroutineScope()

    // 드래그 위치 - Animatable로 spring 복귀 가능
    val offsetAnimatable = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    // 드래그 dismiss 플래그
    var dismissingByDrag by remember { mutableStateOf(false) }

    LaunchedEffect(dismissingByDrag) {
        if (dismissingByDrag) onDismiss()
    }

    // dismiss 거리 임계값 px
    val dismissThreshold = 350f

    // 중앙 복귀
    val springBack: () -> Unit = {
        coroutineScope.launch {
            offsetAnimatable.animateTo(
                targetValue = Offset.Zero,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = Color.Black,
                        tint = HazeTint(Color.Transparent),
                        blurRadius = 24.dp,
                        noiseFactor = 0f
                    )
                }
        )

        // 프로필 이미지, 이미지에서만 드래그
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // dismiss 중엔 fade
            val sharedModifier = if (
                sharedTransitionScope != null &&
                animatedVisibilityScope != null &&
                !dismissingByDrag
            ) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = PROFILE_IMAGE_SHARED_KEY),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            } else Modifier

            // 아바타와 캐시 공유
            val context = LocalContext.current
            val imageRequest = remember(imageUrl, context) {
                imageUrl?.let {
                    ImageRequest.Builder(context)
                        .data(it)
                        .memoryCacheKey(it)
                        .placeholderMemoryCacheKey(it)
                        .size(Size.ORIGINAL)
                        // 투명 PNG 아바타는 crossfade 끔
                        .crossfade(false)
                        .build()
                }
            }

            AsyncImage(
                model = imageRequest,
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(280.dp)
                    .offset {
                        IntOffset(
                            offsetAnimatable.value.x.roundToInt(),
                            offsetAnimatable.value.y.roundToInt()
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    offsetAnimatable.snapTo(offsetAnimatable.value + dragAmount)
                                }
                            },
                            onDragEnd = {
                                if (offsetAnimatable.value.getDistance() > dismissThreshold) {
                                    dismissingByDrag = true
                                } else {
                                    springBack()
                                }
                            },
                            onDragCancel = { springBack() }
                        )
                    }
                    .then(sharedModifier)
                    .clip(CircleShape)
                    // 투명 PNG일 때 뒷배경이 비쳐서 어색해 보이는 것 방지
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_profile_placeholder),
                error = painterResource(R.drawable.ic_profile_placeholder),
                fallback = painterResource(R.drawable.ic_profile_placeholder)
            )
        }

        // 좌측 상단 닫기 버튼
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .bounceClick()
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
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
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProfileImageViewerOverlayPreview() {
    ProfileImageViewerOverlay(
        state = ProfileImageViewerState(
            isVisible = true,
            imageUrl = "https://example.com/profile.jpg"
        ),
        onDismiss = {},
        hazeState = remember { HazeState() }
    )
}
