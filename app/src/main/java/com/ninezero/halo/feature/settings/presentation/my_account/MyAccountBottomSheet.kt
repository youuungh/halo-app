package com.ninezero.halo.feature.settings.presentation.my_account

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.button.ActionButton
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.checkbox.HaloCheckbox
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.AuthTextField
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.auth.domain.model.Device
import com.ninezero.halo.feature.creator.domain.model.CreatorApplicationStatus
import com.ninezero.halo.ui.theme.HaloGradient
import com.ninezero.halo.ui.theme.KakaoYellow
import com.ninezero.halo.ui.theme.NaverGreen
import com.ninezero.halo.ui.theme.Pretendard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyAccountBottomSheet(
    onDismiss: () -> Unit,
    onAccountDeleted: () -> Unit,
    onNavigateToCreatorApplication: () -> Unit,
    viewModel: MyAccountViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val hazeState = remember { HazeState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(MyAccountIntent.Load)
    }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MyAccountSideEffect.ClearFocus -> focusManager.clearFocus()
            is MyAccountSideEffect.AccountDeleted -> onAccountDeleted()
        }
    }

    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            // 시트 VM은 설정 화면과 수명이 같아 다시 열 때 첫 페이지로
            viewModel.onIntent(MyAccountIntent.BackToRoot)
            onDismiss()
        },
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        contentWindowInsets = { WindowInsets(0) },
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // 하위 페이지 back은 Root 복귀
        BackHandler(enabled = state.page != MyAccountPage.ROOT) {
            viewModel.onIntent(MyAccountIntent.BackToRoot)
        }

        // 상태바 아래까지 펼친 채로 열림, 끌어서는 안 닫힘
        // 시트 창이 키보드만큼 줄어 있어 남은 자리가 전체
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                BottomSheetDragHandle()

                // 헤더
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    if (state.page == MyAccountPage.ROOT) {
                        GlassIconButton(
                            onClick = onDismiss,
                            icon = painterResource(R.drawable.ic_close),
                            contentDescription = "닫기",
                            hazeState = hazeState,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            size = 36.dp,
                            iconSize = 20.dp,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )

                        Text(
                            text = "내 계정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        GlassIconButton(
                            onClick = { viewModel.onIntent(MyAccountIntent.BackToRoot) },
                            icon = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = "뒤로가기",
                            hazeState = hazeState,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            size = 36.dp,
                            iconSize = 20.dp,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = state.page,
                    transitionSpec = {
                        if (targetState == MyAccountPage.ROOT) {
                            (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it / 4 } + fadeOut())
                        } else {
                            (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it / 4 } + fadeOut())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .hazeSource(hazeState),
                    label = "my_account_page"
                ) { page ->
                    when (page) {
                        MyAccountPage.ROOT -> MyAccountRootContent(
                            state = state,
                            onIntent = viewModel::onIntent,
                            onNavigateToCreatorApplication = {
                                // 시트를 먼저 내리고 이동, 돌아오면 다시 표시
                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion { onNavigateToCreatorApplication() }
                            }
                        )

                        MyAccountPage.CHANGE_PASSWORD -> ChangePasswordContent(
                            state = state,
                            onIntent = viewModel::onIntent
                        )

                        MyAccountPage.DEVICES -> DevicesContent(
                            state = state,
                            onIntent = viewModel::onIntent
                        )
                    }
                }
            }

            // 시트 창이 키보드만큼 줄어 imePadding 불필요
            DefaultSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(MyAccountIntent.SnackbarDismissed) }
    )

    if (state.showDeleteDialog) {
        DeleteAccountDialog(
            isAgreed = state.isDeleteAgreed,
            onAgreementChange = { viewModel.onIntent(MyAccountIntent.DeleteAgreementChanged(it)) },
            onConfirm = { viewModel.onIntent(MyAccountIntent.ConfirmDeleteAccount) },
            onDismiss = { viewModel.onIntent(MyAccountIntent.DismissDeleteDialog) }
        )
    }

    if (state.showRevokeCreatorDialog) {
        RevokeCreatorDialog(
            isAgreed = state.isRevokeAgreed,
            onAgreementChange = { viewModel.onIntent(MyAccountIntent.RevokeAgreementChanged(it)) },
            onConfirm = { viewModel.onIntent(MyAccountIntent.ConfirmRevokeCreator) },
            onDismiss = { viewModel.onIntent(MyAccountIntent.DismissRevokeDialog) }
        )
    }

    state.deviceToLogout?.let { device ->
        ConfirmDialog(
            title = "이 기기에서 로그아웃할까요?",
            message = device.deviceName,
            confirmText = "로그아웃",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { viewModel.onIntent(MyAccountIntent.ConfirmDeviceLogout) },
            onDismiss = { viewModel.onIntent(MyAccountIntent.DismissDeviceLogoutDialog) }
        )
    }

    if (state.isDeleting || state.isLoggingOutDevice || state.isRevokingCreator) {
        LoadingDialog()
    }
}

