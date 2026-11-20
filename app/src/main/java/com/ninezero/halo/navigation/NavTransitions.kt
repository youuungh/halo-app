package com.ninezero.halo.navigation

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigationevent.NavigationEvent.Companion.EDGE_LEFT
import androidx.navigationevent.NavigationEvent.Companion.EDGE_RIGHT
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

private const val TRANSITION_DURATION = 200  // 모달 위아래
private const val SHARED_AXIS_DURATION = 250

// Shared Axis 순차 fade, 잔상 제거
private const val SHARED_AXIS_FADE_OUT_DURATION = 75
private const val SHARED_AXIS_FADE_IN_DURATION = 175
private const val SHARED_AXIS_FADE_IN_DELAY = 75

// 좌우 이동 전환
private const val PUSH_DURATION = 200
private const val PUSH_FADE_OUT_DURATION = 65
private const val PUSH_FADE_IN_DURATION = 135
private const val PUSH_FADE_IN_DELAY = 65
private const val PUSH_SLIDE_FRACTION = 0.08f  // 화면 폭 대비 밀림

// 모달 뒤 화면 축소 배율
private const val MODAL_BACKDROP_SCALE = 0.92f
private const val MODAL_BACKDROP_FADE_OUT_DURATION = 90  // 축소가 보이도록 일반 전환보다 길게
private const val MODAL_BACKDROP_FADE_IN_DURATION = 125  // 모달이 닫힐 때 다시 나타나는 시간

private val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)  // 빠른 가속, 완만한 감속

private const val PREDICTIVE_BACK_SCALE = 0.9f  // 끝까지 당겼을 때
private const val PREDICTIVE_BACK_SHIFT_FRACTION = 0.03f  // 화면 폭 대비 이동량
private const val PREDICTIVE_BACK_EXIT_DURATION = 150
private const val PREDICTIVE_BACK_DIM_ALPHA = 0.2f  // 뒤 화면을 덮는 어둠
private const val PREDICTIVE_BACK_DIM_RANGE = 0.15f  // 이만큼 당기면 어둠이 다 깔림
private const val PREDICTIVE_BACK_CANCEL_DURATION = 200
private const val PREDICTIVE_BACK_HOLD_DURATION = 450
private val PREDICTIVE_BACK_CORNER_RADIUS = 28.dp  // 기기 모서리 반경을 못 읽을 때

internal fun NavBackStackEntry.isModalDestination(): Boolean =
    destination.hasRoute<Screen.CreatePost>() ||
            destination.hasRoute<Screen.EditPost>() ||
            destination.hasRoute<Screen.CreateCommunityPost>() ||
            destination.hasRoute<Screen.Reply>() ||
            destination.hasRoute<Screen.CreateReview>() ||
            destination.hasRoute<Screen.CreateProduct>()


internal val pushExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (targetState.isModalDestination()) scaleBackOut() else slideOutLeft()
}

internal val pushPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
    {
        if (initialState.isModalDestination()) scaleBackIn() else slideInRight()
    }


internal fun slideInLeft() = slideInHorizontally(
    animationSpec = tween(PUSH_DURATION, easing = FastOutSlowInEasing),
    initialOffsetX = { (it * PUSH_SLIDE_FRACTION).toInt() }
) + pushFadeIn()

private fun slideOutLeft() = slideOutHorizontally(
    animationSpec = tween(PUSH_DURATION, easing = FastOutSlowInEasing),
    targetOffsetX = { -(it * PUSH_SLIDE_FRACTION).toInt() }
) + pushFadeOut()

private fun slideInRight() = slideInHorizontally(
    animationSpec = tween(PUSH_DURATION, easing = FastOutSlowInEasing),
    initialOffsetX = { -(it * PUSH_SLIDE_FRACTION).toInt() }
) + pushFadeIn()

internal fun slideOutRight() = slideOutHorizontally(
    animationSpec = tween(PUSH_DURATION, easing = FastOutSlowInEasing),
    targetOffsetX = { (it * PUSH_SLIDE_FRACTION).toInt() }
) + pushFadeOut()

private fun pushFadeIn() = fadeIn(
    animationSpec = tween(
        durationMillis = PUSH_FADE_IN_DURATION,
        delayMillis = PUSH_FADE_IN_DELAY,
        easing = LinearEasing
    )
)

private fun pushFadeOut() = fadeOut(animationSpec = tween(PUSH_FADE_OUT_DURATION, easing = LinearEasing))

internal fun AnimatedContentTransitionScope<*>.slideInUp() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = SHARED_AXIS_FADE_IN_DURATION,
            delayMillis = SHARED_AXIS_FADE_IN_DELAY,
            easing = LinearEasing
        )
    )

internal fun AnimatedContentTransitionScope<*>.slideOutUp() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(SHARED_AXIS_FADE_OUT_DURATION, easing = LinearEasing))

internal fun AnimatedContentTransitionScope<*>.slideInDown() =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = SHARED_AXIS_FADE_IN_DURATION,
            delayMillis = SHARED_AXIS_FADE_IN_DELAY,
            easing = LinearEasing
        )
    )

internal fun AnimatedContentTransitionScope<*>.slideOutDown() =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(SHARED_AXIS_FADE_OUT_DURATION, easing = LinearEasing))

