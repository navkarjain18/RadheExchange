
package org.exchange.radhe

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import org.exchange.radhe.di.DI
import org.exchange.radhe.features.home.HomeScreen
import org.exchange.radhe.features.login.LoginScreen
import org.exchange.radhe.platform.Platform
import org.exchange.radhe.platform.getPlatform

@Composable
fun App() {
    MaterialTheme {
        val loginRepository = DI.loginRepository
        if (loginRepository.isLoggedIn()) {
            if (getPlatform() == Platform.Android) {
                Navigator(HomeScreen)
            } else {
                Navigator(org.exchange.radhe.features.details.DetailsScreen)
            }
        } else {
            Navigator(LoginScreen)
        }
    }
}
