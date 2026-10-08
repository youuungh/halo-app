package com.ninezero.halo.core.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences"
)

@Singleton
class PreferencesManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.appPreferencesDataStore

    companion object {
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        private val MY_PRODUCTS_GROUP_COLORS_KEY = stringPreferencesKey("my_products_group_colors")
        private val CART_SELECTED_ITEM_IDS_KEY = stringPreferencesKey("cart_selected_item_ids")
        private val CART_KNOWN_ITEM_IDS_KEY = stringPreferencesKey("cart_known_item_ids")
        private val PAYMENT_PROVIDER_BY_USER_KEY = stringPreferencesKey("payment_provider_by_user")
    }

    /** 마지막 선택 결제수단 */
    suspend fun getPaymentProvider(userId: Int): String? {  // 없으면 null
        val preferences = dataStore.data.first()
        return decodeStringMap(preferences[PAYMENT_PROVIDER_BY_USER_KEY])[userId.toString()]
    }

    suspend fun setPaymentProvider(userId: Int, provider: String) {
        dataStore.edit { preferences ->
            val current = decodeStringMap(preferences[PAYMENT_PROVIDER_BY_USER_KEY]).toMutableMap()
            current[userId.toString()] = provider
            preferences[PAYMENT_PROVIDER_BY_USER_KEY] = Json.encodeToString(current)
        }
    }

    private fun decodeStringMap(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching { Json.decodeFromString<Map<String, String>>(raw) }.getOrElse { emptyMap() }
    }


    fun observeThemeMode(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    fun observeMyProductsGroupColors(): Flow<Map<Int, Int>> {
        return dataStore.data.map { preferences ->
            decodeIntMap(preferences[MY_PRODUCTS_GROUP_COLORS_KEY])
        }
    }

    suspend fun setMyProductsGroupColor(tagId: Int, colorArgb: Int?) {
        dataStore.edit { preferences ->
            val current = decodeIntMap(preferences[MY_PRODUCTS_GROUP_COLORS_KEY]).toMutableMap()
            if (colorArgb != null) {
                current[tagId] = colorArgb
            } else {
                current.remove(tagId)
            }
            preferences[MY_PRODUCTS_GROUP_COLORS_KEY] = encodeIntMap(current)
        }
    }

    suspend fun removeMyProductsGroupColors(tagIds: Set<Int>) {
        if (tagIds.isEmpty()) return

        dataStore.edit { preferences ->
            val current = decodeIntMap(preferences[MY_PRODUCTS_GROUP_COLORS_KEY]).toMutableMap()
            tagIds.forEach(current::remove)
            preferences[MY_PRODUCTS_GROUP_COLORS_KEY] = encodeIntMap(current)
        }
    }


    suspend fun getCartSelection(): Pair<Set<Int>, Set<Int>> {  // 선택 id와 마지막으로 본 id
        val preferences = dataStore.data.first()
        return decodeIntSet(preferences[CART_SELECTED_ITEM_IDS_KEY]) to
            decodeIntSet(preferences[CART_KNOWN_ITEM_IDS_KEY])
    }

    suspend fun setCartSelection(selectedItemIds: Set<Int>, knownItemIds: Set<Int>) {
        dataStore.edit { preferences ->
            preferences[CART_SELECTED_ITEM_IDS_KEY] = encodeIntSet(selectedItemIds)
            preferences[CART_KNOWN_ITEM_IDS_KEY] = encodeIntSet(knownItemIds)
        }
    }

    suspend fun setCartSelectedItemIds(selectedItemIds: Set<Int>) {  // 마지막으로 본 id는 유지
        dataStore.edit { preferences ->
            preferences[CART_SELECTED_ITEM_IDS_KEY] = encodeIntSet(selectedItemIds)
        }
    }

    private fun decodeIntSet(raw: String?): Set<Int> {
        if (raw.isNullOrBlank()) return emptySet()
        return runCatching { Json.decodeFromString<Set<Int>>(raw) }.getOrElse { emptySet() }
    }

    private fun encodeIntSet(values: Set<Int>): String = Json.encodeToString(values)

    private fun decodeIntMap(raw: String?): Map<Int, Int> {
        if (raw.isNullOrBlank()) return emptyMap()

        return runCatching {
            Json.decodeFromString<Map<String, Int>>(raw)
                .mapNotNull { (key, value) -> key.toIntOrNull()?.let { it to value } }
                .toMap()
        }.getOrElse { emptyMap() }
    }

    private fun encodeIntMap(values: Map<Int, Int>): String {
        return Json.encodeToString(values.mapKeys { (key, _) -> key.toString() })
    }
}
