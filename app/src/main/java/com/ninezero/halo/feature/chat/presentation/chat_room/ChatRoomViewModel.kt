package com.ninezero.halo.feature.chat.presentation.chat_room

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ninezero.halo.core.fcm.ActiveChatRoomTracker
import com.ninezero.halo.core.model.UserSummary
import com.ninezero.halo.core.ui.components.snackbar.SnackbarState
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.MediaUtils
import com.ninezero.halo.feature.chat.domain.cache.ChatMessageMemoryCache
import com.ninezero.halo.feature.chat.domain.model.ChatDownloadEvent
import com.ninezero.halo.feature.chat.domain.model.ChatEvent
import com.ninezero.halo.feature.chat.domain.model.ChatMessageType
import com.ninezero.halo.feature.chat.domain.model.ChatUploadEvent
import com.ninezero.halo.feature.chat.domain.model.Message
import com.ninezero.halo.feature.chat.domain.model.MessageStatus
import com.ninezero.halo.feature.chat.domain.repository.ChatEventRepository
import com.ninezero.halo.feature.chat.domain.repository.ChatMediaRepository
import com.ninezero.halo.feature.chat.domain.repository.ChatRepository
import com.ninezero.halo.feature.feed.domain.event.BlockEventBus
import com.ninezero.halo.feature.feed.domain.event.FollowEvent
import com.ninezero.halo.feature.feed.domain.event.FollowEventBus
import com.ninezero.halo.feature.feed.domain.event.SubscriptionEvent
import com.ninezero.halo.feature.feed.domain.event.SubscriptionEventBus
import com.ninezero.halo.feature.feed.domain.repository.BlockRepository
import com.ninezero.halo.feature.feed.domain.usecase.BlockUserUseCase
import com.ninezero.halo.feature.notification.fcm.TrayNotifications
import com.ninezero.halo.feature.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.blockingIntent
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.orbitContainer
import timber.log.Timber
import javax.inject.Inject

