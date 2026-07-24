package com.logisparktech.parkingmanagementsystem.core.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthEventManager @Inject constructor() {
    private val _authEvents = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val authEvents = _authEvents.asSharedFlow()

    fun onUnauthorized() {
        _authEvents.tryEmit(AuthEvent.Unauthorized)
    }

    sealed class AuthEvent {
        object Unauthorized : AuthEvent()
    }
}
