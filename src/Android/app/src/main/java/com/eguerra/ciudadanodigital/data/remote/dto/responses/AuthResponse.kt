package com.eguerra.ciudadanodigital.data.remote.dto.responses

data class AuthResponse(
    val token: String,
    val expiresAt: Long,
    val refreshToken: String,
    val refreshExpiresAt: Long
)