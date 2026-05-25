package com.magazines.catalog.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.magazines.catalog.data.remote.session.AuthSessionEventBus
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthSessionEventBusEntryPoint {
    fun authSessionEventBus(): AuthSessionEventBus
}

@Composable
fun ObserveSessionExpired(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val authSessionEventBus = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthSessionEventBusEntryPoint::class.java,
        ).authSessionEventBus()
    }

    LaunchedEffect(navController, authSessionEventBus) {
        authSessionEventBus.sessionExpired.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }
}
