package com.ninezero.halo.core.ui.components.snackbar

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick

data class SnackbarState(
    val message: String,
    val actionLabel: String? = null,
    val secondaryActionLabel: String? = null,  // 보조 액션, host가 직접 렌더
    val persistent: Boolean = false,  // 자동 해제 없음
    val timestamp: Long = System.currentTimeMillis()
)

data class CustomSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val secondaryActionLabel: String? = null,
    val onSecondaryAction: (() -> Unit)? = null  // host가 호출 후 dismiss
) : SnackbarVisuals

@Composable
fun DefaultSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,  // 바텀바 footprint, 키보드와 최댓값 합산
    applyWindowInsets: Boolean = true  // false면 인셋 미적용
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    // 표시 중 hostState 등록, 전역 호스트용
    val snackbarRelay = LocalSnackbarRelay.current
    val relayScope = rememberCoroutineScope()
    DisposableEffect(snackbarRelay, hostState) {  // relay도 키에 넣어 탭 전환 시 재등록
        snackbarRelay.register(hostState, relayScope)
        onDispose { snackbarRelay.unregister(hostState) }
    }

    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        val actionLabel = snackbarData.visuals.actionLabel
        val customVisuals = snackbarData.visuals as? CustomSnackbarVisuals
        val secondaryActionLabel = customVisuals?.secondaryActionLabel
        val hasAction = actionLabel != null || secondaryActionLabel != null

        val containerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f)
        val contentColor = MaterialTheme.colorScheme.inverseOnSurface

        Box(
            modifier = Modifier
                // 키보드와 바텀바 중 높은 쪽 위
                .then(
                    if (applyWindowInsets) Modifier.windowInsetsPadding(
                        WindowInsets.ime.union(
                            WindowInsets.navigationBars.add(WindowInsets(bottom = bottomBarPadding))
                        )
                    ) else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor)
                    .padding(
                        start = 14.dp,
                        end = if (hasAction) 6.dp else 14.dp,
                        top = if (hasAction) 4.dp else 8.dp,
                        bottom = if (hasAction) 4.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    // weight로 줄바꿈, 액션 버튼 보호
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (secondaryActionLabel != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            customVisuals.onSecondaryAction?.invoke()
                            snackbarData.dismiss()
                        },
                        modifier = Modifier
                            .height(28.dp)
                            .bounceClick(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = contentColor.copy(alpha = 0.15f),
                            contentColor = contentColor
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = secondaryActionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (actionLabel != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            snackbarData.performAction()
                        },
                        modifier = Modifier
                            .height(28.dp)
                            .bounceClick(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = contentColor.copy(alpha = 0.15f),
                            contentColor = contentColor
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/** SnackbarState 변경 시 표시 */
@Composable
fun SnackbarHandler(
    snackbarState: SnackbarState?,
    hostState: SnackbarHostState,
    onAction: () -> Unit = {},
    onSecondaryAction: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val latestOnSecondaryAction by rememberUpdatedState(onSecondaryAction)
    val latestOnAction by rememberUpdatedState(onAction)
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(snackbarState?.timestamp) {
        snackbarState?.let { state ->
            hostState.currentSnackbarData?.dismiss()

            val result = hostState.showSnackbar(
                visuals = CustomSnackbarVisuals(
                    message = state.message,
                    actionLabel = state.actionLabel,
                    duration = when {
                        state.persistent -> SnackbarDuration.Indefinite
                        state.actionLabel != null || state.secondaryActionLabel != null -> SnackbarDuration.Long
                        else -> SnackbarDuration.Short
                    },
                    secondaryActionLabel = state.secondaryActionLabel,
                    onSecondaryAction = { latestOnSecondaryAction() }
                )
            )

            when (result) {
                SnackbarResult.ActionPerformed -> latestOnAction()
                SnackbarResult.Dismissed -> latestOnDismiss()
            }
        }
    }

    // 화면을 벗어나면 표시 중이던 '일반' 스낵바 상태를 제거
    val latestSnackbarState by rememberUpdatedState(snackbarState)
    DisposableEffect(Unit) {
        onDispose {
            latestSnackbarState?.let { s ->
                if (!s.persistent) latestOnDismiss()
            }
        }
    }
}

/** 메시지 변경 시 표시, 시트 안 피드백용 */
@Composable
fun SnackbarHandler(
    message: String?,
    hostState: SnackbarHostState,
    onDismiss: () -> Unit
) {
    SnackbarHandler(
        snackbarState = remember(message) { message?.let { SnackbarState(it) } },
        hostState = hostState,
        onDismiss = onDismiss
    )
}
