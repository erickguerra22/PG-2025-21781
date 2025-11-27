package com.eguerra.ciudadanodigital.helpers

import com.eguerra.ciudadanodigital.data.Resource

fun <T> handleException(funName: String, repository: String, ex: Exception): Resource<T> {
    ex.printStackTrace()
    println("ERROR $funName | $repository >>> ${ex.message}")
    val message = when {
        !InternetStatusManager.getLastConnectionState() -> "No hay conexión a internet"
        ex.message?.contains("scoped") == true -> ""
        ex.message?.contains("timeout") == true -> "Tiempo de espera agotado"
        else -> "500: Error de servidor."
    }
    return Resource.Error(500,message)
}