
package org.exchange.radhe.features.details

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.exchange.radhe.di.DI

class DetailsViewModel : ScreenModel {

    private val wsClient = DI.wsClient

    val messages: StateFlow<String> = wsClient.observeMessages()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), "No message yet")

    init {
        screenModelScope.launch {
            wsClient.connect("ws://localhost:8080")
        }
    }

    override fun onDispose() {
        super.onDispose()
        screenModelScope.launch {
            wsClient.disconnect()
        }
    }
}
