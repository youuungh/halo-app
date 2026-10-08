package com.ninezero.halo.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ninezero.halo.core.network.auth.TokenProvider
import com.ninezero.halo.core.security.TokenCipher
import com.ninezero.halo.feature.auth.domain.model.AuthToken
import com.ninezero.halo.feature.auth.domain.repository.TokenStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "auth_tokens"
)

/** 토큰 저장소 */
@Singleton
class TokenManager @Inject constructor(  // Keystore 암호화, 복호화 실패 시 null
    @param:ApplicationContext private val context: Context,
    private val cipher: TokenCipher
) : TokenStore, TokenProvider {
    private val dataStore = context.tokenDataStore

    @Volatile private var cachedAccessToken: String? = null
    @Volatile private var cachedRefreshToken: String? = null
    @Volatile private var cachedExpiresAtMs: Long? = null
    @Volatile private var cachedUserId: Int? = null
    @Volatile private var loaded = false
    private val loadMutex = Mutex()

    private val _loggedIn = MutableStateFlow(false)
    val loggedIn: StateFlow<Boolean> = _loggedIn.asStateFlow()

    // access token 회전 성공 시 방출, 소켓 재연결용
    private val _tokenRefreshed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val tokenRefreshed: SharedFlow<Unit> = _tokenRefreshed.asSharedFlow()

    private fun publishLoginState() {
        _loggedIn.value = cachedAccessToken != null
    }

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val EXPIRES_AT_KEY = longPreferencesKey("access_token_expires_at")
        private val USER_ID_KEY = intPreferencesKey("user_id")
    }

    /** 캐시 1회 로드 */
    private suspend fun ensureLoaded() {  // mutex 직렬화
        if (loaded) return
        loadMutex.withLock {
            if (loaded) return
            val prefs = dataStore.data.firstOrNull()
            cachedAccessToken = prefs?.get(ACCESS_TOKEN_KEY)?.let { cipher.decrypt(it) }
            cachedRefreshToken = prefs?.get(REFRESH_TOKEN_KEY)?.let { cipher.decrypt(it) }
            cachedExpiresAtMs = prefs?.get(EXPIRES_AT_KEY)
            cachedUserId = prefs?.get(USER_ID_KEY)
            loaded = true
            publishLoginState()
        }
    }

    /** 남은 초를 절대 만료 시각으로 */
    private fun expiresAtFrom(expiresInSeconds: Long?): Long? =
        expiresInSeconds?.let { System.currentTimeMillis() + it * 1000 }

    /** 토큰과 사용자 id 저장 */
    override suspend fun saveLoginResult(token: AuthToken, userId: Int) {  // 단일 DataStore 트랜잭션
        val expiresAtMs = expiresAtFrom(token.expiresIn)
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = cipher.encrypt(token.accessToken)
            token.refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = cipher.encrypt(it) }
            expiresAtMs?.let { preferences[EXPIRES_AT_KEY] = it }
            preferences[USER_ID_KEY] = userId
        }
        cachedAccessToken = token.accessToken
        cachedRefreshToken = token.refreshToken
        cachedExpiresAtMs = expiresAtMs
        cachedUserId = userId
        loaded = true
        publishLoginState()
    }

    override suspend fun getAccessToken(): String? {
        ensureLoaded()
        return cachedAccessToken
    }

    override fun getAccessTokenBlocking(): String? {  // 캐시 없으면 1회 runBlocking
        if (loaded) return cachedAccessToken
        return runBlocking {
            ensureLoaded()
            cachedAccessToken
        }
    }

    override fun getExpiresAtBlocking(): Long? {
        if (loaded) return cachedExpiresAtMs
        return runBlocking {
            ensureLoaded()
            cachedExpiresAtMs
        }
    }

    override suspend fun getRefreshToken(): String? {
        ensureLoaded()
        return cachedRefreshToken
    }

    override suspend fun updateTokens(accessToken: String, refreshToken: String, expiresInSeconds: Long?) {
        val expiresAtMs = expiresAtFrom(expiresInSeconds)
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = cipher.encrypt(accessToken)
            preferences[REFRESH_TOKEN_KEY] = cipher.encrypt(refreshToken)
            if (expiresAtMs != null) preferences[EXPIRES_AT_KEY] = expiresAtMs
            else preferences.remove(EXPIRES_AT_KEY)
        }
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
        cachedExpiresAtMs = expiresAtMs
        loaded = true
        publishLoginState()
        _tokenRefreshed.tryEmit(Unit)
    }

    override suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(EXPIRES_AT_KEY)
        }
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedExpiresAtMs = null
        loaded = true
        publishLoginState()
    }

    suspend fun hasToken(): Boolean {
        // 저장값이 있어도 복호화할 수 없으면 로그인 아님
        ensureLoaded()
        return cachedAccessToken != null
    }

    override suspend fun getUserId(): Int? {
        ensureLoaded()
        return cachedUserId
    }

    override suspend fun clearUserId() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
        cachedUserId = null
    }
}
