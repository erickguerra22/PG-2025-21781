package com.eguerra.ciudadanodigital.data.remote.dto.responses

data class VerifyRecoveryResponse(
    val token: String,
    val expiresAt: Long,
    val message: String,
)