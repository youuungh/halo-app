package com.ninezero.halo.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.deviceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "device_id"
)

@Singleton
class DeviceIdProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.deviceDataStore
    private val mutex = Mutex()

    @Volatile private var cached: String? = null

    suspend fun getDeviceId(): String {
        cached?.let { return it }
        return mutex.withLock {
            cached?.let { return it }
            val stored = dataStore.data.map { it[DEVICE_ID_KEY] }.firstOrNull()
            val id = stored ?: UUID.randomUUID().toString().also { newId ->
                dataStore.edit { it[DEVICE_ID_KEY] = newId }
            }
            cached = id
            id
        }
    }

    companion object {
        private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }
}
