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
import java.awt.Point
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField

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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
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
                Spacer(modifier = Modifier.height(16.dp))

                CoordinateSelection(uiState, viewModel)
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

@Composable
fun CoordinateSelection(uiState: HomeUiState, viewModel: HomeViewModelJvm) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Click Coordinates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = if (uiState.captureDelaySeconds == 0) "" else uiState.captureDelaySeconds.toString(),
                onValueChange = viewModel::onCaptureDelayChanged,
                label = { Text("Capture delay (seconds)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Select 3 coordinates to click when an event is received. Click 'Set', move your mouse, and wait ${uiState.captureDelaySeconds} seconds.", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            for (i in 0..2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Coordinate ${i + 1}: ", style = MaterialTheme.typography.bodyLarge)
                    val point = uiState.coordinates.getOrNull(i)
                    if (point != null) {
                        Text("[${point.x}, ${point.y}]", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Not set", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    
                    if (uiState.capturingIndex == i) {
                        Text("Capturing in ${uiState.captureCountdown}...", color = Color.Red, fontWeight = FontWeight.Bold)
                    } else {
                        Button(
                            onClick = { viewModel.startCapturingCoordinate(i) },
                            enabled = uiState.capturingIndex == null
                        ) {
                            Text(if (point == null) "Set" else "Update")
                        }
                    }
                }
                if (i < 2) Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (uiState.delay1to2Ms == 0L) "" else uiState.delay1to2Ms.toString(),
                    onValueChange = viewModel::onDelay1to2Changed,
                    label = { Text("Delay 1 -> 2 (ms)") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    value = if (uiState.delay2to3Ms == 0L) "" else uiState.delay2to3Ms.toString(),
                    onValueChange = viewModel::onDelay2to3Changed,
                    label = { Text("Delay 2 -> 3 (ms)") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
