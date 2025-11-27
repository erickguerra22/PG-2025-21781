package com.eguerra.ciudadanodigital

import android.app.Application
import com.eguerra.ciudadanodigital.helpers.InternetStatusManager
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CiudadanoDigital : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        InternetStatusManager.initialize(this@CiudadanoDigital)
    }
}