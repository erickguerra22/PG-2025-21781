package com.eguerra.ciudadanodigital.data.remote

import com.eguerra.ciudadanodigital.data.remote.dto.MessageDto
import com.eguerra.ciudadanodigital.data.remote.dto.UserDto
import com.eguerra.ciudadanodigital.data.remote.dto.requests.EmailRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.LoginRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.NewChatRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.NewMessageRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.PasswordRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.RefreshRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.RegisterRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.UpdateUserRequest
import com.eguerra.ciudadanodigital.data.remote.dto.requests.VerifyRecoveryRequest
import com.eguerra.ciudadanodigital.data.remote.dto.responses.AuthResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.GetDocumentsResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.GetChatMessagesResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.GetChatsResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.NewChatResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.NewMessageResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.NewResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.SimpleMessageResponse
import com.eguerra.ciudadanodigital.data.remote.dto.responses.VerifyRecoveryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface API {

    // AUTH
    @Headers("Content-Type: application/json")
    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/logout")
    suspend fun logout(
        @Header("authorization") token: String, @Body body: RefreshRequest
    ): Response<SimpleMessageResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Header("authorization") token: String,
        @Body body: RefreshRequest
    ): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/sendRecovery")
    suspend fun sendRecoveryCode(
        @Body body: EmailRequest
    ): Response<SimpleMessageResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/verifyCode")
    suspend fun verifyRecoveryCode(
        @Body body: VerifyRecoveryRequest
    ): Response<VerifyRecoveryResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/recoverPassword")
    suspend fun recoverPassword(
        @Header("authorization") token: String,
        @Body body: PasswordRequest
    ): Response<SimpleMessageResponse>

    // USER
    @Headers("Content-Type: application/json")
    @GET("user/logged")
    suspend fun getLoggedUser(
        @Header("authorization") token: String
    ): Response<UserDto>

    @Headers("Content-Type: application/json")
    @POST("user")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @PUT("user/{userId}")
    suspend fun updateUser(
        @Header("authorization") token: String,
        @Path("userId") userId: Long,
        @Body body: UpdateUserRequest,
    ): Response<UserDto>

    // CHAT
    @Headers("Content-Type: application/json")
    @GET("chat")
    suspend fun getUserChats(
        @Header("authorization") token: String
    ): Response<GetChatsResponse>

    @Headers("Content-Type: application/json")
    @POST("chat")
    suspend fun createChat(
        @Header("authorization") token: String,
        @Body body: NewChatRequest
    ): Response<NewChatResponse>

    // MESSAGE
    @Headers("Content-Type: application/json")
    @GET("message/{chatId}")
    suspend fun getChatMessages(
        @Header("authorization") token: String,
        @Path("chatId") chatId: Long,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<GetChatMessagesResponse>

    @Headers("Content-Type: application/json")
    @POST("message/{chatId}")
    suspend fun createMessage(
        @Header("authorization") token: String,
        @Path("chatId") chatId: Long,
        @Body body: NewMessageRequest
    ): Response<NewMessageResponse>

    @Headers("Content-Type: application/json")
    @POST("message")
    suspend fun createMessageUnassigned(
        @Header("authorization") token: String,
        @Body body: NewMessageRequest
    ): Response<NewMessageResponse>

    @Headers("Content-Type: application/json")
    @PUT("message/{messageId}/{chatId}")
    suspend fun assignMessage(
        @Header("authorization") token: String,
        @Path("messageId") messageId: Long,
        @Path("chatId") chatId: Long,
    ): Response<MessageDto>

    @Headers("Content-Type: application/json")
    @GET("message/response/{chatId}")
    suspend fun getResponse(
        @Header("authorization") token: String,
        @Path("chatId") chatId: Long,
        @Query("question") question: String
    ): Response<NewResponse>

    @Headers("Content-Type: application/json")
    @GET("message/response")
    suspend fun getResponseUnassigned(
        @Header("authorization") token: String,
        @Query("question") question: String
    ): Response<NewResponse>

    // DOCUMENT
    @Multipart
    @POST("document")
    suspend fun saveDocument(
        @Header("authorization") token: String,
        @Part("filename") filename: RequestBody,
        @Part("author") author: RequestBody,
        @Part("year") year: RequestBody,
        @Part("minAge") minAge: RequestBody,
        @Part("maxAge") maxAge: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<SimpleMessageResponse>

    @Headers("Content-Type: application/json")
    @GET("document")
    suspend fun getDocuments(
        @Header("authorization") token: String,
    ): Response<GetDocumentsResponse>

    @Headers("Content-Type: application/json")
    @DELETE("document/{documentId}")
    suspend fun deleteDocument(
        @Header("authorization") token: String,
        @Path("documentId") documentId: Long,
    ): Response<SimpleMessageResponse>
}