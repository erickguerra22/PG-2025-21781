package com.eguerra.ciudadanodigital.ui.fragment.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.DocumentModel
import com.eguerra.ciudadanodigital.data.repository.DocumentRepository
import com.eguerra.ciudadanodigital.ui.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {

    private val _saveDocumentStateFlow: MutableStateFlow<Status<String>> =
        MutableStateFlow(Status.Default())
    val saveDocumentStateFlow: StateFlow<Status<String>> = _saveDocumentStateFlow

    fun saveDocument(
        filename: String, author: String, year: Int, fileUri: Uri, minAge: Int, maxAge: Int
    ) {
        _saveDocumentStateFlow.value = Status.Loading()
        viewModelScope.launch {
            when (val result = repository.saveDocument(
                filename = filename, author = author, year = year, fileUri = fileUri, minAge = minAge, maxAge = maxAge
            )) {
                is Resource.Success -> {
                    _saveDocumentStateFlow.value = Status.Success(result.data)
                }

                else -> {
                    _saveDocumentStateFlow.value = Status.Error(
                        result.code ?: 500,
                        result.message ?: "Ocurrió un error al enviar el mensaje."
                    )
                }
            }
        }
    }

    private val _getDocumentsStateFlow: MutableStateFlow<Status<Pair<String, List<DocumentModel>>>> =
        MutableStateFlow(Status.Default())
    val getDocumentsStateFlow: StateFlow<Status<Pair<String, List<DocumentModel>>>> =
        _getDocumentsStateFlow

    fun getDocuments(remote: Boolean = false) {
        _getDocumentsStateFlow.value = Status.Loading()
        viewModelScope.launch {
            when (val result = repository.getDocuments(remote)) {
                is Resource.Success -> {
                    _getDocumentsStateFlow.value =
                        Status.Success(Pair(result.data.first, result.data.second))
                }

                else -> {
                    _getDocumentsStateFlow.value = Status.Error(
                        result.code ?: 500,
                        result.message ?: "Ocurrió un error al obtener la respuesta."
                    )
                }
            }
        }
    }

    private val _deleteDocumentStateFlow: MutableStateFlow<Status<String>> =
        MutableStateFlow(Status.Default())
    val deleteDocumentStateFlow: StateFlow<Status<String>> =
        _deleteDocumentStateFlow

    fun deleteDocument(documentId: Long) {
        _deleteDocumentStateFlow.value = Status.Loading()
        viewModelScope.launch {
            when (val result = repository.deleteDocument(documentId)) {
                is Resource.Success -> {
                    _deleteDocumentStateFlow.value =
                        Status.Success(result.data)
                }

                else -> {
                    _deleteDocumentStateFlow.value = Status.Error(
                        result.code ?: 500,
                        result.message ?: "Ocurrió un error al obtener la respuesta."
                    )
                }
            }
        }
    }
}
