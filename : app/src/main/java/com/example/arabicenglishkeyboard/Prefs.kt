package com.example.arabicenglishkeyboard

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    // --- الثيمات ---
    var theme: String
        get() = prefs.getString("theme", "light") ?: "light"
        set(value) = prefs.edit().putString("theme", value).apply()

    // --- اللغة (هل يفتح على العربي أم الإنجليزي) ---
    var lastLanguage: String
        get() = prefs.getString("last_lang", "ar") ?: "ar"
        set(value) = prefs.edit().putString("last_lang", value).apply()

    // --- الصوت والاهتزاز ---
    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound", true)
        set(value) = prefs.edit().putBoolean("sound", value).apply()

    var vibrateEnabled: Boolean
        get() = prefs.getBoolean("vibrate", true)
        set(value) = prefs.edit().putBoolean("vibrate", value).apply()

    // --- الإحصائيات ---
    var charsTyped: Int
        get() = prefs.getInt("chars_typed", 0)
        set(value) = prefs.edit().putInt("chars_typed", value).apply()

    fun incrementChars() {
        charsTyped = charsTyped + 1
    }
}
