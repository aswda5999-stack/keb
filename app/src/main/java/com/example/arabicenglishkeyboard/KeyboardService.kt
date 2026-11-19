package com.example.arabicenglishkeyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import android.media.AudioManager
import android.content.Context

class KeyboardService : InputMethodService() {

    private var isArabic = true
    private var shift = false
    private var symbols = false
    private var emojiMode = false
    private var decorationMode = false

    private lateinit var keyboard: LinearLayout
    private lateinit var prefs: Prefs
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null

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
        prefs = Prefs(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        
        // إنشاء قناة الإشعارات وعرض الإشعار الدائم
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.showNotification(this)
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
        
        // جلب الثيم الحالي من الإعدادات
        val colors = Themes.getTheme(prefs.theme)
        keyboard.setBackgroundColor(colors.bg)

        // تحديد الصفوف بناءً على الوضع الحالي
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
                val displayLabel = if (!isArabic && shift && key.length == 1 && key[0].isLetter()) key.uppercase() else key
                addKey(rowLayout, displayLabel, colors) {
                    when (key) {
                        "⇧" -> { shift = !shift; renderKeyboard() }
                        "⌫" -> deleteCharacter()
                        else -> {
                            if (emojiMode) {
                                currentInputConnection?.commitText(key, 1)
                            } else {
                                typeText(key)
                            }
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
        
        // زر الزخرفة
        addKey(bottom, "✨", colors) {
            decorationMode = !decorationMode
            Toast.makeText(this, if (decorationMode) "وضع الزخرفة مفعل" else "وضع الزخرفة مغلق", Toast.LENGTH_SHORT).show()
        }

        addKey(bottom, "مسافة", colors, weight = 3f) {
            typeText(" ")
        }

        addKey(bottom, "↵", colors) { pressEnter() }

        keyboard.addView(bottom, rowParams(54))
    }

    private fun rowParams(heightDp: Int) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(heightDp)
    )

    private fun addKey(row: LinearLayout, label: String, colors: Themes.ThemeColors, weight: Float = 1f, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            textSize = if (label.length > 2) 14f else 18f
            setTextColor(colors.text)
            setBackgroundColor(colors.key)
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
        var finalText = if (!isArabic && shift && text.length == 1 && text[0].isLetter()) text.uppercase() else text
        
        // تطبيق الزخرفة إذا كان الوضع مفعل
        if (decorationMode && finalText != " ") {
             finalText = TextUtils.decorateText(finalText, TextUtils.decorations.random())
        }

        currentInputConnection?.commitText(finalText, 1)
        prefs.incrementChars()
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
        if (prefs.soundEnabled) {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.5f)
        }
    }

    private fun vibrate() {
        if (prefs.vibrateEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
