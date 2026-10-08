package com.ninezero.halo.feature.feed.presentation.reply

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.GlassActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.statusBarsPaddingIgnoringVisibility
import com.ninezero.halo.core.ui.components.popup.ProfilePopupData
import com.ninezero.halo.core.ui.components.popup.ProfilePopupHost
import com.ninezero.halo.core.ui.components.popup.rememberProfilePopupHostState
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.createCameraImageUri
import com.ninezero.halo.feature.feed.presentation.reply.components.ReplyThreadSection
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.collectLatest
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ReplyScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (Int) -> Unit,
    viewModel: ReplyViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isCameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> isCameraPermissionGranted = granted }

    BackHandler {
        viewModel.onIntent(ReplyIntent.NavigateBack)
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ReplySideEffect.NavigateBack -> {
                keyboardController?.hide()
                onNavigateBack()
            }
            is ReplySideEffect.ReplySubmitted -> {
                keyboardController?.hide()
                onNavigateBack()
            }
        }
    }

    ReplyScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateToProfile = onNavigateToProfile,
        isCameraPermissionGranted = isCameraPermissionGranted,
        onRequestCameraPermission = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
    )
}

@Composable
internal fun ReplyScreenContent(
    state: ReplyState,
    onIntent: (ReplyIntent) -> Unit,
    onNavigateToProfile: (Int) -> Unit = {},
    isCameraPermissionGranted: Boolean = true,
    onRequestCameraPermission: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.background
    val density = LocalDensity.current
    var headerHeight by rememberSaveableDp()
    var showClearDialog by remember { mutableStateOf(false) }
    val hasContent = state.replyContent.isNotBlank()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val profilePopupState = rememberProfilePopupHostState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
        // 키보드·헤더 측정으로 maxValue가 단계적으로 커짐
        // maxValue가 바뀔 때마다 바닥으로 재정렬
        snapshotFlow { scrollState.maxValue }
            .collectLatest { max ->
                if (max > 0) scrollState.scrollTo(max)
            }
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onIntent(ReplyIntent.AddMedia(uris))
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            onIntent(ReplyIntent.AddMedia(listOf(cameraImageUri!!)))
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onIntent(ReplyIntent.AddMedia(listOf(it))) }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { onIntent(ReplyIntent.SnackbarDismissed) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 16.dp))

            // 원본 컨텐츠 + 답글 입력
            // CommentDetail에서 이동: 부모 댓글 표시
            // PostDetail에서 이동: 포스트 표시
            val parentComment = state.parentComment
            val post = state.post

            if (parentComment != null) {
                // 부모 댓글에 답글 달기
                val commentMediaAttachments = parentComment.mediaAttachments
                ReplyThreadSection(
                    authorName = parentComment.author.displayName ?: parentComment.author.username,
                    authorRole = parentComment.author.role,
                    authorAvatarUrl = parentComment.author.avatarThumbUrl ?: parentComment.author.avatarUrl,
                    onProfileClick = { offset ->
                        val clickedId = parentComment.author.id
                        when {
                            parentComment.author.isDeleted -> Unit  // 탈퇴자는 프로필로 이동 안 함
                            state.myProfile?.userId == clickedId -> onNavigateToProfile(clickedId)
                            else -> profilePopupState.show(clickedId, offset)
                        }
                    },
                    content = parentComment.content,
                    mediaAttachments = commentMediaAttachments,
                    replyToName = parentComment.author.displayName ?: parentComment.author.username,
                    myProfile = state.myProfile,
                    replyContent = state.replyContent,
                    selectedMediaUris = state.selectedMediaUris,
                    onContentChange = { onIntent(ReplyIntent.UpdateReplyContent(it)) },
                    onImageClick = { imagePickerLauncher.launch("image/*") },
                    onVideoClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onCameraClick = {
                        if (isCameraPermissionGranted) {
                            val uri = context.createCameraImageUri()
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            onRequestCameraPermission()
                        }
                    },
                    onRemoveMedia = { onIntent(ReplyIntent.RemoveMedia(it)) },
                    hasContent = hasContent,
                    onClearClick = { showClearDialog = true },
                    focusRequester = focusRequester
                )
            } else if (post != null) {
                // 포스트에 댓글 달기
                ReplyThreadSection(
                    authorName = post.author.displayName ?: post.author.username,
                    authorRole = post.author.role,
                    authorAvatarUrl = post.author.avatarThumbUrl ?: post.author.avatarUrl,
                    onProfileClick = { offset ->
                        val clickedId = post.author.id
                        when {
                            post.author.isDeleted -> Unit  // 탈퇴자는 프로필로 이동 안 함
                            state.myProfile?.userId == clickedId -> onNavigateToProfile(clickedId)
                            else -> profilePopupState.show(clickedId, offset)
                        }
                    },
                    content = post.content,
                    mediaAttachments = post.mediaAttachments,
                    replyToName = post.author.displayName ?: post.author.username,
                    myProfile = state.myProfile,
                    replyContent = state.replyContent,
                    selectedMediaUris = state.selectedMediaUris,
                    onContentChange = { onIntent(ReplyIntent.UpdateReplyContent(it)) },
                    onImageClick = { imagePickerLauncher.launch("image/*") },
                    onVideoClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onCameraClick = {
                        if (isCameraPermissionGranted) {
                            val uri = context.createCameraImageUri()
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            onRequestCameraPermission()
                        }
                    },
                    onRemoveMedia = { onIntent(ReplyIntent.RemoveMedia(it)) },
                    hasContent = hasContent,
                    onClearClick = { showClearDialog = true },
                    focusRequester = focusRequester
                )
            }
        }

        // 헤더
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .hazeEffect(state = hazeState) {
                    style = HazeStyle(
                        backgroundColor = backgroundColor,
                        tint = HazeTint(backgroundColor),
                        noiseFactor = 0f
                    )
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 0.5f,
                        endIntensity = 0f
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { /* 터치 이벤트 소비 */ }
                }
                .onGloballyPositioned { coordinates ->
                    headerHeight = with(density) { coordinates.size.height.toDp() }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPaddingIgnoringVisibility()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    onClick = { onIntent(ReplyIntent.NavigateBack) },
                    icon = painterResource(R.drawable.ic_close),
                    contentDescription = "닫기",
                    hazeState = hazeState,
                    backgroundColor = backgroundColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "댓글 달기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                val isEnabled = state.replyContent.isNotBlank() && !state.isSubmitting
                GlassActionButton(
                    onClick = { onIntent(ReplyIntent.SubmitReply) },
                    hazeState = hazeState,
                    enabled = isEnabled
                ) {
                    Text(
                        text = "게시",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        if (showClearDialog) {
            ConfirmDialog(
                title = "포스트를 삭제하시겠어요?",
                confirmText = "삭제",
                dismissText = "취소",
                confirmColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    onIntent(ReplyIntent.UpdateReplyContent(""))
                    onIntent(ReplyIntent.ClearAllMedia)
                    showClearDialog = false
                },
                onDismiss = { showClearDialog = false }
            )
        }

        ProfilePopupHost(
            state = profilePopupState,
            resolveAuthor = { authorId ->
                val author = state.post?.author?.takeIf { it.id == authorId }
                    ?: state.parentComment?.author?.takeIf { it.id == authorId }
                author?.let { ProfilePopupData(id = it.id, isFollowing = it.isFollowing) }
            },
            onNavigateToProfile = onNavigateToProfile,
            onFollow = { onIntent(ReplyIntent.ToggleAuthorFollow(it, shouldFollow = true)) },
            onUnfollow = { onIntent(ReplyIntent.ToggleAuthorFollow(it, shouldFollow = false)) }
        )
    }
}

