package com.example.arabicenglishkeyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
            setBackgroundColor(Color.rgb(245, 247, 250))
        }

        val title = TextView(this).apply {
            text = "كيبورد عربي إنجليزي\n(النسخة الاحترافية 2.0)"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(33, 37, 41))
        }

        val info = TextView(this).apply {
            text = "مرحباً بك! لتشغيل الكيبورد:\n1. اضغط تفعيل لوحة المفاتيح\n2. اخترها من قائمة لوحات المفاتيح."
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(73, 80, 87))
            setPadding(0, 32, 0, 32)
        }

        val settingsButton = Button(this).apply {
            text = "تفعيل لوحة المفاتيح"
            isAllCaps = false
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        val chooseButton = Button(this).apply {
            text = "اختيار لوحة المفاتيح"
            isAllCaps = false
            setOnClickListener {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.showInputMethodPicker()
            }
        }

        val prefs = getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

        val soundSwitch = Switch(this).apply {
            text = "تشغيل صوت الكتابة"
            isChecked = prefs.getBoolean("sound", true)
            setPadding(0, 32, 0, 0)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("sound", isChecked).apply()
            }
        }

        val vibrateSwitch = Switch(this).apply {
            text = "تشغيل الاهتزاز عند الضغط"
            isChecked = prefs.getBoolean("vibrate", true)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("vibrate", isChecked).apply()
            }
        }

        val themesLabel = TextView(this).apply {
            text = "تغيير الثيم (اللون):"
            textSize = 16f
            setPadding(0, 32, 0, 8)
            setTextColor(Color.rgb(33, 37, 41))
        }

        val themeButton = Button(this).apply {
            text = "تغيير لون الكيبورد"
            isAllCaps = false
            setOnClickListener {
                val themes = listOf("light", "dark", "green", "blue", "purple")
                val current = prefs.getString("theme", "light") ?: "light"
                val nextIndex = (themes.indexOf(current) + 1) % themes.size
                prefs.edit().putString("theme", themes[nextIndex]).apply()
                text = "الثيم الحالي: ${themes[nextIndex]}"
            }
        }

        val statsLabel = TextView(this).apply {
            val count = prefs.getInt("chars_typed", 0)
            text = "إحصائيات: لقد كتبت $count حرفاً حتى الآن!"
            textSize = 14f
            setPadding(0, 32, 0, 0)
            setTextColor(Color.rgb(100, 100, 100))
        }

        layout.addView(title)
        layout.addView(info)
        layout.addView(settingsButton, buttonParams())
        layout.addView(chooseButton, buttonParams())
        layout.addView(soundSwitch, buttonParams())
        layout.addView(vibrateSwitch, buttonParams())
        layout.addView(themesLabel)
        layout.addView(themeButton, buttonParams())
        layout.addView(statsLabel)

        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun buttonParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { setMargins(0, 12, 0, 12) }
}
