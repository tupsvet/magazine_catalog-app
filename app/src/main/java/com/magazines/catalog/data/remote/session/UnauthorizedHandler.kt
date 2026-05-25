package com.magazines.catalog.data.remote.session

import com.magazines.catalog.data.local.prefs.TokenStorage
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedHandler @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val authSessionEventBus: AuthSessionEventBus,
) {

    fun handleUnauthorized() {
        runBlocking {
            tokenStorage.clearToken()
        }
        authSessionEventBus.emitSessionExpired()
    }
}
