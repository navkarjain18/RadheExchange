package org.exchange.radhe.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

@Composable
actual fun HomeView() {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = remember { HomeViewModelJvm() }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Desktop Controller", style = MaterialTheme.typography.headlineMedium)
            uiState.username?.let {
                Text("Welcome, $it", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(24.dp))

            val connectionText = when (val state = uiState.connectionState) {
                is ConnectionState.Connected -> "Connected"
                is ConnectionState.Connecting -> "Connecting..."
                is ConnectionState.Disconnected -> "Disconnected: ${state.reason}"
            }
            Text("Status: $connectionText")
            Text("Last Command: ${uiState.lastReceivedCommand}")
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Wicket", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.isWicketToggleOn,
                    onCheckedChange = viewModel::onWicketToggleChanged,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Boundary", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.isBoundaryToggleOn,
                    onCheckedChange = viewModel::onBoundaryToggleChanged,
                    modifier = Modifier.padding(start = 8.dp)
                )
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
