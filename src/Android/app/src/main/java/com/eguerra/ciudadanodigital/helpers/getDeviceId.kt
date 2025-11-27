package com.eguerra.ciudadanodigital.helpers

import android.content.Context
import androidx.core.content.edit

fun getDeviceId(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var id = prefs.getString("device_id", null)
    if (id == null) {
        id = java.util.UUID.randomUUID().toString()
        prefs.edit { putString("device_id", id) }
    }
    return id
}