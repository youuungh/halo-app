package com.ninezero.halo.feature.auth.presentation.social

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed interface KakaoTokenResult {
    data class Success(val accessToken: String) : KakaoTokenResult
    data object Cancelled : KakaoTokenResult
    data class Failure(val message: String) : KakaoTokenResult
}

suspend fun requestKakaoAccessToken(context: Context): KakaoTokenResult {
    val client = UserApiClient.instance

    return try {
        val token = if (client.isKakaoTalkLoginAvailable(context)) {
            try {
                client.loginWithKakaoTalkSuspend(context)
            } catch (e: Throwable) {
                // 사용자가 카카오톡 로그인 화면에서 취소한 경우 폴백하지 않음
                if (e.isKakaoCancel()) return KakaoTokenResult.Cancelled
                client.loginWithKakaoAccountSuspend(context)
            }
        } else {
            client.loginWithKakaoAccountSuspend(context)
        }
        KakaoTokenResult.Success(token.accessToken)
    } catch (e: Throwable) {
        if (e.isKakaoCancel()) {
            KakaoTokenResult.Cancelled
        } else {
            Timber.w(e, "카카오 로그인 실패")
            KakaoTokenResult.Failure("카카오 로그인에 실패했습니다. 다시 시도해주세요")
        }
    }
}

private fun Throwable.isKakaoCancel(): Boolean =
    this is ClientError && reason == ClientErrorCause.Cancelled

private suspend fun UserApiClient.loginWithKakaoTalkSuspend(context: Context): OAuthToken =
    suspendCancellableCoroutine { continuation ->
        loginWithKakaoTalk(context) { token, error ->
            when {
                error != null -> continuation.resumeWithException(error)
                token != null -> continuation.resume(token)
                else -> continuation.resumeWithException(IllegalStateException("카카오 토큰이 비어 있습니다"))
            }
        }
    }

private suspend fun UserApiClient.loginWithKakaoAccountSuspend(context: Context): OAuthToken =
    suspendCancellableCoroutine { continuation ->
        loginWithKakaoAccount(context) { token, error ->
            when {
                error != null -> continuation.resumeWithException(error)
                token != null -> continuation.resume(token)
                else -> continuation.resumeWithException(IllegalStateException("카카오 토큰이 비어 있습니다"))
            }
        }
    }
