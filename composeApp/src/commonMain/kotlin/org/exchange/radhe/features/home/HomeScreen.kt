
package org.exchange.radhe.features.home

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

expect object HomeScreen : Screen {
    @Composable
    override fun Content()
}
