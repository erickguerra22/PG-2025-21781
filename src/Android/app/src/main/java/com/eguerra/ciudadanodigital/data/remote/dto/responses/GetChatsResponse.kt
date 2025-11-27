package com.eguerra.ciudadanodigital.data.remote.dto.responses

import com.eguerra.ciudadanodigital.data.remote.dto.ChatDto

data class GetChatsResponse(
    val chats: List<ChatDto>,
)