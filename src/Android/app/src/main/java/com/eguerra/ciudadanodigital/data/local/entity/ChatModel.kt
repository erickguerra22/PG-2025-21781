package com.eguerra.ciudadanodigital.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime
import java.util.Date

@Entity
data class ChatModel(
    @PrimaryKey val chatId: Long,
    val userId: Long,
    val fechaInicio: LocalDateTime,
    val nombre: String,
)