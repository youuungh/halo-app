package com.ninezero.halo.feature.auth.presentation.register

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
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToVerifyEmail: (email: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    val focusManager = LocalFocusManager.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is RegisterSideEffect.ClearFocus -> {
                focusManager.clearFocus()
            }
            is RegisterSideEffect.NavigateToLogin -> {
                onNavigateToLogin()
            }
            is RegisterSideEffect.NavigateToVerifyEmail -> {
                onNavigateToVerifyEmail(sideEffect.email)
            }
        }
    }

    RegisterScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = onNavigateBack
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(RegisterIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun RegisterScreenContent(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
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
                value = state.username,
                onValueChange = { onIntent(RegisterIntent.UsernameChanged(it)) },
                label = "사용자명",
                isError = state.usernameError != null,
                errorMessage = state.usernameError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = state.email,
                onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
                label = "이메일",
                keyboardType = KeyboardType.Email,
                isError = state.emailError != null,
                errorMessage = state.emailError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = state.password,
                onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                label = "비밀번호",
                isPassword = true,
                isPasswordVisible = state.isPasswordVisible,
                onPasswordVisibilityToggle = {
                    onIntent(RegisterIntent.TogglePasswordVisibility)
                },
                keyboardType = KeyboardType.Password,
                isError = state.passwordError != null,
                errorMessage = state.passwordError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = state.confirmPassword,
                onValueChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
                label = "비밀번호 확인",
                isPassword = true,
                isPasswordVisible = state.isConfirmPwVisible,
                onPasswordVisibilityToggle = {
                    onIntent(RegisterIntent.ToggleConfirmPwVisibility)
                },
                keyboardType = KeyboardType.Password,
                isError = state.confirmPasswordError != null,
                errorMessage = state.confirmPasswordError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            ActionButton(
                text = "등록",
                onClick = { onIntent(RegisterIntent.RegisterClicked) },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "이미 계정이 있나요?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { onIntent(RegisterIntent.LoginClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .bounceClick(),
                enabled = !state.isLoading
            ) {
                Text("로그인")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        HazeScreenHeader(
            title = "계정 생성",
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
