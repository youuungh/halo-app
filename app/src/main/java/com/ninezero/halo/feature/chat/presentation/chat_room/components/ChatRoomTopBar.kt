package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurs
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.components.modifiers.haloGlass
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.core.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeState

@Composable
fun ChatRoomTopBar(
    otherUser: UserSummary?,
    hazeState: HazeState,
    backgroundColor: Color,
    onBackClick: () -> Unit,
    onUserClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    otherUserLoaded: Boolean = false,
    onMoreClick: () -> Unit = {},
    showActions: Boolean = true,
    isSearchMode: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onClearSearch: () -> Unit = {},
    searchFocusRequester: FocusRequester? = null
) {
    // 검색에 들어가면 알약이 늘어나 검색창이 됨
    val userAlpha by animateFloatAsState(
        targetValue = if (isSearchMode) 0f else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "chat_top_user_alpha"
    )
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .graphicsLayer { alpha = userAlpha }
                        .clip(MaterialTheme.shapes.small)
                        .clickable(
                            enabled = !isSearchMode,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onUserClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    key(otherUserLoaded) {
                        Avatar(
                            model = otherUser?.avatarThumbUrl ?: otherUser?.avatarUrl,
                            size = 36.dp,
                            contentDescription = "${otherUser?.displayName} 프로필"
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = otherUser?.displayName ?: otherUser?.username ?: "채팅",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            VerifiedBadge(role = otherUser?.role, size = 16.dp)
                        }
                        otherUser?.username?.let { username ->
                            Text(
                                text = username,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (isSearchMode || showActions) {
                    ExpandingSearchPill(
                        expanded = isSearchMode,
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearch = onSearch,
                        onClear = onClearSearch,
                        onSearchClick = onSearchClick,
                        onMoreClick = onMoreClick,
                        hazeState = hazeState,
                        backgroundColor = backgroundColor,
                        focusRequester = searchFocusRequester,
                        modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                    )
                }
            }
        },
        navigationIcon = {
            GlassIconButton(
                onClick = onBackClick,
                icon = painterResource(
                    if (isSearchMode) R.drawable.ic_close else R.drawable.ic_chevron_left
                ),
                contentDescription = if (isSearchMode) "검색 닫기" else "뒤로가기",
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier.haloBlur(hazeState, HaloBlurs.fadeDown(backgroundColor))
    )
}

// 접힌 알약 = 돋보기 칸 46 + 선 + 더보기 칸 46
private val PillCollapsedWidth = 93.dp
private val PillIconSlot = 46.dp

/** 검색 · 더보기 유리 알약, 펼치면 검색창 */
@Composable
private fun ExpandingSearchPill(
    expanded: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onSearchClick: () -> Unit,
    onMoreClick: () -> Unit,
    hazeState: HazeState,
    backgroundColor: Color,
    focusRequester: FocusRequester?,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        val width by animateDpAsState(
            targetValue = if (expanded) maxWidth else PillCollapsedWidth,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            label = "chat_search_pill_width"
        )
        // 더보기는 바로 사라지고 입력칸은 늦게 나타남
        val moreAlpha by animateFloatAsState(
            targetValue = if (expanded) 0f else 1f,
            animationSpec = tween(durationMillis = if (expanded) 100 else 200, delayMillis = if (expanded) 0 else 120),
            label = "chat_search_more_alpha"
        )
        val fieldAlpha by animateFloatAsState(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = tween(durationMillis = 200, delayMillis = if (expanded) 140 else 0),
            label = "chat_search_field_alpha"
        )

        LaunchedEffect(expanded) {
            if (expanded) focusRequester?.requestFocus()
        }

        Box(
            modifier = Modifier
                .width(width)
                .height(40.dp)
                .haloGlass(hazeState, backgroundColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(PillIconSlot)
                    .fillMaxHeight()
                    .clickable(
                        enabled = !expanded,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true)
                    ) { onSearchClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "검색",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            if (moreAlpha > 0f) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .graphicsLayer { alpha = moreAlpha },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(20.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .width(PillIconSlot)
                            .height(40.dp)
                            .clickable(
                                enabled = !expanded,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { onMoreClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_ellipsis_horizontal_circle),
                            contentDescription = "더보기",
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = PillIconSlot - 4.dp, end = 12.dp)
                        .graphicsLayer { alpha = fieldAlpha },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "메시지 검색",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
                                )
                        )
                    }

                    if (query.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                .clickable { onClear() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "지우기",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun ChatRoomTopBarPreview() {
    HaloTheme {
        ChatRoomTopBar(
            otherUser = UserSummary(
                id = 1,
                username = "user1",
                displayName = "김민수",
                avatarUrl = "avatarUrl",
                isFollowing = false
            ),
            hazeState = remember { HazeState() },
            backgroundColor = MaterialTheme.colorScheme.surface,
            onBackClick = {},
            onUserClick = {},
            onSearchClick = {},
            onMoreClick = {}
        )
    }
}

@HaloLightDarkPreview
@Composable
private fun ChatRoomTopBarSearchModePreview() {
    HaloTheme {
        ChatRoomTopBar(
            otherUser = UserSummary(
                id = 1,
                username = "user1",
                displayName = "김민수",
                avatarUrl = "avatarUrl",
                isFollowing = false
            ),
            hazeState = remember { HazeState() },
            backgroundColor = MaterialTheme.colorScheme.surface,
            onBackClick = {},
            onUserClick = {},
            onSearchClick = {},
            onMoreClick = {},
            isSearchMode = true,
            searchQuery = "안녕하세요",
            onSearchQueryChange = {},
            onSearch = {}
        )
    }
}
