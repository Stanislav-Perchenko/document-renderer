package com.webssa.guestbest.documents.model

data class DocumentIdPointer (
        val isLocal: Boolean,

        val localDocumentName: String ?= null,

        val documentId: Long ?= 0
)