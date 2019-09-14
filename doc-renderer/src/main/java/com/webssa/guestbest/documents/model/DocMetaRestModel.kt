package com.webssa.guestbest.documents.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
class DocMetaRestModel (
        @Json(name = "title")
        val documentTitle: String,

        @Json(name = "category")
        val documentCategory: String,

        @Json(name = "theme")
        val documentTheme: String,

        @Json(name = "author")
        val author: String,

        @Json(name = "department")
        val department: String?=null,

        @Json(name = "created_at")
        val createdAt: Date,

        @Json(name = "last_edited")
        val lastEdited: Date?=null,

        @Json(name = "resource_id")
        val resourceId: String?=null,

        @Json(name = "completion")
        val campletionLevel: Float=0f,

        @Json(name = "user_rating")
        val userRating: Float=0f

)