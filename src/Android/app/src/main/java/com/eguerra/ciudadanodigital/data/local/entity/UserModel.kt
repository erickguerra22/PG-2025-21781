package com.eguerra.ciudadanodigital.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime

@Entity
data class UserModel(
    @PrimaryKey val userId: Long,
    val email: String,
    val names: String,
    val lastnames: String,
    val birthdate: LocalDateTime,
    val phoneCode: String,
    val phoneNumber: String,
    val role: String,
)