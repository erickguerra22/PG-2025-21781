package com.eguerra.ciudadanodigital.data.repository

import com.eguerra.ciudadanodigital.data.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<Boolean>
    suspend fun logout(): Resource<Pair<Boolean, String>>
    suspend fun refreshToken(): Resource<String>
    suspend fun sendRecovery(email: String): Resource<String>
    suspend fun verifyCode(email: String, code: Int): Resource<Pair<Boolean, String>>
    suspend fun recoverPassword(newPassword: String): Resource<Pair<Boolean, String>>
}