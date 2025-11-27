package com.eguerra.ciudadanodigital.data.repository

import android.content.Context
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.Database
import com.eguerra.ciudadanodigital.data.local.MyDataStore
import com.eguerra.ciudadanodigital.data.local.entity.UserModel
import com.eguerra.ciudadanodigital.data.remote.API
import com.eguerra.ciudadanodigital.data.remote.ErrorParser
import com.eguerra.ciudadanodigital.data.remote.dto.requests.RegisterRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.UpdateUserRequest
import com.eguerra.ciudadanodigital.data.remote.dto.responses.AuthResponse
import com.eguerra.ciudadanodigital.data.remote.dto.toUserModel
import com.eguerra.ciudadanodigital.helpers.getDeviceId
import com.eguerra.ciudadanodigital.helpers.handleException
import com.eguerra.ciudadanodigital.ui.util.castDateToISO
import javax.inject.Inject

class UserRepositoryImp @Inject constructor(
    private val api: API,
    val context: Context,
    private val database: Database,
    private val errorParser: ErrorParser,
    private val authRepository: AuthRepository
) : UserRepository {

    private var repositoryName: String = "UserRepository"

    override suspend fun getLoggedUserData(remote: Boolean): Resource<UserModel> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    println("ERROR: ${activeSession.message}")
                    return Resource.Error(403,activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    println("TOKEN: $token")
                    val result = api.getLoggedUser("Bearer $token")
                    println("RESULT: $result")

                    return if (result.isSuccessful) {
                        val user: UserModel? = result.body()?.toUserModel()
                        println("USER: $user")
                        if (user == null) {
                            return Resource.Error(404,"Respuesta vacía del servidor.")
                        }

                        Resource.Success(user)
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(result.code(),
                            error?.error ?: "No se pudo obtener los datos del usuario."
                        )
                    }
                }

            }
        } catch (ex: Exception) {
            return handleException("getLoggedUserData", repositoryName, ex)
        }
    }

    override suspend fun register(
        email: String,
        name: String,
        lastname: String,
        phoneCode: String,
        phoneNumber: String,
        password: String,
        birthdate: String
    ): Resource<Boolean> {
        try {
            val result = api.register(
                RegisterRequest(
                    email = email,
                    names = name,
                    lastnames = lastname,
                    phoneCode = phoneCode,
                    phoneNumber = phoneNumber,
                    password = password,
                    birthdate = castDateToISO(birthdate),
                    deviceId = getDeviceId(context)
                )
            )

            return if (result.isSuccessful) {
                val result: AuthResponse? = result.body()
                if (result == null) {
                    return Resource.Error(404,"Respuesta vacía del servidor.")
                }

                return authRepository.login(email, password)
            } else {
                val error = errorParser.parseErrorObject(result.errorBody())
                Resource.Error(result.code(),
                    error?.error ?: "No se pudo registrar al usuario."
                )
            }
        } catch (ex: Exception) {
            return handleException("register", repositoryName, ex)
        }
    }

    override suspend fun updateUser(
        email: String?,
        name: String?,
        lastname: String?,
        phoneCode: String?,
        phoneNumber: String?,
        birthdate: String?
    ): Resource<UserModel> {
        try {
            when (val activeSession = authRepository.refreshToken()) {
                is Resource.Error -> {
                    return Resource.Error(403,activeSession.message ?: "Error al validar sesión")
                }

                is Resource.Success -> {
                    val token = activeSession.data
                    val ds = MyDataStore(context)
                    val storedUserId =
                        ds.getValueFromKey("userId") ?: return Resource.Error(404,"No userId")

                    val userId = storedUserId.toLongOrNull()

                    if (userId == null) return Resource.Error(400,"El userId no es válido")

                    val result = api.updateUser(
                        "Bearer $token", userId, UpdateUserRequest(
                            email = email,
                            names = name,
                            lastnames = lastname,
                            phoneCode = phoneCode,
                            phoneNumber = phoneNumber,
                            birthdate = castDateToISO(birthdate)
                        )
                    )

                    return if (result.isSuccessful) {
                        val user: UserModel? = result.body()?.toUserModel()
                        if (user == null) {
                            return Resource.Error(404,"Respuesta vacía del servidor.")
                        }

                        Resource.Success(user)
                    } else {
                        val error = errorParser.parseErrorObject(result.errorBody())
                        Resource.Error(result.code(),
                            error?.error ?: "No se pudo actualizar el usuario."
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            return handleException("updateUser", repositoryName, ex)
        }
    }

}