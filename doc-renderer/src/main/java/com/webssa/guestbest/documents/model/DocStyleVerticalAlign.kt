package com.webssa.guestbest.documents.model

import com.squareup.moshi.Json

enum class DocStyleVerticalAlign {
    @Json(name = "bottom") BOT,
    @Json(name = "middle") MID,
    @Json(name = "top") TOP
}