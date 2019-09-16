package com.webssa.guestbest.documents.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocSectionHeaderRestModel (
        @Json(name = "margin_top")
        val marginTop: Int = 0,

        @Json(name = "text")
        val title: String,

        @Json(name = "style")
        val style: DocStyleRestModel ?= null
)