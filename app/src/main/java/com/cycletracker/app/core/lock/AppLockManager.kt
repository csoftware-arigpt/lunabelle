package com.cycletracker.app.core.lock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-scoped app-lock auth state (never persisted). The interactive lock gates the UI;
 * it is separate from the SQLCipher at-rest encryption (which uses a Keystore key that does
 * NOT require user auth, so background work can still run).
 */
object AppLockManager {
    private val _authenticated = MutableStateFlow(false)
    val authenticated: StateFlow<Boolean> = _authenticated
    fun markAuthenticated() { _authenticated.value = true }
    fun lock() { _authenticated.value = false }
}
