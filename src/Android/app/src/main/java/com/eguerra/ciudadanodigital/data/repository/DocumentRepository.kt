package com.eguerra.ciudadanodigital.data.repository

import android.net.Uri
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.DocumentModel

interface DocumentRepository {
    suspend fun getDocuments(remote: Boolean): Resource<Pair<String, List<DocumentModel>>>
    suspend fun deleteDocument(documentId: Long): Resource<String>
    suspend fun saveDocument(
        filename: String, author: String, year: Int, fileUri: Uri, minAge: Int, maxAge: Int
    ): Resource<String>
}