package com.ninezero.halo.feature.chat.presentation.chat_room

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ninezero.halo.core.fcm.ActiveChatRoomTracker
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.ProfileOptionBottomSheet
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.media_viewer.MediaViewerOverlay
import com.ninezero.halo.core.ui.components.media_viewer.MediaViewerState
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.media_viewer.ViewerMediaType
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.scroll.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.core.util.isSameDay
import com.ninezero.halo.core.util.isSameMinute
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.MessageStatus
import com.ninezero.halo.feature.chat.presentation.chat_room.components.BlockedByOtherOverlay
import com.ninezero.halo.feature.chat.presentation.chat_room.components.BlockedUserOverlay
import com.ninezero.halo.feature.chat.presentation.chat_room.components.ChatRoomProfileHeader
import com.ninezero.halo.feature.chat.presentation.chat_room.components.ChatRoomTopBar
import com.ninezero.halo.feature.chat.presentation.chat_room.components.DateSeparator
import com.ninezero.halo.feature.chat.presentation.chat_room.components.MessageBubble
import com.ninezero.halo.feature.chat.presentation.chat_room.components.MessageInputBar
import com.ninezero.halo.feature.chat.presentation.chat_room.components.NewMessageBar
import com.ninezero.halo.feature.chat.presentation.chat_room.components.ScrollToBottomFAB
import com.ninezero.halo.feature.chat.presentation.chat_room.components.UnreadSeparator
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ChatRoomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (Int) -> Unit = {},
    onNavigateToPostDetail: (Int) -> Unit = {},
    onNavigateToProductDetail: (Int) -> Unit = {},
    viewModel: ChatRoomViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val searchFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    // 이 방 트레이 알림 제거, 보는 방으로 등록
    // roomId가 key라 draft 승격 시 재등록
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state.roomId) {
        val rid = state.roomId
        // RESUMED 중 승격이면 바로 등록
        if (rid > 0 && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            ActiveChatRoomTracker.enter(rid)
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.onIntent(ChatRoomIntent.ScreenResumed)
                    ActiveChatRoomTracker.enter(rid)
                }
                Lifecycle.Event.ON_PAUSE -> ActiveChatRoomTracker.leave(rid)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            ActiveChatRoomTracker.leave(rid)
        }
    }

    var mediaViewerState by remember { mutableStateOf(MediaViewerState()) }
    var centerToastVisible by remember { mutableStateOf(false) }
    var centerToastMessage by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MediaUtils.MAX_IMAGES_PER_MESSAGE)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onIntent(ChatRoomIntent.AddImages(uris))
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onIntent(ChatRoomIntent.SetVideo(it)) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onIntent(ChatRoomIntent.SetFile(it)) }
    }

    // 검색 중에만 가로챔, 평소엔 예측형 뒤로 가기
    BackHandler(enabled = state.isSearchMode) {
        viewModel.onIntent(ChatRoomIntent.ToggleSearchMode)
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ChatRoomSideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is ChatRoomSideEffect.ScrollToBottom -> {
                coroutineScope.launch { listState.animateScrollToItem(0) }
            }
            is ChatRoomSideEffect.HideKeyboard -> {
                focusManager.clearFocus()
            }
            is ChatRoomSideEffect.FocusSearchField -> {
                try {
                    searchFocusRequester.requestFocus()
                } catch (_: Exception) { }
            }
            is ChatRoomSideEffect.ScrollToMessage -> {
                val index = state.messages.indexOfFirst { it.id == sideEffect.messageId }
                if (index >= 0) {
                    coroutineScope.launch { listState.animateScrollToItem(index) }
                }
            }
            is ChatRoomSideEffect.ScrollToUnread -> {
                // 첫 안 읽은 메시지를 위쪽에, 다 들어가면 맨 아래
                val index = state.messages.indexOfFirst { it.id == sideEffect.lastReadMessageId } - 1
                if (index >= 0) {
                    coroutineScope.launch {
                        listState.scrollToItem(index)
                        val layoutInfo = listState.layoutInfo
                        val itemSize = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.size ?: return@launch
                        val contentHeight = layoutInfo.viewportSize.height -
                            layoutInfo.beforeContentPadding - layoutInfo.afterContentPadding
                        listState.scrollBy((itemSize - contentHeight).toFloat())  // 화면보다 긴 메시지는 위쪽을 맞춤
                    }
                }
            }
            is ChatRoomSideEffect.NavigateToPostDetail -> onNavigateToPostDetail(sideEffect.postId)
            is ChatRoomSideEffect.NavigateToProductDetail -> onNavigateToProductDetail(sideEffect.productId)
            is ChatRoomSideEffect.ShowCenterToast -> {
                centerToastMessage = sideEffect.message
                centerToastVisible = true
            }
            is ChatRoomSideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(ChatRoomIntent.SnackbarDismissed) }
    )

    if (state.isSearching) {
        LoadingDialog()
    }

    if (state.showDeleteDialog) {
        ConfirmDialog(
            title = "메시지 삭제",
            message = "이 메시지를 삭제하시겠습니까?",
            confirmText = "삭제",
            dismissText = "취소",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { viewModel.onIntent(ChatRoomIntent.ConfirmDeleteMessage) },
            onDismiss = { viewModel.onIntent(ChatRoomIntent.DismissDeleteDialog) }
        )
    }

    if (state.showProfileOptionsSheet) {
        val reportRequester = LocalReportRequester.current
        ProfileOptionBottomSheet(
            isBlocked = state.isBlocked,
            onDismiss = { viewModel.onIntent(ChatRoomIntent.DismissProfileOptions) },
            onReport = {
                state.otherUser?.let { reportRequester(ReportTargetType.USER, it.id) }
            },
            onBlock = {
                if (state.isBlocked) {
                    viewModel.onIntent(ChatRoomIntent.ShowUnblockConfirmDialog)
                } else {
                    viewModel.onIntent(ChatRoomIntent.ShowBlockConfirmDialog)
                }
            }
        )
    }

    state.otherUser?.let { otherUser ->
        if (state.showBlockConfirmDialog) {
            ConfirmDialog(
                profileImageUrl = otherUser.avatarUrl,
                title = "${otherUser.displayName ?: otherUser.username}님을 차단하시겠어요?",
                message = "차단하면 서로의 포스트와 프로필을 볼 수 없습니다.",
                confirmText = "차단",
                dismissText = "취소",
                confirmColor = MaterialTheme.colorScheme.error,
                onConfirm = { viewModel.onIntent(ChatRoomIntent.ConfirmBlockUser) },
                onDismiss = { viewModel.onIntent(ChatRoomIntent.DismissBlockConfirmDialog) }
            )
        }

        if (state.showUnblockConfirmDialog) {
            ConfirmDialog(
                profileImageUrl = otherUser.avatarUrl,
                title = "${otherUser.displayName ?: otherUser.username}님의 차단을 해제하시겠어요?",
                message = "차단 해제하면 서로의 포스트와 프로필을 다시 볼 수 있습니다.",
                confirmText = "차단 해제",
                dismissText = "취소",
                onConfirm = { viewModel.onIntent(ChatRoomIntent.ConfirmUnblockUser) },
                onDismiss = { viewModel.onIntent(ChatRoomIntent.DismissUnblockConfirmDialog) }
            )
        }

    }

    ChatRoomScreenContent(
        state = state,
        listState = listState,
        searchFocusRequester = searchFocusRequester,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onImagePickerClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onVideoPickerClick = {
            videoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        },
        onFilePickerClick = {
            filePickerLauncher.launch(arrayOf("*/*"))
        },
        onUserClick = {
            state.otherUser?.let { onNavigateToUserProfile(it.id) }
        },
        onProductClick = { productId, canAccess ->
            viewModel.onIntent(ChatRoomIntent.ProductLinkClicked(productId, canAccess))
        },
        onPostClick = { postId, canAccess ->
            viewModel.onIntent(ChatRoomIntent.PostLinkClicked(postId, canAccess))
        },
        onImageClick = { imageUrls, index ->
            mediaViewerState = MediaViewerState(
                isVisible = true,
                items = imageUrls.map { ViewerMediaItem(url = it) },
                initialIndex = index
            )
        },
        onVideoClick = { videoUrl, thumbnailUrl ->
            mediaViewerState = MediaViewerState(
                isVisible = true,
                items = listOf(ViewerMediaItem(url = videoUrl, thumbnailUrl = thumbnailUrl, type = ViewerMediaType.VIDEO))
            )
        },
        onFileDownload = { url, fileName ->
            viewModel.onIntent(ChatRoomIntent.DownloadFile(url, fileName))
        }
    )

    MediaViewerOverlay(
        state = mediaViewerState,
        onDismiss = { mediaViewerState = mediaViewerState.copy(isVisible = false) },
        showVideoMoreButton = true
    )

    CenterToast(
        visible = centerToastVisible,
        message = centerToastMessage,
        onDismiss = { centerToastVisible = false }
    )
}

