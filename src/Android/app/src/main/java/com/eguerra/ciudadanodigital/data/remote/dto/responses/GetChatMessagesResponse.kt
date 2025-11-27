package com.eguerra.ciudadanodigital.data.remote.dto.responses

import com.eguerra.ciudadanodigital.data.remote.dto.MessageDto

data class GetChatMessagesResponse(
    val messages: List<MessageDto>,
)