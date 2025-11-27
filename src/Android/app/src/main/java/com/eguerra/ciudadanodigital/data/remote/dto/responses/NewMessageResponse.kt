package com.eguerra.ciudadanodigital.data.remote.dto.responses

import com.eguerra.ciudadanodigital.data.remote.dto.MessageDto

data class NewMessageResponse(
    val message: String,
    val chatMessage: MessageDto,
)