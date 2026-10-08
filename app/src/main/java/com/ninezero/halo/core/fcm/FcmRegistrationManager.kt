package com.ninezero.halo.core.fcm

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fcmDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "fcm_registration"
)

/** FCM 식별자 캐시 */
@Singleton
class FcmRegistrationManager @Inject constructor(  // FID는 onRegistered로만 도착
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.fcmDataStore
    private val fid = MutableStateFlow<String?>(null)

    /** FID 저장 */
    suspend fun cache(installationId: String) {  // 대기 중인 getFid 깨움
        dataStore.edit { it[FID_KEY] = installationId }
        fid.value = installationId
    }

    /** FID 삭제 */
    suspend fun clear() {
        dataStore.edit { it.remove(FID_KEY) }
        fid.value = null
    }

    fun requestRegistration() {  // 결과는 onRegistered로 도착
        runCatching { Firebase.messaging.register() }
            .onFailure { Timber.e(it, "FCM 등록 요청 실패") }
    }

    suspend fun getFid(): String? {  // 없으면 등록 후 짧게 대기, 실패 시 null
        fid.value?.let { return it }

        dataStore.data.map { it[FID_KEY] }.firstOrNull()?.let { stored ->
            fid.value = stored
            return stored
        }

        return try {
            Firebase.messaging.register().await()
            withTimeoutOrNull(REGISTER_TIMEOUT_MS) { fid.filterNotNull().first() }
        } catch (e: Exception) {
            Timber.e(e, "FCM 등록 실패")
            null
        }
    }

    companion object {
        private val FID_KEY = stringPreferencesKey("fid")
        private const val REGISTER_TIMEOUT_MS = 5_000L
    }
}