@Composable
internal fun MyAccountRootContent(
    state: MyAccountState,
    onIntent: (MyAccountIntent) -> Unit,
    onNavigateToCreatorApplication: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(), overscrollEffect = null)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 못 받았으면 스켈레톤, 연결되면 채움
        if (!state.isAccountLoaded) ProfileCardSkeleton() else ProfileCard(
            displayName = state.displayName,
            username = state.username,
            avatarUrl = state.avatarUrl,
            socialProviders = state.socialProviders,
            isCreator = state.isCreator,
            applicationStatus = state.applicationStatus,
            onCreatorClick = onNavigateToCreatorApplication,
            onRevokeCreatorClick = { onIntent(MyAccountIntent.RevokeCreatorClick) }
        )

        if (state.hasPassword) {
            AccountActionCard(
                iconRes = R.drawable.ic_key,
                title = "비밀번호 변경",
                subtitle = "현재 비밀번호 확인 후 변경할 수 있어요",
                onClick = { onIntent(MyAccountIntent.ChangePasswordClick) }
            )
        }

        AccountActionCard(
            iconRes = R.drawable.ic_tablet_smartphone,
            title = "로그인된 기기",
            subtitle = "내 계정에 로그인된 기기를 확인하고 로그아웃할 수 있어요",
            onClick = { onIntent(MyAccountIntent.DevicesClick) }
        )

        AccountActionCard(
            iconRes = R.drawable.ic_user_minus,
            title = "계정 삭제하기",
            subtitle = "삭제하면 복구할 수 없으며, 포스트와 댓글은 '탈퇴한 사용자'로 남아요",
            tint = MaterialTheme.colorScheme.error,
            onClick = { onIntent(MyAccountIntent.DeleteAccountClick) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ProfileCardSkeleton() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "내 정보 불러오는 중" },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).shimmer())
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Box(modifier = Modifier.size(width = 96.dp, height = 14.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.size(width = 64.dp, height = 12.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(12.dp)).shimmer())
        }
    }
}

@Composable
private fun ProfileCard(
    displayName: String,
    username: String,
    avatarUrl: String?,
    socialProviders: List<String>,
    isCreator: Boolean,
    applicationStatus: CreatorApplicationStatus?,
    onCreatorClick: () -> Unit,
    onRevokeCreatorClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "프로필 사진",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_profile_placeholder),
                    error = painterResource(R.drawable.ic_profile_placeholder),
                    fallback = painterResource(R.drawable.ic_profile_placeholder)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (socialProviders.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    SocialProviderPill(providers = socialProviders)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 크리에이터 상태 배너
            CreatorBanner(
                isCreator = isCreator,
                applicationStatus = applicationStatus,
                onClick = onCreatorClick,
                onRevokeClick = onRevokeCreatorClick
            )
        }
    }
}

@Composable
private fun SocialProviderPill(providers: List<String>) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            providers.forEach { provider ->
                SocialProviderIcon(provider = provider)
            }
        }
    }
}

@Composable
private fun SocialProviderIcon(provider: String) {
    val (containerColor, iconRes) = when (provider) {
        "KAKAO" -> KakaoYellow to R.drawable.ic_kakao
        "NAVER" -> NaverGreen to R.drawable.ic_naver
        else -> Color.White to R.drawable.ic_google
    }

    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(containerColor)
            .then(
                if (provider == "GOOGLE") {
                    Modifier.border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        CircleShape
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = provider,
            modifier = Modifier.size(12.dp)
        )
    }
}

