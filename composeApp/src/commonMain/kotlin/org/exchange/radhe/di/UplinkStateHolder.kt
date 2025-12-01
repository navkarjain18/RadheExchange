package org.exchange.radhe.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ConnectionStatus {
    object Disconnected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    object Connected : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}

object UplinkStateHolder {
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _lastCommand = MutableStateFlow<String?>("None")
    val lastCommand = _lastCommand.asStateFlow()

    fun updateConnectionStatus(status: ConnectionStatus) {
        _connectionStatus.value = status
    }

    fun updateLastCommand(command: String) {
        _lastCommand.value = command
    }
}
