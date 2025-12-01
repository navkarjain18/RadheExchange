package org.exchange.radhe

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import org.exchange.radhe.features.home.HomeScreen

@Composable
actual fun App() {
    MaterialTheme {
        Navigator(HomeScreen())
    }
}
