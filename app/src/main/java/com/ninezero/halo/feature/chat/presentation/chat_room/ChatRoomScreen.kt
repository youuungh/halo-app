package com.ninezero.halo.feature.chat.presentation.chat_room

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ninezero.halo.core.fcm.ActiveChatRoomTracker
import com.ninezero.halo.core.media.MediaUtils
import com.ninezero.halo.core.model.ReportTargetType
import com.ninezero.halo.core.ui.components.bottomsheet.ProfileOptionBottomSheet
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.list.InfiniteScrollTrigger
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.ListLoadMoreIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.overlay.CenterToast
import com.ninezero.halo.core.ui.components.post.LocalReportRequester
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.viewer.MediaViewerOverlay
import com.ninezero.halo.core.ui.components.viewer.MediaViewerState
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaItem
import com.ninezero.halo.core.ui.components.viewer.ViewerMediaType
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
import kotlinx.coroutines.delay
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
        val roomId = state.roomId
        // RESUMED 중 승격이면 바로 등록
        if (roomId > 0 && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            ActiveChatRoomTracker.enter(roomId)
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.onIntent(ChatRoomIntent.ScreenResumed)
                    ActiveChatRoomTracker.enter(roomId)
                }
                Lifecycle.Event.ON_PAUSE -> ActiveChatRoomTracker.leave(roomId)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            ActiveChatRoomTracker.leave(roomId)
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

    val searchHighlight = remember { Animatable(0f) }
    var searchHighlightId by remember { mutableStateOf<Int?>(null) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ChatRoomSideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is ChatRoomSideEffect.ScrollToBottom -> {
                coroutineScope.launch { listState.animateScrollToBottom() }
            }
            is ChatRoomSideEffect.HideKeyboard -> {
                focusManager.clearFocus()
            }
            is ChatRoomSideEffect.ScrollToMessage -> {
                val index = state.messages.indexOfFirst { it.id == sideEffect.messageId }
                if (index >= 0) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(index)
                        // 멈춘 뒤 그 줄을 잠깐 강조
                        searchHighlightId = sideEffect.messageId
                        searchHighlight.snapTo(1f)
                        delay(400)
                        searchHighlight.animateTo(0f, animationSpec = tween(durationMillis = 1200))
                    }
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
            is ChatRoomSideEffect.NavigateToPostDetail -> {
                focusManager.clearFocus()
                onNavigateToPostDetail(sideEffect.postId)
            }
            is ChatRoomSideEffect.NavigateToProductDetail -> {
                focusManager.clearFocus()
                onNavigateToProductDetail(sideEffect.productId)
            }
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
        // 묶어 보낸 사진은 한 메시지라 함께 삭제
        val photoCount = state.messageToDelete
            ?.takeIf { it.chatMessageType == ChatMessageType.IMAGE }
            ?.mediaAttachments?.size ?: 0
        ConfirmDialog(
            title = "메시지 삭제",
            message = if (photoCount > 1) "사진 ${photoCount}장을 모두 삭제하시겠습니까?" else "이 메시지를 삭제하시겠습니까?",
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
        searchHighlightId = searchHighlightId,
        searchHighlightAlpha = { searchHighlight.value },
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
            state.otherUser?.let {
                focusManager.clearFocus()
                onNavigateToUserProfile(it.id)
            }
        },
        onProductClick = { productId, canAccess ->
            viewModel.onIntent(ChatRoomIntent.ProductLinkClicked(productId, canAccess))
        },
        onPostClick = { postId, canAccess ->
            viewModel.onIntent(ChatRoomIntent.PostLinkClicked(postId, canAccess))
        },
        onImageClick = { imageUrls, index ->
            focusManager.clearFocus()
            mediaViewerState = MediaViewerState(
                isVisible = true,
                items = imageUrls.map { ViewerMediaItem(url = it) },
                initialIndex = index
            )
        },
        onVideoClick = { videoUrl, thumbnailUrl ->
            focusManager.clearFocus()
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
    searchHighlightId: Int? = null,
    searchHighlightAlpha: () -> Float = { 0f },
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
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var topBarHeight by rememberSaveableDp()
    var bottomBarHeight by rememberSaveableDp()

    // 맨 아래에서 올라온 거리로 판단, 깜빡임 방지
    // reverseLayout이라 index > 0이면 맨 아래가 화면 밖
    val scrollToBottomThresholdPx = with(density) { 200.dp.roundToPx() }
    val showScrollToBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > scrollToBottomThresholdPx
        }
    }

    // 위로 올려 보던 중 도착한 새 메시지
    var newMessageBarData by remember { mutableStateOf<Message?>(null) }
    // 맨 밑 메시지 키
    val latestKey = state.messages.firstOrNull()?.listKey
    val previousLatestKey = remember { mutableStateOf(latestKey) }

    // 맨 밑 메시지가 바뀔 때만 실행
    LaunchedEffect(latestKey) {
        if (latestKey != null && latestKey != previousLatestKey.value) {
            val latestMessage = state.messages.firstOrNull()
            // 스크롤 전에 기록, 효과가 재시작돼도 남음
            val hadPrevious = previousLatestKey.value != null
            previousLatestKey.value = latestKey
            when {
                // 내가 보내거나 다시 보낸 메시지는 맨 밑으로
                latestMessage?.isFromMe == true -> listState.animateScrollToBottom()
                // 위로 올려 둔 상태면 새 메시지 바만 띄움
                latestMessage != null && showScrollToBottom -> newMessageBarData = latestMessage
                // 맨 밑에서 보던 중이면 따라 내려감
                // 첫 로드는 안 읽은 위치를 지키려고 제외
                latestMessage != null && hadPrevious -> listState.animateScrollToBottom()
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
    // 새 메시지 바가 뜬 동안만 블러를 바 위까지 늘림
    val newMessageBarVisible = (previewNewMessageBar ?: newMessageBarData) != null && !state.isBlocked && !state.isBlockedByOther
    val inputBlurExtension by animateDpAsState(
        targetValue = if (newMessageBarVisible) 52.dp else 0.dp,
        label = "input_blur_extension"
    )
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

    // 첫 페이지 전엔 이전 페이지 로드 보류
    InfiniteScrollTrigger(
        listState = listState,
        isLoading = state.isLoadingMore || state.isEntryLoading,
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
                state.entryPhase == EntryPhase.CACHE_CHECKING && state.messages.isEmpty() -> {}

                // 첫 로드 실패
                state.entryPhase == EntryPhase.FAILED && state.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topBarHeight, bottom = bottomBarHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        ListErrorState(
                            title = "메시지를 불러오지 못했습니다",
                            reason = state.error,
                            onRetry = { onIntent(ChatRoomIntent.Retry) }
                        )
                    }
                }

                (state.entryPhase == EntryPhase.LOADING || state.otherUser == null) && state.messages.isEmpty() -> {
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
                    // 다 보이면 남는 높이, 넘치면 0
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
                                key = { _, message -> message.listKey },
                                contentType = { _, message -> message.chatMessageType }
                            ) { index, message ->
                                MessageListItem(
                                    index = index,
                                    message = message,
                                    state = state,
                                    unreadSeparatorIndex = unreadSeparatorIndex,
                                    highlightProgress = if (message.id == searchHighlightId) searchHighlightAlpha else null,
                                    onIntent = onIntent,
                                    onImageClick = onImageClick,
                                    onVideoClick = onVideoClick,
                                    onProductClick = onProductClick,
                                    onPostClick = onPostClick,
                                    onFileDownload = onFileDownload
                                )
                            }

                            if (state.isLoadingMore) {
                                item {
                                    ListLoadMoreIndicator()
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
                onMoreClick = {
                    focusManager.clearFocus(force = true)
                    onIntent(ChatRoomIntent.ShowProfileOptions)
                },
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
            visible = showScrollToBottom && !state.isBlocked && !state.isBlockedByOther && !showNewMessageBar && previewNewMessageBar == null &&
                !state.isSearchMode,  // 검색 결과 이동과 헷갈려 숨김
            onClick = {
                coroutineScope.launch {
                    listState.animateScrollToBottom()
                }
            },
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = bottomBarHeight)
        )

        // 새 메시지 바
        val activeNewMessageBar = previewNewMessageBar ?: newMessageBarData
        NewMessageBar(
            visible = newMessageBarVisible,
            messageType = activeNewMessageBar?.chatMessageType ?: ChatMessageType.TEXT,
            messageContent = activeNewMessageBar?.content,
            onClick = {
                newMessageBarData = null
                coroutineScope.launch {
                    listState.animateScrollToBottom()
                }
            },
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarHeight)
                .zIndex(1f)  // 입력 바 블러가 바 위까지 늘어나도 그 위에 그려짐
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
                        blurTopExtension = { inputBlurExtension },
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

/** 메시지 한 줄 */
@Composable
private fun MessageListItem(
    index: Int,
    message: Message,
    state: ChatRoomState,
    unreadSeparatorIndex: Int?,
    highlightProgress: (() -> Float)?,  // 검색으로 이동한 메시지만
    onIntent: (ChatRoomIntent) -> Unit,
    onImageClick: (List<String>, Int) -> Unit,
    onVideoClick: (String, String?) -> Unit,
    onProductClick: (Int, Boolean) -> Unit,
    onPostClick: (Int, Boolean) -> Unit,
    onFileDownload: (String, String) -> Unit
) {
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

    // 검색 결과인 메시지만 검색어 강조
    val searchHighlightColor = MaterialTheme.colorScheme.primary
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
            modifier = if (highlightProgress != null) {
                Modifier.drawBehind {
                    val alpha = highlightProgress() * 0.08f
                    if (alpha > 0f) drawRect(searchHighlightColor.copy(alpha = alpha))
                }
            } else {
                Modifier
            },
            showTime = showTime,
            searchQuery = searchQueryForHighlight,
            isCurrentSearchMatch = searchQueryForHighlight.isNotEmpty() &&
                state.searchResults.getOrNull(state.currentSearchIndex)?.id == message.id,
            isDownloading = isDownloading,
            downloadProgress = downloadProgress ?: 0f,
            onLongClick = {
                // 보낸 메시지만 삭제, 실패·전송 중은 말풍선에서 취소
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
            },
            onFileDownloadCancel = { onIntent(ChatRoomIntent.CancelFileDownload(fileUrl)) }
        )
    }
}

