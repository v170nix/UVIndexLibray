@file:Suppress("unused")

package uv.index.lib.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class UVSkinRepository(private val preferences: DataStore<Preferences>) {

    suspend fun getSkinOrNull(): UVSkinType? = runCatching {
        val preferences = preferences.data.firstOrNull()
        UVSkinType.values()[preferences?.get(NEW_PREF_KEY) ?: -1]
    }.getOrNull()

    suspend fun getSkinOrNull(default: UVSkinType): UVSkinType = runCatching {
        val preferences = preferences.data.firstOrNull()
        UVSkinType.values()[preferences?.get(NEW_PREF_KEY) ?: -1]
    }.getOrDefault(default)

    suspend fun setSkin(skin: UVSkinType) {
        preferences.edit {
            it[NEW_PREF_KEY] = skin.ordinal
        }
    }

    fun asFlow(default: UVSkinType): Flow<UVSkinType> {
        return preferences.data
            .map { it[NEW_PREF_KEY] ?: -1 }
            .map { UVSkinType.values()[it] }
            .catch {
                emit(default)
            }
    }

    private companion object {
        private const val PREF_KEY = "app.skin.type"
        private val NEW_PREF_KEY = intPreferencesKey(name = "app.pref.skin.type")
    }

}