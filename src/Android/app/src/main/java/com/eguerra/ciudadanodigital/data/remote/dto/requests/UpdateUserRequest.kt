package com.eguerra.ciudadanodigital.data.remote.dto.requests

data class UpdateUserRequest(
    val email: String?,
    val names: String?,
    val lastnames: String?,
    val phoneCode: String?,
    val phoneNumber: String?,
    val birthdate: String?,
)