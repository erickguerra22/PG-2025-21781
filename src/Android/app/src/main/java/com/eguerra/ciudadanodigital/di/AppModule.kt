package com.eguerra.ciudadanodigital.di

import android.content.Context
import androidx.room.Room
import com.eguerra.ciudadanodigital.BuildConfig
import com.eguerra.ciudadanodigital.data.local.Database
import com.eguerra.ciudadanodigital.data.remote.API
import com.eguerra.ciudadanodigital.data.remote.ErrorParser
import com.eguerra.ciudadanodigital.data.repository.AuthRepository
import com.eguerra.ciudadanodigital.data.repository.AuthRepositoryImp
import com.eguerra.ciudadanodigital.data.repository.ChatRepository
import com.eguerra.ciudadanodigital.data.repository.ChatRepositoryImp
import com.eguerra.ciudadanodigital.data.repository.DocumentRepository
import com.eguerra.ciudadanodigital.data.repository.DocumentRepositoryImp
import com.eguerra.ciudadanodigital.data.repository.MessageRepository
import com.eguerra.ciudadanodigital.data.repository.MessageRepositoryImp
import com.eguerra.ciudadanodigital.data.repository.UserRepository
import com.eguerra.ciudadanodigital.data.repository.UserRepositoryImp
import com.eguerra.ciudadanodigital.helpers.DEV
import com.eguerra.ciudadanodigital.helpers.InternetStatusListener
import com.eguerra.ciudadanodigital.helpers.InternetStatusListenerImpl
import com.eguerra.ciudadanodigital.helpers.SessionManager
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val browserHeadersInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestWithHeaders = original.newBuilder()
            .header(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 9; SM-G960F) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            )
            .header("Accept", "application/json")
            .header("Accept-Language", "es-ES,es;q=0.9")
            .header("Cache-Control", "no-cache")
            .header("X-Client-Type", "mobile")
            .header("Pragma", "no-cache")
            .method(original.method, original.body)
            .build()
        chain.proceed(requestWithHeaders)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 30s, no minutos
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .cookieJar(object : CookieJar {
                private val cookieStore = HashMap<HttpUrl, List<Cookie>>()
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url] ?: emptyList()
                }
            })
            .addInterceptor(browserHeadersInterceptor)

        val environment: String = BuildConfig.ENVIRONMENT

        when (environment) {
            DEV -> clientBuilder.addInterceptor(loggingInterceptor)
            else -> {}
        }

        return clientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val apiUrl: String = BuildConfig.API_URL
        val gson = GsonBuilder()
            .create()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(apiUrl)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(
        retrofit: Retrofit
    ): API {
        return retrofit.create(API::class.java)
    }

    @Provides
    @Singleton
    fun provideInternetStatusListener(): InternetStatusListener {
        return InternetStatusListenerImpl()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(
            context, Database::class.java, "database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideErrorParser(
        retrofit: Retrofit
    ): ErrorParser {
        return ErrorParser(retrofit)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: API,
        @ApplicationContext context: Context,
        database: Database,
        errorParser: ErrorParser,
        sessionManager: SessionManager
    ): AuthRepository {
        return AuthRepositoryImp(
            api = api,
            context = context,
            database = database,
            errorParser = errorParser,
            sessionManager = sessionManager
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        api: API,
        @ApplicationContext context: Context,
        database: Database,
        errorParser: ErrorParser,
        authRepository: AuthRepository
    ): UserRepository {
        return UserRepositoryImp(
            api = api,
            context = context,
            database = database,
            errorParser = errorParser,
            authRepository = authRepository
        )
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        api: API,
        @ApplicationContext context: Context,
        database: Database,
        errorParser: ErrorParser,
        authRepository: AuthRepository
    ): ChatRepository {
        return ChatRepositoryImp(
            api = api,
            context = context,
            database = database,
            errorParser = errorParser,
            authRepository = authRepository
        )
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        api: API,
        @ApplicationContext context: Context,
        database: Database,
        errorParser: ErrorParser,
        authRepository: AuthRepository
    ): MessageRepository {
        return MessageRepositoryImp(
            api = api,
            context = context,
            database = database,
            errorParser = errorParser,
            authRepository = authRepository
        )
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        api: API,
        @ApplicationContext context: Context,
        database: Database,
        errorParser: ErrorParser,
        authRepository: AuthRepository
    ): DocumentRepository {
        return DocumentRepositoryImp(
            api = api,
            context = context,
            database = database,
            errorParser = errorParser,
            authRepository = authRepository
        )
    }
}
