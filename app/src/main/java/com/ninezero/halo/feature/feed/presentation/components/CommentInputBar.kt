package com.ninezero.halo.feature.feed.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.feed.domain.model.Comment
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState

@Composable
fun CommentInputBar(
    targetAuthorName: String?,
    replyTo: Comment?,
    hazeState: HazeState,
    onInputClick: () -> Unit,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String = "댓글",
    disabledText: String? = null
) {
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { /* 터치 이벤트 소비 */ }
            }
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = backgroundColor,
                    tint = HaloBlurTint(backgroundColor),
                    noiseFactor = 0f
                )
                progressive = HazeProgressive.verticalGradient(
                    startIntensity = 0f,
                    endIntensity = 0.5f
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val fieldGlassTint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (disabledText == null) {
                            Modifier
                                .bounceClick()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onInputClick
                                )
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .haloBlur(state = hazeState) {
                        style = HaloBlurStyle(
                            backgroundColor = backgroundColor,
                            tint = HaloBlurTint(fieldGlassTint),
                            blurRadius = 20.dp,
                            noiseFactor = 0f
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = disabledText ?: replyTo?.let {
                        "${it.author.displayName ?: it.author.username}님에게 답글 남기기"
                    } ?: targetAuthorName?.let {
                        "${it}님에게 $actionText 남기기"
                    } ?: "${actionText}을 입력하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentInputBarEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "개발자님에게 댓글 남기기",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun CommentInputBarReplyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "사용자1님에게 답글 남기기",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
