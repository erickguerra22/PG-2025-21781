package com.eguerra.ciudadanodigital.data.remote.dto

import com.eguerra.ciudadanodigital.data.local.entity.ChatModel
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter


data class ChatDto(
    val chatid: Long,
    val userid: Long,
    val fechainicio: String?,
    val nombre: String
)

fun ChatDto.toChatModel(): ChatModel {
    return ChatModel(
        chatId = this.chatid,
        userId = this.userid,
        fechaInicio = LocalDateTime.parse(
            this.fechainicio,
            DateTimeFormatter.ISO_DATE_TIME
        ).atOffset(ZoneOffset.UTC).toLocalDateTime(),
        nombre = this.nombre
    )
}