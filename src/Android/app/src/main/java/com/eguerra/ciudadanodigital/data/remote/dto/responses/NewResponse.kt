package com.eguerra.ciudadanodigital.data.remote.dto.responses

import com.eguerra.ciudadanodigital.data.remote.dto.MessageDto

data class NewResponse(
    val message: String,
    val newChat: Boolean,
    val chatMessage: MessageDto,
)