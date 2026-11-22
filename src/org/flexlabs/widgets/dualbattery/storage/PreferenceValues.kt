package org.flexlabs.widgets.dualbattery.storage

import android.content.SharedPreferences

object PreferenceValues {
    @JvmStatic
    fun getInt(preferences: SharedPreferences, key: String, defaultValue: Int): Int {
        return when (val value = preferences.all[key]) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: defaultValue
            else -> defaultValue
        }
    }

    @JvmStatic
    fun migrateToString(preferences: SharedPreferences, key: String) {
        val value = preferences.all[key] as? Number ?: return
        preferences.edit().putString(key, value.toString()).apply()
    }
}
