package com.ninezero.halo.feature.share.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ninezero.halo.R
import com.ninezero.halo.core.deeplink.copyLinkToClipboard
import com.ninezero.halo.core.deeplink.shareTextExternally
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.button.SheetActionButton
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.preview.HaloLightDarkPreview
import com.ninezero.halo.feature.share.domain.model.ShareResult
import com.ninezero.halo.feature.share.presentation.components.SelectableUserItem
import com.ninezero.halo.ui.theme.HaloTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private val UserRowHeight = 100.dp

@Composable
fun ShareBottomSheet(
    viewModel: ShareViewModel,
    onDismiss: () -> Unit,
    onShareSuccess: (ShareResult) -> Unit,
    shareAction: (ShareViewModel) -> Unit,
    modifier: Modifier = Modifier,
    externalLink: String? = null
) {
    val context = LocalContext.current
    val state = viewModel.collectAsState().value
    val currentIsSending = rememberUpdatedState(state.isSending)
    val confirmValueChange = remember {
        { nextValue: SheetValue ->
            !(currentIsSending.value && nextValue == SheetValue.Hidden)
        }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = confirmValueChange
    )

    // 실패는 시트 안에서, 고른 사람·메시지는 유지
    var shareError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onIntent(ShareIntent.LoadFollowing)
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ShareSideEffect.ShareSuccess -> {
                onShareSuccess(sideEffect.result)
                onDismiss()
            }
            is ShareSideEffect.ShareError -> shareError = sideEffect.message.ifBlank { "공유에 실패했습니다" }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!state.isSending) onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Box {
            Column {
                BottomSheetDragHandle()
                ShareBottomSheetContent(
                    state = state,
                    onSearchQueryChange = { viewModel.onIntent(ShareIntent.SearchUsers(it)) },
                    onToggleUser = { viewModel.onIntent(ShareIntent.ToggleUser(it)) },
                    onMessageChange = { viewModel.onIntent(ShareIntent.UpdateMessage(it)) },
                    onSendClick = { shareAction(viewModel) },
                    externalLink = externalLink,
                    onCopyLink = {
                        externalLink?.let(context::copyLinkToClipboard)
                        onDismiss()
                    },
                    onShareExternal = {
                        externalLink?.let(context::shareTextExternally)
                        onDismiss()
                    },
                    modifier = modifier
                )
            }
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
                bottomBarPadding = 56.dp  // 보내기 버튼 48 + 하단 여백 8
            )
        }
    }

    SnackbarHandler(
        message = shareError,
        hostState = snackbarHostState,
        onDismiss = { shareError = null }
    )
}

@Composable
private fun ShareBottomSheetContent(
    state: ShareState,
    onSearchQueryChange: (String) -> Unit,
    onToggleUser: (Int) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    externalLink: String? = null,
    onCopyLink: () -> Unit = {},
    onShareExternal: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 검색
            SearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 팔로잉 유저 목록
            if (state.isLoading) {
                CenteredLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(UserRowHeight),
                    size = LoadingIndicatorSize.Medium
                )
            } else if (state.filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(UserRowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.searchQuery.isNotBlank()) "검색 결과가 없습니다" else "팔로잉한 유저가 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.height(UserRowHeight),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    overscrollEffect = null
                ) {
                    items(state.filteredUsers, key = { it.id }) { user ->
                        SelectableUserItem(
                            user = user,
                            isSelected = user.id in state.selectedUserIds,
                            onToggle = { onToggleUser(user.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                thickness = 0.25.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 하단
            AnimatedContent(
                targetState = state.selectedUserIds.isEmpty() && externalLink != null,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = 90)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 90)))
                        .using(SizeTransform(clip = false))
                },
                label = "share_sheet_bottom_mode"
            ) { isExternalMode ->
                if (isExternalMode) {
                    ExternalShareRow(
                        onCopyLink = onCopyLink,
                        onShareExternal = onShareExternal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        MessageInput(
                            message = state.message,
                            onMessageChange = onMessageChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val canSend = state.selectedUserIds.isNotEmpty()
                        SheetActionButton(
                            text = if (state.selectedUserIds.size > 1) {
                                "${state.selectedUserIds.size}명에게 보내기"
                            } else {
                                "보내기"
                            },
                            onClick = onSendClick,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            isLoading = state.isSending,
                            enabled = canSend
                        )
                    }
                }
            }
        }

        // 전송 중 입력 차단
        if (state.isSending) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
            )
        }
    }
}

