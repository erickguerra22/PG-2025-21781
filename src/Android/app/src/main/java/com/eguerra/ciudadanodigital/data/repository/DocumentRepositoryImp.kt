package com.eguerra.ciudadanodigital.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.Database
import com.eguerra.ciudadanodigital.data.local.entity.DocumentModel
import com.eguerra.ciudadanodigital.data.remote.API
import com.eguerra.ciudadanodigital.data.remote.ErrorParser
import com.eguerra.ciudadanodigital.data.remote.dto.responses.GetDocumentsResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.SimpleMessageResponse
import com.eguerra.ciudadanodigital.data.remote.dto.toDocumentModel
import com.eguerra.ciudadanodigital.helpers.handleException
import com.eguerra.ciudadanodigital.helpers.queryFileName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class DocumentRepositoryImp @Inject constructor(
    private val api: API,
    val context: Context,
    private val database: Database,
    private val errorParser: ErrorParser,
    private val authRepository: AuthRepository
) : DocumentRepository {

    private var repositoryName: String = "DocumentRepository"
    override suspend fun getDocuments(remote: Boolean): Resource<Pair<String, List<DocumentModel>>> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403, activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val result = api.getDocuments("Bearer $token")

                    return if (result.isSuccessful) {
                        val response: GetDocumentsResponse? = result.body()
                        if (response == null) {
                            return Resource.Error(404, "Respuesta vacía del servidor.")
                        }

                        val (message, documents) = response
                        Resource.Success(Pair(message, documents.map { it.toDocumentModel() }))
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(
                            result.code(), error?.error ?: "No se obtuvieron resultados."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("getDocuments", repositoryName, ex)
        }
    }

    override suspend fun saveDocument(
        filename: String, author: String, year: Int, fileUri: Uri, minAge: Int, maxAge:Int
    ): Resource<String> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403, activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data

                    val filenameBody =
                        filename.toRequestBody("text/plain".toMediaTypeOrNull())
                    val authorBody = author.toRequestBody("text/plain".toMediaTypeOrNull())
                    val yearBody =
                        year.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val minAgeBody = minAge.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val maxAgeBody = maxAge.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                    val contentResolver = context.contentResolver
                    val mimeType = contentResolver.getType(fileUri)
                        ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            MimeTypeMap.getFileExtensionFromUrl(fileUri.toString())
                        )
                        ?: "application/octet-stream"

                    val documentName = queryFileName(contentResolver, fileUri) ?: filename

                    val inputStream = contentResolver.openInputStream(fileUri)
                        ?: return Resource.Error(400, "No se pudo abrir el archivo.")
                    val fileBytes = inputStream.readBytes()
                    val fileRequestBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())

                    val filePart =
                        MultipartBody.Part.createFormData("file", documentName, fileRequestBody)

                    val result = api.saveDocument(
                        token = "Bearer $token",
                        filename = filenameBody,
                        author = authorBody,
                        year = yearBody,
                        file = filePart,
                        minAge = minAgeBody,
                        maxAge = maxAgeBody,
                    )

                    return if (result.isSuccessful) {
                        val response: SimpleMessageResponse? = result.body()
                        if (response == null) {
                            return Resource.Error(404, "Respuesta vacía del servidor.")
                        }
                        Resource.Success(response.message)
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(
                            result.code(),
                            error?.error ?: "No se pudo almacenar el documento."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("saveDocument", repositoryName, ex)
        }
    }

    override suspend fun deleteDocument(documentId: Long): Resource<String> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403, activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val result = api.deleteDocument("Bearer $token",
                        documentId)

                    return if (result.isSuccessful) {
                        val response: SimpleMessageResponse? = result.body()
                        if (response == null) {
                            return Resource.Error(404, "Respuesta vacía del servidor.")
                        }

                        val (message) = response
                        Resource.Success(message)
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(
                            result.code(), error?.error ?: "No se obtuvieron resultados."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("getDocuments", repositoryName, ex)
        }
    }
}