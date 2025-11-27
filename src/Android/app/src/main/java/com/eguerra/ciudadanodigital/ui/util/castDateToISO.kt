package com.eguerra.ciudadanodigital.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun castDateToISO(dateString: String?): String {
    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getTimeZone("UTC")

    return try {
        if (dateString.isNullOrBlank()) {
            outputFormat.format(Date())
        } else {
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        }
    } catch (e: Exception) {
        e.printStackTrace()
        outputFormat.format(Date())
    }
}