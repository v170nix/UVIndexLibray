@file:Suppress("unused")

package uv.index.lib.data

import android.content.SharedPreferences

class UVSkinRepository(private val preferences: SharedPreferences) {

    fun getSkinOrNull(): UVSkinType? = runCatching {
        UVSkinType.values()[preferences.getInt(PREF_KEY, -1)]
    }.getOrNull()

    fun setSkin(skin: UVSkinType) {
        preferences.edit().putInt(PREF_KEY, skin.ordinal).apply()
    }

    private companion object {
        private const val PREF_KEY = "app.skin.type"
    }

}