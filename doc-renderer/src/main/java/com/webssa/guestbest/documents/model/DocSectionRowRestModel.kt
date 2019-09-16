package com.webssa.guestbest.documents.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocSectionRowRestModel (
        @Json(name = "margin_top")
        val marginTop: Int = 0,

        @Json(name = "cells")
        val cells: List<DocCellRestModel>?
)