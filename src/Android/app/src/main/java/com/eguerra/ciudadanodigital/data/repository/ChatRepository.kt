package com.eguerra.ciudadanodigital.data.repository

import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.ChatModel

interface ChatRepository {
    suspend fun getUserChats(remote: Boolean): Resource<List<ChatModel>>
    suspend fun createChat(
        name: String,
    ): Resource<Pair<ChatModel, String>>
}