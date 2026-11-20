package com.ninezero.halo.feature.notification.presentation.notification_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.list.ListErrorState
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.notification.presentation.notification_settings.components.NotificationSettingsToggleItem
import com.ninezero.halo.feature.notification.presentation.notification_settings.components.SettingsDivider
import com.ninezero.halo.feature.notification.presentation.notification_settings.components.SettingsGroup
import com.ninezero.halo.feature.notification.presentation.notification_settings.components.SettingsGroupLabel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreatorNotifications: () -> Unit = {},
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is NotificationSettingsSideEffect.NavigateBack -> onNavigateBack()
            is NotificationSettingsSideEffect.NavigateToCreatorNotifications -> onNavigateToCreatorNotifications()
        }
    }

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(NotificationSettingsIntent.SnackbarDismissed) }
    )

    NotificationSettingsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun NotificationSettingsScreenContent(
    state: NotificationSettingsState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (NotificationSettingsIntent) -> Unit = {}
) {
    val hazeState = remember { HazeState() }
    val backgroundColor = MaterialTheme.colorScheme.background
    var headerHeight by rememberSaveableDp()
    val categoryControlsEnabled = state.mainEnabled

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                        .padding(top = headerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    CenteredLoadingIndicator(size = LoadingIndicatorSize.Large)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                        .padding(top = headerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    ListErrorState(
                        title = "알림 설정을 불러오지 못했습니다",
                        onRetry = { onIntent(NotificationSettingsIntent.Retry) }
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                        .verticalScroll(rememberScrollState())
                        .padding(top = headerHeight + 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    SettingsGroup {
                        NotificationSettingsToggleItem(
                            title = "푸시 알림",
                            checked = state.mainEnabled,
                            enabled = true,
                            onCheckedChange = { onIntent(NotificationSettingsIntent.ToggleAll(it)) }
                        )
                    }

                    SettingsGroupLabel(text = "알림 종류")
                    SettingsGroup {
                        NotificationSettingsToggleItem(
                            title = "소셜",
                            description = "팔로우, 좋아요, 댓글, 답글, 멘션",
                            checked = state.socialEnabled,
                            enabled = categoryControlsEnabled,
                            onCheckedChange = { onIntent(NotificationSettingsIntent.ToggleSocial(it)) }
                        )
                        SettingsDivider()
                        NotificationSettingsToggleItem(
                            title = "크리에이터 활동",
                            description = "팔로우 중인 크리에이터의 새 포스트, 새 상품",
                            checked = state.creatorActivityEnabled,
                            enabled = categoryControlsEnabled,
                            onCheckedChange = { onIntent(NotificationSettingsIntent.ToggleCreatorActivity(it)) }
                        )
                        SettingsDivider()
                        NotificationSettingsToggleItem(
                            title = "쇼핑",
                            description = "주문, 배송, 리뷰",
                            checked = state.commerceEnabled,
                            enabled = categoryControlsEnabled,
                            onCheckedChange = { onIntent(NotificationSettingsIntent.ToggleCommerce(it)) }
                        )
                        SettingsDivider()
                        NotificationSettingsToggleItem(
                            title = "채팅",
                            description = "새 메시지",
                            checked = state.chatEnabled,
                            enabled = categoryControlsEnabled,
                            onCheckedChange = { onIntent(NotificationSettingsIntent.ToggleChat(it)) }
                        )
                        SettingsDivider()
                        NotificationSettingsToggleItem(
                            title = "시스템",
                            description = "공지사항, 크리에이터 승인, 멤버십, 포인트",
                            checked = state.systemEnabled,
                            enabled = categoryControlsEnabled,
                            onCheckedChange = { onIntent(NotificationSettingsIntent.ToggleSystem(it)) }
                        )
                    }
                    if (!state.mainEnabled) {
                        Text(
                            text = "푸시 알림을 켜면 종류별로 설정할 수 있습니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    // 크리에이터별 알림은 마스터와 따로
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsGroup {
                        CreatorNotificationsRow(
                            onClick = { onIntent(NotificationSettingsIntent.OpenCreatorNotifications) }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        HazeScreenHeader(
            title = "알림 설정",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(NotificationSettingsIntent.NavigateBack) },
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CreatorNotificationsRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) { onClick() }
            .heightIn(min = 56.dp)  // 한 줄 목록 높이
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "크리에이터별 알림 관리",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp)
        )
    }
}
