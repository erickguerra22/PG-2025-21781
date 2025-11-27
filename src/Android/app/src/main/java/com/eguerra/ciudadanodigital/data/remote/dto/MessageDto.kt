package com.eguerra.ciudadanodigital.data.remote.dto

import com.eguerra.ciudadanodigital.data.local.entity.MessageModel
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter


data class MessageDto(
    val messageid: Long,
    val chatid: Long?,
    val source: String,
    val content: String,
    val reference: String?,
    val timestamp: String,
    val assigned: Boolean,
    val responsetime: Long?
)

fun MessageDto.toMessageModel(): MessageModel {
    return MessageModel(
        messageId = this.messageid,
        chatId = this.chatid,
        source = this.source,
        content = this.content,
        reference = this.reference,
        sentAt = LocalDateTime.parse(
            this.timestamp,
            DateTimeFormatter.ISO_DATE_TIME
        ).atOffset(ZoneOffset.UTC).toLocalDateTime(),
        assigned = this.assigned,
        responseTime = this.responsetime
    )
}