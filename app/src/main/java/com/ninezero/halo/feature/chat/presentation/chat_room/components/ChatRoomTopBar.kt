package com.ninezero.halo.feature.chat.presentation.chat_room.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.badge.VerifiedBadge
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurStyle
import com.ninezero.halo.core.ui.components.modifiers.HaloBlurTint
import com.ninezero.halo.core.ui.components.modifiers.haloBlur
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.ui.theme.HaloTheme
import dev.chrisbanes.haze.HazeProgressive
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
    TopAppBar(
        title = {
            if (isSearchMode) {
                ChatRoomSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearch,
                    onClear = onClearSearch,
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    focusRequester = searchFocusRequester,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 8.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true)
                        ) { onUserClick() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    key(otherUserLoaded) {
                        AsyncImage(
                            model = otherUser?.avatarThumbUrl ?: otherUser?.avatarUrl,
                            contentDescription = "${otherUser?.displayName} 프로필",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_profile_placeholder),
                            error = painterResource(R.drawable.ic_profile_placeholder),
                            fallback = painterResource(R.drawable.ic_profile_placeholder)
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
        actions = {
            if (!isSearchMode && showActions) {
                GlassPillButtons(
                    hazeState = hazeState,
                    backgroundColor = backgroundColor,
                    onSearchClick = onSearchClick,
                    onMoreClick = onMoreClick,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier.haloBlur(state = hazeState) {
            style = HaloBlurStyle(
                backgroundColor = backgroundColor,
                tint = HaloBlurTint(backgroundColor),
                noiseFactor = 0f
            )
            progressive = HazeProgressive.verticalGradient(
                startIntensity = 0.5f,
                endIntensity = 0f
            )
        }
    )
}

@Composable
private fun ChatRoomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    hazeState: HazeState,
    backgroundColor: Color,
    focusRequester: FocusRequester?,
    modifier: Modifier = Modifier
) {
    val glassTint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = backgroundColor,
                    tint = HaloBlurTint(glassTint),
                    blurRadius = 20.dp,
                    noiseFactor = 0f
                )
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "메시지 검색",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            if (focusRequester != null) {
                                Modifier.focusRequester(focusRequester)
                            } else {
                                Modifier
                            }
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

@Composable
private fun GlassPillButtons(
    hazeState: HazeState,
    backgroundColor: Color,
    onSearchClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .haloBlur(state = hazeState) {
                style = HaloBlurStyle(
                    backgroundColor = backgroundColor.copy(alpha = 0.7f),
                    tint = HaloBlurTint(backgroundColor.copy(alpha = 0.4f)),
                    blurRadius = 20.dp,
                    noiseFactor = 0f
                )
            }
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true)
                ) { onSearchClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "검색",
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .width(0.5.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true)
                ) { onMoreClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
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
