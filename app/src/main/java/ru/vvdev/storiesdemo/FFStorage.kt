package ru.vvdev.storiesdemo

import android.content.Context
import android.content.SharedPreferences
import ru.vvdev.storiesdemo.AppFeatures.IS_AUTO_OPEN_UNREAD

/**
 * Пример хранения фф в апке
 * */
class FFStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "storageApp", Context.MODE_PRIVATE
    )

    fun saveToken(string: String) {
        prefs.edit().putString("TOKEN", string).apply()
    }

    fun getToken(): String? {
        return prefs.getString("TOKEN", null)
    }

    fun setIsAutoOpenUnread(enable: Boolean) {
        prefs.edit().putBoolean(IS_AUTO_OPEN_UNREAD, enable).apply()
    }

    fun getIsAutoOpenUnread(): Boolean {
        return prefs.getBoolean(IS_AUTO_OPEN_UNREAD, false)
    }
}

object AppFeatures {
    val IS_AUTO_OPEN_UNREAD = "is_auto_open_unread"
}
