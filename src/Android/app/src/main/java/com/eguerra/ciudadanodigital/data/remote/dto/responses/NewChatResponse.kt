package com.eguerra.ciudadanodigital.data.remote.dto.responses

import com.eguerra.ciudadanodigital.data.remote.dto.ChatDto

data class NewChatResponse(
    val message: String,
    val chat: ChatDto,
)