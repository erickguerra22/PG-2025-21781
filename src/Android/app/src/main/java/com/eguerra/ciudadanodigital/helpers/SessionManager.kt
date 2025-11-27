package com.eguerra.ciudadanodigital.helpers

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    private val _logoutEvents = MutableSharedFlow<String>(replay = 0)
    val logoutEvents: SharedFlow<String> = _logoutEvents

    suspend fun triggerLogout(reason: String) {
        _logoutEvents.emit(reason)
    }
}