/** 전송 전후로 같은 말풍선 키 */
private val Message.listKey: String
    get() = clientTempId ?: "id_$id"

// animateScrollToItem은 평균 높이로 추정해 중간에 멈춤
// 맨 밑으로 옮겨 실제 거리를 잰 뒤 그만큼만 내려감
private suspend fun LazyListState.animateScrollToBottom() {
    val info = layoutInfo
    val start = info.visibleItemsInfo.firstOrNull()  // reverseLayout이라 화면 맨 아래 메시지
    if (start == null || info.visibleItemsInfo.any { it.index == 0 }) {
        animateScrollToItem(0)
        return
    }
    val viewport = info.viewportSize.height - info.beforeContentPadding - info.afterContentPadding
    scrollToItem(0)
    // 가까우면 그 자리에서, 멀면 한 화면 위에서 출발
    val startAtBottom = layoutInfo.visibleItemsInfo.firstOrNull { it.key == start.key }
    val distance = startAtBottom?.let { it.offset - start.offset } ?: viewport
    val moved = scrollBy(distance.toFloat())  // reverseLayout이라 양수 = 위로
    animateScrollBy(-moved)
    scrollToItem(0)  // 내려가는 동안 높이가 바뀐 메시지가 있으면 남은 만큼 맞춤
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
