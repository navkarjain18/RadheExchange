
package org.exchange.radhe

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import org.exchange.radhe.di.DI
import org.exchange.radhe.features.home.HomeScreen
import org.exchange.radhe.features.login.LoginScreen

@Composable
fun App() {
    MaterialTheme {
        val loginRepository = DI.loginRepository
        val startScreen = if (loginRepository.isLoggedIn()) {
            HomeScreen
        } else {
            LoginScreen
        }
        Navigator(startScreen)
    }
}
