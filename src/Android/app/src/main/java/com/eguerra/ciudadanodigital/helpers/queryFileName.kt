package com.eguerra.ciudadanodigital.helpers

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri

fun queryFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val cursor = contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        val nameIndex = it.getColumnIndexOpenableColumnsDisplayName()
        if (it.moveToFirst() && nameIndex != -1) it.getString(nameIndex) else null
    }
}

// Compatibilidad con diferentes Android SDKs
fun Cursor.getColumnIndexOpenableColumnsDisplayName(): Int {
    return try {
        getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
    } catch (e: Exception) {
        -1
    }
}