private const val SEARCH_PAGE_SIZE = 50

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val blockRepository: BlockRepository,
    private val blockUserUseCase: BlockUserUseCase,
    private val blockEventBus: BlockEventBus,
    private val followEventBus: FollowEventBus,
    private val subscriptionEventBus: SubscriptionEventBus,
    private val chatEventRepository: ChatEventRepository,
    private val chatMediaRepository: ChatMediaRepository,
    private val profileRepository: ProfileRepository,
    private val messageCache: ChatMessageMemoryCache,
    private val trayNotifications: TrayNotifications,
    @param:ApplicationContext private val context: Context
) : ViewModel(), OrbitContainerHost<ChatRoomState, ChatRoomState, ChatRoomSideEffect> {

    @Volatile
    private var roomId: Int = savedStateHandle.get<String>("roomId")?.toIntOrNull() ?: 0
    private val targetUserId: Int = savedStateHandle.get<Int>("targetUserId") ?: 0

    private val seededOtherUser: UserSummary? = run {  // draft 헤더 즉시 표시용, 로드 후 덮어씀
        fun arg(key: String) = savedStateHandle.get<String>(key).orEmpty()

        val username = arg("targetUsername")
        val displayName = arg("targetDisplayName")
        val avatarUrl = arg("targetAvatarUrl")
        val role = savedStateHandle.get<String>("targetRole") ?: "USER"
        if (targetUserId > 0 && (displayName.isNotBlank() || username.isNotBlank())) {
            UserSummary(
                id = targetUserId,
                username = username,
                displayName = displayName.ifBlank { null },
                avatarUrl = avatarUrl.ifBlank { null },
                role = role
            )
        } else null
    }

    private var entryUnreadInitialized = false

    private val isDraftMode: Boolean
        get() = roomId == 0 && targetUserId > 0

    private val seededMessages: List<Message> = messageCache.get(roomId).orEmpty()  // 재진입 첫 프레임용 캐시 시드

    override val container: OrbitContainer<ChatRoomState, ChatRoomState, ChatRoomSideEffect> =
        orbitContainer(
            ChatRoomState(
                roomId = roomId,
                otherUser = seededOtherUser,
                messages = seededMessages
            )
        )

    private var observeMessagesJob: Job? = null  // roomId 갱신 시 재시작

    init {
        // draft 모드는 기존 방 조회, 없으면 첫 전송 때 생성
        if (isDraftMode) {
            initDraftMode()
        } else {
            failStuckTextMessages()
            loadChatRoom()
            loadMessages(isEntry = true)
        }
        observeMessagesFromDb()
        observeRoomHeaderFromCache()
        observeWebSocket()
        observeUploadResults()
        observeDownloadResults()
        observeBlockEvents()
        observeFollowEvents()
        observeSubscriptionEvents()
    }

    private fun failStuckTextMessages() = intent {
        chatRepository.failStuckTextMessages(roomId)
    }

    private fun observeMessagesFromDb() {
        observeMessagesJob?.cancel()
        if (roomId == 0) return
        observeMessagesJob = chatRepository.observeMessages(roomId)
            .onEach { messages ->
                // 재진입 즉시 표시용 캐시 갱신
                if (messages.isNotEmpty()) messageCache.put(roomId, messages)
                intent {
                    reduce {
                        // 빈 emit도 반영
                        state.copy(
                            messages = messages,
                            messagesResolved = true,
                            // 로딩 해제는 실제 메시지 도착 시점에 위임
                            isLoading = if (messages.isNotEmpty()) false else state.isLoading
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeRoomHeaderFromCache() {  // 네트워크 로드 후엔 덮지 않음
        if (roomId == 0) return
        chatRepository.observeChatRoom(roomId)
            .onEach { chat ->
                if (chat != null) {
                    intent {
                        if (!state.otherUserLoaded) {
                            reduce { state.copy(otherUser = chat.otherUser) }
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeBlockEvents() {  // 같은 상대면 차단 상태 재조회
        blockEventBus.events
            .onEach { event ->
                val targetId = container.stateFlow.value.otherUser?.id ?: return@onEach
                if (event.userId == targetId) {
                    loadBlockStatus(targetId)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeFollowEvents() {  // 같은 상대면 isFollowing 갱신
        followEventBus.events
            .onEach { event ->
                intent {
                    val other = state.otherUser ?: return@intent
                    if (event.userId != other.id) return@intent
                    val newIsFollowing = event is FollowEvent.Followed
                    if (other.isFollowing == newIsFollowing) return@intent
                    reduce {
                        state.copy(otherUser = other.copy(isFollowing = newIsFollowing))
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSubscriptionEvents() {  // 구독 시 잠금 카드 언락 위해 재동기화
        subscriptionEventBus.events
            .onEach { event ->
                if (event is SubscriptionEvent.Subscribed) {
                    intent {
                        val affectsRoom = state.messages.any {
                            it.postInfo?.creatorId == event.creatorId ||
                                    it.productInfo?.creatorId == event.creatorId
                        }
                        if (affectsRoom) loadMessages(isEntry = false)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun initDraftMode() = intent {  // 기존 방 있으면 정상 모드
        chatRepository.findChatRoomWithUser(targetUserId)
            .onSuccess { existingChat ->
                if (existingChat != null) {
                    // 기존 채팅방 발견: 정상 모드로 전환
                    roomId = existingChat.id
                    // 방 진입이라 앱 종료로 멈춘 전송 중 말풍선은 실패로
                    chatRepository.failStuckTextMessages(existingChat.id)
                    // 첫 ON_RESUME은 roomId 0이라 한 번 더 정리
                    clearRoomTrayNotifications()
                    reduce {
                        state.copy(
                            roomId = existingChat.id,
                            otherUser = existingChat.otherUser,
                            otherUserLoaded = true
                        )
                    }
                    observeMessagesFromDb()  // roomId 갱신 후 observe 재시작
                    loadBlockStatus(existingChat.otherUser.id)
                    loadMessages(isEntry = true)
                } else {
                    // 채팅방 없음이면 빈 draft 확정, 로딩 해제
                    reduce { state.copy(messagesResolved = true) }
                    loadOtherUserProfile()
                }
            }
            .onFailure { error ->
                Timber.e(error, "기존 채팅방 조회 실패 — draft로 대체")
                reduce { state.copy(messagesResolved = true) }
                loadOtherUserProfile()
            }
    }

    private fun loadOtherUserProfile() = intent {
        profileRepository.getUserProfile(targetUserId)
            .onSuccess { profile ->
                val summary = UserSummary(
                    id = profile.userId,
                    username = profile.username.ifBlank { profile.displayName },
                    displayName = profile.displayName,
                    avatarUrl = profile.avatarUrl,
                    avatarThumbUrl = profile.avatarThumbUrl,
                    role = profile.role,
                    followerCount = profile.followerCount,
                    followingCount = profile.followingCount,
                    postCount = profile.postCount,
                    isFollowing = profile.isFollowing
                )
                reduce { state.copy(otherUser = summary, otherUserLoaded = true) }
                loadBlockStatus(summary.id)
            }
            .onFailure { error ->
                Timber.e(error, "draft 상대 사용자 로드 실패")
            }
    }

    fun onIntent(intent: ChatRoomIntent) {
        when (intent) {
            is ChatRoomIntent.ScreenResumed -> onScreenResumed()
            is ChatRoomIntent.LoadMoreMessages -> loadMoreMessages()
            is ChatRoomIntent.Retry -> loadMessages(isEntry = true)
            is ChatRoomIntent.MessageInputChanged -> messageInputChanged(intent.text)
            is ChatRoomIntent.SendMessage -> sendMessage()
            is ChatRoomIntent.RetryFailedMessage -> retryFailedMessage(intent.clientTempId)
            is ChatRoomIntent.CancelFailedMessage -> cancelFailedMessage(intent.clientTempId)
            is ChatRoomIntent.ShowDeleteDialog -> showDeleteDialog(intent.message)
            is ChatRoomIntent.DismissDeleteDialog -> dismissDeleteDialog()
            is ChatRoomIntent.ConfirmDeleteMessage -> confirmDeleteMessage()
            is ChatRoomIntent.NavigateBack -> navigateBack()
            is ChatRoomIntent.PostLinkClicked -> postLinkClicked(intent.postId, intent.canAccess)
            is ChatRoomIntent.ProductLinkClicked -> productLinkClicked(
                intent.productId,
                intent.canAccess
            )

            is ChatRoomIntent.ToggleSearchMode -> toggleSearchMode()
            is ChatRoomIntent.SearchQueryChanged -> searchQueryChanged(intent.query)
            is ChatRoomIntent.SearchMessages -> searchMessages()
            is ChatRoomIntent.ClearSearch -> clearSearch()
            is ChatRoomIntent.NavigateToPreviousResult -> navigateToPreviousResult()
            is ChatRoomIntent.NavigateToNextResult -> navigateToNextResult()
            // 차단
            is ChatRoomIntent.ShowProfileOptions -> showProfileOptions()
            is ChatRoomIntent.DismissProfileOptions -> dismissProfileOptions()
            is ChatRoomIntent.ShowBlockConfirmDialog -> showBlockConfirmDialog()
            is ChatRoomIntent.DismissBlockConfirmDialog -> dismissBlockConfirmDialog()
            is ChatRoomIntent.ShowUnblockConfirmDialog -> showUnblockConfirmDialog()
            is ChatRoomIntent.DismissUnblockConfirmDialog -> dismissUnblockConfirmDialog()
            is ChatRoomIntent.ConfirmBlockUser -> confirmBlockUser()
            is ChatRoomIntent.ConfirmUnblockUser -> confirmUnblockUser()
            is ChatRoomIntent.ShowLockedInfo -> showLockedInfo(intent.message)
            is ChatRoomIntent.SnackbarDismissed -> dismissSnackbar()
            // 첨부
            is ChatRoomIntent.ToggleAttachmentExpanded -> toggleAttachmentExpanded()
            is ChatRoomIntent.AddImages -> addImages(intent.uris)
            is ChatRoomIntent.RemoveImage -> removeImage(intent.index)
            is ChatRoomIntent.SetVideo -> setVideo(intent.uri)
            is ChatRoomIntent.RemoveVideo -> removeVideo()
            is ChatRoomIntent.SetFile -> setFile(intent.uri)
            is ChatRoomIntent.RemoveFile -> removeFile()
            is ChatRoomIntent.DownloadFile -> downloadFile(intent.fileUrl, intent.fileName)
        }
    }

    private fun loadChatRoom() = intent {
        chatRepository.getChatRoom(roomId)
            .onSuccess { chat ->
                reduce { state.copy(otherUser = chat.otherUser, otherUserLoaded = true) }
                loadBlockStatus(chat.otherUser.id)
            }
            .onFailure { error ->
                Timber.e(error, "채팅방 로드 실패")
            }
    }

    /** 양쪽 차단 관계 로드 */
    private fun loadBlockStatus(userId: Int) = intent {
        blockRepository.getBlockRelation(userId)
            .onSuccess { relation ->
                reduce {
                    state.copy(
                        isBlocked = relation.isBlockedByMe,
                        isBlockedByOther = relation.isBlockedByOther
                    )
                }
            }
    }


    // 옛 캐시 비우기는 진입 때만
    private fun loadMessages(isEntry: Boolean) = intent {
        val pageSize = Constants.DEFAULT_PAGE_SIZE * 2

        // 메시지 목록은 DB 관찰이 갱신
        if (state.messages.isEmpty()) {
            reduce { state.copy(isLoading = true, error = null) }
        }

        chatRepository.getMessages(
            roomId = roomId,
            limit = pageSize,
            beforeMessageId = null
        ).onSuccess { result ->
            if (isEntry) {
                chatRepository.cacheLatestMessages(roomId, result.messages)
            } else {
                chatRepository.cacheMessages(result.messages)
            }
            reduce {
                state.copy(
                    // 빈 방만 즉시 해제, 그 외는 DB 관찰이 해제
                    isLoading = if (result.messages.isEmpty()) false else state.isLoading,
                    hasMore = result.hasNext,
                    error = null
                )
            }
            if (!entryUnreadInitialized) {
                // DB 반영 전일 수 있어 먼저 합쳐 경계 계산
                val oldestFetchedId = result.messages.minOfOrNull { it.id } ?: Int.MAX_VALUE
                reduce {
                    val kept = if (isEntry) {
                        state.messages.filter { it.status != MessageStatus.SENT || it.id >= oldestFetchedId }
                    } else {
                        state.messages
                    }
                    state.copy(
                        messages = (result.messages + kept)
                            .distinctBy { it.id }
                            .sortedByDescending { it.createdAt }
                    )
                }
                initEntryUnread(result.lastReadMessageId)
                entryUnreadInitialized = true
            }
            if (isRoomVisible()) markAsRead()
        }.onFailure { error ->
            if (state.messages.isEmpty()) {
                reduce {
                    state.copy(
                        isLoading = false,
                        error = error.message ?: "메시지를 불러올 수 없습니다"
                    )
                }
            } else {
                reduce { state.copy(isLoading = false) }
            }
        }
    }


    // 복귀마다 트레이 알림 정리와 읽음 처리
    private fun onScreenResumed() {
        clearRoomTrayNotifications()
        // 첫 진입은 조회 뒤에 읽음, 안 읽음 경계 보존
        if (roomId > 0 && entryUnreadInitialized) markAsRead()
    }

    private fun clearRoomTrayNotifications() {
        if (roomId > 0) trayNotifications.cancelChatRoom(roomId)
    }

    private fun loadMoreMessages() = intent {
        if (state.isLoadingMore || !state.hasMore || state.messages.isEmpty()) return@intent

        val oldestMessageId = state.messages.oldestServerMessageId() ?: return@intent
        reduce { state.copy(isLoadingMore = true) }

        chatRepository.getMessages(
            roomId = roomId,
            limit = Constants.DEFAULT_PAGE_SIZE,
            beforeMessageId = oldestMessageId
        ).onSuccess { result ->
            chatRepository.cacheMessages(result.messages)
            reduce {
                state.copy(
                    isLoadingMore = false,
                    hasMore = result.hasNext
                )
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isLoadingMore = false,
                    snackbarState = SnackbarState("더 불러오기 실패")
                )
            }
        }
    }

    private fun messageInputChanged(text: String) = blockingIntent {
        reduce { state.copy(messageInput = text) }
    }

    private fun retryFailedMessage(clientTempId: String) = intent {
        val message = state.messages.find { it.clientTempId == clientTempId }
        val type = message?.chatMessageType
        val localUris = message?.mediaAttachments

        when {
            // 미디어는 워커 재enqueue
            type != null && type != ChatMessageType.TEXT && !localUris.isNullOrEmpty() -> {
                // 대기 기록·사본 재사용, 워커만 재시작
                val pendingId = clientTempId.toLongOrNull()
                if (pendingId != null && chatMediaRepository.retryUpload(pendingId)) {
                    chatRepository.markMediaMessageSending(clientTempId)
                    return@intent
                }

                // pending 레코드 없는 구버전 실패분은 새로 enqueue
                chatRepository.cancelFailedMessage(clientTempId)
                val handle = chatMediaRepository.enqueueUpload(
                    roomId = roomId,
                    content = message.content,
                    messageType = type,
                    mediaUris = localUris
                )
                val newClientTempId = handle.pendingMessageId.toString()
                when (type) {
                    ChatMessageType.IMAGE -> chatRepository.insertImageMessage(
                        roomId = roomId,
                        localUris = handle.mediaUris,
                        clientTempId = newClientTempId,
                        content = message.content,
                        mediaWidth = message.mediaWidth,
                        mediaHeight = message.mediaHeight
                    )

                    else -> chatRepository.insertMediaMessage(
                        roomId = roomId,
                        messageType = type,
                        localUri = handle.mediaUris.first(),
                        clientTempId = newClientTempId,
                        content = message.content,
                        fileName = message.fileName,
                        fileSize = message.fileSize,
                        duration = message.duration,
                        mediaWidth = message.mediaWidth,
                        mediaHeight = message.mediaHeight
                    )
                }
            }
            // 텍스트: API 직접 재호출
            else -> {
                chatRepository.retryFailedMessage(clientTempId)
                    .onFailure { error ->
                        reduce {
                            state.copy(snackbarState = SnackbarState(error.message ?: "재전송 실패"))
                        }
                    }
            }
        }
    }

    private fun cancelFailedMessage(clientTempId: String) = intent {
        chatRepository.cancelFailedMessage(clientTempId)
        // 미디어 업로드면 pending 레코드·스테이징 파일도 파기
        clientTempId.toLongOrNull()?.let { chatMediaRepository.discardUpload(it) }
    }

    private fun sendMessage() = intent {
        val content = state.messageInput.trim()
        val hasImages = state.selectedImages.isNotEmpty()
        val hasVideo = state.selectedVideo != null
        val hasFile = state.selectedFile != null

        if (content.isBlank() && !hasImages && !hasVideo && !hasFile) return@intent

        if (content.length > Constants.Content.MAX_MESSAGE_LENGTH) {
            reduce {
                state.copy(snackbarState = SnackbarState("메시지는 ${Constants.Content.MAX_MESSAGE_LENGTH}자를 초과할 수 없습니다"))
            }
            return@intent
        }

        reduce {
            state.copy(
                isSending = true,
                messageInput = "",
                isAttachmentExpanded = false
            )
        }
        postSideEffect(ChatRoomSideEffect.HideKeyboard)

        // 비운 뒤엔 나가도 방 생성·첫 전송 계속
        withContext<Unit>(NonCancellable) {
            // draft 모드: 첫 메시지 전송 직전에 채팅방을 생성함
            if (isDraftMode) {
                val created = chatRepository.createOrGetChatRoom(targetUserId)
                created.onFailure { error ->
                    reduce {
                        state.copy(
                            isSending = false,
                            messageInput = content,
                            snackbarState = SnackbarState(error.message ?: "채팅방을 열 수 없습니다")
                        )
                    }
                    return@withContext
                }
                val chat = created.getOrNull() ?: run {
                    reduce {
                        state.copy(
                            isSending = false,
                            messageInput = content,
                            snackbarState = SnackbarState("채팅방을 열 수 없습니다")
                        )
                    }
                    return@withContext
                }
                roomId = chat.id
                // 기존 방이면 멈춘 말풍선 실패 처리, 알림 정리
                chatRepository.failStuckTextMessages(chat.id)
                clearRoomTrayNotifications()
                reduce {
                    state.copy(
                        roomId = chat.id,
                        otherUser = chat.otherUser,
                        otherUserLoaded = true
                    )
                }
                observeMessagesFromDb()     // roomId 갱신 후 observe 재시작
                loadMessages(isEntry = true)  // 기존 방이면 히스토리도 로드
            }

            try {
                when {
                    hasImages -> {
                        sendImagesWithText(content.ifBlank { null })
                    }

                    hasVideo -> {
                        sendVideoWithText(content.ifBlank { null })
                    }

                    hasFile -> {
                        sendFileWithText(content.ifBlank { null })
                    }

                    else -> {
                        // 텍스트만 전송
                        chatRepository.sendMessage(
                            roomId = roomId,
                            content = content,
                            messageType = ChatMessageType.TEXT
                        ).onSuccess {
                            reduce { state.copy(isSending = false) }
                            postSideEffect(ChatRoomSideEffect.ScrollToBottom)
                        }.onFailure { error ->
                            // 복구는 실패 말풍선에서, 입력창은 비운 채
                            reduce {
                                state.copy(
                                    isSending = false,
                                    snackbarState = SnackbarState(error.message ?: "메시지 전송 실패")
                                )
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "메시지 전송 실패")
                reduce {
                    state.copy(
                        isSending = false,
                        messageInput = content,
                        snackbarState = SnackbarState("메시지 전송 중 오류가 발생했습니다")
                    )
                }
            }
        }
    }

    // 첨부

    private fun toggleAttachmentExpanded() = intent {
        reduce { state.copy(isAttachmentExpanded = !state.isAttachmentExpanded) }
    }

    // 첨부 고른 순번, 나중에 고른 종류가 남게
    // 검사를 통과한 고르기만 기록
    private val attachPickSeq = AtomicInteger(0)
    @Volatile private var lastImagePickSeq = 0
    @Volatile private var lastOtherPickSeq = 0  // 영상·파일

    private fun addImages(uris: List<Uri>) = intent {
        val pickSeq = attachPickSeq.incrementAndGet()

        val currentCount = state.selectedImages.size
        val availableSlots = MediaUtils.MAX_IMAGES_PER_MESSAGE - currentCount

        if (availableSlots <= 0) {
            postSideEffect(ChatRoomSideEffect.ShowToast("이미지는 최대 10개까지 첨부할 수 있습니다"))
            return@intent
        }

        val newUris = uris.take(availableSlots)
        val newAttachments = newUris.mapNotNull { uri ->
            val fileInfo = MediaUtils.getFileInfo(context, uri) ?: return@mapNotNull null

            val validation = MediaUtils.validateImage(fileInfo)
            if (!validation.isValid) {
                postSideEffect(
                    ChatRoomSideEffect.ShowToast(
                        validation.errorMessage ?: "유효하지 않은 이미지"
                    )
                )
                return@mapNotNull null
            }

            val thumbnail = MediaUtils.createImageThumbnail(context, uri)

            AttachmentState(
                uri = uri,
                fileName = fileInfo.fileName,
                fileSize = fileInfo.fileSize,
                thumbnail = thumbnail
            )
        }

        if (newAttachments.isEmpty()) return@intent
        // 썸네일을 만드는 사이 들어온 첨부까지 한 종류·상한 유지
        var overflowed = false
        reduce {
            if (lastOtherPickSeq > pickSeq) return@reduce state  // 그사이 영상·파일을 고름
            if (pickSeq > lastImagePickSeq) lastImagePickSeq = pickSeq
            val merged = state.selectedImages + newAttachments
            overflowed = merged.size > MediaUtils.MAX_IMAGES_PER_MESSAGE
            state.copy(
                selectedImages = merged.take(MediaUtils.MAX_IMAGES_PER_MESSAGE),
                selectedVideo = null,
                selectedFile = null,
                isAttachmentExpanded = false
            )
        }
        if (overflowed) postSideEffect(ChatRoomSideEffect.ShowToast("이미지는 최대 10개까지 첨부할 수 있습니다"))
    }

    private fun removeImage(index: Int) = intent {
        if (index in state.selectedImages.indices) {
            reduce {
                state.copy(
                    selectedImages = state.selectedImages.toMutableList().apply { removeAt(index) }
                )
            }
        }
    }

    private fun setVideo(uri: Uri) = intent {
        val pickSeq = attachPickSeq.incrementAndGet()

        val fileInfo = MediaUtils.getFileInfo(context, uri)
        if (fileInfo == null) {
            postSideEffect(ChatRoomSideEffect.ShowToast("비디오를 불러올 수 없습니다"))
            return@intent
        }

        val validation = MediaUtils.validateVideo(fileInfo)
        if (!validation.isValid) {
            postSideEffect(ChatRoomSideEffect.ShowToast(validation.errorMessage ?: "유효하지 않은 비디오"))
            return@intent
        }

        val thumbnail = MediaUtils.extractVideoThumbnail(context, uri)
        val duration = MediaUtils.getVideoDuration(context, uri)

        reduce {
            if (maxOf(lastImagePickSeq, lastOtherPickSeq) > pickSeq) return@reduce state  // 그사이 다른 첨부를 고름
            lastOtherPickSeq = pickSeq
            state.copy(
                selectedVideo = AttachmentState(
                    uri = uri,
                    fileName = fileInfo.fileName,
                    fileSize = fileInfo.fileSize,
                    thumbnail = thumbnail,
                    duration = duration
                ),
                selectedImages = emptyList(),  // 추출하는 사이 들어온 사진 제거
                selectedFile = null,
                isAttachmentExpanded = false
            )
        }
    }

    private fun removeVideo() = intent {
        reduce { state.copy(selectedVideo = null) }
    }

    private fun setFile(uri: Uri) = intent {
        val pickSeq = attachPickSeq.incrementAndGet()

        val fileInfo = MediaUtils.getFileInfo(context, uri)
        if (fileInfo == null) {
            postSideEffect(ChatRoomSideEffect.ShowToast("파일을 불러올 수 없습니다"))
            return@intent
        }

        val validation = MediaUtils.validateFile(fileInfo)
        if (!validation.isValid) {
            postSideEffect(ChatRoomSideEffect.ShowToast(validation.errorMessage ?: "유효하지 않은 파일"))
            return@intent
        }

        reduce {
            if (maxOf(lastImagePickSeq, lastOtherPickSeq) > pickSeq) return@reduce state  // 그사이 다른 첨부를 고름
            lastOtherPickSeq = pickSeq
            state.copy(
                selectedFile = AttachmentState(
                    uri = uri,
                    fileName = fileInfo.fileName,
                    fileSize = fileInfo.fileSize
                ),
                selectedImages = emptyList(),
                selectedVideo = null,
                isAttachmentExpanded = false
            )
        }
    }

    private fun removeFile() = intent {
        reduce { state.copy(selectedFile = null) }
    }

    private fun downloadFile(fileUrl: String, fileName: String) = intent {
        chatMediaRepository.enqueueFileDownload(fileUrl, fileName)
    }


    private suspend fun Syntax<ChatRoomState, ChatRoomSideEffect>.sendImagesWithText(content: String?) {
        val imageUris = state.selectedImages.map { it.uri.toString() }
        val (imgWidth, imgHeight) = if (state.selectedImages.size == 1) {
            MediaUtils.getImageDimensions(context, state.selectedImages.first().uri)
        } else null to null

        // 복사 중 방을 나가도 업로드 예약과 임시 메시지가 함께 남음
        withContext(NonCancellable) {
            val handle = chatMediaRepository.enqueueUpload(
                roomId = roomId,
                content = content,
                messageType = ChatMessageType.IMAGE,
                mediaUris = imageUris
            )

            // Optimistic: DB에 임시 메시지 insert
            // clientTempId는 워커가 찾는 대기 id
            // 스테이징 URI로 표시, 재시작 후에도 유효
            val clientTempId = handle.pendingMessageId.toString()
            chatRepository.insertImageMessage(
                roomId = roomId,
                localUris = handle.mediaUris,
                clientTempId = clientTempId,
                content = content,
                mediaWidth = imgWidth,
                mediaHeight = imgHeight
            )
        }

        reduce {
            state.copy(
                isSending = false,
                selectedImages = emptyList()
            )
        }
        postSideEffect(ChatRoomSideEffect.ScrollToBottom)
    }

    private suspend fun Syntax<ChatRoomState, ChatRoomSideEffect>.sendVideoWithText(content: String?) {
        val video = state.selectedVideo ?: return
        val (vidWidth, vidHeight) = MediaUtils.getVideoDimensions(context, video.uri)
        val videoDuration = MediaUtils.getVideoDuration(context, video.uri)

        // 복사 중 방을 나가도 업로드 예약과 임시 메시지가 함께 남음
        withContext(NonCancellable) {
            val handle = chatMediaRepository.enqueueUpload(
                roomId = roomId,
                content = content,
                messageType = ChatMessageType.VIDEO,
                mediaUris = listOf(video.uri.toString())
            )

            // 임시 영상 메시지, 스테이징 URI·파일명 보존
            val clientTempId = handle.pendingMessageId.toString()
            chatRepository.insertMediaMessage(
                roomId = roomId,
                messageType = ChatMessageType.VIDEO,
                localUri = handle.mediaUris.first(),
                clientTempId = clientTempId,
                content = content,
                fileName = video.fileName,
                fileSize = video.fileSize,
                duration = videoDuration,
                mediaWidth = vidWidth,
                mediaHeight = vidHeight
            )
        }

        reduce {
            state.copy(
                isSending = false,
                selectedVideo = null
            )
        }
        postSideEffect(ChatRoomSideEffect.ScrollToBottom)
    }

    private suspend fun Syntax<ChatRoomState, ChatRoomSideEffect>.sendFileWithText(content: String?) {
        val file = state.selectedFile ?: return

        // 복사 중 방을 나가도 업로드 예약과 임시 메시지가 함께 남음
        withContext(NonCancellable) {
            val handle = chatMediaRepository.enqueueUpload(
                roomId = roomId,
                content = content,
                messageType = ChatMessageType.FILE,
                mediaUris = listOf(file.uri.toString())
            )

            // 임시 파일 메시지, 스테이징 URI·파일명 보존
            val clientTempId = handle.pendingMessageId.toString()
            chatRepository.insertMediaMessage(
                roomId = roomId,
                messageType = ChatMessageType.FILE,
                localUri = handle.mediaUris.first(),
                clientTempId = clientTempId,
                content = content,
                fileName = file.fileName,
                fileSize = file.fileSize
            )
        }

        reduce {
            state.copy(
                isSending = false,
                selectedFile = null
            )
        }
        postSideEffect(ChatRoomSideEffect.ScrollToBottom)
    }

    private fun showDeleteDialog(message: Message) = intent {
        reduce {
            state.copy(
                messageToDelete = message,
                showDeleteDialog = true
            )
        }
    }

    private fun dismissDeleteDialog() = intent {
        reduce {
            state.copy(
                messageToDelete = null,
                showDeleteDialog = false
            )
        }
    }

    private fun confirmDeleteMessage() = intent {
        val messageId = state.messageToDelete?.id ?: return@intent

        // 낙관적 갱신
        reduce {
            state.copy(
                showDeleteDialog = false,
                messages = state.messages.map { message ->
                    if (message.id == messageId) message.copy(isDeleted = true)
                    else message
                }
            )
        }

        chatRepository.deleteMessage(messageId)
            .onSuccess {
                // 삭제 표시는 저장소가 반영
                reduce {
                    state.copy(
                        messageToDelete = null,
                        snackbarState = SnackbarState("메시지가 삭제되었습니다")
                    )
                }
            }
            .onFailure { error ->
                // 롤백
                reduce {
                    state.copy(
                        messages = state.messages.map { message ->
                            if (message.id == messageId) message.copy(isDeleted = false)
                            else message
                        },
                        messageToDelete = null,
                        snackbarState = SnackbarState(error.message ?: "삭제 실패")
                    )
                }
            }
    }

    private fun markAsRead() = intent {
        chatRepository.markMessagesAsRead(roomId)
    }

    private fun navigateBack() = intent {
        postSideEffect(ChatRoomSideEffect.NavigateBack)
    }

    private fun productLinkClicked(productId: Int, canAccess: Boolean) = intent {
        if (!canAccess) {
            postSideEffect(ChatRoomSideEffect.ShowCenterToast("멤버십 전용 상품입니다"))
            return@intent
        }
        postSideEffect(ChatRoomSideEffect.NavigateToProductDetail(productId))
    }

    private fun postLinkClicked(postId: Int, canAccess: Boolean) = intent {
        if (!canAccess) {
            postSideEffect(ChatRoomSideEffect.ShowCenterToast("접근할 수 없는 포스트입니다"))
            return@intent
        }
        postSideEffect(ChatRoomSideEffect.NavigateToPostDetail(postId))
    }

    private fun toggleSearchMode() = intent {
        val enteringSearchMode = !state.isSearchMode
        reduce {
            state.copy(
                isSearchMode = enteringSearchMode,
                searchQuery = "",
                searchResults = emptyList(),
                currentSearchIndex = 0,
                searchPage = 1,
                searchHasMore = false,
                searchTotalCount = 0
            )
        }
        if (enteringSearchMode) {
            postSideEffect(ChatRoomSideEffect.FocusSearchField)
        } else {
            postSideEffect(ChatRoomSideEffect.HideKeyboard)
        }
    }

    private fun searchQueryChanged(query: String) = blockingIntent {
        reduce { state.copy(searchQuery = query) }
    }

    private fun searchMessages() = intent {
        val query = state.searchQuery.trim()
        if (query.isBlank()) return@intent

        reduce { state.copy(isSearching = true) }

        chatRepository.searchMessages(
            roomId = roomId,
            query = query,
            page = 1,
            limit = SEARCH_PAGE_SIZE
        ).onSuccess { result ->
            // 최신순 응답을 뒤집어 저장, 0번이 가장 오래됨
            val ordered = result.items.reversed()
            reduce {
                state.copy(
                    searchResults = ordered,
                    currentSearchIndex = 0,
                    searchPage = 1,
                    searchHasMore = result.hasNext,
                    searchTotalCount = result.totalCount
                )
            }
            postSideEffect(ChatRoomSideEffect.HideKeyboard)

            if (ordered.isNotEmpty()) {
                // 가장 오래된 검색 결과까지 메시지 페이지 로드 후 점프 가능
                val oldestResultId = ordered.first().id
                loadPagesUntilMessage(oldestResultId)
                reduce { state.copy(isSearching = false) }
                postSideEffect(ChatRoomSideEffect.ScrollToMessage(oldestResultId))
            } else {
                reduce { state.copy(isSearching = false) }
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isSearching = false,
                    snackbarState = SnackbarState(error.message ?: "검색 실패")
                )
            }
            postSideEffect(ChatRoomSideEffect.HideKeyboard)
        }
    }

    private suspend fun Syntax<ChatRoomState, ChatRoomSideEffect>.loadMoreSearchResults(): Boolean {
        if (!state.searchHasMore || state.isSearching) return false
        val query = state.searchQuery.trim()
        if (query.isBlank()) return false

        reduce { state.copy(isSearching = true) }
        val nextPage = state.searchPage + 1
        val result = chatRepository.searchMessages(
            roomId = roomId,
            query = query,
            page = nextPage,
            limit = SEARCH_PAGE_SIZE
        )
        result.onSuccess { searchResult ->
            val olderItems = searchResult.items.reversed()
            val merged = olderItems + state.searchResults
            reduce {
                state.copy(
                    searchResults = merged,
                    searchPage = nextPage,
                    searchHasMore = searchResult.hasNext,
                    searchTotalCount = searchResult.totalCount,
                    currentSearchIndex = state.currentSearchIndex + olderItems.size,
                    isSearching = false
                )
            }
            if (olderItems.isNotEmpty()) {
                loadPagesUntilMessage(olderItems.first().id)
            }
        }.onFailure { error ->
            reduce {
                state.copy(
                    isSearching = false,
                    snackbarState = SnackbarState(error.message ?: "더 불러오기 실패")
                )
            }
        }
        return result.isSuccess
    }

    private fun clearSearch() = intent {
        reduce {
            state.copy(
                searchQuery = "",
                searchResults = emptyList(),
                currentSearchIndex = 0,
                searchPage = 1,
                searchHasMore = false,
                searchTotalCount = 0
            )
        }
    }

    private fun navigateToPreviousResult() = intent {
        val results = state.searchResults
        if (results.isEmpty()) return@intent

        // 시작 도달 시 더 오래된 페이지 prepend
        if (state.currentSearchIndex <= 0) {
            if (!loadMoreSearchResults()) return@intent
        }

        val updatedResults = state.searchResults
        val newIndex = (state.currentSearchIndex - 1).coerceAtLeast(0)
        if (newIndex >= updatedResults.size) return@intent

        reduce { state.copy(currentSearchIndex = newIndex) }
        postSideEffect(ChatRoomSideEffect.ScrollToMessage(updatedResults[newIndex].id))
    }

    private fun navigateToNextResult() = intent {
        val currentIndex = state.currentSearchIndex
        val results = state.searchResults
        // 페이지 1이 항상 끝이라 results 끝 도달
        if (results.isEmpty() || currentIndex >= results.size - 1) return@intent

        val newIndex = currentIndex + 1
        reduce { state.copy(currentSearchIndex = newIndex) }
        postSideEffect(ChatRoomSideEffect.ScrollToMessage(results[newIndex].id))
    }

    // 임시 음수 id는 서버 페이지 기준에서 제외
    private fun List<Message>.oldestServerMessageId(): Int? = lastOrNull { it.id > 0 }?.id

    private suspend fun Syntax<ChatRoomState, ChatRoomSideEffect>.loadPagesUntilMessage(
        messageId: Int
    ): Boolean {
        if (state.messages.any { it.id == messageId }) return true

        var hasMore = state.hasMore

        while (hasMore) {
            val oldestMessageId = state.messages.oldestServerMessageId() ?: break

            val apiResult = chatRepository.getMessages(
                roomId = roomId,
                limit = Constants.DEFAULT_PAGE_SIZE,
                beforeMessageId = oldestMessageId
            )

            apiResult.onSuccess { result ->
                chatRepository.cacheMessages(result.messages)

                reduce {
                    state.copy(
                        messages = (state.messages + result.messages).distinctBy { it.id },
                        hasMore = result.hasNext
                    )
                }
                hasMore = result.hasNext

                if (result.messages.any { it.id == messageId }) {
                    return true
                }
            }.onFailure {
                return false
            }
        }

        return state.messages.any { it.id == messageId }
    }

    private suspend fun Syntax<ChatRoomState, ChatRoomSideEffect>.initEntryUnread(
        lastReadMessageId: Int?
    ) {
        if (lastReadMessageId == null) {
            reduce {
                state.copy(
                    unreadStartMessageId = null,
                    entryUnreadCount = 0
                )
            }
            postSideEffect(ChatRoomSideEffect.ScrollToBottom)
            return
        }

        var boundaryIndex = state.messages.indexOfFirst { it.id == lastReadMessageId }
        if (boundaryIndex == -1 && state.hasMore) {
            loadPagesUntilMessage(lastReadMessageId)
            boundaryIndex = state.messages.indexOfFirst { it.id == lastReadMessageId }
        }

        if (boundaryIndex == -1) {
            reduce {
                state.copy(
                    unreadStartMessageId = null,
                    entryUnreadCount = 0
                )
            }
            postSideEffect(ChatRoomSideEffect.ScrollToBottom)
            return
        }

        val entryUnreadCount = state.messages
            .take(boundaryIndex)
            .count { !it.isFromMe }

        if (entryUnreadCount <= 0) {
            reduce {
                state.copy(
                    unreadStartMessageId = null,
                    entryUnreadCount = 0
                )
            }
            postSideEffect(ChatRoomSideEffect.ScrollToBottom)
            return
        }

        reduce {
            state.copy(
                unreadStartMessageId = lastReadMessageId,
                entryUnreadCount = entryUnreadCount
            )
        }
        postSideEffect(ChatRoomSideEffect.ScrollToUnread(lastReadMessageId))
    }

    private fun showLockedInfo(message: String) = intent {
        reduce { state.copy(snackbarState = SnackbarState(message)) }
    }

    private fun dismissSnackbar() = intent {
        reduce { state.copy(snackbarState = null) }
    }

    // 차단
    private fun showProfileOptions() = intent {
        reduce { state.copy(showProfileOptionsSheet = true) }
    }

    private fun dismissProfileOptions() = intent {
        reduce { state.copy(showProfileOptionsSheet = false) }
    }

    private fun showBlockConfirmDialog() = intent {
        reduce { state.copy(showProfileOptionsSheet = false, showBlockConfirmDialog = true) }
    }

    private fun dismissBlockConfirmDialog() = intent {
        reduce { state.copy(showBlockConfirmDialog = false) }
    }

    private fun showUnblockConfirmDialog() = intent {
        reduce { state.copy(showProfileOptionsSheet = false, showUnblockConfirmDialog = true) }
    }

    private fun dismissUnblockConfirmDialog() = intent {
        reduce { state.copy(showUnblockConfirmDialog = false) }
    }

    private fun confirmBlockUser() = intent {
        val userId = state.otherUser?.id ?: return@intent
        reduce { state.copy(showBlockConfirmDialog = false) }

        blockUserUseCase(userId)
            .onSuccess { isBlocked ->
                reduce { state.copy(isBlocked = isBlocked) }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "차단 실패"))
                }
            }
    }

    private fun confirmUnblockUser() = intent {
        val userId = state.otherUser?.id ?: return@intent
        reduce { state.copy(showUnblockConfirmDialog = false) }

        blockUserUseCase(userId)
            .onSuccess { isBlocked ->
                reduce {
                    state.copy(
                        isBlocked = isBlocked,
                        snackbarState = SnackbarState("차단이 해제되었습니다")
                    )
                }
            }
            .onFailure { error ->
                reduce {
                    state.copy(snackbarState = SnackbarState(error.message ?: "차단 해제 실패"))
                }
            }
    }

    // 캐시 반영은 저장소, 여기선 읽음 처리만
    private fun observeWebSocket() {
        chatEventRepository.events
            .onEach { event ->
                when (event) {
                    is ChatEvent.MessageReceived -> {
                        if (event.message.roomId == roomId && isRoomVisible()) markAsRead()
                    }

                    is ChatEvent.Connected -> {
                        // 재연결 시 놓친 메시지 REST 재조회
                        if (roomId != 0) loadMessages(isEntry = false)
                    }

                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    // 뒤 화면·백그라운드에선 읽음 처리 안 함
    private fun isRoomVisible(): Boolean = ActiveChatRoomTracker.activeRoomId == roomId

    private fun observeUploadResults() {
        chatMediaRepository.uploadEvents
            .onEach { result ->
                if (result !is ChatUploadEvent.Failure || result.roomId != roomId) return@onEach
                intent {
                    postSideEffect(ChatRoomSideEffect.ShowToast("업로드 실패: ${result.errorMessage}"))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeDownloadResults() {
        chatMediaRepository.downloadEvents
            .onEach { result ->
                intent {
                    when (result) {
                        is ChatDownloadEvent.Progress -> {
                            reduce {
                                state.copy(
                                    downloadingFiles = state.downloadingFiles + (result.fileUrl to result.progress)
                                )
                            }
                        }

                        is ChatDownloadEvent.Success -> {
                            reduce { state.copy(downloadingFiles = state.downloadingFiles - result.fileUrl) }
                            postSideEffect(ChatRoomSideEffect.ShowToast("${result.fileName} 다운로드 완료"))
                        }

                        is ChatDownloadEvent.Failure -> {
                            reduce { state.copy(downloadingFiles = state.downloadingFiles - result.fileUrl) }
                            postSideEffect(ChatRoomSideEffect.ShowToast("다운로드 실패"))
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}
