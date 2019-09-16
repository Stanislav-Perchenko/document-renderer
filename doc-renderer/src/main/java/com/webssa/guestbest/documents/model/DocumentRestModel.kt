package com.webssa.guestbest.documents.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
class DocumentRestModel (
        @Json(name = "document_id")
        val documentId: Long,

        @Json(name = "meta")
        val meta: DocMetaRestModel,

        @Json(name = "sections")
        val sections: List<DocSectionRestModel>
)