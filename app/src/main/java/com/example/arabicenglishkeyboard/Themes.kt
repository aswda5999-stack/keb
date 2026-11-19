package com.example.arabicenglishkeyboard

import android.graphics.Color

object Themes {

    data class ThemeColors(
        val bg: Int,
        val key: Int,
        val text: Int,
        val accent: Int
    )

    fun getTheme(themeName: String): ThemeColors {
        return when (themeName) {
            "dark" -> ThemeColors(
                bg = Color.rgb(30, 30, 30),
                key = Color.rgb(60, 60, 60),
                text = Color.WHITE,
                accent = Color.rgb(100, 100, 100)
            )
            "green" -> ThemeColors(
                bg = Color.rgb(200, 230, 200),
                key = Color.rgb(150, 200, 150),
                text = Color.BLACK,
                accent = Color.rgb(120, 180, 120)
            )
            "blue" -> ThemeColors(
                bg = Color.rgb(200, 220, 240),
                key = Color.rgb(150, 180, 220),
                text = Color.BLACK,
                accent = Color.rgb(120, 150, 200)
            )
            "purple" -> ThemeColors(
                bg = Color.rgb(230, 200, 240),
                key = Color.rgb(200, 150, 220),
                text = Color.BLACK,
                accent = Color.rgb(170, 120, 190)
            )
            else -> ThemeColors(
                bg = Color.rgb(225, 229, 235),
                key = Color.rgb(250, 250, 250),
                text = Color.BLACK,
                accent = Color.rgb(180, 185, 195)
            )
        }
    }
}
