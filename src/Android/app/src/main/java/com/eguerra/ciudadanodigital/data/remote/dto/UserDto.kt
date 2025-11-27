package com.eguerra.ciudadanodigital.data.remote.dto

import com.eguerra.ciudadanodigital.data.local.entity.UserModel
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter


data class UserDto(
    val userid: Long,
    val email: String,
    val names: String,
    val lastnames: String,
    val birthdate: String,
    val phonecode: String,
    val phonenumber: String,
    val role: String,
)

fun UserDto.toUserModel(): UserModel {
    return UserModel(
        userId = this.userid,
        email = this.email,
        names = this.names,
        lastnames = this.lastnames,
        birthdate = LocalDateTime.parse(
            this.birthdate,
            DateTimeFormatter.ISO_DATE_TIME
        ).atOffset(ZoneOffset.UTC).toLocalDateTime(),
        phoneCode = this.phonecode,
        phoneNumber = this.phonenumber,
        role = this.role,
    )
}