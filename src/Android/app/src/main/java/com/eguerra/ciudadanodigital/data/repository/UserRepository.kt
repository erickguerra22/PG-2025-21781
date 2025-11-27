package com.eguerra.ciudadanodigital.data.repository

import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.entity.UserModel

interface UserRepository {
    suspend fun getLoggedUserData(remote: Boolean): Resource<UserModel>
    suspend fun register(
        email: String,
        name: String,
        lastname: String,
        phoneCode: String,
        phoneNumber: String,
        password: String,
        birthdate: String,
    ): Resource<Boolean>

    suspend fun updateUser(
        email: String?,
        name: String?,
        lastname: String?,
        phoneCode: String?,
        phoneNumber: String?,
        birthdate: String?,
    ): Resource<UserModel>
}