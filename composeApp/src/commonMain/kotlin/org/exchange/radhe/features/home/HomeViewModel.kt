
package org.exchange.radhe.features.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.exchange.radhe.di.DI
import org.exchange.radhe.di.KeyAction
import org.exchange.radhe.di.KeyEventBus

class HomeViewModel : ScreenModel {
    private val loginRepository = DI.loginRepository
    private val wsClient = DI.wsClient

    init {
        screenModelScope.launch {
            wsClient.connect("ws://10.0.2.2:8080")
        }
        observeKeyEvents()
    }

    private fun observeKeyEvents() {
        KeyEventBus.events.onEach { action ->
            when (action) {
                KeyAction.VOLUME_UP -> sendMessage("wicket_click")
                KeyAction.VOLUME_DOWN -> sendMessage("boundary_click")
            }
        }.launchIn(screenModelScope)
    }

    fun logout() {
        loginRepository.setLoggedIn(false)
        screenModelScope.launch {
            wsClient.disconnect()
        }
    }

    fun sendMessage(message: String) {
        screenModelScope.launch {
            wsClient.send(message)
        }
    }

    override fun onDispose() {
        super.onDispose()
        screenModelScope.launch {
            wsClient.disconnect()
        }
    }
}
