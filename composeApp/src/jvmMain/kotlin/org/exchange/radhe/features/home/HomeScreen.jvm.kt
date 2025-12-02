package org.exchange.radhe.features.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.exchange.radhe.features.login.LoginScreen

actual class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { HomeViewModelJvm() }
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(uiState.isLoggedOut) {
            if (uiState.isLoggedOut) {
                navigator.replaceAll(LoginScreen())
            }
        }

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
                Text("Desktop Controller", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                uiState.username?.let {
                    Text("Welcome, $it", style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(24.dp))

                StatusCard(uiState.connectionState)
                Spacer(modifier = Modifier.height(16.dp))
                CommandCard(uiState.lastReceivedCommand)
                Spacer(modifier = Modifier.height(24.dp))

                ControlToggles(uiState, viewModel)
                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = viewModel::logout) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun StatusCard(state: ConnectionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (state) {
                is ConnectionState.Connected -> {
                    Icon(Icons.Default.CheckCircle, "Connected", tint = Color.Green)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Connected", style = MaterialTheme.typography.bodyLarge)
                }
                is ConnectionState.Connecting -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Connecting...", style = MaterialTheme.typography.bodyLarge)
                }
                is ConnectionState.Disconnected -> {
                    Icon(Icons.Default.Warning, "Disconnected", tint = Color.Gray)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Disconnected: ${state.reason}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun CommandCard(lastCommand: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, "Last Command")
            Spacer(modifier = Modifier.size(8.dp))
            Text("Last Command: ", style = MaterialTheme.typography.bodyLarge)
            Text(lastCommand, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ControlToggles(uiState: HomeUiState, viewModel: HomeViewModelJvm) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Wicket", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1.0f))
            Switch(
                checked = uiState.isWicketToggleOn,
                onCheckedChange = viewModel::onWicketToggleChanged
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Boundary", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1.0f))
            Switch(
                checked = uiState.isBoundaryToggleOn,
                onCheckedChange = viewModel::onBoundaryToggleChanged
            )
        }
    }
}
