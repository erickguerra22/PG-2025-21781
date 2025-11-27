package com.eguerra.ciudadanodigital.helpers

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object InternetStatusManager {
    private val listeners = mutableListOf<InternetStatusListener>()
    private var lastConnectionState: Boolean? = null

    fun addListener(listener: InternetStatusListener) {
        listeners.add(listener)
        lastConnectionState?.let { listener.onInternetStatusChanged(it) }
    }

    fun removeListener(listener: InternetStatusListener) {
        listeners.remove(listener)
    }

    fun notifyStatusChange(isConnected: Boolean) {
        if (lastConnectionState != isConnected) {
            lastConnectionState = isConnected
            listeners.forEach { it.onInternetStatusChanged(isConnected) }
        }
    }

    fun initialize(context: Context) {
        val quickCheck = Internet.checkForInternet(context)
        lastConnectionState = quickCheck
        notifyStatusChange(quickCheck)

        CoroutineScope(Dispatchers.IO).launch {
            val realCheck = Internet.hasRealInternetAccess() && Internet.checkForInternet(context)
            lastConnectionState = realCheck
            notifyStatusChange(realCheck)
        }
    }

    fun getLastConnectionState(): Boolean = lastConnectionState ?: false
}