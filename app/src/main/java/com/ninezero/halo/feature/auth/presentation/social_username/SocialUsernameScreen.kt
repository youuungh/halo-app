package com.ninezero.halo.feature.auth.presentation.social_username

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.button.ActionButton
import com.ninezero.halo.core.ui.components.input.AuthTextField
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun SocialUsernameScreen(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SocialUsernameViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    val focusManager = LocalFocusManager.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SocialUsernameSideEffect.ClearFocus -> {
                focusManager.clearFocus()
            }
            is SocialUsernameSideEffect.NavigateToHome -> {
                onNavigateToHome()
            }
            is SocialUsernameSideEffect.NavigateBack -> {
                onNavigateBack()
            }
        }
    }

    SocialUsernameScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(SocialUsernameIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun SocialUsernameScreenContent(
    state: SocialUsernameState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (SocialUsernameIntent) -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val focusManager = LocalFocusManager.current

    val hazeState = remember { HazeState() }

    var headerHeight by rememberSaveableDp()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 16.dp))

            Text(
                text = "Halo에서 사용할 이름을 정해주세요.\n사용자 이름은 나중에 변경할 수 없어요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            AuthTextField(
                value = state.username,
                onValueChange = { onIntent(SocialUsernameIntent.UsernameChanged(it)) },
                label = "사용자 이름",
                keyboardType = KeyboardType.Text,
                isError = state.usernameError != null,
                errorMessage = state.usernameError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "시작하기",
                onClick = { onIntent(SocialUsernameIntent.SubmitClicked) },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        HazeScreenHeader(
            title = "사용자 이름 설정",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(SocialUsernameIntent.NavigateBack) },
            onHeightMeasured = { headerHeight = it }
        )

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* 터치 이벤트 소비 */ }
                    }
            )
        }
    }
}
