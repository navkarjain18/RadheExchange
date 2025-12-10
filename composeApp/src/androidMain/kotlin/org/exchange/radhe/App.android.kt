package org.exchange.radhe

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import org.exchange.radhe.features.login.LoginScreen

/**
 * Android implementation of the shared [App] entry point.
 * Sets up the Material Theme and the root navigation host.
 *
 * Starts with [LoginScreen] as the initial destination.
 */
@Composable
actual fun App() {
    MaterialTheme {
        Navigator(LoginScreen())
    }
}
