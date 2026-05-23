package com.magazines.catalog.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.magazines.catalog.data.local.prefs.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    data object None : SplashDestination()
    data object Main : SplashDestination()
    data object Login : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_DELAY_MS)
            val hasToken = tokenStorage.getToken() != null
            _destination.value = if (hasToken) {
                SplashDestination.Main
            } else {
                SplashDestination.Login
            }
        }
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1_500L
    }
}
