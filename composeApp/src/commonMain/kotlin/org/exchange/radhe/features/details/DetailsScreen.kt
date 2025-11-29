
package org.exchange.radhe.features.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

object DetailsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { DetailsViewModel() }
        val message by viewModel.message.collectAsState()
        val wicketToggle by viewModel.wicketToggle.collectAsState()
        val boundaryToggle by viewModel.boundaryToggle.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(message, modifier = Modifier.padding(bottom = 24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Wicket")
                Switch(
                    checked = wicketToggle,
                    onCheckedChange = viewModel::onWicketToggleChanged,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Boundary")
                Switch(
                    checked = boundaryToggle,
                    onCheckedChange = viewModel::onBoundaryToggleChanged,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
