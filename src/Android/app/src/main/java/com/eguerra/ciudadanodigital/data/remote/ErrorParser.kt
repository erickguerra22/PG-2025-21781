package com.eguerra.ciudadanodigital.data.remote

import com.eguerra.ciudadanodigital.data.remote.dto.ErrorDto
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import javax.inject.Inject

class ErrorParser @Inject constructor(private val retrofit: Retrofit) {

    fun parseErrorObject(errorBody: ResponseBody?): ErrorDto? {
        val converter: Converter<ResponseBody, ErrorDto> = retrofit.responseBodyConverter(
            ErrorDto::class.java, arrayOfNulls<Annotation>(0)
        )
        return errorBody?.let { converter.convert(it) }
    }
}