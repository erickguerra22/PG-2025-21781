package com.eguerra.ciudadanodigital.data.repository

import android.content.Context
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.Database
import com.eguerra.ciudadanodigital.data.local.entity.ChatModel
import com.eguerra.ciudadanodigital.data.remote.API
import com.eguerra.ciudadanodigital.data.remote.ErrorParser
import com.eguerra.ciudadanodigital.data.remote.dto.requests.NewChatRequest
import com.eguerra.ciudadanodigital.data.remote.dto.responses.GetChatsResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.NewChatResponse
import com.eguerra.ciudadanodigital.data.remote.dto.toChatModel
import com.eguerra.ciudadanodigital.helpers.handleException
import javax.inject.Inject

class ChatRepositoryImp @Inject constructor(
    private val api: API,
    val context: Context,
    private val database: Database,
    private val errorParser: ErrorParser,
    private val authRepository: AuthRepository
) : ChatRepository {

    private var repositoryName: String = "ChatRepository"
    override suspend fun getUserChats(remote: Boolean): Resource<List<ChatModel>> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403,activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val result = api.getUserChats("Bearer $token")

                    return if (result.isSuccessful) {
                        val response: GetChatsResponse? = result.body()
                        if (response == null) {
                            return Resource.Error(404,"Respuesta vacía del servidor.")
                        }

                        val (chats) = response
                        Resource.Success(chats.map { chat -> chat.toChatModel() })
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(result.code(),
                            error?.error ?: "No se obtuvieron resultados."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("getUseChats", repositoryName, ex)
        }
    }

    override suspend fun createChat(name: String): Resource<Pair<ChatModel, String>> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403,activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val result = api.createChat(
                        "Bearer $token", NewChatRequest(
                            name = name
                        )
                    )

                    return if (result.isSuccessful) {
                        val response: NewChatResponse? = result.body()
                        if (response == null) {
                            return Resource.Error(404,"Respuesta vacía del servidor.")
                        }

                        val (message, chat) = response
                        Resource.Success(Pair(chat.toChatModel(), message))
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(result.code(),
                            error?.error ?: "No se pudo obtener los datos del usuario."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("createChat", repositoryName, ex)
        }
    }
}