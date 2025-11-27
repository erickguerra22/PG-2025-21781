package com.eguerra.ciudadanodigital.data

sealed class Resource<T>(val code: Int?=null, val message: String? = null) {

    class Success<T>(val data: T) : Resource<T>()
    class Error<T>(code: Int, message: String) : Resource<T>(code = code, message = message)
}