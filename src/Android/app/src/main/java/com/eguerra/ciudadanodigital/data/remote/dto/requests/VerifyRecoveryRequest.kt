package com.eguerra.ciudadanodigital.data.remote.dto.requests

data class VerifyRecoveryRequest(
    val email: String,
    val code: Int
)