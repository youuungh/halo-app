package com.ninezero.halo.feature.chat.presentation.chat_list

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.bottomsheet.ChatOptionMode
import com.ninezero.halo.core.ui.components.bottomsheet.ChatOptionsBottomSheet
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.ListEmptyState
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.navigation.BottomBarDefaults
import com.ninezero.halo.core.ui.components.navigation.LocalBottomBarPadding
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.refresh.HaloPullToRefreshBox
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.chat.presentation.chat_list.components.ChatListItemSkeleton
import com.ninezero.halo.feature.chat.presentation.components.ChatListItem
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.Flow
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ChatListScreen(
    onNavigateToChatRoom: (Int) -> Unit,
    tabReselectFlow: Flow<Unit>? = null,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ChatListSideEffect.NavigateToChatRoom -> onNavigateToChatRoom(sideEffect.roomId)
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(ChatListIntent.SnackbarDismissed) }
    )

    ChatListScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        tabReselectFlow = tabReselectFlow,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun ChatListScreenContent(
    state: ChatListState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    tabReselectFlow: Flow<Unit>? = null,
    onIntent: (ChatListIntent) -> Unit
) {
    val hazeState = remember { HazeState() }
    val topBarBlurColor = MaterialTheme.colorScheme.background
    val listState = rememberLazyListState()
    val archiveSheetState = rememberModalBottomSheetState()

    LaunchedEffect(tabReselectFlow, listState) {
        tabReselectFlow?.collect {
            val atTop = listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
            if (atTop) {
                onIntent(ChatListIntent.RefreshChatRooms)
            } else {
                listState.animateScrollToItem(0)
            }
        }
    }

    // 다이얼로그 중엔 시트만 닫고 selectedChat 유지
    if (state.selectedChat != null && !state.showDeleteDialog) {
        ChatOptionsBottomSheet(
            sheetState = archiveSheetState,
            mode = ChatOptionMode.ARCHIVE,
            onActionClick = { onIntent(ChatListIntent.ArchiveSelectedChat) },
            onDeleteClick = { onIntent(ChatListIntent.OpenDeleteDialog) },
            onDismiss = { onIntent(ChatListIntent.DismissArchiveSheet) }
        )
    }

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "대화를 삭제하시겠어요?",
            message = "회원님의 계정에서 메시지 기록이 지워집니다.",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(ChatListIntent.DeleteSelectedChat) },
            onDismiss = { onIntent(ChatListIntent.DismissDeleteDialog) }
        )
    }

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoading,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(ChatListIntent.LoadMoreChatRooms) }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        snackbarHost = {
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = BottomBarDefaults.PillFootprint)
            )
        },
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "메시지",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
                    .hazeEffect(state = hazeState) {
                        style = HazeStyle(
                            backgroundColor = topBarBlurColor,
                            tint = HazeTint(topBarBlurColor),
                            noiseFactor = 0f
                        )
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0.5f,
                            endIntensity = 0f
                        )
                    }
            )
        }
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()

        HaloPullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onIntent(ChatListIntent.RefreshChatRooms) },
            modifier = Modifier.fillMaxSize(),
            indicatorPadding = PaddingValues(top = topPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                val isInitialLoading = state.isLoading && state.chatRooms.isEmpty()

                when {
                    // 로딩 스켈레톤
                    isInitialLoading -> {
                        if (topPadding > 0.dp) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = topPadding, bottom = LocalBottomBarPadding.current)
                            ) {
                                items(10) {
                                    ChatListItemSkeleton()
                                }
                            }
                        }
                    }

                    // 에러 상태
                    state.error != null && state.chatRooms.isEmpty() -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize().padding(top = topPadding),
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
                                            onClick = { onIntent(ChatListIntent.LoadChatRooms) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.bounceClick()
                                        ) {
                                            Text("재시도")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 빈 상태
                    state.chatRooms.isEmpty() -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize().padding(top = topPadding),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ListEmptyState(
                                        title = "아직 대화가 없어요",
                                        description = "다른 사용자의 프로필에서 메시지를 보내보세요"
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = topPadding, bottom = LocalBottomBarPadding.current)
                        ) {
                            itemsIndexed(
                                items = state.chatRooms,
                                key = { _, chat -> chat.id }
                            ) { index, chat ->
                                Column {
                                    ChatListItem(
                                        chat = chat,
                                        onClick = { onIntent(ChatListIntent.NavigateToChatRoom(chat.id)) },
                                        onLongClick = { onIntent(ChatListIntent.SelectChatForArchive(chat)) }
                                    )

                                    if (index < state.chatRooms.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 84.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }

                            if (state.isLoading && state.chatRooms.isNotEmpty()) {
                                items(3) {
                                    ChatListItemSkeleton()
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        CenterToast(
            visible = state.showArchiveToast,
            message = state.archiveToastMessage,
            onDismiss = { onIntent(ChatListIntent.DismissArchiveToast) }
        )
    }
}
