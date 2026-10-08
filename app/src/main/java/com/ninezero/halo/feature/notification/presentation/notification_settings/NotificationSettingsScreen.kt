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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.ninezero.halo.core.ui.components.loading.CenteredLoadingIndicator
import com.ninezero.halo.core.ui.components.loading.LoadingIndicatorSize
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.notification.presentation.notification_settings.components.NotificationSettingsToggleItem
import com.ninezero.halo.feature.notification.presentation.notification_settings.components.SettingsDivider
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
    val categoryControlsEnabled = state.masterEnabled

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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { onIntent(NotificationSettingsIntent.Retry) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.bounceClick()
                        ) {
                            Text("재시도")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                        .verticalScroll(rememberScrollState())
                        .padding(top = headerHeight)
                ) {
                    NotificationSettingsToggleItem(
                        title = "푸시 알림",
                        checked = state.masterEnabled,
                        enabled = true,
                        onCheckedChange = { onIntent(NotificationSettingsIntent.ToggleAll(it)) }
                    )

                    SettingsDivider()

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

                    // 크리에이터별 알림은 마스터와 독립이라 늘 진입
                    CreatorNotificationsRow(
                        onClick = { onIntent(NotificationSettingsIntent.OpenCreatorNotifications) }
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "크리에이터별 알림 관리",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "팔로우 중인 크리에이터별로 알림을 켜고 끌 수 있어요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp)
        )
    }
}

