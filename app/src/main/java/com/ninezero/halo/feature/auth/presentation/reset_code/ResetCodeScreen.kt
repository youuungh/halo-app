package com.ninezero.halo.feature.auth.presentation.reset_code

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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.core.ui.components.button.ActionButton
import com.ninezero.halo.core.ui.components.button.ActionButtonStyle
import com.ninezero.halo.core.ui.components.layout.HazeScreenHeader
import com.ninezero.halo.core.ui.components.layout.rememberSaveableDp
import com.ninezero.halo.core.util.Constants
import com.ninezero.halo.core.util.showToast
import com.ninezero.halo.feature.auth.presentation.components.CodeInputField
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ResetCodeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResetPassword: (resetToken: String) -> Unit,
    viewModel: ResetCodeViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ResetCodeSideEffect.ClearFocus -> {
                focusManager.clearFocus()
            }
            is ResetCodeSideEffect.ShowToast -> {
                context.showToast(sideEffect.message, Toast.LENGTH_SHORT)
            }
            is ResetCodeSideEffect.NavigateBack -> {
                onNavigateBack()
            }
            is ResetCodeSideEffect.NavigateToResetPassword -> {
                onNavigateToResetPassword(sideEffect.resetToken)
            }
        }
    }

    ResetCodeScreenContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun ResetCodeScreenContent(
    state: ResetCodeState,
    onIntent: (ResetCodeIntent) -> Unit
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
                text = "인증 코드 입력",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${state.email} 주소로 전송된 인증 코드를 입력하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CodeInputField(
                value = state.code,
                onValueChange = { onIntent(ResetCodeIntent.CodeChanged(it)) },
                isError = state.codeError != null,
                enabled = !state.isLoading
            )

            if (state.codeError != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.codeError,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.remainingSeconds > 0) {
                Text(
                    text = "남은 시간 %d:%02d".format(
                        state.remainingSeconds / 60,
                        state.remainingSeconds % 60
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                Text(
                    text = "인증 코드가 만료되었습니다. 재전송해주세요.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "확인",
                onClick = { onIntent(ResetCodeIntent.VerifyCode) },
                isLoading = state.isLoading,
                enabled = state.code.length == Constants.Auth.VERIFICATION_CODE_LENGTH
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                text = if (state.resendCooldownSeconds > 0) {
                    "인증 코드 재전송 (${state.resendCooldownSeconds}초)"
                } else {
                    "인증 코드 재전송"
                },
                onClick = { onIntent(ResetCodeIntent.ResendCode) },
                style = ActionButtonStyle.Text,
                enabled = state.resendCooldownSeconds == 0 && !state.isLoading
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        HazeScreenHeader(
            title = "비밀번호 찾기",
            hazeState = hazeState,
            backgroundColor = backgroundColor,
            onNavigateBack = { onIntent(ResetCodeIntent.NavigateBack) },
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