@Composable
private fun CreatorBanner(
    isCreator: Boolean,
    applicationStatus: CreatorApplicationStatus?,
    onClick: () -> Unit,
    onRevokeClick: () -> Unit
) {
    val hasApplied = isCreator || applicationStatus != null

    val (title, subtitle) = when {
        isCreator ->
            "크리에이터로 활동 중" to "나만의 스토어를 운영하고 있어요"
        applicationStatus == CreatorApplicationStatus.PENDING ->
            "크리에이터 심사 중" to "신청서를 검토하고 있어요"
        applicationStatus != null ->
            "크리에이터 신청" to "심사 결과가 도착했어요"
        else ->
            "크리에이터 신청" to "나만의 스토어를 운영해보세요"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = HaloGradient,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isCreator) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BannerActionButton(
                        text = "신청 현황",
                        modifier = Modifier.weight(1f),
                        onClick = onClick
                    )
                    BannerActionButton(
                        text = "해제",
                        modifier = Modifier.weight(1f),
                        backgroundAlpha = 0.15f,
                        onClick = onRevokeClick
                    )
                }
            } else {
                BannerActionButton(
                    text = if (hasApplied) "신청 현황" else "신청하기",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun BannerActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundAlpha: Float = 0.25f
) {
    Box(
        modifier = modifier
            .bounceClick()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = backgroundAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun AccountActionCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = title,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp),
                tint = tint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = tint
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ChangePasswordContent(
    state: MyAccountState,
    onIntent: (MyAccountIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(), overscrollEffect = null)
            .padding(horizontal = 24.dp)  // 시트 창이 키보드만큼 줄어 imePadding 불필요
    ) {
        Text(
            text = "비밀번호 변경",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "변경하면 현재 기기를 제외한 모든 기기에서 로그아웃됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthTextField(
            value = state.currentPassword,
            onValueChange = { onIntent(MyAccountIntent.CurrentPasswordChanged(it)) },
            label = "현재 비밀번호",
            isPassword = true,
            isPasswordVisible = state.isCurrentPasswordVisible,
            onPasswordVisibilityToggle = { onIntent(MyAccountIntent.ToggleCurrentPasswordVisibility) },
            keyboardType = KeyboardType.Password,
            isError = state.currentPasswordError != null,
            errorMessage = state.currentPasswordError,
            enabled = !state.isSubmitting
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = state.newPassword,
            onValueChange = { onIntent(MyAccountIntent.NewPasswordChanged(it)) },
            label = "새 비밀번호",
            isPassword = true,
            isPasswordVisible = state.isNewPasswordVisible,
            onPasswordVisibilityToggle = { onIntent(MyAccountIntent.ToggleNewPasswordVisibility) },
            keyboardType = KeyboardType.Password,
            isError = state.newPasswordError != null,
            errorMessage = state.newPasswordError,
            enabled = !state.isSubmitting
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = state.confirmPassword,
            onValueChange = { onIntent(MyAccountIntent.ConfirmPasswordChanged(it)) },
            label = "새 비밀번호 확인",
            isPassword = true,
            isPasswordVisible = state.isConfirmPasswordVisible,
            onPasswordVisibilityToggle = { onIntent(MyAccountIntent.ToggleConfirmPasswordVisibility) },
            keyboardType = KeyboardType.Password,
            isError = state.confirmPasswordError != null,
            errorMessage = state.confirmPasswordError,
            enabled = !state.isSubmitting
        )

        Spacer(modifier = Modifier.height(24.dp))

        ActionButton(
            text = "변경하기",
            onClick = { onIntent(MyAccountIntent.SubmitPasswordChange) },
            isLoading = state.isSubmitting
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
internal fun DevicesContent(
    state: MyAccountState,
    onIntent: (MyAccountIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState(), overscrollEffect = null)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "로그인된 기기",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "내 계정에 로그인된 기기 목록입니다.\n낯선 기기가 있다면 로그아웃해주세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            state.isDevicesLoading -> {
                CenteredLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                )
            }

            state.devicesError != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.devicesError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "다시 시도",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onIntent(MyAccountIntent.RetryLoadDevices) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.devices.forEach { device ->
                        DeviceCard(
                            device = device,
                            onLogoutClick = { onIntent(MyAccountIntent.RequestDeviceLogout(device)) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DeviceCard(
    device: Device,
    onLogoutClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.deviceName.ifBlank { "알 수 없는 기기" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (device.isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "현재 기기",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = listOfNotNull(
                        device.os.takeIf { it.isNotBlank() },
                        "${device.lastActiveAt.toRelativeTime()} 활동"
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val locationText = device.location?.takeIf { it.isNotBlank() }
                    ?: device.ipAddress.takeIf { it.isNotBlank() }
                if (locationText != null) {
                    Text(
                        text = locationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!device.isCurrent) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "로그아웃",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onLogoutClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
internal fun DeleteAccountDialog(
    isAgreed: Boolean,
    onAgreementChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AgreementConfirmDialog(
        title = "계정을 삭제하시겠어요?",
        message = "삭제한 계정은 다시 로그인하거나 복구할 수 없습니다.\n" +
                "작성한 포스트와 댓글은 '탈퇴한 사용자'로 표시되어 남으며,\n" +
                "주문 및 결제 내역은 관계 법령에 따라 보관됩니다.",
        confirmText = "삭제하기",
        isAgreed = isAgreed,
        onAgreementChange = onAgreementChange,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
internal fun RevokeCreatorDialog(
    isAgreed: Boolean,
    onAgreementChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AgreementConfirmDialog(
        title = "크리에이터를 해제하시겠어요?",
        message = "판매 중인 상품이 모두 판매종료되고\n" +
                "멤버십 플랜이 비활성화됩니다.\n" +
                "커뮤니티 글과 멤버십 전용 글은 숨겨지며 재승인 시 복구됩니다.\n" +
                "해제 후 7일이 지나면 다시 신청할 수 있습니다.",
        confirmText = "해제하기",
        isAgreed = isAgreed,
        onAgreementChange = onAgreementChange,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun AgreementConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    isAgreed: Boolean,
    onAgreementChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = Pretendard,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 동의 체크박스
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onAgreementChange(!isAgreed) }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HaloCheckbox(
                            checked = isAgreed,
                            contentDescription = "동의함"
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "위 내용을 확인했으며 동의합니다",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = Pretendard,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // 확인 버튼, 동의해야 활성
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.onSurface),
                            enabled = isAgreed
                        ) { onConfirm() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confirmText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAgreed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        }
                    )
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // 취소 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = MaterialTheme.colorScheme.onSurface)
                        ) { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "취소",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Pretendard,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
