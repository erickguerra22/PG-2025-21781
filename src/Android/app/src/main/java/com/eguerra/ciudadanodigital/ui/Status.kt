package com.eguerra.ciudadanodigital.ui

sealed class Status<T> {
    class Default<T> : Status<T>()
    class Loading<T> : Status<T>()
    class Success<T>(val value: T) : Status<T>()
    class Error<T>(
        val code: Int, val error: String
    ) : Status<T>()
}