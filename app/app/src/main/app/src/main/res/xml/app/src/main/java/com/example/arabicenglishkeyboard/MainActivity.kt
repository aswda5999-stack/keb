package com.example.arabicenglishkeyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }

        val title = TextView(this).apply {
            text = "كيبورد عربي إنجليزي"
            textSize = 24f
        }

        val info = TextView(this).apply {
            text = "فعّل لوحة المفاتيح من إعدادات الجهاز، ثم اخترها عند الكتابة."
            textSize = 16f
            setPadding(0, 20, 0, 20)
        }

        val settingsButton = Button(this).apply {
            text = "تفعيل لوحة المفاتيح"
            setOnClickListener {
                startActivity(Intent("android.settings.INPUT_METHOD_SETTINGS"))
            }
        }

        val chooseButton = Button(this).apply {
            text = "اختيار لوحة المفاتيح"
            setOnClickListener {
                val manager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.showInputMethodPicker()
            }
        }

        layout.addView(title)
        layout.addView(info)
        layout.addView(settingsButton)
        layout.addView(chooseButton)
        setContentView(layout)
    }
}
