package com.webssa.guestbest.config.model

import android.net.Uri
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfigModel(

        @Json(name = "color_primary")
        val colorPrimary: ConfigColor,

        @Json(name = "color_accent")
        val colorAccent: ConfigColor,

        @Json(name = "text_color_primary")
        val textColorPrimary: ConfigColor,

        @Json(name = "ic_document_menu_item")
        val iconDocumentMenuItem: Uri ?= null,

        @Json(name = "ic_document_section_toc_item")
        val iconDocumentSectionTOCItem: Uri ?= null,

        @Json(name = "menu_icon_color_as_text")
        val isMenuIconColorAsText: Boolean = false
)
