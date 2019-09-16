package com.webssa.guestbest.documents.model

import android.net.Uri
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocCellRestModel (
        @Json(name = "aspect_ratio")
        val aspectRatio: Float = 0f,

        @Json(name = "width")
        val width: Int = 0,

        @Json(name = "height")
        val height: Int = 0,

        @Json(name = "style")
        val style: DocStyleRestModel ?= null,

        @Json(name = "text")
        val optText: String ?= null,

        @Json(name = "image")
        val optImage: Uri ?= null,

        @Json(name = "video")
        val optVideo: Uri ?= null
)