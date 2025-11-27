package com.eguerra.ciudadanodigital.data.repository

import android.content.Context
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.Database
import com.eguerra.ciudadanodigital.data.local.entity.MessageModel
import com.eguerra.ciudadanodigital.data.remote.API
import com.eguerra.ciudadanodigital.data.remote.ErrorParser
import com.eguerra.ciudadanodigital.data.remote.dto.requests.NewMessageRequest
import com.eguerra.ciudadanodigital.data.remote.dto.responses.GetChatMessagesResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.NewMessageResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.NewResponse
import com.eguerra.ciudadanodigital.data.remote.dto.toMessageModel
import com.eguerra.ciudadanodigital.helpers.handleException
import javax.inject.Inject

class MessageRepositoryImp @Inject constructor(
    private val api: API,
    val context: Context,
    private val database: Database,
    private val errorParser: ErrorParser,
    private val authRepository: AuthRepository
) : MessageRepository {

    private var repositoryName: String = "MessageRepository"
    override suspend fun getChatMessages(
        chatId: String, limit: Int?, offset: Int?, remote: Boolean
    ): Resource<List<MessageModel>> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403,activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val formattedChatId = chatId.toLongOrNull()

                    if (formattedChatId == null) return Resource.Error(400,"El chatId no es válido")

                    val result = api.getChatMessages(
                        "Bearer $token", formattedChatId, limit, offset
                    )

                    return if (result.isSuccessful) {
                        val response: GetChatMessagesResponse? = result.body()
                        if (response == null) {
                            return Resource.Error(404,"Respuesta vacía del servidor.")
                        }

                        val (messages) = response
                        Resource.Success(messages.map { message -> message.toMessageModel() })
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(result.code(),
                            error?.error ?: "No se pudo obtener los mensajes del chat."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("getChatMessages", repositoryName, ex)
        }
    }

    override suspend fun createMessage(
        chatId: String?, content: String
    ): Resource<Pair<MessageModel, String>> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403,activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val formattedChatId = chatId?.toLongOrNull()

                    val result = if (formattedChatId == null) api.createMessageUnassigned(
                        "Bearer $token", NewMessageRequest(
                            content = content
                        )
                    )
                    else api.createMessage(
                        "Bearer $token", formattedChatId, NewMessageRequest(
                            content = content
                        )
                    )

                    return if (result.isSuccessful) {
                        val response: NewMessageResponse? = result.body()
                        if (response == null) {
                            return Resource.Error(404,"Respuesta vacía del servidor.")
                        }

                        val (message, chatMessage) = response
                        Resource.Success(Pair(chatMessage.toMessageModel(), message))
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(result.code(),
                            error?.error ?: "No se pudo enviar el mensaje."
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            return handleException("createMessage", repositoryName, ex)
        }
    }

    override suspend fun assignMessage(
        messageId: String, chatId: String
    ): Resource<Boolean> {
        println("MESSAGEID: $messageId")
        println("CHATID: $chatId")
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403,activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val formattedChatId = chatId.toLongOrNull()
                    val formattedMessageId = messageId.toLongOrNull()

                    if (formattedChatId == null) return Resource.Error(400,"El chatId no es válido")
                    if (formattedMessageId == null) return Resource.Error(400,"El messageId no es válido")

                    val result = api.assignMessage(
                        "Bearer $token",
                        formattedMessageId,
                        formattedChatId,
                    )

                    return if (result.isSuccessful) {
                        val message: MessageModel? = result.body()?.toMessageModel()
                        if (message == null) {
                            return Resource.Error(404,"Respuesta vacía del servidor.")
                        }

                        Resource.Success(true)
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(result.code(),
                            error?.error ?: "No se pudo obtener los mensajes del chat."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("getChatMessages", repositoryName, ex)
        }
    }

    override suspend fun getResponse(
        chatId: String?, question: String
    ): Resource<Triple<MessageModel, String, Boolean>> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403,activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val formattedChatId = chatId?.toLongOrNull()

                    val result = if(formattedChatId != null) api.getResponse(
                        "Bearer $token",
                        formattedChatId,
                        question
                    ) else api.getResponseUnassigned(
                        "Bearer $token",
                        question
                    )

                    return if (result.isSuccessful) {
                        val response: NewResponse? = result.body()
                        if (response == null) {
                            return Resource.Error(404,"Respuesta vacía del servidor.")
                        }

                        val (message, newChat, chatMessage) = response
                        Resource.Success(Triple(chatMessage.toMessageModel(), message, newChat))
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(result.code(),
                            error?.error ?: "No se pudo obtener los mensajes del chat."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("getResponse", repositoryName, ex)
        }
    }

}