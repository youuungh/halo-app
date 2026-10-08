package com.ninezero.halo.core.ui.components.popup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme

/** 프로필 팝업 메뉴 */
@Composable
fun ProfilePopupMenu(
    isFollowing: Boolean,
    onDismiss: () -> Unit,
    onVisitProfile: () -> Unit,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
    offset: IntOffset = IntOffset(0, 0)
) {
    val density = LocalDensity.current
    val verticalPadding = with(density) { 8.dp.roundToPx() }

    Popup(
        onDismissRequest = onDismiss,
        offset = IntOffset(offset.x, offset.y + verticalPadding),
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(6.dp),
            modifier = modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = 140.dp)
        ) {
            Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                // 프로필 방문
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onVisitProfile()
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "프로필 방문",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(R.drawable.ic_profile),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 0.3.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // 팔로우 / 팔로우 취소
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onToggleFollow()
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFollowing) "팔로우 취소" else "팔로우",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(
                                if (isFollowing) R.drawable.ic_circle_minus
                                else R.drawable.ic_circle_plus
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProfilePopupMenuNotFollowingPreview() {
    HaloTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 140.dp)
            ) {
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "프로필 방문",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_profile),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.3.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "팔로우",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_circle_plus),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ProfilePopupMenuFollowingPreview() {
    HaloTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 140.dp)
            ) {
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "프로필 방문",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_profile),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    HorizontalDivider(
                        thickness = 0.3.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "팔로우 취소",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.ic_circle_minus),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
