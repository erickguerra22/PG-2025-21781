package com.eguerra.ciudadanodigital.data.remote.dto

import com.eguerra.ciudadanodigital.data.local.entity.DocumentModel


data class DocumentDto(
    val documentid: Long,
    val userid: Long,
    val category: Int?,
    val title: String,
    val author: String,
    val year: Int,
    val presignedUrl: String
)

fun DocumentDto.toDocumentModel(): DocumentModel {
    return DocumentModel(
        documentId = this.documentid,
        userId = this.userid,
        category = this.category ?: 0,
        title = this.title,
        author = this.author,
        year = this.year,
        documentUrl = this.presignedUrl
    )
}