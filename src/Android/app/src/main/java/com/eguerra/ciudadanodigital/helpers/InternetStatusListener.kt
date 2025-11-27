package com.eguerra.ciudadanodigital.helpers

import javax.inject.Inject

interface InternetStatusListener {
    fun onInternetStatusChanged(isConnected: Boolean)
}

class InternetStatusListenerImpl @Inject constructor() : InternetStatusListener {
    override fun onInternetStatusChanged(isConnected: Boolean) {
    }
}