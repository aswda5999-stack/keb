package com.example.arabicenglishkeyboard

import android.graphics.Color
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

class KeyboardService : InputMethodService() {
    private var arabic = true
    private var upperCase = false
    private lateinit var keyboard: LinearLayout

    private val arabicRows = listOf(
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
        listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط"),
        listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ")
    )

    private val englishRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫")
    )

    override fun onCreateInputView(): View {
        keyboard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 8, 4, 8)
            setBackgroundColor(Color.rgb(225, 229, 235))
        }
        renderKeyboard()
        return keyboard
    }

    private fun renderKeyboard() {
        keyboard.removeAllViews()

        val rows = if (arabic) arabicRows else englishRows

        rows.forEachIndexed { index, row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            row.forEach { key ->
                addKey(rowLayout, key) {
                    when (key) {
                        "⇧" -> {
                            upperCase = !upperCase
                            renderKeyboard()
                        }
                        "⌫" -> deleteCharacter()
                        else -> {
                            val text =
                                if (!arabic && upperCase) key.uppercase() else key
                            typeText(text)
                        }
                    }
                }
            }

            keyboard.addView(
                rowLayout,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(if (index == 1) 52 else 50)
                )
            )
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addKey(bottom, if (arabic) "EN" else "ع") {
            arabic = !arabic
            upperCase = false
            renderKeyboard()
        }
        addKey(bottom, "؟123") { typeText("123") }
        addKey(bottom, "،") { typeText(if (arabic) "،" else ",") }
        addKey(bottom, "مسافة", weight = 4f) { typeText(" ") }
        addKey(bottom, ".") { typeText(".") }
        addKey(bottom, "↵") { pressEnter() }

        keyboard.addView(
            bottom,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )
    }

    private fun addKey(
        row: LinearLayout,
        label: String,
        weight: Float = 1f,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = if (label.length > 2) 13f else 18f
            setTextColor(Color.BLACK)
            setTypeface(typeface, Typeface.NORMAL)
            isAllCaps = false
            setPadding(0, 0, 0, 0)
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.MATCH_PARENT,
            weight
        )
        params.setMargins(dp(2), dp(2), dp(2), dp(2))
        row.addView(button, params)
    }

    private fun typeText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun deleteCharacter() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun pressEnter() {
        val connection = currentInputConnection ?: return
        val action = currentInputEditorInfo
            ?.imeOptions
            ?.and(EditorInfo.IME_MASK_ACTION)
            ?: EditorInfo.IME_ACTION_NONE

        if (action != EditorInfo.IME_ACTION_NONE &&
            action != EditorInfo.IME_ACTION_UNSPECIFIED
        ) {
            connection.performEditorAction(action)
        } else {
            typeText("\n")
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
