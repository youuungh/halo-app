package com.ninezero.halo.feature.auth.presentation.social

import android.content.Context
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.core.data.errorcode.NidOAuthErrorCode
import com.navercorp.nid.oauth.util.NidOAuthCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

sealed interface NaverTokenResult {
    data class Success(val accessToken: String) : NaverTokenResult
    data object Cancelled : NaverTokenResult
    data class Failure(val message: String) : NaverTokenResult
}

suspend fun requestNaverAccessToken(context: Context): NaverTokenResult =
    suspendCancellableCoroutine { continuation ->
        NidOAuth.requestLogin(context, object : NidOAuthCallback {
            override fun onSuccess() {
                if (!continuation.isActive) return
                val token = NidOAuth.getAccessToken()
                continuation.resume(
                    if (token != null) {
                        NaverTokenResult.Success(token)
                    } else {
                        NaverTokenResult.Failure("네이버 토큰이 비어 있습니다")
                    }
                )
            }

            override fun onFailure(errorCode: String, errorDesc: String) {
                if (!continuation.isActive) return
                val isCancel =
                    NidOAuth.getLastErrorCode() == NidOAuthErrorCode.CLIENT_USER_CANCEL
                continuation.resume(
                    if (isCancel) {
                        NaverTokenResult.Cancelled
                    } else {
                        Timber.w("네이버 로그인 실패($errorCode): $errorDesc")
                        NaverTokenResult.Failure("네이버 로그인에 실패했습니다. 다시 시도해주세요")
                    }
                )
            }
        })
    }

/** 기기에 남은 네이버 토큰 삭제 */
fun clearNaverSession() {  // 다음 네이버 로그인에서 계정을 다시 고름
    if (NidOAuth.getRefreshToken() == null) return
    NidOAuth.logout(object : NidOAuthCallback {
        override fun onSuccess() = Unit

        override fun onFailure(errorCode: String, errorDesc: String) {
            Timber.w("네이버 로그아웃 실패($errorCode): $errorDesc")
        }
    })
}

suspend fun disconnectNaver() {  // 탈퇴 때 서버 쪽 연동까지 끊음
    if (NidOAuth.getRefreshToken() == null) return
    suspendCancellableCoroutine { continuation ->
        NidOAuth.disconnect(object : NidOAuthCallback {
            override fun onSuccess() {
                if (continuation.isActive) continuation.resume(Unit)
            }

            override fun onFailure(errorCode: String, errorDesc: String) {
                Timber.w("네이버 연동 해제 실패($errorCode): $errorDesc")
                if (continuation.isActive) continuation.resume(Unit)
            }
        })
    }
}
