package com.ninezero.halo.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ninezero.halo.R
import com.ninezero.halo.core.local.ThemeMode
import com.ninezero.halo.core.ui.components.bottomsheet.ThemeBottomSheet
import com.ninezero.halo.core.ui.components.dialog.ConfirmDialog
import com.ninezero.halo.core.ui.components.dialog.LoadingDialog
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.settings.presentation.components.SettingsMenuItem
import com.ninezero.halo.feature.settings.presentation.components.SettingsMenuItemData
import com.ninezero.halo.feature.settings.presentation.my_account.MyAccountBottomSheet
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToSavedPosts: () -> Unit = {},
    onNavigateToLikedPosts: () -> Unit = {},
    onNavigateToArchivedChats: () -> Unit = {},
    onNavigateToBlockedProfiles: () -> Unit = {},
    onNavigateToPaymentMethods: () -> Unit = {},
    onNavigateToInfo: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToCreatorApplication: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SettingsSideEffect.NavigateToNotificationSettings -> onNavigateToNotificationSettings()
            is SettingsSideEffect.NavigateToSavedPosts -> onNavigateToSavedPosts()
            is SettingsSideEffect.NavigateToLikedPosts -> onNavigateToLikedPosts()
            is SettingsSideEffect.NavigateToArchivedChats -> onNavigateToArchivedChats()
            is SettingsSideEffect.NavigateToBlockedProfiles -> onNavigateToBlockedProfiles()
            is SettingsSideEffect.NavigateToPaymentMethods -> onNavigateToPaymentMethods()
            is SettingsSideEffect.NavigateToInfo -> onNavigateToInfo()
            is SettingsSideEffect.NavigateToLogin -> onNavigateToLogin()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(SettingsIntent.SnackbarDismissed) }
    )

    SettingsScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState
    )

    var isSheetParked by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { isSheetParked = false }

    if (state.showMyAccountSheet && !isSheetParked) {
        MyAccountBottomSheet(
            onDismiss = { viewModel.onIntent(SettingsIntent.DismissMyAccountSheet) },
            onAccountDeleted = {
                viewModel.onIntent(SettingsIntent.DismissMyAccountSheet)
                onNavigateToLogin()
            },
            // 시트 상태는 열린 채로 두고 이동, 복귀 시 다시 표시
            onNavigateToCreatorApplication = {
                isSheetParked = true
                onNavigateToCreatorApplication()
            }
        )
    }
}

@Composable
internal fun SettingsScreenContent(
    state: SettingsState,
    onNavigateBack: () -> Unit,
    onIntent: (SettingsIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeState = remember { HazeState() }

    var headerHeight by rememberSaveableDp()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 16.dp))

            SettingsMenuItem(
                iconRes = R.drawable.ic_circle_user_round,
                title = "내 계정",
                onClick = { onIntent(SettingsIntent.MyAccountClick) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            val menuItems = listOf(
                SettingsMenuItemData(R.drawable.ic_notification, "알림", SettingsIntent.NotificationClick),
                SettingsMenuItemData(R.drawable.ic_saved, "저장됨", SettingsIntent.SavedPostsClick),
                SettingsMenuItemData(R.drawable.ic_like, "좋아요", SettingsIntent.LikedPostsClick),
                SettingsMenuItemData(R.drawable.ic_archive, "보관됨", SettingsIntent.ArchivedChatsClick),
                SettingsMenuItemData(R.drawable.ic_user_block, "차단된 프로필", SettingsIntent.BlockedProfilesClick),
                SettingsMenuItemData(R.drawable.ic_card, "결제수단", SettingsIntent.PaymentMethodsClick)
            )

            menuItems.forEach { item ->
                SettingsMenuItem(
                    iconRes = item.iconRes,
                    title = item.title,
                    onClick = { onIntent(item.intent) }
                )
            }

            SettingsMenuItem(
                iconRes = R.drawable.ic_theme,
                title = "테마",
                trailingText = ThemeMode.toDisplayName(state.themeMode),
                onClick = { onIntent(SettingsIntent.ThemeClick) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            SettingsMenuItem(
                iconRes = R.drawable.ic_circle_help,
                title = "정보",
                onClick = { onIntent(SettingsIntent.InfoClick) }
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = { onIntent(SettingsIntent.LogoutClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .height(56.dp)
                    .bounceClick(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "로그아웃",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        HazeScreenHeader(
            title = "설정",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onNavigateBack,
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.showLogoutDialog) {
        ConfirmDialog(
            title = "Halo에서\n로그아웃하시겠어요?",
            confirmText = "로그아웃",
            confirmColor = MaterialTheme.colorScheme.error,
            onConfirm = { onIntent(SettingsIntent.ConfirmLogout) },
            onDismiss = { onIntent(SettingsIntent.DismissLogoutDialog) }
        )
    }

    if (state.showThemeSheet) {
        ThemeBottomSheet(
            currentMode = state.themeMode,
            onModeChange = { mode -> onIntent(SettingsIntent.ChangeTheme(mode)) },
            onDismiss = { onIntent(SettingsIntent.DismissThemeSheet) }
        )
    }

    if (state.isLoading) {
        LoadingDialog()
    }
}
