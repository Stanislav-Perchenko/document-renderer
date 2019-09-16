package com.webssa.guestbest.documents.model

import com.squareup.moshi.Json

enum class DocStyleTextAlign {
    @Json(name = "center") CENTER,
    @Json(name = "left") LEFT,
    @Json(name = "right") RIGHT
}