@Composable
private fun ExternalShareRow(
    onCopyLink: () -> Unit,
    onShareExternal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        ExternalShareAction(
            iconRes = R.drawable.ic_link,
            label = "링크 복사",
            onClick = onCopyLink
        )
        ExternalShareAction(
            iconRes = R.drawable.ic_share,
            label = "공유하기",
            onClick = onShareExternal
        )
    }
}

@Composable
private fun ExternalShareAction(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .bounceClick()
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "Search",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            // lineHeightStyle 명시로 줄 높이 고정
            val textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.None
                ),
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "검색",
                        style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Clear",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    Box(modifier = modifier) {
        if (message.isEmpty()) {
            Text(
                text = "메시지 입력...",
                style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        BasicTextField(
            value = message,
            onValueChange = onMessageChange,
            singleLine = false,
            maxLines = 3,
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val previewUsers = listOf(
    UserSummary(id = 1, username = "user1", displayName = "김민수", avatarUrl = null),
    UserSummary(id = 2, username = "user2", displayName = "이영희", avatarUrl = null),
    UserSummary(id = 3, username = "user3", displayName = "박지훈", avatarUrl = null),
    UserSummary(id = 4, username = "user4", displayName = "최수연", avatarUrl = null),
    UserSummary(id = 5, username = "user5", displayName = "정다은", avatarUrl = null)
)

@Preview(showBackground = true, name = "Default - 1. Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Default - 2. Dark")
@Composable
private fun ShareBottomSheetContentPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShareBottomSheetContent(
                state = ShareState(
                    filteredUsers = previewUsers,
                    followingUsers = previewUsers
                ),
                onSearchQueryChange = {},
                onToggleUser = {},
                onMessageChange = {},
                onSendClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "External - 1. Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "External - 2. Dark")
@Composable
private fun ShareBottomSheetContentExternalPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShareBottomSheetContent(
                state = ShareState(
                    filteredUsers = previewUsers,
                    followingUsers = previewUsers
                ),
                onSearchQueryChange = {},
                onToggleUser = {},
                onMessageChange = {},
                onSendClick = {},
                externalLink = "https://example.com/post/1"
            )
        }
    }
}

@Preview(showBackground = true, name = "Selected - 1. Light")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Selected - 2. Dark")
@Composable
private fun ShareBottomSheetContentSelectedPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShareBottomSheetContent(
                state = ShareState(
                    filteredUsers = previewUsers,
                    followingUsers = previewUsers,
                    selectedUserIds = setOf(1, 3),
                    message = "테스트입니다"
                ),
                onSearchQueryChange = {},
                onToggleUser = {},
                onMessageChange = {},
                onSendClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ShareBottomSheetContentEmptyPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShareBottomSheetContent(
                state = ShareState(),
                onSearchQueryChange = {},
                onToggleUser = {},
                onMessageChange = {},
                onSendClick = {}
            )
        }
    }
}

@HaloLightDarkPreview
@Composable
private fun ShareBottomSheetContentLoadingPreview() {
    HaloTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShareBottomSheetContent(
                state = ShareState(
                    searchQuery = "테",
                    isLoading = true
                ),
                onSearchQueryChange = {},
                onToggleUser = {},
                onMessageChange = {},
                onSendClick = {}
            )
        }
    }
}
