
package org.exchange.radhe.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

actual object HomeScreen : Screen {
    @Composable
    actual override fun Content() {
        val viewModel = rememberScreenModel { HomeViewModelJvm() }
        val connectionStatus by viewModel.connectionStatus.collectAsState()
        val lastCommand by viewModel.lastCommand.collectAsState()
        val wicketToggle by viewModel.wicketToggle.collectAsState()
        val boundaryToggle by viewModel.boundaryToggle.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Desktop Controller", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            Text("Status: $connectionStatus")
            Text("Last Command: $lastCommand")
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Wicket", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = wicketToggle,
                    onCheckedChange = viewModel::onWicketToggleChanged,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Boundary", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = boundaryToggle,
                    onCheckedChange = viewModel::onBoundaryToggleChanged,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
