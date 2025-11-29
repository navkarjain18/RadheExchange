
package org.exchange.radhe.features.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.exchange.radhe.WebSocketService

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { HomeViewModel() }
        val context = LocalContext.current

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                Intent(context, WebSocketService::class.java).also {
                    context.startService(it)
                }
            }) {
                Text("Start Service")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                Intent(context, WebSocketService::class.java).also {
                    context.stopService(it)
                }
            }) {
                Text("Stop Service")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                viewModel.logout()
                navigator.pop()
            }) {
                Text("Logout")
            }
        }
    }
}
