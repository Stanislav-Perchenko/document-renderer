package com.webssa.guestbest.documents.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.webssa.guestbest.config.model.ConfigColor

@JsonClass(generateAdapter = true)
data class DocStyleRestModel (
        @Json(name = "background-color")
        val bgColor: ConfigColor ?= null,

        @Json(name = "text-color")
        val textColor: ConfigColor ?= null,

        @Json(name = "font-size")
        val textSize: Int ?= 0,

        @Json(name = "font-weight")
        val textWeight: DocStyleTextWeight = DocStyleTextWeight.REGULAR,

        @Json(name = "text-align")
        val textAlign: DocStyleTextAlign = DocStyleTextAlign.LEFT,

        @Json(name = "vertical-align")
        val verticalAlign: DocStyleVerticalAlign = DocStyleVerticalAlign.TOP,

        @Json(name = "text-decoration")
        val textDecoration: DocStyleTextDecoration ?= null,

        @Json(name = "text-decoration-color")
        val textDecorationColor: ConfigColor ?= null
) {
    companion object {
        val DEFAULT_TEXT_STYLE = DocStyleRestModel(bgColor= ConfigColor.TRANSPARENT, textColor = ConfigColor.TEXT_DARK, textSize = null)
    }
}