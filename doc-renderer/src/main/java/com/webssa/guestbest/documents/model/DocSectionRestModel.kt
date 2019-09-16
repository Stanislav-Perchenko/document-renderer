package com.webssa.guestbest.documents.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocSectionRestModel (
        @Json(name = "is_foldable")
        val isFoldable: Boolean = false,

        @Json(name = "is_folded")
        val isFolded: Boolean = false,

        @Json(name = "header")
        val sectionHeader: DocSectionHeaderRestModel ?= null,

        @Json(name = "rows")
        val rows: List<DocSectionRowRestModel>
)