internal fun predictiveBackHold(): ExitTransition = fadeOut(
    animationSpec = tween(PREDICTIVE_BACK_HOLD_DURATION, easing = LinearEasing),
    targetAlpha = 0.99f
)

// 모달 뒤 화면 축소
private fun scaleBackOut(): ExitTransition = scaleOut(
    targetScale = MODAL_BACKDROP_SCALE,
    animationSpec = tween(SHARED_AXIS_DURATION, easing = EmphasizedEasing)
) + fadeOut(animationSpec = tween(MODAL_BACKDROP_FADE_OUT_DURATION, easing = LinearEasing))

private fun scaleBackIn(): EnterTransition = scaleIn(
    initialScale = MODAL_BACKDROP_SCALE,
    animationSpec = tween(SHARED_AXIS_DURATION, easing = EmphasizedEasing)
) + fadeIn(
    animationSpec = tween(
        durationMillis = MODAL_BACKDROP_FADE_IN_DURATION,
        delayMillis = SHARED_AXIS_FADE_IN_DELAY,
        easing = LinearEasing
    )
)

/** 예측형 뒤로 가기 */
@Composable
internal fun AnimatedContentScope.PredictiveBackLayer(
    entry: NavBackStackEntry,
    content: @Composable () -> Unit
) {
    val dispatcherOwner = LocalNavigationEventDispatcherOwner.current
    // 모달 화면은 예측형 축소 대상에서 제외
    if (dispatcherOwner == null || entry.isModalDestination()) {
        content()
        return
    }

    val gestureState by dispatcherOwner.navigationEventDispatcher.transitionState.collectAsState()
    val gesture = gestureState as? NavigationEventTransitionState.InProgress
    val exiting = transition.targetState == EnterExitState.PostExit

    // 제스처로 시작된 퇴장만, 손을 뗀 뒤 남은 전환까지 유지
    var predictiveExit by remember { mutableStateOf(false) }
    SideEffect { predictiveExit = exiting && (gesture != null || predictiveExit) }

    // 확정되면 CREATED로 내려감, 취소면 그대로
    val lifecycleState by entry.lifecycle.currentStateFlow.collectAsState()
    val committed = predictiveExit && gesture == null && !lifecycleState.isAtLeast(Lifecycle.State.STARTED)

    val drag = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }
    var swipeEdge by remember { mutableIntStateOf(EDGE_LEFT) }

    // 잡는 동안은 손가락을 그대로 따라감
    LaunchedEffect(gesture, exiting) {
        if (gesture != null && exiting) {
            swipeEdge = gesture.latestEvent.swipeEdge
            drag.snapTo(gesture.latestEvent.progress)
        }
    }

    LaunchedEffect(gesture == null, committed) {
        if (gesture != null || !predictiveExit) return@LaunchedEffect
        if (committed) {
            // 놓은 자리에서 이어 밀려 나감
            val remaining = (PREDICTIVE_BACK_HOLD_DURATION * (1f - drag.value)).toInt()
            exit.animateTo(
                1f,
                tween(minOf(PREDICTIVE_BACK_EXIT_DURATION, remaining), easing = LinearEasing)
            )
        } else {
            // 확정 여부가 같은 프레임에 늦게 올 수 있어 한 프레임 기다림
            withFrameNanos { }
            drag.animateTo(0f, tween(PREDICTIVE_BACK_CANCEL_DURATION, easing = FastOutSlowInEasing))
        }
    }

    // 기기 모서리 반경, 못 읽으면 기본값
    val view = LocalView.current
    val deviceCorners = remember(view, predictiveExit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val insets = view.rootWindowInsets
            val top = insets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius ?: 0
            val bottom = insets?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)?.radius ?: 0
            if (top > 0 || bottom > 0) top.toFloat() to bottom.toFloat() else null
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (predictiveExit) {
                    Modifier
                        // 카드 뒤 이전 화면을 어둡게
                        .drawBehind {
                            val dim = (drag.value / PREDICTIVE_BACK_DIM_RANGE).coerceAtMost(1f) * (1f - exit.value)
                            drawRect(Color.Black.copy(alpha = PREDICTIVE_BACK_DIM_ALPHA * dim))
                        }
                        .graphicsLayer {
                            val pull = LinearOutSlowInEasing.transform(drag.value)
                            val leave = exit.value
                            val direction = if (swipeEdge == EDGE_RIGHT) -1f else 1f
                            val scale = 1f - (1f - PREDICTIVE_BACK_SCALE) * pull
                            scaleX = scale
                            scaleY = scale
                            // 놓으면 지금 위치에서 화면 밖까지 밀려 나감
                            translationX = direction * size.width *
                                lerp(PREDICTIVE_BACK_SHIFT_FRACTION * pull, 1f, FastOutSlowInEasing.transform(leave))
                            // 잡는 순간부터 기기 화면 모서리 그대로
                            val fallback = PREDICTIVE_BACK_CORNER_RADIUS.toPx()
                            val top = deviceCorners?.first ?: fallback
                            val bottom = deviceCorners?.second ?: fallback
                            clip = true
                            shape = RoundedCornerShape(
                                topStart = top,
                                topEnd = top,
                                bottomEnd = bottom,
                                bottomStart = bottom
                            )
                        }
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}
