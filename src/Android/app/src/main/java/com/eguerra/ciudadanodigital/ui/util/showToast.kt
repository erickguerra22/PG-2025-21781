package com.eguerra.ciudadanodigital.ui.util

import android.content.Context
import android.widget.Toast
import android.widget.Toast.makeText

fun showToast(message: String, context: Context, long:Boolean? = false) {
    makeText(context, message, if(long==true) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}