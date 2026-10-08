package com.ninezero.halo.feature.auth.presentation.reset_password

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.button.ActionButton
import com.ninezero.halo.core.ui.components.input.AuthTextField
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.core.util.showToast
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ResetPasswordSideEffect.ClearFocus -> {
                focusManager.clearFocus()
            }
            is ResetPasswordSideEffect.ShowToast -> {
                context.showToast(sideEffect.message, Toast.LENGTH_LONG)
            }
            is ResetPasswordSideEffect.NavigateBack -> {
                onNavigateBack()
            }
            is ResetPasswordSideEffect.NavigateToLogin -> {
                onNavigateToLogin()
            }
        }
    }

    ResetPasswordScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(ResetPasswordIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun ResetPasswordScreenContent(
    state: ResetPasswordState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (ResetPasswordIntent) -> Unit
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
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(headerHeight + 16.dp))

            Text(
                text = "새 비밀번호 설정",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "새로 사용할 비밀번호를 입력하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = state.password,
                onValueChange = { onIntent(ResetPasswordIntent.PasswordChanged(it)) },
                label = "새 비밀번호",
                isPassword = true,
                isPasswordVisible = state.isPasswordVisible,
                onPasswordVisibilityToggle = { onIntent(ResetPasswordIntent.TogglePasswordVisibility) },
                keyboardType = KeyboardType.Password,
                isError = state.passwordError != null,
                errorMessage = state.passwordError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = state.confirmPassword,
                onValueChange = { onIntent(ResetPasswordIntent.ConfirmPasswordChanged(it)) },
                label = "새 비밀번호 확인",
                isPassword = true,
                isPasswordVisible = state.isConfirmPwVisible,
                onPasswordVisibilityToggle = { onIntent(ResetPasswordIntent.ToggleConfirmPwVisibility) },
                keyboardType = KeyboardType.Password,
                isError = state.confirmPasswordError != null,
                errorMessage = state.confirmPasswordError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "비밀번호 변경",
                onClick = { onIntent(ResetPasswordIntent.Submit) },
                isLoading = state.isLoading,
                enabled = state.password.isNotBlank() && state.confirmPassword.isNotBlank()
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        HazeScreenHeader(
            title = "비밀번호 찾기",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(ResetPasswordIntent.NavigateBack) },
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
