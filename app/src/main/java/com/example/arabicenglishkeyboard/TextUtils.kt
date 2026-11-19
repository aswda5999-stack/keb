package com.example.arabicenglishkeyboard

object TextUtils {

    // أشكال الزخرفة الجاهزة
    val decorations = listOf(
        "☬", "✿", "❀", "✾", "❁", "♔", "♕", "★", "☆", "✪", "✯", "✰",
        "❥", "❤", "♡", "➳", "➶", "✈", "☾", "☽", "☀", "☁", "⚡", "☘"
    )

    // زخرفة النص (إحاطة النص بالرمز)
    fun decorateText(text: String, decoration: String): String {
        if (text.isEmpty()) return decoration
        return "$decoration $text $decoration"
    }

    // تحويل الأرقام إلى أرقام عربية مزخرفة (اختياري)
    fun toArabicNumbers(input: String): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val builder = StringBuilder()
        for (char in input) {
            if (char in '0'..'9') {
                builder.append(arabicDigits[char - '0'])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }

    // تحويل النص الإنجليزي إلى حروف مزخرفة (Stylish Font)
    fun fancyEnglish(input: String): String {
        val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val fancy = "𝓪𝓫𝓬𝓭𝓮𝓯𝓰𝓱𝓲𝓳𝓴𝓵𝓶𝓷𝓸𝓹𝓺𝓻𝓼𝓽𝓾𝓿𝔀𝔁𝔂𝔃𝓐𝓑𝓒𝓓𝓔𝓕𝓖𝓗𝓘𝓙𝓚𝓛𝓜𝓝𝓞𝓟𝓠𝓡𝓢𝓣𝓤𝓥𝓦𝓧𝓨𝓩"
        val builder = StringBuilder()
        for (char in input) {
            val index = normal.indexOf(char)
            if (index != -1) {
                builder.append(fancy[index])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }
}
