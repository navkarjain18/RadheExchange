
package org.exchange.radhe.features.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import org.exchange.radhe.di.DI

class HomeViewModel : ScreenModel {
    private val loginRepository = DI.loginRepository
    private val wsClient = DI.wsClient

    init {
        screenModelScope.launch {
            wsClient.connect("ws://10.0.2.2:8080") // Use 10.0.2.2 for Android emulator to connect to localhost
        }
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
