package com.ninezero.halo.feature.auth.presentation.social

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ninezero.halo.BuildConfig
import timber.log.Timber

sealed interface GoogleIdTokenResult {
    data class Success(val idToken: String) : GoogleIdTokenResult
    data object Cancelled : GoogleIdTokenResult
    data class Failure(val message: String) : GoogleIdTokenResult
}

suspend fun requestGoogleIdToken(context: Context): GoogleIdTokenResult {
    val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_SERVER_CLIENT_ID).build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    return try {
        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            GoogleIdTokenResult.Failure("지원하지 않는 자격 증명 형식입니다")
        }
    } catch (e: GetCredentialCancellationException) {
        GoogleIdTokenResult.Cancelled
    } catch (e: NoCredentialException) {
        GoogleIdTokenResult.Failure("기기에 구글 계정이 없습니다. 계정을 추가한 뒤 다시 시도해주세요")
    } catch (e: GetCredentialException) {
        Timber.w(e, "구글 로그인 실패")
        GoogleIdTokenResult.Failure("구글 로그인에 실패했습니다. 다시 시도해주세요")
    }
}
