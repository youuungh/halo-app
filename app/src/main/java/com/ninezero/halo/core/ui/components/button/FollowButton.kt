package com.ninezero.halo.core.ui.components.button

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.preview.HaloSurfacePreview

private val CheckScaleSpec = spring<Float>(dampingRatio = 0.5f, stiffness = 500f)
private val CheckSizeSpec = spring<IntSize>(dampingRatio = 0.8f, stiffness = 500f)

/** 팔로우 알약 버튼 */
@Composable
fun FollowButton(
    isFollowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    followText: String = "팔로우"
) {
    val colorSpec = tween<Color>(durationMillis = if (isFollowing) 220 else 120)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outlineVariant
    val container by animateColorAsState(
        if (isFollowing) onSurface.copy(alpha = 0f) else onSurface,
        colorSpec,
        label = "followContainer"
    )
    val content by animateColorAsState(
        if (isFollowing) onSurface else MaterialTheme.colorScheme.surface,
        colorSpec,
        label = "followContent"
    )
    val border by animateColorAsState(
        if (isFollowing) outline else outline.copy(alpha = 0f),
        colorSpec,
        label = "followBorder"
    )

    PillButtonFrame(onClick = onClick, background = container, borderColor = border, modifier = modifier) {
        AnimatedVisibility(
            visible = isFollowing,
            enter = expandHorizontally(CheckSizeSpec) + scaleIn(CheckScaleSpec, initialScale = 0.4f) + fadeIn(),
            exit = shrinkHorizontally(tween(120)) + fadeOut(tween(100))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(14.dp),
                tint = content
            )
        }
        PillButtonText(if (isFollowing) "팔로잉" else followText, content)
    }
}

/** 테두리 알약 버튼 */
@Composable
fun OutlinedPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PillButtonFrame(
        onClick = onClick,
        background = Color.Transparent,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        modifier = modifier
    ) {
        PillButtonText(text, MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun PillButtonFrame(
    onClick: () -> Unit,
    background: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .bounceClick()
            .height(32.dp)
            .clip(CircleShape)
            .background(background)
            .border(0.5.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun PillButtonText(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        maxLines = 1
    )
}

@HaloLightDarkPreview
@Composable
private fun FollowButtonPreview() {
    HaloSurfacePreview {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FollowButton(isFollowing = false, onClick = {})
            FollowButton(isFollowing = true, onClick = {})
            FollowButton(isFollowing = false, onClick = {}, followText = "맞팔로우")
            OutlinedPillButton(text = "메시지 보내기", onClick = {})
        }
    }
}
