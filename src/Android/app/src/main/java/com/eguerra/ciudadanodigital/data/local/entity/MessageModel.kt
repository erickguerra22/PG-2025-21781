package com.eguerra.ciudadanodigital.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime
import java.util.Date

@Entity
data class MessageModel(
    @PrimaryKey val messageId: Long,
    val chatId: Long?,
    val source: String,
    val content: String,
    val reference: String?,
    val sentAt: LocalDateTime,
    val assigned: Boolean,
    val responseTime: Long?
)