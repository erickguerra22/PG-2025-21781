package com.eguerra.ciudadanodigital.helpers

fun getDocumentType(extension: String): DocumentType {
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "bmp", "webp" -> DocumentType.IMAGE
        "pdf" -> DocumentType.PDF
        "doc", "docx" -> DocumentType.WORD
        "txt", "text" -> DocumentType.TEXT
        "xls", "xlsx" -> DocumentType.EXCEL
        "ppt", "pptx" -> DocumentType.POWERPOINT
        else -> DocumentType.UNKNOWN
    }
}

enum class DocumentType {
    IMAGE, PDF, WORD, TEXT, EXCEL, POWERPOINT, UNKNOWN
}