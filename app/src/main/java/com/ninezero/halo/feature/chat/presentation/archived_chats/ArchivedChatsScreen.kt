package com.ninezero.halo.feature.chat.presentation.archived_chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.bottomsheet.ChatOptionMode
import com.ninezero.halo.core.ui.components.bottomsheet.ChatOptionsBottomSheet
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.ListEmptyState
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.chat.presentation.components.ChatListItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ArchivedChatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChatRoom: (Int) -> Unit,
    viewModel: ArchivedChatsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ArchivedChatsSideEffect.NavigateToChatRoom -> onNavigateToChatRoom(sideEffect.roomId)
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(ArchivedChatsIntent.SnackbarDismissed) }
    )

    ArchivedChatsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun ArchivedChatsScreenContent(
    state: ArchivedChatsState,
    onNavigateBack: () -> Unit,
    onIntent: (ArchivedChatsIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }
    var headerHeight by rememberSaveableDp()
    val listState = rememberLazyListState()
    val unarchiveSheetState = rememberModalBottomSheetState()

    if (state.selectedChat != null && !state.showDeleteDialog) {
        ChatOptionsBottomSheet(
            sheetState = unarchiveSheetState,
            mode = ChatOptionMode.UNARCHIVE,
            onActionClick = { onIntent(ArchivedChatsIntent.UnarchiveSelectedChat) },
            onDeleteClick = { onIntent(ArchivedChatsIntent.OpenDeleteDialog) },
            onDismiss = { onIntent(ArchivedChatsIntent.DismissUnarchiveSheet) }
        )
    }

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "대화를 삭제하시겠어요?",
            message = "회원님의 계정에서 메시지 기록이 지워집니다.",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(ArchivedChatsIntent.DeleteSelectedChat) },
            onDismiss = { onIntent(ArchivedChatsIntent.DismissDeleteDialog) }
        )
    }

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(ArchivedChatsIntent.LoadMoreArchivedChats) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing || (state.isLoading && state.archivedChats.isEmpty()),
            onRefresh = { onIntent(ArchivedChatsIntent.RefreshArchivedChats) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = headerHeight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                when {
                    state.error != null && state.archivedChats.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = { onIntent(ArchivedChatsIntent.LoadArchivedChats) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.bounceClick()
                                ) {
                                    Text("재시도")
                                }
                            }
                        }
                    }

                    state.archivedChats.isEmpty() -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ListEmptyState(
                                        title = "보관된 대화가 없어요",
                                        description = "채팅 목록에서 대화를 보관할 수 있어요"
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = headerHeight + 16.dp, bottom = 16.dp)
                        ) {
                            itemsIndexed(
                                items = state.archivedChats,
                                key = { _, chat -> chat.id }
                            ) { index, chat ->
                                Column {
                                    ChatListItem(
                                        chat = chat,
                                        onClick = { onIntent(ArchivedChatsIntent.NavigateToChatRoom(chat.id)) },
                                        onLongClick = { onIntent(ArchivedChatsIntent.SelectChatForUnarchive(chat)) }
                                    )

                                    if (index < state.archivedChats.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 84.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }

                            if (state.isLoading && state.archivedChats.isNotEmpty()) {
                                item(key = "loading_more") {
                                    ListLoadMoreIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }

        HazeScreenHeader(
            title = "보관됨",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        CenterToast(
            visible = state.showUnarchiveToast,
            message = state.unarchiveToastMessage,
            onDismiss = { onIntent(ArchivedChatsIntent.DismissUnarchiveToast) }
        )
    }
}
