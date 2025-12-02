package org.exchange.radhe.features.home

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.exchange.radhe.WebSocketService
import org.exchange.radhe.di.ConnectionStatus
import org.exchange.radhe.di.UplinkStateHolder
import org.exchange.radhe.utils.isAccessibilityServiceEnabled

actual class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val connectionStatus by UplinkStateHolder.connectionStatus.collectAsState()
        val lastCommand by UplinkStateHolder.lastCommand.collectAsState()

        var showAccessibilityDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (!isAccessibilityServiceEnabled(context)) {
                showAccessibilityDialog = true
            }
        }

        if (showAccessibilityDialog) {
            AlertDialog(
                onDismissRequest = { showAccessibilityDialog = false },
                title = { Text("Enable Accessibility Service") },
                text = { Text("To use the volume buttons to control the desktop, you must enable the Accessibility Service for this app.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAccessibilityDialog = false
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }) {
                        Text("Go to Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAccessibilityDialog = false }) {
                        Text("Dismiss")
                    }
                })
        }

        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Radhe Exchange Controller",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))

                StatusCard(connectionStatus)
                Spacer(modifier = Modifier.height(16.dp))
                CommandCard(lastCommand)
                Spacer(modifier = Modifier.height(32.dp))

                ServiceControls(connectionStatus)
            }
        }
    }
}

@Composable
fun StatusCard(status: ConnectionStatus) {
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
            when (status) {
                is ConnectionStatus.Connected -> {
                    Icon(Icons.Default.CheckCircle, "Connected", tint = Color.Green)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Connected", style = MaterialTheme.typography.bodyLarge)
                }
                is ConnectionStatus.Connecting -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Connecting...", style = MaterialTheme.typography.bodyLarge)
                }
                is ConnectionStatus.Disconnected -> {
                    Icon(Icons.Default.Warning, "Disconnected", tint = Color.Gray)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Disconnected", style = MaterialTheme.typography.bodyLarge)
                }
                is ConnectionStatus.Error -> {
                    Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(status.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CommandCard(lastCommand: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SignalWifi4Bar, "Last Command")
            Spacer(modifier = Modifier.size(8.dp))
            Text("Last Command: ", style = MaterialTheme.typography.bodyLarge)
            Text(lastCommand ?: "None", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ServiceControls(connectionStatus: ConnectionStatus) {
    val context = LocalContext.current
    val serviceCanBeStarted = when (connectionStatus) {
        is ConnectionStatus.Connected, is ConnectionStatus.Connecting -> false
        else -> true
    }

    Row {
        Button(
            onClick = {
                Intent(context, WebSocketService::class.java).also {
                    context.startService(it)
                }
            },
            enabled = serviceCanBeStarted
        ) {
            Text("Start Service")
        }
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = {
                Intent(context, WebSocketService::class.java).also {
                    context.stopService(it)
                }
            },
            enabled = !serviceCanBeStarted,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Stop Service")
        }
    }
}
