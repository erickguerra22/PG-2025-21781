package com.eguerra.ciudadanodigital.data.remote.dto.responses

import com.eguerra.ciudadanodigital.data.remote.dto.DocumentDto

data class GetDocumentsResponse(
    val message: String,
    val documents: List<DocumentDto>,
)