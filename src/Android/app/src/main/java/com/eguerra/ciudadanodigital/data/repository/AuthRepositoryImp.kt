package com.eguerra.ciudadanodigital.data.repository

import android.content.Context
import com.eguerra.ciudadanodigital.data.Resource
import com.eguerra.ciudadanodigital.data.local.Database
import com.eguerra.ciudadanodigital.data.local.MyDataStore
import com.eguerra.ciudadanodigital.data.remote.API
import com.eguerra.ciudadanodigital.data.remote.ErrorParser
import com.eguerra.ciudadanodigital.data.remote.dto.requests.EmailRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.LoginRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.PasswordRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.RefreshRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.VerifyRecoveryRequest
import com.eguerra.ciudadanodigital.data.remote.dto.responses.AuthResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.SimpleMessageResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.VerifyRecoveryResponse
import com.eguerra.ciudadanodigital.data.remote.dto.toUserModel
import com.eguerra.ciudadanodigital.helpers.InternetStatusManager
import com.eguerra.ciudadanodigital.helpers.SessionManager
import com.eguerra.ciudadanodigital.helpers.getDeviceId
import com.eguerra.ciudadanodigital.helpers.handleException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AuthRepositoryImp @Inject constructor(
    private val api: API,
    val context: Context,
    private val database: Database,
    private val errorParser: ErrorParser,
    private val sessionManager: SessionManager
) : AuthRepository {

    private var repositoryName: String = "AuthRepository"
    private suspend fun getUser(token: String): Resource<Boolean> {
        val resultUser = api.getLoggedUser("Bearer $token")
        val rawData = resultUser.body()

        return if (resultUser.isSuccessful && rawData != null) {
            val userData = rawData.toUserModel()

            val ds = MyDataStore(context)
            ds.saveKeyValue("userId", userData.userId.toString())
            database.userDao().insertUser(userData)
            Resource.Success(true)

        } else {
            val errorBody = resultUser.errorBody()
            val error = errorParser.parseErrorObject(errorBody)

            Resource.Error(resultUser.code(), error?.let {
                it.error ?: "Error al obtener los datos del usuario."
            } ?: "Error al obtener los datos del usuario.")
        }
    }

    override suspend fun login(email: String, password: String): Resource<Boolean> {
        try {
            val deviceId = getDeviceId(context)
            val result =
                api.login(LoginRequest(deviceId = deviceId, email = email, password = password))

            return if (result.isSuccessful) {

                val response: AuthResponse? = result.body()
                if (response == null) {
                    return Resource.Error(result.code(), "Respuesta vacía del servidor.")
                }

                val ds = MyDataStore(context)
                val (token, expiresAtSeconds, refresh) = response
                val expiresAtMillis = expiresAtSeconds * 1000L
                val expireDate = Date(expiresAtMillis)

                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val expireDateString = formatter.format(expireDate)

                ds.saveKeyValue("token", token)
                ds.saveKeyValue("expire", expireDateString)
                ds.saveKeyValue("refreshToken", refresh)

                return try {
                    getUser(token)
                } catch (ex: Exception) {
                    handleException("login", repositoryName, ex)
                }
            } else {
                val error = errorParser.parseErrorObject(result.errorBody())
                Resource.Error(result.code(),
                    error?.error ?: "Usuario o contraseña incorrectos."
                )
            }

        } catch (ex: Exception) {
            return handleException("login", repositoryName, ex)
        }
    }

    override suspend fun refreshToken(): Resource<String> {
        try {
            val ds = MyDataStore(context)
            val expire = ds.getValueFromKey("expire") ?: return Resource.Error(404,"No expire")
            val token: String = ds.getValueFromKey("token") ?: return Resource.Error(404,"No token")
            val refreshToken =
                ds.getValueFromKey("refreshToken") ?: return Resource.Error(404,"No refreshToken")

            val expireDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expireDate: Date = expireDateFormat.parse(expire) ?: Date()
            val currentTime = Calendar.getInstance().time

            val internetConnection = InternetStatusManager.getLastConnectionState()

            if (!internetConnection) {
                println("NO INTERNET")
                ds.saveKeyValue("offline", "true")

                return if (token.isNotBlank()) {
                    Resource.Success(token)
                } else {
                    Resource.Error(500,"No hay conexión a internet y no se encontró token válido almacenado")
                }
            }

            println("CURRENTTIME: $currentTime")
            println("EXPIREDATE: $expireDate")

            if (currentTime >= expireDate) {
                if (refreshToken.isBlank()) return Resource.Error(404,"No refreshToken")

                val result = api.refreshToken(token = "Bearer $token", RefreshRequest(refreshToken))

                if (result.isSuccessful) {
                    val response: AuthResponse? = result.body()

                    if (response == null) {
                        return Resource.Error(404,"Respuesta vacía del servidor.")
                    }

                    val (newToken, expiresIn, newRefresh) = response

                    val expiresInMillis = expiresIn * 1000L
                    val expireDate = Date(System.currentTimeMillis() + expiresInMillis)

                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val expireDateString = formatter.format(expireDate)

                    ds.saveKeyValue("token", newToken)
                    ds.saveKeyValue("expire", expireDateString)
                    ds.saveKeyValue("refreshToken", newRefresh)
                    ds.saveKeyValue("offline", "false")

                    return Resource.Success(newToken)
                } else {
                    val error = errorParser.parseErrorObject(result.errorBody())
                    sessionManager.triggerLogout(error?.error ?: "La sesión ha caducado.")
                    return Resource.Error(403,"Sesión inválida")
                }
            }
            ds.saveKeyValue("offline", "false")
            return if (token.isNotBlank()) Resource.Success(token) else Resource.Error(404,"No token")
        } catch (ex: Exception) {
            return handleException("refreshToken", repositoryName, ex)
        }
    }

    override suspend fun sendRecovery(email: String): Resource<String> {
        try {
            val result = api.sendRecoveryCode(EmailRequest(email))

            return if (result.isSuccessful) {
                val response: SimpleMessageResponse? = result.body()
                if (response == null) {
                    return Resource.Error(404,"Respuesta vacía del servidor.")
                }

                val (message) = response
                Resource.Success(message)
            } else {
                val error = errorParser.parseErrorObject(result.errorBody())
                Resource.Error(result.code(),
                    error?.error ?: "No se pudo enviar el código de recuperación."
                )
            }
        } catch (ex: Exception) {
            return handleException("sendRecovery", repositoryName, ex)
        }
    }

    override suspend fun verifyCode(
        email: String, code: Int
    ): Resource<Pair<Boolean, String>> {
        try {
            val result = api.verifyRecoveryCode(
                VerifyRecoveryRequest(
                    email = email,
                    code = code
                )
            )

            return if (result.isSuccessful) {
                val response: VerifyRecoveryResponse? = result.body()
                if (response == null) {
                    return Resource.Error(404,"Respuesta vacía del servidor.")
                }

                val ds = MyDataStore(context)
                val (token, expiresAt, message) = response
                val expiresAtMills = expiresAt * 1000L
                val expireDate = Date(expiresAtMills)

                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val expireDateString = formatter.format(expireDate)

                ds.saveKeyValue("recoveryToken", token)
                ds.saveKeyValue("recoveryExpire", expireDateString)

                Resource.Success(Pair(true, message))
            } else {
                val error = errorParser.parseErrorObject(result.errorBody())
                Resource.Error(result.code(),
                    error?.error ?: "No se pudo verificar el código de recuperación."
                )
            }
        } catch (ex: Exception) {
            return handleException("verifyCode", repositoryName, ex)
        }
    }

    override suspend fun recoverPassword(newPassword: String): Resource<Pair<Boolean, String>> {
        try {
            val ds = MyDataStore(context)
            val recoveryToken: String =
                ds.getValueFromKey("recoveryToken") ?: return Resource.Error(404,"No token")
            val recoveryExpire =
                ds.getValueFromKey("recoveryExpire") ?: return Resource.Error(404,"No expire")

            val expireDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expireDate: Date = expireDateFormat.parse(recoveryExpire) ?: Date()
            val currentTime = Calendar.getInstance().time

            if (currentTime >= expireDate) return Resource.Error(403,"El token indicado ha expirado")

            val result = api.recoverPassword("Bearer $recoveryToken", PasswordRequest(newPassword))

            return if (result.isSuccessful) {
                val response: SimpleMessageResponse? = result.body()
                if (response == null) {
                    return Resource.Error(404,"Respuesta vacía del servidor.")
                }

                val (message) = response
                Resource.Success(Pair(true, message))
            } else {
                val error = errorParser.parseErrorObject(result.errorBody())
                Resource.Error(result.code(),
                    error?.error ?: "No se pudo actualizar la contraseña."
                )
            }
        } catch (ex: Exception) {
            return handleException("recoverPasword", repositoryName, ex)
        }
    }

    override suspend fun logout(): Resource<Pair<Boolean, String>> {
        try {
            val ds = MyDataStore(context)
            val token: String = ds.getValueFromKey("token") ?: return Resource.Error(404,"No token")
            val internetConnection = InternetStatusManager.getLastConnectionState()
            val refreshToken =
                ds.getValueFromKey("refreshToken") ?: return Resource.Error(404,"No refreshToken")

            ds.clearData()
            database.userDao().deleteAll()

            if (!internetConnection) {
                if (token.isNotBlank()) ds.saveKeyValue("logout", refreshToken)
                ds.saveKeyValue("logoutToken", token)

                return Resource.Success(Pair(true, "Sesión cerrada"))
            }

            if (refreshToken.isBlank()) return Resource.Error(404,"No refreshToken")

            val result = api.logout(token = "Bearer $token", RefreshRequest(refreshToken))

            if (result.isSuccessful) {
                val response: SimpleMessageResponse? = result.body()

                if (response == null) {
                    return Resource.Error(404,"Respuesta vacía del servidor.")
                }

                val (message) = response

                return Resource.Success(Pair(true, message))
            } else {
                val error = errorParser.parseErrorObject(result.errorBody())
                sessionManager.triggerLogout(error?.error ?: "La sesión ha caducado.")
                return Resource.Error(403,"Sesión inválida")
            }
        } catch (ex: Exception) {
            return handleException("logout", repositoryName, ex)
        }
    }
}