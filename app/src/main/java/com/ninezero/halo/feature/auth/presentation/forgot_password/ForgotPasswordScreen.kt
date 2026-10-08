package com.ninezero.halo.feature.auth.presentation.forgot_password

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.button.ActionButton
import com.ninezero.halo.core.ui.components.input.AuthTextField
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResetCode: (email: String) -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value

    val focusManager = LocalFocusManager.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ForgotPasswordSideEffect.ClearFocus -> {
                focusManager.clearFocus()
            }
            is ForgotPasswordSideEffect.NavigateBack -> {
                onNavigateBack()
            }
            is ForgotPasswordSideEffect.NavigateToResetCode -> {
                onNavigateToResetCode(sideEffect.email)
            }
        }
    }

    ForgotPasswordScreenContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun ForgotPasswordScreenContent(
    state: ForgotPasswordState,
    onIntent: (ForgotPasswordIntent) -> Unit
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
                text = "비밀번호 재설정",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "가입 시 사용한 이메일로 인증 코드를 보내드립니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                value = state.email,
                onValueChange = { onIntent(ForgotPasswordIntent.EmailChanged(it)) },
                label = "이메일",
                isError = state.emailError != null,
                errorMessage = state.emailError,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "인증 코드 전송",
                onClick = { onIntent(ForgotPasswordIntent.SendResetCode) },
                isLoading = state.isLoading,
                enabled = state.email.isNotBlank()
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        HazeScreenHeader(
            title = "비밀번호 찾기",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(ForgotPasswordIntent.NavigateBack) },
            onHeightMeasured = { headerHeight = it }
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
