package com.eguerra.ciudadanodigital.helpers

fun extractFileExtension(url: String): String {
    val cleanUrl = url.split("?").firstOrNull() ?: url
    return cleanUrl.substringAfterLast(".", "").lowercase()
}