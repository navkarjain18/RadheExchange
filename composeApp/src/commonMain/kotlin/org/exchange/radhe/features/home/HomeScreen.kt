package org.exchange.radhe.features.home

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeView()
    }
}

@Composable
expect fun HomeView()