@Composable
internal fun ChatRoomScreenContent(
    state: ChatRoomState,
    onIntent: (ChatRoomIntent) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    searchFocusRequester: FocusRequester = remember { FocusRequester() },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onImagePickerClick: () -> Unit = {},
    onVideoPickerClick: () -> Unit = {},
    onFilePickerClick: () -> Unit = {},
    onUserClick: () -> Unit = {},
    onProductClick: (Int, Boolean) -> Unit = { _, _ -> },
    onPostClick: (Int, Boolean) -> Unit = { _, _ -> },
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    onVideoClick: (String, String?) -> Unit = { _, _ -> },
    onFileDownload: (String, String) -> Unit = { _, _ -> },
    previewNewMessageBar: Message? = null
) {
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var topBarHeight by rememberSaveableDp()
    var bottomBarHeight by rememberSaveableDp()

    // reverseLayout이라 index > 0이면 위로 스크롤됨
    val showScrollToBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 2 ||
                (listState.firstVisibleItemIndex > 0 && listState.firstVisibleItemScrollOffset > 100)
        }
    }

    // 새 메시지 바 상태: 스크롤 중일 때 새 메시지 도착 시 표시
    var newMessageBarData by remember { mutableStateOf<Message?>(null) }
    // "맨밑 메시지의 변화"를 추적
    val latestKey = state.messages.firstOrNull()?.let { it.clientTempId ?: "id_${it.id}" }
    val previousLatestKey = remember { mutableStateOf(latestKey) }

    LaunchedEffect(latestKey, showScrollToBottom) {
        if (latestKey != null && latestKey != previousLatestKey.value) {
            val latestMessage = state.messages.firstOrNull()
            when {
                // 내가 보낸/재시도한 메시지는 항상 맨밑으로 이동
                latestMessage?.isFromMe == true -> listState.animateScrollToItem(0)
                // 상대 메시지인데 위로 스크롤된 상태 → 최신 메시지 바 표시
                latestMessage != null && showScrollToBottom -> newMessageBarData = latestMessage
            }
        }
        previousLatestKey.value = latestKey
    }

    // 맨 밑으로 스크롤하면 새 메시지 바 숨기기
    LaunchedEffect(showScrollToBottom) {
        if (!showScrollToBottom) {
            newMessageBarData = null
        }
    }

    val showNewMessageBar = newMessageBarData != null
    val unreadSeparatorIndex = remember(
        state.messages,
        state.unreadStartMessageId,
        state.entryUnreadCount
    ) {
        findUnreadSeparatorIndex(
            messages = state.messages,
            boundaryMessageId = state.unreadStartMessageId,
            entryUnreadCount = state.entryUnreadCount
        )
    }

    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoadingMore,
        hasMore = state.hasMore,
        onLoadMore = { onIntent(ChatRoomIntent.LoadMoreMessages) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            when {
                // 캐시 확정 전엔 아무것도 안 그림
                !state.messagesResolved && state.messages.isEmpty() -> {}

                // 첫 로드 실패
                state.error != null && state.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topBarHeight, bottom = bottomBarHeight),
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
                                onClick = { onIntent(ChatRoomIntent.Retry) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.bounceClick()
                            ) {
                                Text("재시도")
                            }
                        }
                    }
                }

                (state.isLoading || state.otherUser == null) && state.messages.isEmpty() -> {
                    // 캐시 없음 확정 후 콜드 로딩만
                    CenteredLoadingIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topBarHeight, bottom = bottomBarHeight),
                        size = LoadingIndicatorSize.Large
                    )
                }

                // 빈 상태
                state.messages.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topBarHeight, bottom = bottomBarHeight),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ChatRoomProfileHeader(
                            otherUser = state.otherUser,
                            onProfileClick = onUserClick
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "메시지를 보내 대화를 시작해보세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    }
                }

                // 메시지 목록
                else -> {
                    // spacer 계산:
                    // 다 보이면 viewport - 콘텐츠합
                    // 넘치면 spacer = 0
                    val spacerHeightPx by remember {
                        derivedStateOf {
                            val info = listState.layoutInfo
                            if (info.totalItemsCount == 0) return@derivedStateOf 0
                            val allVisible = info.visibleItemsInfo.size == info.totalItemsCount
                            if (!allVisible) return@derivedStateOf 0
                            val viewportInside = info.viewportSize.height -
                                info.beforeContentPadding - info.afterContentPadding
                            val contentSum = info.visibleItemsInfo
                                .filter { it.key != "spacer" }
                                .sumOf { it.size }
                            (viewportInside - contentSum).coerceAtLeast(0)
                        }
                    }
                    val spacerHeight = with(density) { spacerHeightPx.toDp() }

                    // 짧은 채팅은 스페이서 측정까지 가림
                    // 긴 채팅은 처음부터 노출
                    var listRevealed by remember { mutableStateOf(state.hasMore) }
                    LaunchedEffect(state.hasMore) {
                        if (state.hasMore) {
                            listRevealed = true
                        } else {
                            var frames = 0
                            while (spacerHeightPx == 0 && frames < 5) {
                                withFrameNanos { }
                                frames++
                            }
                            listRevealed = true
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (listRevealed) 1f else 0f),
                            contentPadding = PaddingValues(
                                top = topBarHeight + 8.dp,
                                bottom = bottomBarHeight + 8.dp
                            ),
                            reverseLayout = true
                        ) {
                            itemsIndexed(
                                items = state.messages,
                                key = { _, message -> message.id },
                                contentType = { _, message -> message.chatMessageType }
                            ) { index, message ->
                                val messages = state.messages

                                // 마지막이거나 다음 메시지와 발신자·분이 다르면 시간 표시
                                val showTime = if (message.status != MessageStatus.SENT) {
                                    false
                                } else {
                                    val nextSent = (index - 1 downTo 0).asSequence()
                                        .map { messages[it] }
                                        .firstOrNull { it.status == MessageStatus.SENT }
                                    nextSent == null ||
                                        message.sender.id != nextSent.sender.id ||
                                        !message.createdAt.isSameMinute(nextSent.createdAt)
                                }

                                // 가장 오래된 메시지거나 이전과 날짜 다르면 구분선
                                val showDateSeparator = index == messages.lastIndex ||
                                    !message.createdAt.isSameDay(messages[index + 1].createdAt)

                                // 검색 모드에서 현재 메시지가 검색 결과에 포함되면 검색어 전달
                                val searchQueryForHighlight = if (state.isSearchMode &&
                                    state.searchResults.any { it.id == message.id }) {
                                    state.searchQuery
                                } else ""

                                Column {
                                    if (showDateSeparator) {
                                        DateSeparator(timestamp = message.createdAt)
                                    }

                                    if (index == unreadSeparatorIndex) {
                                        UnreadSeparator()
                                    }

                                    val fileUrl = message.mediaAttachments?.firstOrNull() ?: ""
                                    val downloadProgress = state.downloadingFiles[fileUrl]
                                    val isDownloading = downloadProgress != null

                                    MessageBubble(
                                        message = message,
                                        showTime = showTime,
                                        searchQuery = searchQueryForHighlight,
                                        isDownloading = isDownloading,
                                        downloadProgress = downloadProgress ?: 0f,
                                        onLongClick = {
                                            // 실패·전송 중은 서버에 없는 메시지라 삭제 대신 말풍선의 취소
                                            if (message.isFromMe && message.status == MessageStatus.SENT) {
                                                onIntent(ChatRoomIntent.ShowDeleteDialog(message))
                                            }
                                        },
                                        onImageClick = onImageClick,
                                        onVideoClick = onVideoClick,
                                        onProductClick = onProductClick,
                                        onPostClick = onPostClick,
                                        onLockedInfo = { onIntent(ChatRoomIntent.ShowLockedInfo(it)) },
                                        onFileClick = onFileDownload,
                                        onRetryFailed = {
                                            message.clientTempId?.let {
                                                onIntent(ChatRoomIntent.RetryFailedMessage(it))
                                            }
                                        },
                                        onCancelFailed = {
                                            message.clientTempId?.let {
                                                onIntent(ChatRoomIntent.CancelFailedMessage(it))
                                            }
                                        }
                                    )
                                }
                            }

                            if (state.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }

                            if (!state.hasMore && spacerHeight > 0.dp) {
                                item(key = "spacer") {
                                    Spacer(modifier = Modifier.height(spacerHeight))
                                }
                            }

                            if (!state.hasMore) {
                                item(key = "profile_header") {
                                    ChatRoomProfileHeader(
                                        otherUser = state.otherUser,
                                        onProfileClick = onUserClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    topBarHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            ChatRoomTopBar(
                otherUser = state.otherUser,
                otherUserLoaded = state.otherUserLoaded,
                hazeState = hazeState,
                backgroundColor = backgroundColor,
                onBackClick = {
                    if (state.isSearchMode) {
                        onIntent(ChatRoomIntent.ToggleSearchMode)
                    } else {
                        onIntent(ChatRoomIntent.NavigateBack)
                    }
                },
                onUserClick = onUserClick,
                onSearchClick = { onIntent(ChatRoomIntent.ToggleSearchMode) },
                onMoreClick = { onIntent(ChatRoomIntent.ShowProfileOptions) },
                showActions = state.messages.isNotEmpty(),
                isSearchMode = state.isSearchMode,
                searchQuery = state.searchQuery,
                onSearchQueryChange = { onIntent(ChatRoomIntent.SearchQueryChanged(it)) },
                onSearch = { onIntent(ChatRoomIntent.SearchMessages) },
                onClearSearch = { onIntent(ChatRoomIntent.ClearSearch) },
                searchFocusRequester = searchFocusRequester
            )
        }

        // 맨 아래로 버튼, 새 메시지 바 표시 중엔 숨김
        ScrollToBottomFAB(
            visible = showScrollToBottom && !state.isBlocked && !state.isBlockedByOther && !showNewMessageBar && previewNewMessageBar == null,
            onClick = {
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            },
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = bottomBarHeight)
        )

        // NewMessageBar
        val activeNewMessageBar = previewNewMessageBar ?: newMessageBarData
        NewMessageBar(
            visible = activeNewMessageBar != null && !state.isBlocked && !state.isBlockedByOther,
            messageType = activeNewMessageBar?.chatMessageType ?: ChatMessageType.TEXT,
            messageContent = activeNewMessageBar?.content,
            onClick = {
                newMessageBarData = null
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            },
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarHeight + 2.dp)
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            applyWindowInsets = false,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarHeight)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        bottomBarHeight = with(density) { coordinates.size.height.toDp() }
                    }
            ) {
                if (state.isBlocked) {
                    // 내가 차단한 상태
                    BlockedUserOverlay(
                        userName = state.otherUser?.displayName ?: state.otherUser?.username ?: "",
                        onUnblockClick = { onIntent(ChatRoomIntent.ShowUnblockConfirmDialog) }
                    )
                } else if (state.isBlockedByOther) {
                    // 상대가 나를 차단한 상태
                    BlockedByOtherOverlay()
                } else {
                    MessageInputBar(
                        value = state.messageInput,
                        onValueChange = { onIntent(ChatRoomIntent.MessageInputChanged(it)) },
                        onSendClick = { onIntent(ChatRoomIntent.SendMessage) },
                        isSending = state.isSending,
                        hazeState = hazeState,
                        backgroundColor = backgroundColor,
                        isSearchMode = state.isSearchMode,
                        // 시간순 위치
                        searchResultIndex = state.searchTotalCount -
                            state.searchResults.size + state.currentSearchIndex,
                        searchResultCount = state.searchTotalCount,
                        onPreviousResult = { onIntent(ChatRoomIntent.NavigateToPreviousResult) },
                        onNextResult = { onIntent(ChatRoomIntent.NavigateToNextResult) },
                        isAttachmentExpanded = state.isAttachmentExpanded,
                        onAttachmentToggle = { onIntent(ChatRoomIntent.ToggleAttachmentExpanded) },
                        onImageClick = onImagePickerClick,
                        onVideoClick = onVideoPickerClick,
                        onFileClick = onFilePickerClick,
                        selectedImages = state.selectedImages,
                        selectedVideo = state.selectedVideo,
                        selectedFile = state.selectedFile,
                        onRemoveImage = { index -> onIntent(ChatRoomIntent.RemoveImage(index)) },
                        onRemoveVideo = { onIntent(ChatRoomIntent.RemoveVideo) },
                        onRemoveFile = { onIntent(ChatRoomIntent.RemoveFile) },
                        canSend = state.canSend
                    )
                }
            }
        }
    }
}

private fun findUnreadSeparatorIndex(
    messages: List<Message>,
    boundaryMessageId: Int?,
    entryUnreadCount: Int
): Int? {
    if (boundaryMessageId == null || entryUnreadCount <= 0) return null

    val boundaryIndex = messages.indexOfFirst { it.id == boundaryMessageId }
    if (boundaryIndex <= 0) return null

    return boundaryIndex - 1
}
