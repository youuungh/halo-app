package com.ninezero.halo.feature.settings.presentation.my_account

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.bottomsheet.BottomSheetDragHandle
import com.ninezero.halo.core.ui.components.bottomsheet.FloatingBottomSheet
import com.ninezero.halo.core.ui.components.button.GlassIconButton
import com.ninezero.halo.core.ui.components.button.PrimaryButton
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.input.LabeledTextField
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.media.Avatar
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.modifiers.shimmer
import com.ninezero.halo.core.ui.components.selector.segmentedTabTransition
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.ui.components.toggle.HaloCheckbox
import com.ninezero.halo.core.ui.theme.GroupShape
import com.ninezero.halo.core.ui.theme.KakaoYellow
import com.ninezero.halo.core.ui.theme.MembershipColors
import com.ninezero.halo.core.ui.theme.NaverGreen
import com.ninezero.halo.core.ui.theme.Pretendard
import com.ninezero.halo.core.util.toRelativeTime
import com.ninezero.halo.feature.auth.domain.model.Device
import com.ninezero.halo.feature.creator.domain.model.CreatorApplicationStatus
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

    FloatingBottomSheet(
        onDismissRequest = {
            // 시트 VM은 설정 화면과 수명이 같아 다시 열 때 첫 페이지로
            viewModel.onIntent(MyAccountIntent.BackToRoot)
            onDismiss()
        },
        sheetState = sheetState,
        showDragHandle = false  // 헤더 안에 있음
    ) {
        // 하위 페이지 back은 Root 복귀
        BackHandler(enabled = state.page != MyAccountPage.ROOT) {
            viewModel.onIntent(MyAccountIntent.BackToRoot)
        }

        // 내용만큼 높이, 하위 페이지도 시트 안에서
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                BottomSheetDragHandle()

                // 헤더: 닫기 또는 뒤로 + 제목
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val isRoot = state.page == MyAccountPage.ROOT
                    GlassIconButton(
                        onClick = if (isRoot) onDismiss else { { viewModel.onIntent(MyAccountIntent.BackToRoot) } },
                        icon = painterResource(if (isRoot) R.drawable.ic_close else R.drawable.ic_chevron_left),
                        contentDescription = if (isRoot) "닫기" else "뒤로가기",
                        hazeState = hazeState,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        size = 36.dp,
                        iconSize = 20.dp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Text(
                        text = when (state.page) {
                            MyAccountPage.ROOT -> "내 계정"
                            MyAccountPage.CHANGE_PASSWORD -> "비밀번호 변경"
                            MyAccountPage.DEVICES -> "로그인된 기기"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                AnimatedContent(
                    targetState = state.page,
                    transitionSpec = { segmentedTabTransition() },  // 탭 시트들과 같은 제자리 전환
                    modifier = Modifier.hazeSource(hazeState),
                    label = "my_account_page"
                ) { page ->
                    when (page) {
                        MyAccountPage.ROOT -> MyAccountRootContent(
                            state = state,
                            onIntent = viewModel::onIntent,
                            onNavigateToCreatorApplication = {
                                // 시트를 먼저 내리고 이동
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

    if (state.showLeaveCreatorDialog) {
        LeaveCreatorDialog(
            isAgreed = state.isLeaveAgreed,
            onAgreementChange = { viewModel.onIntent(MyAccountIntent.LeaveAgreementChanged(it)) },
            onConfirm = { viewModel.onIntent(MyAccountIntent.ConfirmLeaveCreator) },
            onDismiss = { viewModel.onIntent(MyAccountIntent.DismissLeaveDialog) }
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

    if (state.isDeleting || state.isLoggingOutDevice || state.isLeavingCreator) {
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
            .fillMaxWidth()
            .verticalScroll(rememberScrollState(), overscrollEffect = null)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 못 받았으면 스켈레톤, 연결되면 채움
        if (!state.isAccountLoaded) ProfileSkeleton() else {
            ProfileRow(
                displayName = state.displayName,
                username = state.username,
                avatarUrl = state.avatarUrl,
                socialProviders = state.socialProviders
            )
            CreatorRow(
                isCreator = state.isCreator,
                applicationStatus = state.applicationStatus,
                onClick = onNavigateToCreatorApplication
            )
        }

        AccountMenuGroup(
            items = listOfNotNull(
                AccountMenuItem(R.drawable.ic_key, "비밀번호 변경") {
                    onIntent(MyAccountIntent.ChangePasswordClick)
                }.takeIf { state.hasPassword },
                AccountMenuItem(R.drawable.ic_tablet_smartphone, "로그인된 기기") {
                    onIntent(MyAccountIntent.DevicesClick)
                },
                AccountMenuItem(R.drawable.ic_user_minus, "크리에이터 해제") {
                    onIntent(MyAccountIntent.LeaveCreatorClick)
                }.takeIf { state.isCreator }
            )
        )

        // 되돌릴 수 없어 따로 한 묶음
        AccountMenuGroup(
            items = listOf(
                AccountMenuItem(R.drawable.ic_user_minus, "계정 삭제하기", isDanger = true) {
                    onIntent(MyAccountIntent.DeleteAccountClick)
                }
            )
        )
    }
}

@Composable
private fun ProfileSkeleton() {
    Column(
        modifier = Modifier.semantics { contentDescription = "내 정보 불러오는 중" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).shimmer())
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(modifier = Modifier.size(width = 96.dp, height = 14.dp).clip(MaterialTheme.shapes.extraSmall).shimmer())
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.size(width = 64.dp, height = 12.dp).clip(MaterialTheme.shapes.extraSmall).shimmer())
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(CreatorRowShape).shimmer())
    }
}

@Composable
private fun ProfileRow(
    displayName: String,
    username: String,
    avatarUrl: String?,
    socialProviders: List<String>
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            model = avatarUrl,
            size = 48.dp,
            contentDescription = "프로필 사진"
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

private val CreatorRowShape = RoundedCornerShape(14.dp)

/** 크리에이터 한 줄, 신청 상태는 테두리 알약 */
@Composable
private fun CreatorRow(
    isCreator: Boolean,
    applicationStatus: CreatorApplicationStatus?,
    onClick: () -> Unit
) {
    val status: Pair<String, Boolean>? = when {
        isCreator || applicationStatus == CreatorApplicationStatus.APPROVED -> "승인됨" to true
        applicationStatus == CreatorApplicationStatus.PENDING -> "심사 중" to true
        applicationStatus == CreatorApplicationStatus.REJECTED -> "반려됨" to false
        applicationStatus == CreatorApplicationStatus.REVOKED -> "해제됨" to false
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
            .clip(CreatorRowShape)
            .background(MembershipColors.groupCardBrush, CreatorRowShape)
            .border(0.5.dp, MembershipColors.groupCardBorder, CreatorRowShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_store),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "크리에이터",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (status != null) {
            val (label, isActive) = status
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .border(
                        width = 0.5.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (status != null) "신청 현황" else "신청하기",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private class AccountMenuItem(
    val iconRes: Int,
    val title: String,
    val isDanger: Boolean = false,
    val onClick: () -> Unit
)

/** 테두리 한 묶음 목록, 줄 사이 가는 선 */
@Composable
private fun AccountMenuGroup(items: List<AccountMenuItem>) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GroupShape)
            .border(0.5.dp, lineColor, GroupShape)
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(thickness = 0.5.dp, color = lineColor)
            val tint = if (item.isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ) { item.onClick() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = tint
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = tint,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            .fillMaxWidth()
            .verticalScroll(rememberScrollState(), overscrollEffect = null)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)  // 시트 창이 키보드만큼 줄어 imePadding 불필요
    ) {
        Text(
            text = "변경하면 현재 기기를 제외한 모든 기기에서 로그아웃됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LabeledTextField(
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

        LabeledTextField(
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

        LabeledTextField(
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

        PrimaryButton(
            text = "변경하기",
            onClick = { onIntent(MyAccountIntent.SubmitPasswordChange) },
            isLoading = state.isSubmitting
        )
    }
}

@Composable
internal fun DevicesContent(
    state: MyAccountState,
    onIntent: (MyAccountIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState(), overscrollEffect = null)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
    ) {
        Text(
            text = "내 계정에 로그인된 기기 목록입니다.\n낯선 기기가 있다면 로그아웃해주세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isDevicesLoading -> {
                CenteredLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                )
            }

            state.devicesError != null -> {
                ListErrorState(
                    title = "기기 목록을 불러오지 못했습니다",
                    reason = state.devicesError,
                    onRetry = { onIntent(MyAccountIntent.RetryLoadDevices) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                )
            }

            else -> {
                val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(GroupShape)
                        .border(0.5.dp, lineColor, GroupShape)
                ) {
                    state.devices.forEachIndexed { index, device ->
                        if (index > 0) HorizontalDivider(thickness = 0.5.dp, color = lineColor)
                        DeviceRow(
                            device = device,
                            onLogoutClick = { onIntent(MyAccountIntent.RequestDeviceLogout(device)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_tablet_smartphone),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = device.deviceName.ifBlank { "알 수 없는 기기" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (device.isCurrent) {
                    // 주소 관리 '기본'과 같은 표시
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "현재 기기",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
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
                    .clip(MaterialTheme.shapes.small)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onLogoutClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
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
internal fun LeaveCreatorDialog(
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
                            .clip(MaterialTheme.shapes.small)
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
