package com.webssa.guestbest.config.model

import android.graphics.Color

data class ConfigColor (val textColor: String, val intColor: Int) {
    constructor(textColor: String) : this(textColor, Color.parseColor(textColor))

    constructor(intColor: Int) : this(String.format("#%08X", 0xFFFFFFFFL and intColor.toLong()), intColor)

    companion object {
        val TRANSPARENT = ConfigColor("#00FFFFFF")
        val TEXT_DARK = ConfigColor("#00222222")
    }
}
