package com.eguerra.ciudadanodigital.data.remote.dto.responses

import com.eguerra.ciudadanodigital.data.remote.dto.ChatDto

data class GetResponseResponse(
    val chats: List<ChatDto>,
)