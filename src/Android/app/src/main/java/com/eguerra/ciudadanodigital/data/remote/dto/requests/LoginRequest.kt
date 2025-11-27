package com.eguerra.ciudadanodigital.data.remote.dto.requests

data class LoginRequest(
    val email: String, val password: String, val deviceId: String
)