package org.exchange.radhe.features.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.exchange.radhe.WebSocketService
import org.exchange.radhe.utils.isAccessibilityServiceEnabled

@Composable
actual fun HomeView() {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = remember { HomeViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navigator.pop()
        }
    }

    LaunchedEffect(Unit) {
        if (!isAccessibilityServiceEnabled(context)) {
            showDialog = true
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enable Accessibility Service") },
            text = { Text("To use the volume buttons to control the desktop, you must enable the Accessibility Service for this app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Dismiss")
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Radhe Exchange Controller", style = MaterialTheme.typography.headlineMedium)
        Text("Welcome, ${uiState.username}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            Intent(context, WebSocketService::class.java).also {
                it.putExtra(WebSocketService.EXTRA_USERNAME, uiState.username)
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
        }) {
            Text("Logout")
        }
    }
}
