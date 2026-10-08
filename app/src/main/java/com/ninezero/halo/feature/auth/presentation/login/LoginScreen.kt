package com.ninezero.halo.feature.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.TextButton
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
import com.ninezero.halo.core.ui.components.modifiers.bounceClick
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    val focusManager = LocalFocusManager.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LoginSideEffect.ClearFocus -> {
                focusManager.clearFocus()
            }
            is LoginSideEffect.NavigateToHome -> {
                onNavigateToHome()
            }
            is LoginSideEffect.NavigateToRegister -> {
                onNavigateToRegister()
            }
            is LoginSideEffect.NavigateToForgotPassword -> {
                onNavigateToForgotPassword()
            }
        }
    }

    LoginScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = onNavigateBack
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(LoginIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun LoginScreenContent(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
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

            AuthTextField(
                value = state.email,
                onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
                label = "이메일",
                keyboardType = KeyboardType.Email,
                isError = state.emailError != null,
                errorMessage = state.emailError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = state.password,
                onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                label = "비밀번호",
                isPassword = true,
                isPasswordVisible = state.isPasswordVisible,
                onPasswordVisibilityToggle = {
                    onIntent(LoginIntent.TogglePasswordVisibility)
                },
                keyboardType = KeyboardType.Password,
                isError = state.passwordError != null,
                errorMessage = state.passwordError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "비밀번호 찾기",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !state.isLoading
                    ) { onIntent(LoginIntent.ForgotPasswordClicked) }
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "로그인",
                onClick = { onIntent(LoginIntent.LoginClicked) },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "계정이 없나요?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { onIntent(LoginIntent.RegisterClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bounceClick(),
                enabled = !state.isLoading
            ) {
                Text("가입하기")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        HazeScreenHeader(
            title = "로그인",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = onBackClick,
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
