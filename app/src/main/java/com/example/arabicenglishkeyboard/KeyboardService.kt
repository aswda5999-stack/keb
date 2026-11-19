package com.example.arabicenglishkeyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

class KeyboardService : InputMethodService() {

    private var isArabic = true
    private var shift = false
    private var symbols = false
    private var emojiMode = false
    private var theme = "light"

    private lateinit var keyboard: LinearLayout
    private lateinit var prefs: android.content.SharedPreferences
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null

    // إحصائيات
    private var charsTyped = 0

    private val arabicRows = listOf(
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
        listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
        listOf("⇧", "ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ", "⌫")
    )

    private val englishRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫")
    )

    private val symbolsRows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")"),
        listOf("!", "\"", "'", ":", ";", "/", "?", "،", "⌫")
    )

    private val emojiRows = listOf(
        listOf("😀", "😁", "😂", "🤣", "😊", "😍", "😘", "😎", "🤩", "🥳"),
        listOf("😢", "😭", "😡", "🤔", "😴", "🥺", "😱", "🤯", "😇", "🤗"),
        listOf("👍", "👎", "👏", "🙏", "💪", "👋", "✌️", "🤝", "❤️", "💔"),
        listOf("🔥", "✨", "⭐", "🌟", "🌹", "🌸", "🎉", "🎁", "💡", "💯")
    )

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
        theme = prefs.getString("theme", "light") ?: "light"
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    override fun onCreateInputView(): View {
        keyboard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 8, 4, 8)
        }
        renderKeyboard()
        return keyboard
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun renderKeyboard() {
        keyboard.removeAllViews()
        val colors = getThemeColors(theme)
        keyboard.setBackgroundColor(colors.bg)

        val rows = when {
            emojiMode -> emojiRows
            symbols -> symbolsRows
            isArabic -> arabicRows
            else -> englishRows
        }

        rows.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { key ->
                addKey(rowLayout, if (!isArabic && shift && key.length == 1 && key[0].isLetter()) key.uppercase() else key, colors) {
                    when (key) {
                        "⇧" -> { shift = !shift; renderKeyboard() }
                        "⌫" -> deleteCharacter()
                        else -> {
                            if (emojiMode) typeEmoji(key) else typeText(key)
                        }
                    }
                }
            }
            keyboard.addView(rowLayout, rowParams(52))
        }

        // صف التحكم السفلي
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addKey(bottom, if (isArabic) "EN" else "ع", colors) {
            isArabic = !isArabic
            shift = false
            emojiMode = false
            symbols = false
            renderKeyboard()
        }

        addKey(bottom, if (emojiMode) "ABC" else "😊", colors) {
            emojiMode = !emojiMode
            symbols = false
            renderKeyboard()
        }

        addKey(bottom, if (symbols) "أبج" else "؟123", colors) {
            symbols = !symbols
            emojiMode = false
            renderKeyboard()
        }

        addKey(bottom, "مسافة", colors, weight = 4f) {
            if (!emojiMode) typeText(" ") else typeText(" ")
        }

        addKey(bottom, "↵", colors) { pressEnter() }

        keyboard.addView(bottom, rowParams(54))
    }

    private fun rowParams(heightDp: Int) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(heightDp)
    )

    private fun addKey(row: LinearLayout, label: String, colors: ThemeColors, weight: Float = 1f, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = if (label.length > 2) 14f else 18f
            setTextColor(colors.text)
            setBackgroundColor(colors.key)
            setTypeface(typeface, Typeface.NORMAL)
            isAllCaps = false
            setPadding(0, 0, 0, 0)
            setOnClickListener {
                playSound()
                vibrate()
                action()
            }
        }

        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight)
            .apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
        row.addView(button, params)
    }

    private fun typeText(text: String) {
        val finalText = if (!isArabic && shift && text.length == 1 && text[0].isLetter()) text.uppercase() else text
        currentInputConnection?.commitText(finalText, 1)
        charsTyped++
        prefs.edit().putInt("chars_typed", prefs.getInt("chars_typed", 0) + 1).apply()
    }

    private fun typeEmoji(emoji: String) {
        currentInputConnection?.commitText(emoji, 1)
    }

    private fun deleteCharacter() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun pressEnter() {
        val connection = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            connection.performEditorAction(action)
        } else {
            connection.commitText("\n", 1)
        }
    }

    private fun playSound() {
        if (prefs.getBoolean("sound", true)) {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.5f)
        }
    }

    private fun vibrate() {
        if (prefs.getBoolean("vibrate", true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // --- الألوان ---
    data class ThemeColors(val bg: Int, val key: Int, val text: Int)

    private fun getThemeColors(theme: String): ThemeColors {
        return when (theme) {
            "dark" -> ThemeColors(Color.rgb(30, 30, 30), Color.rgb(60, 60, 60), Color.WHITE)
            "green" -> ThemeColors(Color.rgb(200, 230, 200), Color.rgb(150, 200, 150), Color.BLACK)
            "blue" -> ThemeColors(Color.rgb(200, 220, 240), Color.rgb(150, 180, 220), Color.BLACK)
            "purple" -> ThemeColors(Color.rgb(230, 200, 240), Color.rgb(200, 150, 220), Color.BLACK)
            else -> ThemeColors(Color.rgb(225, 229, 235), Color.rgb(250, 250, 250), Color.BLACK)
        }
    }
}
