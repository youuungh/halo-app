package com.ninezero.halo.feature.auth.presentation.welcome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ninezero.halo.R
import com.ninezero.halo.core.ui.components.button.ActionButton
import com.ninezero.halo.core.ui.components.button.ActionButtonStyle
import com.ninezero.halo.core.ui.components.snackbar.DefaultSnackbarHost
import com.ninezero.halo.core.ui.components.snackbar.SnackbarHandler
import com.ninezero.halo.feature.auth.presentation.social.GoogleIdTokenResult
import com.ninezero.halo.feature.auth.presentation.social.KakaoTokenResult
import com.ninezero.halo.feature.auth.presentation.social.NaverTokenResult
import com.ninezero.halo.feature.auth.presentation.social.SocialIconButton
import com.ninezero.halo.feature.auth.presentation.social.requestGoogleIdToken
import com.ninezero.halo.feature.auth.presentation.social.requestKakaoAccessToken
import com.ninezero.halo.feature.auth.presentation.social.requestNaverAccessToken
import com.ninezero.halo.ui.theme.GodoRounded
import com.ninezero.halo.ui.theme.KakaoYellow
import com.ninezero.halo.ui.theme.NaverGreen
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val PROVIDER_GOOGLE = "GOOGLE"
private const val PROVIDER_KAKAO = "KAKAO"
private const val PROVIDER_NAVER = "NAVER"

@Composable
fun WelcomeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToUsernameOnboarding: (signupToken: String, suggestedUsername: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val state = viewModel.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is WelcomeSideEffect.NavigateToHome -> {
                onNavigateToHome()
            }
            is WelcomeSideEffect.NavigateToUsernameOnboarding -> {
                onNavigateToUsernameOnboarding(sideEffect.signupToken, sideEffect.suggestedUsername)
            }
        }
    }

    WelcomeScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onKakaoClick = {
            coroutineScope.launch {
                when (val result = requestKakaoAccessToken(context)) {
                    is KakaoTokenResult.Success ->
                        viewModel.onIntent(
                            WelcomeIntent.SocialCredentialReceived(PROVIDER_KAKAO, result.accessToken)
                        )
                    is KakaoTokenResult.Failure ->
                        viewModel.onIntent(WelcomeIntent.SocialSignInFailed(result.message))
                    KakaoTokenResult.Cancelled -> Unit
                }
            }
        },
        onNaverClick = {
            coroutineScope.launch {
                when (val result = requestNaverAccessToken(context)) {
                    is NaverTokenResult.Success ->
                        viewModel.onIntent(
                            WelcomeIntent.SocialCredentialReceived(PROVIDER_NAVER, result.accessToken)
                        )
                    is NaverTokenResult.Failure ->
                        viewModel.onIntent(WelcomeIntent.SocialSignInFailed(result.message))
                    NaverTokenResult.Cancelled -> Unit
                }
            }
        },
        onGoogleClick = {
            coroutineScope.launch {
                when (val result = requestGoogleIdToken(context)) {
                    is GoogleIdTokenResult.Success ->
                        viewModel.onIntent(
                            WelcomeIntent.SocialCredentialReceived(PROVIDER_GOOGLE, result.idToken)
                        )
                    is GoogleIdTokenResult.Failure ->
                        viewModel.onIntent(WelcomeIntent.SocialSignInFailed(result.message))
                    GoogleIdTokenResult.Cancelled -> Unit
                }
            }
        },
        onRegisterClick = onNavigateToRegister,
        onLoginClick = onNavigateToLogin
    )

    SnackbarHandler(
        snackbarState = state.snackbarState,
        hostState = snackbarHostState,
        onDismiss = { viewModel.onIntent(WelcomeIntent.SnackbarDismissed) }
    )
}

@Composable
internal fun WelcomeScreenContent(
    state: WelcomeState,
    onKakaoClick: () -> Unit,
    onNaverClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-60).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_halo_logo),
                contentDescription = "Halo 로고",
                modifier = Modifier.size(120.dp)
            )

            Text(
                text = "Halo",
                style = MaterialTheme.typography.displayLarge,
                fontFamily = GodoRounded,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 간편로그인 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SocialIconButton(
                    onClick = onKakaoClick,
                    icon = painterResource(id = R.drawable.ic_kakao),
                    contentDescription = "카카오로 계속하기",
                    containerColor = KakaoYellow,
                    modifier = Modifier.weight(1f),
                    isLoading = state.loadingProvider == PROVIDER_KAKAO,
                    enabled = !state.isSocialLoading,
                    loadingColor = SocialIconDark
                )

                SocialIconButton(
                    onClick = onNaverClick,
                    icon = painterResource(id = R.drawable.ic_naver),
                    contentDescription = "네이버로 계속하기",
                    containerColor = NaverGreen,
                    modifier = Modifier.weight(1f),
                    isLoading = state.loadingProvider == PROVIDER_NAVER,
                    enabled = !state.isSocialLoading,
                    loadingColor = Color.White
                )

                SocialIconButton(
                    onClick = onGoogleClick,
                    icon = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google로 계속하기",
                    containerColor = Color.White,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f),
                    isLoading = state.loadingProvider == PROVIDER_GOOGLE,
                    enabled = !state.isSocialLoading,
                    loadingColor = SocialIconDark
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "회원가입",
                onClick = onRegisterClick,
                enabled = !state.isSocialLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionButton(
                text = "로그인",
                onClick = onLoginClick,
                style = ActionButtonStyle.Text,
                enabled = !state.isSocialLoading
            )
        }

        DefaultSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private val SocialIconDark = Color(0xFF191919)
