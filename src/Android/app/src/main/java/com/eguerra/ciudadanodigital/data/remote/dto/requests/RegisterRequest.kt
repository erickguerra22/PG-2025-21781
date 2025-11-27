package com.eguerra.ciudadanodigital.data.remote.dto.requests

data class RegisterRequest(
    val email: String,
    val names: String,
    val lastnames: String,
    val phoneCode: String,
    val phoneNumber: String,
    val password: String,
    val birthdate: String,
    val deviceId: String,
)