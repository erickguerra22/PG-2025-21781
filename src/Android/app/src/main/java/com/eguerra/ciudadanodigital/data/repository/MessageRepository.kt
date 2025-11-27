package com.eguerra.ciudadanodigital.data.repository

import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.MessageModel

interface MessageRepository {
    suspend fun getChatMessages(
        chatId: String,
        limit: Int?,
        offset: Int?,
        remote: Boolean
    ): Resource<List<MessageModel>>

    suspend fun createMessage(
        chatId: String?,
        content: String,
    ): Resource<Pair<MessageModel, String>>

    suspend fun assignMessage(
        messageId: String,
        chatId: String,
    ): Resource<Boolean>

    suspend fun getResponse(
        chatId: String?,
        question: String,
    ): Resource<Triple<MessageModel, String, Boolean>>
}